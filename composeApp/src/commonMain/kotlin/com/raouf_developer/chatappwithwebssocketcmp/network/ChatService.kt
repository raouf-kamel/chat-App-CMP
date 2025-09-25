package com.raouf_developer.chatappwithwebssocketcmp.network

import com.raouf_developer.chatappwithwebssocketcmp.model.ChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json

class ChatService(
    private val json: Json,
    private val httpClient: HttpClient
) {

    private val _incomingMessages = MutableSharedFlow<ChatMessage>()
    val incomingMessages: SharedFlow<ChatMessage> = _incomingMessages

    private var isConnected = false
    private var session: DefaultClientWebSocketSession? = null

    suspend fun connect(serverAddress: String, username: String) {
        try {
            httpClient.webSocket(
                host = serverAddress,
                port = 8080,
                path = "/ws"
            ) {
                session = this
                isConnected = true

                // STOMP CONNECT
                val connectFrame = "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000"
                send(Frame.Text(connectFrame))
                println("STOMP connected! 1- ok $connectFrame")
                // SUBSCRIBE
                val subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:/topic/public\n\n\u0000"
                send(Frame.Text(subscribeFrame))

                println("STOMP subscribeFrame! 2- ok $subscribeFrame")

                //  JOIN
                val joinMessage = ChatMessage(
                    type = "JOIN",
                    sender = username,
                    content = "$username joined the chat 111 "
                )


                println("")
                sendMessage(joinMessage)
                // receive messages
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val payload = frame.readText()
                        if (payload.startsWith("MESSAGE")) {
                            val body = payload.substringAfter("\n\n").trimEnd('\u0000')
                            try {
                                val message = json.decodeFromString(ChatMessage.serializer(), body)
                                _incomingMessages.emit(message)

                                println("receive message: $message")

                            } catch (e: Exception) {
                                println(" Failed to parse body: $body")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("WebSocket/STOMP connection failed: ${e.message}")
        }
    }



    suspend fun sendMessage(message: ChatMessage) {
        if (session != null) {
            val body = json.encodeToString(ChatMessage.serializer(), message)
            val frame = "SEND\ndestination:/app/chat.sendMessage\n\n$body\u0000"
            session!!.send(Frame.Text(frame))
            println("SEND message $message")
        } else {
            println(" Cannot send message, WebSocket session is not active")
        }
    }



    suspend fun disconnect(username: String) {
        try {
            val leaveMessage = ChatMessage(
                type = "LEAVE",
                sender = username,
                content = "$username left the chat"
            )
            sendMessage(leaveMessage)
            isConnected = false
        } catch (e: Exception) {
            println("Error during WebSocket disconnect: ${e.message}")
        }
    }
    private fun connectFrame() = "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\u0000"
    private fun subscribeFrame() = "SUBSCRIBE\nid:sub-0\ndestination:/topic/public\n\n\u0000"
    private suspend fun DefaultClientWebSocketSession.sendSerialized(message: ChatMessage) {
        val text = json.encodeToString(message)
        send(Frame.Text(text))
    }
}
