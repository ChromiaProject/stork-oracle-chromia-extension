package net.postchain.stork.integration.rest

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.time.delay
import mu.KLogging
import net.postchain.core.Shutdownable
import net.postchain.stork.integration.gson.StorkGsonConfig.auto
import net.postchain.stork.integration.OraclePriceMessage
import net.postchain.stork.integration.PriceUpdateHandler
import net.postchain.stork.integration.StorkOraclePricesMapper
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.cookie.StandardCookieSpec
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.util.Timeout
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.filter.GzipCompressionMode
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException

class StorkRestPriceUpdateDispatcher(
        private val url: String,
        username: String,
        password: String,
        private val assets: List<String>,
        private val priceUpdateHandler: PriceUpdateHandler
) : Shutdownable {

    companion object : KLogging() {
        const val FAILED_REQUEST_LIMIT = 5

        val POLL_INTERVAL: Duration = Duration.ofSeconds(1L)
        val BACK_OFF_TIME: Duration = Duration.ofSeconds(60L)
    }

    private val oraclePriceLens = Body.auto<OraclePriceMessage>().toLens()
    private var consecutiveFailedRequests = 0

    private val client = ClientFilters.AcceptGZip(GzipCompressionMode.Streaming())
            .then(ClientFilters.BasicAuth(username, password))
            .then(
                ApacheClient(HttpClients.custom().setDefaultRequestConfig(
                    RequestConfig.custom()
                        .setRedirectsEnabled(false)
                        .setCookieSpec(StandardCookieSpec.IGNORE)
                        .setConnectionRequestTimeout(Timeout.ofSeconds(10))
                        .setResponseTimeout(Timeout.ofSeconds(10))
                        .build()
                ).build())
            )

    private val priceUpdateJob: Job = CoroutineScope(Dispatchers.IO).launch(CoroutineName("stork-price-update") + MDCContext()) {
        while (isActive) {
            try {
                consecutiveFailedRequests = if (pollPrices()) 0 else
                    minOf(FAILED_REQUEST_LIMIT, consecutiveFailedRequests + 1)
            } catch (e: CancellationException) {
                break
            } catch (e: Exception) {
                logger.error("Failed to poll prices from Stork REST API: ${e.message}", e)
                consecutiveFailedRequests = minOf(FAILED_REQUEST_LIMIT, consecutiveFailedRequests + 1)
            }
            delay(
                    if (consecutiveFailedRequests >= FAILED_REQUEST_LIMIT) BACK_OFF_TIME else POLL_INTERVAL
            )
        }
    }

    private fun pollPrices(): Boolean {
        val response = client(Request(Method.GET, "${url}/v1/prices/latest?assets=${assets.joinToString(",")}"))
        return if (response.status == Status.OK) {
            val priceUpdates = StorkOraclePricesMapper.mapMessageToAssetOraclePrices(oraclePriceLens.extract(response))
            priceUpdates.forEach { priceUpdate ->
                if (assets.contains(priceUpdate.asset)) {
                    priceUpdateHandler.onPriceUpdate(priceUpdate)
                } else {
                    logger.error("Received a stork price update for unknown asset ${priceUpdate.asset}. Discarding update.")
                }
            }
            true
        } else {
            logger.error("Got unexpected response with status ${response.status} and payload: ${response.bodyString()}")
            false
        }
    }

    override fun shutdown() {
        priceUpdateJob.cancel()
    }
}
