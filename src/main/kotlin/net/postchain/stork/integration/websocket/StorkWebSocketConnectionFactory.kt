package net.postchain.stork.integration.websocket

import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension
import java.util.Base64

class StorkWebSocketConnectionFactory(
        private val url: String,
        private val username: String,
        private val password: String,
) : WebSocketConnectionFactory {

    override fun createConnection(onConnected: (Websocket) -> Unit): Websocket = WebsocketClient.nonBlocking(
            Uri.of(url),
            listOf(
                    "Authorization" to "Basic ${Base64.getEncoder().encodeToString("${username}:${password}".toByteArray())}"
            ),
            draft = Draft_6455(PerMessageDeflateExtension()),
    ) {
        onConnected(it)
    }

}