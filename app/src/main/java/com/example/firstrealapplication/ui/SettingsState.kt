package com.example.firstrealapplication.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

/**
 * Central settings state backed by SharedPreferences.
 * Holds all configurable options for the traffic sniffer.
 */
class SettingsState(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sniffer_settings", Context.MODE_PRIVATE)

    // ── Capture Settings ──────────────────────────────────────────────
    val captureOnStart = mutableStateOf(prefs.getBoolean("capture_on_start", false))
    val maxPacketLogLines = mutableStateOf(prefs.getInt("max_log_lines", 500))
    val snapLength = mutableStateOf(prefs.getInt("snap_length", 65535))
    val promiscuousMode = mutableStateOf(prefs.getBoolean("promiscuous_mode", false))
    val autoStopAfterMb = mutableStateOf(prefs.getInt("auto_stop_mb", 0)) // 0 = unlimited
    val autoStopAfterMinutes = mutableStateOf(prefs.getInt("auto_stop_minutes", 0))

    // ── Protocol Filters ──────────────────────────────────────────────
    val captureTcp = mutableStateOf(prefs.getBoolean("capture_tcp", true))
    val captureUdp = mutableStateOf(prefs.getBoolean("capture_udp", true))
    val captureIcmp = mutableStateOf(prefs.getBoolean("capture_icmp", true))
    val captureDns = mutableStateOf(prefs.getBoolean("capture_dns", true))
    val captureHttp = mutableStateOf(prefs.getBoolean("capture_http", true))
    val captureHttps = mutableStateOf(prefs.getBoolean("capture_https", true))

    // ── VPN Settings ──────────────────────────────────────────────────
    val vpnAddress = mutableStateOf(prefs.getString("vpn_address", "10.0.0.2") ?: "10.0.0.2")
    val vpnDns = mutableStateOf(prefs.getString("vpn_dns", "8.8.8.8") ?: "8.8.8.8")
    val vpnMtu = mutableStateOf(prefs.getInt("vpn_mtu", 1500))
    val vpnAllowBypass = mutableStateOf(prefs.getBoolean("vpn_allow_bypass", false))
    val vpnPerAppMode = mutableStateOf(prefs.getBoolean("vpn_per_app", false))
    val vpnAllowedApps = mutableStateOf(prefs.getString("vpn_allowed_apps", "") ?: "")

    // ── Proxy / MITM Settings ─────────────────────────────────────────
    val proxyEnabled = mutableStateOf(prefs.getBoolean("proxy_enabled", false))
    val proxyPort = mutableStateOf(prefs.getInt("proxy_port", 8899))
    val mitmDecryptTls = mutableStateOf(prefs.getBoolean("mitm_decrypt_tls", false))
    val mitmCertInstalled = mutableStateOf(prefs.getBoolean("mitm_cert_installed", false))

    // ── Parser / Display Settings ─────────────────────────────────────
    val resolveHostnames = mutableStateOf(prefs.getBoolean("resolve_hostnames", false))
    val showAbsoluteTimestamps = mutableStateOf(prefs.getBoolean("show_absolute_ts", true))
    val hexDumpPayload = mutableStateOf(prefs.getBoolean("hex_dump_payload", false))
    val colorCodeProtocols = mutableStateOf(prefs.getBoolean("color_protocols", true))
    val packetListFontSize = mutableStateOf(prefs.getInt("font_size", 12))

    // ── Export / Storage Settings ─────────────────────────────────────
    val exportFormat = mutableStateOf(prefs.getString("export_format", "pcap") ?: "pcap")
    val autoSave = mutableStateOf(prefs.getBoolean("auto_save", true))
    val maxCaptureFiles = mutableStateOf(prefs.getInt("max_capture_files", 10))
    val compressExports = mutableStateOf(prefs.getBoolean("compress_exports", false))

    // ── Notification Settings ─────────────────────────────────────────
    val showNotification = mutableStateOf(prefs.getBoolean("show_notification", true))
    val showPacketCount = mutableStateOf(prefs.getBoolean("show_packet_count", true))
    val vibrateOnCapture = mutableStateOf(prefs.getBoolean("vibrate_on_capture", false))

    // ── Advanced Settings ─────────────────────────────────────────────
    val bufferSizeKb = mutableStateOf(prefs.getInt("buffer_size_kb", 64))
    val enableIpv6 = mutableStateOf(prefs.getBoolean("enable_ipv6", false))
    val logLevel = mutableStateOf(prefs.getString("log_level", "Info") ?: "Info")
    val keepScreenOn = mutableStateOf(prefs.getBoolean("keep_screen_on", false))

    // ── Persistence helpers ───────────────────────────────────────────

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun saveInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    fun saveString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun resetAll() {
        prefs.edit { clear() }
        // Reset all in-memory state to defaults
        captureOnStart.value = false
        maxPacketLogLines.value = 500
        snapLength.value = 65535
        promiscuousMode.value = false
        autoStopAfterMb.value = 0
        autoStopAfterMinutes.value = 0
        captureTcp.value = true
        captureUdp.value = true
        captureIcmp.value = true
        captureDns.value = true
        captureHttp.value = true
        captureHttps.value = true
        vpnAddress.value = "10.0.0.2"
        vpnDns.value = "8.8.8.8"
        vpnMtu.value = 1500
        vpnAllowBypass.value = false
        vpnPerAppMode.value = false
        vpnAllowedApps.value = ""
        proxyEnabled.value = false
        proxyPort.value = 8899
        mitmDecryptTls.value = false
        mitmCertInstalled.value = false
        resolveHostnames.value = false
        showAbsoluteTimestamps.value = true
        hexDumpPayload.value = false
        colorCodeProtocols.value = true
        packetListFontSize.value = 12
        exportFormat.value = "pcap"
        autoSave.value = true
        maxCaptureFiles.value = 10
        compressExports.value = false
        showNotification.value = true
        showPacketCount.value = true
        vibrateOnCapture.value = false
        bufferSizeKb.value = 64
        enableIpv6.value = false
        logLevel.value = "Info"
        keepScreenOn.value = false
    }
}
