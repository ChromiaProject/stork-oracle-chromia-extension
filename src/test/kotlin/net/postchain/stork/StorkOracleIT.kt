package net.postchain.stork

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import net.postchain.concurrent.util.get
import net.postchain.devtools.IntegrationTestSetup
import net.postchain.devtools.PostchainTestNode.Companion.DEFAULT_CHAIN_IID
import net.postchain.gtv.GtvArray
import net.postchain.gtv.GtvFactory.gtv
import net.postchain.stork.integration.SubscriptionRequestMessage
import net.postchain.stork.integration.SubscriptionResponseData
import net.postchain.stork.integration.SubscriptionResponseMessage
import net.postchain.stork.integration.gson.StorkGsonConfig.auto
import org.awaitility.Awaitility
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class StorkOracleIT : IntegrationTestSetup() {

    @Test
    fun testStorkOracle() {
        val testApp: WsHandler = { _ ->
            { socket ->
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
            setProperty("stork.url", "ws://localhost:${testWsServer.port()}")
            setProperty("stork.username", "test")
            setProperty("stork.password", "test")
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
