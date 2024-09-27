package net.postchain.stork.integration.websocket

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import mu.KLogging
import net.postchain.stork.StorkOraclePrices
import net.postchain.stork.integration.MockStorkWebsocket
import net.postchain.stork.integration.PriceUpdateHandler
import net.postchain.stork.integration.SubscriptionRequestMessage
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.testing.testWebsocket
import org.http4k.websocket.WsHandler
import org.junit.jupiter.api.Test
import java.math.BigInteger

class StorkWebSocketPriceUpdateDispatcherTest {


    companion object : KLogging()

    // TODO: test connections closing, invalid messages or invalid orders

    @Test
    fun `Handle normal message flow`() {
        val testAssets = listOf("BTCUSD", "ETHUSD")
        val mockStorkWebsocket = MockStorkWebsocket()
        val priceUpdates = mutableMapOf<String, StorkOraclePrices>()
        val dispatcher = setupPriceUpdateDispatcher(mockStorkWebsocket, testAssets) { update ->
            priceUpdates[update.asset] = update
        }

        assertThat(mockStorkWebsocket.receivedSubscription).isEqualTo(SubscriptionRequestMessage(data = testAssets))

        mockStorkWebsocket.sendSubscriptionResponseMessage(testAssets)

        assertThat(priceUpdates["BTCUSD"]).isNull()
        mockStorkWebsocket.sendPriceUpdate("/net/postchain/stork/sample_response.json")

        assertThat(priceUpdates["BTCUSD"]!!.storkPrice.price).isEqualTo(BigInteger("63762242334000003000000"))

        assertThat(mockStorkWebsocket.connected).isTrue()
        dispatcher.shutdown()
        assertThat(mockStorkWebsocket.connected).isFalse()
    }

    @Test
    fun `Ignore price messages arriving before subscription`() {
        val testAssets = listOf("BTCUSD", "ETHUSD")
        val mockStorkWebsocket = MockStorkWebsocket()
        val priceUpdates = mutableMapOf<String, StorkOraclePrices>()
        setupPriceUpdateDispatcher(mockStorkWebsocket, testAssets) { update ->
            priceUpdates[update.asset] = update
        }

        assertThat(mockStorkWebsocket.receivedSubscription).isEqualTo(SubscriptionRequestMessage(data = testAssets))

        assertThat(priceUpdates["BTCUSD"]).isNull()
        mockStorkWebsocket.sendPriceUpdate("/net/postchain/stork/sample_response.json")
        assertThat(priceUpdates["BTCUSD"]).isNull()
    }

    private fun setupPriceUpdateDispatcher(mockStorkWebsocket: MockStorkWebsocket, assets: List<String>, priceUpdateHandler: PriceUpdateHandler) : StorkWebSocketPriceUpdateDispatcher {
        val testApp: WsHandler = { _ ->
            { socket ->
                mockStorkWebsocket.onConnected(socket)
            }
        }

        return StorkWebSocketPriceUpdateDispatcher(
                { onConnected ->
                    val socket = testApp.testWebsocket(Request(Method.GET, "/"))
                    onConnected(socket)
                    socket
                },
                assets,
                priceUpdateHandler
        )
    }
}
