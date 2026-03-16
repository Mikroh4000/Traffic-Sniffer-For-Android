package com.example.firstrealapplication.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(
    name = "sniffer_settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "sniffer_settings"))
    }
)

/**
 * Central settings state backed by DataStore Preferences.
 * Holds all configurable options for the traffic sniffer.
 */
class SettingsState(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private object Defaults {
        const val CAPTURE_ON_START = false
        const val MAX_PACKET_LOG_LINES = 500
        const val SNAP_LENGTH = 65535
        const val PROMISCUOUS_MODE = false
        const val AUTO_STOP_AFTER_MB = 0
        const val AUTO_STOP_AFTER_MINUTES = 0

        const val CAPTURE_TCP = true
        const val CAPTURE_UDP = true
        const val CAPTURE_ICMP = true
        const val CAPTURE_DNS = true
        const val CAPTURE_HTTP = true
        const val CAPTURE_HTTPS = true

        const val VPN_ADDRESS = "10.0.0.2"
        const val VPN_DNS = "8.8.8.8"
        const val VPN_MTU = 1500
        const val VPN_ALLOW_BYPASS = false
        const val VPN_PER_APP_MODE = false
        const val VPN_ALLOWED_APPS = ""

        const val PROXY_ENABLED = false
        const val PROXY_PORT = 8899
        const val MITM_DECRYPT_TLS = false
        const val MITM_CERT_INSTALLED = false

        const val RESOLVE_HOSTNAMES = false
        const val SHOW_ABSOLUTE_TIMESTAMPS = true
        const val HEX_DUMP_PAYLOAD = false
        const val COLOR_CODE_PROTOCOLS = true
        const val PACKET_LIST_FONT_SIZE = 12

        const val EXPORT_FORMAT = "pcap"
        const val AUTO_SAVE = true
        const val MAX_CAPTURE_FILES = 10
        const val COMPRESS_EXPORTS = false

        const val SHOW_NOTIFICATION = true
        const val SHOW_PACKET_COUNT = true
        const val VIBRATE_ON_CAPTURE = false

        const val BUFFER_SIZE_KB = 64
        const val ENABLE_IPV6 = false
        const val LOG_LEVEL = "Info"
        const val KEEP_SCREEN_ON = false
    }

    private object Keys {
        val CAPTURE_ON_START = booleanPreferencesKey("capture_on_start")
        val MAX_LOG_LINES = intPreferencesKey("max_log_lines")
        val SNAP_LENGTH = intPreferencesKey("snap_length")
        val PROMISCUOUS_MODE = booleanPreferencesKey("promiscuous_mode")
        val AUTO_STOP_MB = intPreferencesKey("auto_stop_mb")
        val AUTO_STOP_MINUTES = intPreferencesKey("auto_stop_minutes")

        val CAPTURE_TCP = booleanPreferencesKey("capture_tcp")
        val CAPTURE_UDP = booleanPreferencesKey("capture_udp")
        val CAPTURE_ICMP = booleanPreferencesKey("capture_icmp")
        val CAPTURE_DNS = booleanPreferencesKey("capture_dns")
        val CAPTURE_HTTP = booleanPreferencesKey("capture_http")
        val CAPTURE_HTTPS = booleanPreferencesKey("capture_https")

        val VPN_ADDRESS = stringPreferencesKey("vpn_address")
        val VPN_DNS = stringPreferencesKey("vpn_dns")
        val VPN_MTU = intPreferencesKey("vpn_mtu")
        val VPN_ALLOW_BYPASS = booleanPreferencesKey("vpn_allow_bypass")
        val VPN_PER_APP = booleanPreferencesKey("vpn_per_app")
        val VPN_ALLOWED_APPS = stringPreferencesKey("vpn_allowed_apps")

        val PROXY_ENABLED = booleanPreferencesKey("proxy_enabled")
        val PROXY_PORT = intPreferencesKey("proxy_port")
        val MITM_DECRYPT_TLS = booleanPreferencesKey("mitm_decrypt_tls")
        val MITM_CERT_INSTALLED = booleanPreferencesKey("mitm_cert_installed")

        val RESOLVE_HOSTNAMES = booleanPreferencesKey("resolve_hostnames")
        val SHOW_ABSOLUTE_TS = booleanPreferencesKey("show_absolute_ts")
        val HEX_DUMP_PAYLOAD = booleanPreferencesKey("hex_dump_payload")
        val COLOR_PROTOCOLS = booleanPreferencesKey("color_protocols")
        val FONT_SIZE = intPreferencesKey("font_size")

        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val MAX_CAPTURE_FILES = intPreferencesKey("max_capture_files")
        val COMPRESS_EXPORTS = booleanPreferencesKey("compress_exports")

        val SHOW_NOTIFICATION = booleanPreferencesKey("show_notification")
        val SHOW_PACKET_COUNT = booleanPreferencesKey("show_packet_count")
        val VIBRATE_ON_CAPTURE = booleanPreferencesKey("vibrate_on_capture")

        val BUFFER_SIZE_KB = intPreferencesKey("buffer_size_kb")
        val ENABLE_IPV6 = booleanPreferencesKey("enable_ipv6")
        val LOG_LEVEL = stringPreferencesKey("log_level")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    // ── Capture Settings ──────────────────────────────────────────────
    val captureOnStart = mutableStateOf(Defaults.CAPTURE_ON_START)
    val maxPacketLogLines = mutableStateOf(Defaults.MAX_PACKET_LOG_LINES)
    val snapLength = mutableStateOf(Defaults.SNAP_LENGTH)
    val promiscuousMode = mutableStateOf(Defaults.PROMISCUOUS_MODE)
    val autoStopAfterMb = mutableStateOf(Defaults.AUTO_STOP_AFTER_MB) // 0 = unlimited
    val autoStopAfterMinutes = mutableStateOf(Defaults.AUTO_STOP_AFTER_MINUTES)

    // ── Protocol Filters ──────────────────────────────────────────────
    val captureTcp = mutableStateOf(Defaults.CAPTURE_TCP)
    val captureUdp = mutableStateOf(Defaults.CAPTURE_UDP)
    val captureIcmp = mutableStateOf(Defaults.CAPTURE_ICMP)
    val captureDns = mutableStateOf(Defaults.CAPTURE_DNS)
    val captureHttp = mutableStateOf(Defaults.CAPTURE_HTTP)
    val captureHttps = mutableStateOf(Defaults.CAPTURE_HTTPS)

    // ── VPN Settings ──────────────────────────────────────────────────
    val vpnAddress = mutableStateOf(Defaults.VPN_ADDRESS)
    val vpnDns = mutableStateOf(Defaults.VPN_DNS)
    val vpnMtu = mutableStateOf(Defaults.VPN_MTU)
    val vpnAllowBypass = mutableStateOf(Defaults.VPN_ALLOW_BYPASS)
    val vpnPerAppMode = mutableStateOf(Defaults.VPN_PER_APP_MODE)
    val vpnAllowedApps = mutableStateOf(Defaults.VPN_ALLOWED_APPS)

    // ── Proxy / MITM Settings ─────────────────────────────────────────
    val proxyEnabled = mutableStateOf(Defaults.PROXY_ENABLED)
    val proxyPort = mutableStateOf(Defaults.PROXY_PORT)
    val mitmDecryptTls = mutableStateOf(Defaults.MITM_DECRYPT_TLS)
    val mitmCertInstalled = mutableStateOf(Defaults.MITM_CERT_INSTALLED)

    // ── Parser / Display Settings ─────────────────────────────────────
    val resolveHostnames = mutableStateOf(Defaults.RESOLVE_HOSTNAMES)
    val showAbsoluteTimestamps = mutableStateOf(Defaults.SHOW_ABSOLUTE_TIMESTAMPS)
    val hexDumpPayload = mutableStateOf(Defaults.HEX_DUMP_PAYLOAD)
    val colorCodeProtocols = mutableStateOf(Defaults.COLOR_CODE_PROTOCOLS)
    val packetListFontSize = mutableStateOf(Defaults.PACKET_LIST_FONT_SIZE)

    // ── Export / Storage Settings ─────────────────────────────────────
    val exportFormat = mutableStateOf(Defaults.EXPORT_FORMAT)
    val autoSave = mutableStateOf(Defaults.AUTO_SAVE)
    val maxCaptureFiles = mutableStateOf(Defaults.MAX_CAPTURE_FILES)
    val compressExports = mutableStateOf(Defaults.COMPRESS_EXPORTS)

    // ── Notification Settings ─────────────────────────────────────────
    val showNotification = mutableStateOf(Defaults.SHOW_NOTIFICATION)
    val showPacketCount = mutableStateOf(Defaults.SHOW_PACKET_COUNT)
    val vibrateOnCapture = mutableStateOf(Defaults.VIBRATE_ON_CAPTURE)

    // ── Advanced Settings ─────────────────────────────────────────────
    val bufferSizeKb = mutableStateOf(Defaults.BUFFER_SIZE_KB)
    val enableIpv6 = mutableStateOf(Defaults.ENABLE_IPV6)
    val logLevel = mutableStateOf(Defaults.LOG_LEVEL)
    val keepScreenOn = mutableStateOf(Defaults.KEEP_SCREEN_ON)

    init {
        scope.launch {
            appContext.dataStore.data.collectFromDataStore()
        }
    }

    // ── Persistence helpers ───────────────────────────────────────────

    fun saveBoolean(key: String, value: Boolean) {
        scope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[booleanPreferencesKey(key)] = value
            }
        }
    }

    fun saveInt(key: String, value: Int) {
        scope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[intPreferencesKey(key)] = value
            }
        }
    }

    fun saveString(key: String, value: String) {
        scope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[stringPreferencesKey(key)] = value
            }
        }
    }

    fun resetAll() {
        scope.launch {
            appContext.dataStore.edit { it.clear() }
        }
        // Reset all in-memory state to defaults
        captureOnStart.value = Defaults.CAPTURE_ON_START
        maxPacketLogLines.value = Defaults.MAX_PACKET_LOG_LINES
        snapLength.value = Defaults.SNAP_LENGTH
        promiscuousMode.value = Defaults.PROMISCUOUS_MODE
        autoStopAfterMb.value = Defaults.AUTO_STOP_AFTER_MB
        autoStopAfterMinutes.value = Defaults.AUTO_STOP_AFTER_MINUTES
        captureTcp.value = Defaults.CAPTURE_TCP
        captureUdp.value = Defaults.CAPTURE_UDP
        captureIcmp.value = Defaults.CAPTURE_ICMP
        captureDns.value = Defaults.CAPTURE_DNS
        captureHttp.value = Defaults.CAPTURE_HTTP
        captureHttps.value = Defaults.CAPTURE_HTTPS
        vpnAddress.value = Defaults.VPN_ADDRESS
        vpnDns.value = Defaults.VPN_DNS
        vpnMtu.value = Defaults.VPN_MTU
        vpnAllowBypass.value = Defaults.VPN_ALLOW_BYPASS
        vpnPerAppMode.value = Defaults.VPN_PER_APP_MODE
        vpnAllowedApps.value = Defaults.VPN_ALLOWED_APPS
        proxyEnabled.value = Defaults.PROXY_ENABLED
        proxyPort.value = Defaults.PROXY_PORT
        mitmDecryptTls.value = Defaults.MITM_DECRYPT_TLS
        mitmCertInstalled.value = Defaults.MITM_CERT_INSTALLED
        resolveHostnames.value = Defaults.RESOLVE_HOSTNAMES
        showAbsoluteTimestamps.value = Defaults.SHOW_ABSOLUTE_TIMESTAMPS
        hexDumpPayload.value = Defaults.HEX_DUMP_PAYLOAD
        colorCodeProtocols.value = Defaults.COLOR_CODE_PROTOCOLS
        packetListFontSize.value = Defaults.PACKET_LIST_FONT_SIZE
        exportFormat.value = Defaults.EXPORT_FORMAT
        autoSave.value = Defaults.AUTO_SAVE
        maxCaptureFiles.value = Defaults.MAX_CAPTURE_FILES
        compressExports.value = Defaults.COMPRESS_EXPORTS
        showNotification.value = Defaults.SHOW_NOTIFICATION
        showPacketCount.value = Defaults.SHOW_PACKET_COUNT
        vibrateOnCapture.value = Defaults.VIBRATE_ON_CAPTURE
        bufferSizeKb.value = Defaults.BUFFER_SIZE_KB
        enableIpv6.value = Defaults.ENABLE_IPV6
        logLevel.value = Defaults.LOG_LEVEL
        keepScreenOn.value = Defaults.KEEP_SCREEN_ON
    }

    private suspend fun kotlinx.coroutines.flow.Flow<Preferences>.collectFromDataStore() {
        collect { prefs ->
            captureOnStart.value = prefs[Keys.CAPTURE_ON_START] ?: Defaults.CAPTURE_ON_START
            maxPacketLogLines.value = prefs[Keys.MAX_LOG_LINES] ?: Defaults.MAX_PACKET_LOG_LINES
            snapLength.value = prefs[Keys.SNAP_LENGTH] ?: Defaults.SNAP_LENGTH
            promiscuousMode.value = prefs[Keys.PROMISCUOUS_MODE] ?: Defaults.PROMISCUOUS_MODE
            autoStopAfterMb.value = prefs[Keys.AUTO_STOP_MB] ?: Defaults.AUTO_STOP_AFTER_MB
            autoStopAfterMinutes.value = prefs[Keys.AUTO_STOP_MINUTES] ?: Defaults.AUTO_STOP_AFTER_MINUTES

            captureTcp.value = prefs[Keys.CAPTURE_TCP] ?: Defaults.CAPTURE_TCP
            captureUdp.value = prefs[Keys.CAPTURE_UDP] ?: Defaults.CAPTURE_UDP
            captureIcmp.value = prefs[Keys.CAPTURE_ICMP] ?: Defaults.CAPTURE_ICMP
            captureDns.value = prefs[Keys.CAPTURE_DNS] ?: Defaults.CAPTURE_DNS
            captureHttp.value = prefs[Keys.CAPTURE_HTTP] ?: Defaults.CAPTURE_HTTP
            captureHttps.value = prefs[Keys.CAPTURE_HTTPS] ?: Defaults.CAPTURE_HTTPS

            vpnAddress.value = prefs[Keys.VPN_ADDRESS] ?: Defaults.VPN_ADDRESS
            vpnDns.value = prefs[Keys.VPN_DNS] ?: Defaults.VPN_DNS
            vpnMtu.value = prefs[Keys.VPN_MTU] ?: Defaults.VPN_MTU
            vpnAllowBypass.value = prefs[Keys.VPN_ALLOW_BYPASS] ?: Defaults.VPN_ALLOW_BYPASS
            vpnPerAppMode.value = prefs[Keys.VPN_PER_APP] ?: Defaults.VPN_PER_APP_MODE
            vpnAllowedApps.value = prefs[Keys.VPN_ALLOWED_APPS] ?: Defaults.VPN_ALLOWED_APPS

            proxyEnabled.value = prefs[Keys.PROXY_ENABLED] ?: Defaults.PROXY_ENABLED
            proxyPort.value = prefs[Keys.PROXY_PORT] ?: Defaults.PROXY_PORT
            mitmDecryptTls.value = prefs[Keys.MITM_DECRYPT_TLS] ?: Defaults.MITM_DECRYPT_TLS
            mitmCertInstalled.value = prefs[Keys.MITM_CERT_INSTALLED] ?: Defaults.MITM_CERT_INSTALLED

            resolveHostnames.value = prefs[Keys.RESOLVE_HOSTNAMES] ?: Defaults.RESOLVE_HOSTNAMES
            showAbsoluteTimestamps.value = prefs[Keys.SHOW_ABSOLUTE_TS] ?: Defaults.SHOW_ABSOLUTE_TIMESTAMPS
            hexDumpPayload.value = prefs[Keys.HEX_DUMP_PAYLOAD] ?: Defaults.HEX_DUMP_PAYLOAD
            colorCodeProtocols.value = prefs[Keys.COLOR_PROTOCOLS] ?: Defaults.COLOR_CODE_PROTOCOLS
            packetListFontSize.value = prefs[Keys.FONT_SIZE] ?: Defaults.PACKET_LIST_FONT_SIZE

            exportFormat.value = prefs[Keys.EXPORT_FORMAT] ?: Defaults.EXPORT_FORMAT
            autoSave.value = prefs[Keys.AUTO_SAVE] ?: Defaults.AUTO_SAVE
            maxCaptureFiles.value = prefs[Keys.MAX_CAPTURE_FILES] ?: Defaults.MAX_CAPTURE_FILES
            compressExports.value = prefs[Keys.COMPRESS_EXPORTS] ?: Defaults.COMPRESS_EXPORTS

            showNotification.value = prefs[Keys.SHOW_NOTIFICATION] ?: Defaults.SHOW_NOTIFICATION
            showPacketCount.value = prefs[Keys.SHOW_PACKET_COUNT] ?: Defaults.SHOW_PACKET_COUNT
            vibrateOnCapture.value = prefs[Keys.VIBRATE_ON_CAPTURE] ?: Defaults.VIBRATE_ON_CAPTURE

            bufferSizeKb.value = prefs[Keys.BUFFER_SIZE_KB] ?: Defaults.BUFFER_SIZE_KB
            enableIpv6.value = prefs[Keys.ENABLE_IPV6] ?: Defaults.ENABLE_IPV6
            logLevel.value = prefs[Keys.LOG_LEVEL] ?: Defaults.LOG_LEVEL
            keepScreenOn.value = prefs[Keys.KEEP_SCREEN_ON] ?: Defaults.KEEP_SCREEN_ON
        }
    }
}
