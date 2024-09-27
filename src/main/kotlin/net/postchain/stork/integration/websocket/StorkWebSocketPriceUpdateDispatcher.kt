package net.postchain.stork.integration.websocket

import mu.KLogging
import net.postchain.core.Shutdownable
import net.postchain.stork.integration.MessageType
import net.postchain.stork.integration.OraclePriceMessage
import net.postchain.stork.integration.PriceUpdateHandler
import net.postchain.stork.integration.StorkOraclePricesMapper
import net.postchain.stork.integration.SubscriptionRequestMessage
import net.postchain.stork.integration.SubscriptionResponseMessage
import net.postchain.stork.integration.gson.StorkGsonConfig.auto
import org.http4k.websocket.WsMessage

class StorkWebSocketPriceUpdateDispatcher(
        webSocketConnectionFactory: WebSocketConnectionFactory,
        private val assets: List<String>,
        private val priceUpdateHandler: PriceUpdateHandler
) : Shutdownable {

    companion object : KLogging()

    private var subscribed = false
    private var closed = false
    private val subscriptionResponseLens = WsMessage.auto<SubscriptionResponseMessage>().toLens()
    private val oraclePriceLens = WsMessage.auto<OraclePriceMessage>().toLens()

    private val webSocketClient = webSocketConnectionFactory.createConnection {
        logger.info("Stork connection established...")

        it.onMessage(::handleMessage)

        it.onClose { status ->
            if (closed) {
                logger.info("Stork connection was closed")
            } else {
                // TODO: Reconnect?
                logger.error("Stork connection was unexpectedly closed with status: $status")
            }
        }

        it.onError { e ->
            logger.error("Error in Stork web socket connection", e)
        }

        it.send(
                WsMessage.auto<SubscriptionRequestMessage>()
                        .toLens()
                        .create(SubscriptionRequestMessage(data = assets))
        )
    }


    private fun handleMessage(message: WsMessage) {
        try {
            // We expect a confirmation of subscription as the first message
            if (!subscribed) {
                val subscriptionResponse = subscriptionResponseLens.extract(message)
                when (val type = subscriptionResponse.type) {
                    MessageType.subscribe -> {
                        logger.info("Received Stork subscription response: $subscriptionResponse")

                        if (assets.size != subscriptionResponse.data.subscriptions.size
                                || !assets.containsAll(subscriptionResponse.data.subscriptions)) {
                            logger.error("Subscription response does not contain subscribed assets")
                            // TODO: Reconnect?
                        } else {
                            subscribed = true
                        }
                    }

                    else -> {
                        logger.error("Received Stork message of type: '$type' before receiving subscription response")
                        // TODO: Reconnect?
                    }
                }
            } else {
                // Handle oracle price
                val oraclePrice = oraclePriceLens.extract(message)
                if (oraclePrice.type != MessageType.oracle_prices) {
                    logger.error("Received unexpected message type: '${oraclePrice.type}'")
                    return
                }

                val assetPrices = StorkOraclePricesMapper.mapMessageToAssetOraclePrices(oraclePrice)

                for (assetPrice in assetPrices) {
                    if (assets.contains(assetPrice.asset)) {
                        priceUpdateHandler.onPriceUpdate(assetPrice)
                    } else {
                        logger.error("Received a stork price update for unknown asset ${assetPrice.asset} with trace-id: ${oraclePrice.traceId}. Discarding update.")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to handle message from Stork web socket", e)
        }
    }

    override fun shutdown() {
        if (!closed) {
            closed = true
            webSocketClient.close()
        }
    }
}
