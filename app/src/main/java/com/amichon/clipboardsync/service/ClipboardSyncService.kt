package com.amichon.clipboardsync

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

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

    @SuppressLint("MissingPermission")
    private fun setupWebSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            val baseIp = getLocalBaseIp()
            if (baseIp != null) {
                val serverIp = findServerInNetwork(baseIp, 8080)
                if (serverIp != null) {
                    val webSocketUrl = "ws://$serverIp:8080"
                    println("Server found at $serverIp. Connecting to $webSocketUrl...")
                    webSocketHandler = WebSocketHandler(webSocketUrl, this@ClipboardSyncService)
                    webSocketHandler.connect()
                } else {
                    println("No WebSocket server found in the network.")
                }
            } else {
                println("Could not determine the local base IP address.")
            }
        }
    }



    private fun getLocalBaseIp(): String? {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return if (ipAddress != 0) {
            val ipBytes = ByteArray(4)
            for (i in 0..3) {
                ipBytes[i] = (ipAddress shr (i * 8) and 0xFF).toByte()
            }
            val localIp = InetAddress.getByAddress(ipBytes).hostAddress
            localIp.substringBeforeLast(".")
        } else {
            null
        }
    }

    private suspend fun findServerInNetwork(baseIp: String, port: Int): String? {
        for (i in 1..254) {
            val testIp = "$baseIp.$i"
            if (isPortOpen(testIp, port)) {
                return testIp
            }
        }
        return null
    }

    private suspend fun isPortOpen(ip: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 200)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
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

    override fun onTextMessageReceived(receivedText: String) {
        if (receivedText != lastReceivedText && receivedText != lastSentText) {
            lastReceivedText = receivedText
            clipboardHandler.updateClipboard(receivedText)
        }
    }

    override fun onImageMessageReceived(receivedImageBase64: String) {
        if (receivedImageBase64 != lastReceivedImage && receivedImageBase64 != lastSentImage) {
            lastReceivedImage = receivedImageBase64
            val image = ImageUtils.decodeBase64ToBitmap(receivedImageBase64)
            clipboardHandler.updateClipboardWithImage(image)
        }
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
