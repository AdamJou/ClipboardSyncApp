package com.amichon.clipboardsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NotificationUtils {

    fun createNotificationChannelIfNeeded(context: Context, channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun createServiceNotification(context: Context, channelId: String): Notification {
        val stopIntent = Intent(context, ClipboardSyncService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelId)
        } else {
            Notification.Builder(context)
        }

        return builder
            .setContentTitle("Clipboard Sync")
            .setContentText("Synchronizing clipboard in the background")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .addAction(
                android.R.drawable.ic_delete,
                "Stop Service",
                stopPendingIntent
            )
            .build()
    }
}
