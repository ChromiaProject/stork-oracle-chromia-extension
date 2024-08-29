package net.postchain.stork.integration.websocket

import org.http4k.websocket.Websocket

fun interface WebSocketConnectionFactory {
    fun createConnection(onConnected: (Websocket) -> Unit): Websocket
}