package net.postchain.stork.integration

import net.postchain.stork.integration.gson.StorkGsonConfig.auto
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

class MockStorkWebsocket {

    var receivedSubscription: SubscriptionRequestMessage? = null
    var connected = false
    lateinit var socket: Websocket

    fun onConnected(socket: Websocket) {
        this.socket = socket
        connected = true
        socket.onMessage {
            if (receivedSubscription == null) {
                receivedSubscription = WsMessage.auto<SubscriptionRequestMessage>().toLens().extract(it)
            }
        }
        socket.onClose {
            connected = false
            receivedSubscription = null
        }
    }

    fun sendSubscriptionResponseMessage(assets: List<String>) {
        socket.send(
                WsMessage.auto<SubscriptionResponseMessage>()
                        .toLens()
                        .create(SubscriptionResponseMessage(traceId = "test", data = SubscriptionResponseData(assets)))
        )
    }

    fun sendPriceUpdate(fileName: String) {
        val priceUpdate = javaClass.getResource(fileName).readText()
        socket.send(WsMessage(priceUpdate))
    }
}