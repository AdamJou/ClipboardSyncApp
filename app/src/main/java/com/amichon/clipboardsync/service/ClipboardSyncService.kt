package com.amichon.clipboardsync

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder

class ClipboardSyncService : Service(), WebSocketHandler.WebSocketEvents {

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var webSocketHandler: WebSocketHandler
    private lateinit var clipboardHandler: ClipboardHandler

    private var lastSentText: String? = null
    private var lastReceivedText: String? = null
    private var lastSentImage: String? = null
    private var lastReceivedImage: String? = null

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardHandler = ClipboardHandler(this, clipboardManager)

        setupWebSocket()
        startForegroundService()
    }

    private fun setupWebSocket() {
        webSocketHandler = WebSocketHandler(BuildConfig.WEBSOCKET_URL, this)
        webSocketHandler.connect()
    }

    override fun onTextMessageReceived(receivedText: String) {
        if (receivedText != lastReceivedText && receivedText != lastSentText) {
            lastReceivedText = receivedText
            clipboardHandler.updateClipboard(receivedText)
        }
    }

    override fun onImageMessageReceived(receivedImageBase64: String) {
        if (receivedImageBase64 != lastReceivedImage && receivedImageBase64 != lastSentImage) {
            lastReceivedImage = receivedImageBase64
            val image: Bitmap = ImageUtils.decodeBase64ToBitmap(receivedImageBase64)
            clipboardHandler.updateClipboardWithImage(image)
        }
    }

    @SuppressLint("NewApi", "ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "ClipboardSyncChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this, channelId, "Clipboard Sync Service")
        }

        val notification = NotificationUtils.createServiceNotification(this, channelId)
        startForeground(1, notification)
    }

    override fun onWebSocketError(t: Throwable) {
        t.printStackTrace()
    }

    override fun onWebSocketClosed(reason: String) {
        println("WebSocket closed: $reason")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        webSocketHandler.close()
        super.onDestroy()
    }
}
