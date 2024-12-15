package com.amichon.clipboardsync

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


            if (hasAllPermissions()) {
                startClipboardSyncService()
            } else {
                requestPermissions()
            }
    }

    private fun hasAllPermissions(): Boolean {
        val requiredPermissions = listOf(
            android.Manifest.permission.ACCESS_WIFI_STATE
        )
        return requiredPermissions.all { permission ->
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_WIFI_STATE
        )
        permissionsLauncher.launch(permissions)
    }

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.all { it }
            if (granted) {
                startClipboardSyncService()
            } else {
                println("Required permissions not granted. The service cannot start.")
            }
        }

    private fun startClipboardSyncService() {
        val intent = Intent(this, ClipboardSyncService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
         finish()
    }
}
