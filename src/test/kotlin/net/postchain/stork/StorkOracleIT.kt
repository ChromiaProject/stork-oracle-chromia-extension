package net.postchain.stork

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import io.undertow.util.Headers
import net.postchain.concurrent.util.get
import net.postchain.devtools.IntegrationTestSetup
import net.postchain.devtools.PostchainTestNode.Companion.DEFAULT_CHAIN_IID
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.stork.config.StorkApiType
import net.postchain.stork.integration.SubscriptionRequestMessage
import net.postchain.stork.integration.SubscriptionResponseData
import net.postchain.stork.integration.SubscriptionResponseMessage
import net.postchain.stork.integration.gson.StorkGsonConfig.auto
import org.awaitility.Awaitility
import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class StorkOracleIT : IntegrationTestSetup() {

    @Test
    fun testStorkOracleRest() {
        val mockServer = { _: Request ->
            Response(OK)
                    .header(Headers.CONTENT_TYPE_STRING, ContentType.APPLICATION_JSON.value)
                    .body(
                            javaClass.getResource("/net/postchain/stork/sample_response.json").readText()
                    )
        }.asServer(Undertow(0)).start()

        with(configOverrides) {
            setProperty("extension.stork.url", "http://localhost:${mockServer.port()}")
            setProperty("extension.stork.username", "test")
            setProperty("extension.stork.password", "test")
            setProperty("extension.stork.api_type", StorkApiType.REST.name)
        }

        val nodes = createNodes(4, "/net/postchain/stork/stork_it.xml")

        val blockQueries = nodes.map { it.blockQueries() }
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilAsserted {
            buildBlock(DEFAULT_CHAIN_IID)

            for (blockQuery in blockQueries) {
                val bscPrice = blockQuery.query(OP_STORK_TEST_LATEST_UPDATE_QUERY, gtv("BTCUSD")).get()
                assertThat(bscPrice.isNull()).isFalse()
                assertThat(StorkOraclePrices.fromGtvArray(bscPrice as GtvArray).storkPrice.price).isEqualTo(BigInteger("63762242334000003000000"))
            }
        }

        mockServer.stop()
    }

    @Test
    fun testStorkOracleWebsocket() {
        val testApp: WsHandler = { _ ->
            WsResponse { socket ->
                socket.onMessage {
                    val subscriptionRequest = WsMessage.auto<SubscriptionRequestMessage>().toLens().extract(it)
                    val subscriptionResponse = WsMessage.auto<SubscriptionResponseMessage>()
                            .toLens()
                            .create(SubscriptionResponseMessage(traceId = "test", data = SubscriptionResponseData(subscriptionRequest.data)))
                    socket.send(subscriptionResponse)

                    val priceUpdate = javaClass.getResource("/net/postchain/stork/sample_response.json").readText()
                    socket.send(WsMessage(priceUpdate))
                }
            }
        }
        val testWsServer = testApp.asServer(Undertow(0)).start()

        with(configOverrides) {
            setProperty("extension.stork.url", "ws://localhost:${testWsServer.port()}")
            setProperty("extension.stork.username", "test")
            setProperty("extension.stork.password", "test")
            setProperty("extension.stork.api_type", StorkApiType.WEBSOCKET.name)
        }

        val nodes = createNodes(4, "/net/postchain/stork/stork_it.xml")

        val blockQueries = nodes.map { it.blockQueries() }
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilAsserted {
            buildBlock(DEFAULT_CHAIN_IID)

            for (blockQuery in blockQueries) {
                val bscPrice = blockQuery.query(OP_STORK_TEST_LATEST_UPDATE_QUERY, gtv("BTCUSD")).get()
                assertThat(bscPrice.isNull()).isFalse()
                assertThat(StorkOraclePrices.fromGtvArray(bscPrice as GtvArray).storkPrice.price).isEqualTo(BigInteger("63762242334000003000000"))
            }
        }

        testWsServer.stop()
    }
}
