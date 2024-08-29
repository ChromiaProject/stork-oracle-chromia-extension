package net.postchain.stork.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.testing.testWebsocket
import org.http4k.websocket.WsHandler
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.Security

class StorkOracleEventProcessorTest {

    init {
        // We add this provider so that we can get keccak-256 message digest instances
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    companion object : KLogging()

    // TODO: test connections closing, invalid messages or invalid orders

    @Test
    fun `Handle normal message flow`() {
        val testAssets = listOf("BTCUSD", "ETHUSD")
        val mockStorkWebsocket = MockStorkWebsocket()
        val processor = setupOracleProcessor(mockStorkWebsocket, testAssets)

        assertThat(mockStorkWebsocket.receivedSubscription).isEqualTo(SubscriptionRequestMessage(data = testAssets))

        mockStorkWebsocket.sendSubscriptionResponseMessage(testAssets)

        assertThat(processor.assetPriceUpdates["BTCUSD"]).isNull()
        mockStorkWebsocket.sendPriceUpdate("/net/postchain/stork/sample_response.json")

        assertThat(processor.assetPriceUpdates["BTCUSD"]!!.storkPrice.price).isEqualTo(BigInteger("63762242334000003000000"))

        assertThat(mockStorkWebsocket.connected).isTrue()
        processor.shutdown()
        assertThat(mockStorkWebsocket.connected).isFalse()
    }

    @Test
    fun `Ignore price messages arriving before subscription`() {
        val testAssets = listOf("BTCUSD", "ETHUSD")
        val mockStorkWebsocket = MockStorkWebsocket()
        val processor = setupOracleProcessor(mockStorkWebsocket, testAssets)

        assertThat(mockStorkWebsocket.receivedSubscription).isEqualTo(SubscriptionRequestMessage(data = testAssets))

        assertThat(processor.assetPriceUpdates["BTCUSD"]).isNull()
        mockStorkWebsocket.sendPriceUpdate("/net/postchain/stork/sample_response.json")
        assertThat(processor.assetPriceUpdates["BTCUSD"]).isNull()
    }

    private fun setupOracleProcessor(mockStorkWebsocket: MockStorkWebsocket, assets: List<String>) : StorkOracleEventProcessor {
        val testApp: WsHandler = { _ ->
            { socket ->
                mockStorkWebsocket.onConnected(socket)
            }
        }

        return StorkOracleEventProcessor(
                { onConnected ->
                    val socket = testApp.testWebsocket(Request(Method.GET, "/"))
                    onConnected(socket)
                    socket
                },
                assets
        )
    }
}
