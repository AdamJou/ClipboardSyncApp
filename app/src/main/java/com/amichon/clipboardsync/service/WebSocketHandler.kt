package com.amichon.clipboardsync

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class WebSocketHandler(
    private val url: String,
    private val events: WebSocketEvents
) {

    interface WebSocketEvents {
        fun onTextMessageReceived(receivedText: String)
        fun onImageMessageReceived(receivedImageBase64: String)
        fun onWebSocketError(t: Throwable)
        fun onWebSocketClosed(reason: String)
    }

    private var webSocket: WebSocket? = null

    fun connect() {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    if (json.has("type")) {
                        val type = json.getString("type")
                        when (type) {
                            "clipboard" -> {
                                if (json.has("data")) {
                                    val receivedText = json.getString("data")
                                    events.onTextMessageReceived(receivedText)
                                } else {
                                    println("JSON message missing 'data' field for clipboard type")
                                }
                            }

                            "clipboard-image" -> {
                                if (json.has("data")) {
                                    val receivedImageBase64 = json.getString("data")
                                    events.onImageMessageReceived(receivedImageBase64)
                                } else {
                                    println("JSON message missing 'data' field for clipboard-image type")
                                }
                            }

                            else -> {
                                println("Unknown message type received: $type")
                            }
                        }
                    } else {
                        println("JSON message missing 'type' field")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                events.onWebSocketError(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                events.onWebSocketClosed(reason)
            }
        })
    }

    fun close() {
        webSocket?.close(1000, null)
    }
}
