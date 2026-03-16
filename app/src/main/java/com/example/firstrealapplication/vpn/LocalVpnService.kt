package com.example.firstrealapplication.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firstrealapplication.capture.PacketParser
import com.example.firstrealapplication.capture.PcapWriter
import com.example.firstrealapplication.capture.TunReader
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Android VpnService that creates a TUN interface to intercept all device traffic.
 * Reads raw IP packets, parses them, optionally writes to pcap, and forwards them.
 */
class LocalVpnService : VpnService() {

    companion object {
        const val TAG = "LocalVpnService"
        const val ACTION_START = "com.example.firstrealapplication.vpn.START"
        const val ACTION_STOP = "com.example.firstrealapplication.vpn.STOP"
        const val EXTRA_SNAP_LENGTH = "com.example.firstrealapplication.vpn.EXTRA_SNAP_LENGTH"
        const val NOTIFICATION_CHANNEL_ID = "vpn_channel"
        const val NOTIFICATION_ID = 1
        const val DEFAULT_SNAP_LENGTH = 65535

        var instance: LocalVpnService? = null
            private set

        // Callback to push parsed packet summaries to the UI
        var onPacketCaptured: ((String) -> Unit)? = null
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunReader: TunReader? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pcapWriter: PcapWriter? = null
    private var currentSnapLength = DEFAULT_SNAP_LENGTH

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                START_NOT_STICKY
            }
            else -> {
                currentSnapLength = intent
                    ?.getIntExtra(EXTRA_SNAP_LENGTH, DEFAULT_SNAP_LENGTH)
                    ?.coerceIn(64, DEFAULT_SNAP_LENGTH)
                    ?: DEFAULT_SNAP_LENGTH
                startVpn()
                START_STICKY
            }
        }
    }

    private fun startVpn() {
        if (vpnInterface != null) return

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Traffic Sniffer Active")
            .setContentText("Capturing network packets…")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        try {
            vpnInterface = Builder()
                .setSession("TrafficSniffer")
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .setMtu(1500)
                .setBlocking(true)
                .establish()

            vpnInterface?.let { pfd ->
                val inputStream = FileInputStream(pfd.fileDescriptor)
                val outputStream = FileOutputStream(pfd.fileDescriptor)

                // Initialize pcap writer (writes to app-internal storage)
                pcapWriter = PcapWriter(filesDir, currentSnapLength).also { it.open() }

                // Initialize the TUN reader that processes packets
                tunReader = TunReader(
                    inputStream = inputStream,
                    outputStream = outputStream,
                    onPacket = { rawPacket ->
                        handlePacket(rawPacket)
                    }
                )

                // Start reading packets on a background coroutine
                serviceScope.launch {
                    tunReader?.readLoop()
                }
            }

            Log.i(TAG, "VPN started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopVpn()
        }
    }

    private fun handlePacket(rawPacket: ByteArray) {
        // Parse the raw IP packet
        val parsed = PacketParser.parse(rawPacket) ?: return

        // Snap length affects stored bytes in pcap, not parser input.
        val capturedLength = minOf(rawPacket.size, currentSnapLength)
        val packetForPcap = if (capturedLength == rawPacket.size) rawPacket else rawPacket.copyOf(capturedLength)
        pcapWriter?.writePacket(packetForPcap, rawPacket.size)

        // Build a summary string and deliver to UI
        val summary = PacketParser.summarize(parsed)
        onPacketCaptured?.invoke(summary)
    }

    private fun stopVpn() {
        serviceScope.cancel()
        tunReader?.stop()
        tunReader = null

        pcapWriter?.close()
        pcapWriter = null

        vpnInterface?.close()
        vpnInterface = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.i(TAG, "VPN stopped")
    }

    override fun onDestroy() {
        stopVpn()
        instance = null
        super.onDestroy()
    }

    fun getCurrentPcapFile(): java.io.File? = pcapWriter?.getCurrentFile()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
