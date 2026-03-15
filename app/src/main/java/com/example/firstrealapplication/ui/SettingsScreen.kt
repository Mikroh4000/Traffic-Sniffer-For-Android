package com.example.firstrealapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstrealapplication.ui.components.AppHeader

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    settingsState: SettingsState? = null
) {
    // Fallback if no state provided (shouldn't happen when wired properly)
    val s = settingsState ?: return

    Scaffold(
        topBar = {
            AppHeader(
                title = "Settings",
                onSettingsClick = { /* Already in settings */ }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ━━━━━ CAPTURE SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.PlayArrow, title = "Capture") }

            item {
                SettingsToggle(
                    title = "Start capture on launch",
                    subtitle = "Automatically begin capturing when app opens",
                    checked = s.captureOnStart.value,
                    onCheckedChange = {
                        s.captureOnStart.value = it
                        s.saveBoolean("capture_on_start", it)
                    }
                )
            }
            item {
                SettingsSlider(
                    title = "Max log lines",
                    subtitle = "Maximum packet lines kept in memory",
                    value = s.maxPacketLogLines.value.toFloat(),
                    valueRange = 100f..5000f,
                    steps = 48,
                    valueLabel = "${s.maxPacketLogLines.value}",
                    onValueChange = {
                        val v = it.toInt()
                        s.maxPacketLogLines.value = v
                        s.saveInt("max_log_lines", v)
                    }
                )
            }
            item {
                SettingsSlider(
                    title = "Snap length (bytes)",
                    subtitle = "Max bytes captured per packet",
                    value = s.snapLength.value.toFloat(),
                    valueRange = 64f..65535f,
                    steps = 0,
                    valueLabel = "${s.snapLength.value}",
                    onValueChange = {
                        val v = it.toInt()
                        s.snapLength.value = v
                        s.saveInt("snap_length", v)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Promiscuous mode",
                    subtitle = "Capture all packets, not just those addressed to this device",
                    checked = s.promiscuousMode.value,
                    onCheckedChange = {
                        s.promiscuousMode.value = it
                        s.saveBoolean("promiscuous_mode", it)
                    }
                )
            }
            item {
                SettingsNumberField(
                    title = "Auto-stop after (MB)",
                    subtitle = "Stop capture after this size (0 = unlimited)",
                    value = s.autoStopAfterMb.value,
                    onValueChange = {
                        s.autoStopAfterMb.value = it
                        s.saveInt("auto_stop_mb", it)
                    }
                )
            }
            item {
                SettingsNumberField(
                    title = "Auto-stop after (minutes)",
                    subtitle = "Stop capture after this duration (0 = unlimited)",
                    value = s.autoStopAfterMinutes.value,
                    onValueChange = {
                        s.autoStopAfterMinutes.value = it
                        s.saveInt("auto_stop_minutes", it)
                    }
                )
            }

            // ━━━━━ PROTOCOL FILTERS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.FilterList, title = "Protocol Filters") }

            item {
                SettingsToggle("TCP", "Capture TCP packets", s.captureTcp.value) {
                    s.captureTcp.value = it; s.saveBoolean("capture_tcp", it)
                }
            }
            item {
                SettingsToggle("UDP", "Capture UDP packets", s.captureUdp.value) {
                    s.captureUdp.value = it; s.saveBoolean("capture_udp", it)
                }
            }
            item {
                SettingsToggle("ICMP", "Capture ICMP packets (ping)", s.captureIcmp.value) {
                    s.captureIcmp.value = it; s.saveBoolean("capture_icmp", it)
                }
            }
            item {
                SettingsToggle("DNS", "Capture DNS queries and responses", s.captureDns.value) {
                    s.captureDns.value = it; s.saveBoolean("capture_dns", it)
                }
            }
            item {
                SettingsToggle("HTTP", "Capture plain HTTP traffic", s.captureHttp.value) {
                    s.captureHttp.value = it; s.saveBoolean("capture_http", it)
                }
            }
            item {
                SettingsToggle("HTTPS / TLS", "Capture encrypted HTTPS traffic", s.captureHttps.value) {
                    s.captureHttps.value = it; s.saveBoolean("capture_https", it)
                }
            }

            // ━━━━━ VPN SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.VpnKey, title = "VPN Configuration") }

            item {
                SettingsTextField(
                    title = "VPN Address",
                    subtitle = "Local TUN interface IP address",
                    value = s.vpnAddress.value,
                    onValueChange = {
                        s.vpnAddress.value = it; s.saveString("vpn_address", it)
                    }
                )
            }
            item {
                SettingsTextField(
                    title = "DNS Server",
                    subtitle = "DNS server used by the VPN tunnel",
                    value = s.vpnDns.value,
                    onValueChange = {
                        s.vpnDns.value = it; s.saveString("vpn_dns", it)
                    }
                )
            }
            item {
                SettingsNumberField(
                    title = "MTU",
                    subtitle = "Maximum transmission unit size",
                    value = s.vpnMtu.value,
                    onValueChange = {
                        s.vpnMtu.value = it; s.saveInt("vpn_mtu", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Allow apps to bypass VPN",
                    subtitle = "Let apps that request it skip the VPN tunnel",
                    checked = s.vpnAllowBypass.value,
                    onCheckedChange = {
                        s.vpnAllowBypass.value = it; s.saveBoolean("vpn_allow_bypass", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Per-app VPN",
                    subtitle = "Only capture traffic from selected apps",
                    checked = s.vpnPerAppMode.value,
                    onCheckedChange = {
                        s.vpnPerAppMode.value = it; s.saveBoolean("vpn_per_app", it)
                    }
                )
            }
            if (s.vpnPerAppMode.value) {
                item {
                    SettingsClickable(
                        title = "Select Apps",
                        subtitle = if (s.vpnAllowedApps.value.isBlank()) "No apps selected"
                        else "${s.vpnAllowedApps.value.split(",").size} apps selected",
                        onClick = { onNavigateToDetail("App Selector") }
                    )
                }
            }

            // ━━━━━ PROXY / MITM SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.Security, title = "Proxy / MITM") }

            item {
                SettingsToggle(
                    title = "Enable MITM Proxy",
                    subtitle = "Run local proxy to inspect traffic content",
                    checked = s.proxyEnabled.value,
                    onCheckedChange = {
                        s.proxyEnabled.value = it; s.saveBoolean("proxy_enabled", it)
                    }
                )
            }
            if (s.proxyEnabled.value) {
                item {
                    SettingsNumberField(
                        title = "Proxy port",
                        subtitle = "Local port for the MITM proxy",
                        value = s.proxyPort.value,
                        onValueChange = {
                            s.proxyPort.value = it; s.saveInt("proxy_port", it)
                        }
                    )
                }
                item {
                    SettingsToggle(
                        title = "Decrypt TLS traffic",
                        subtitle = "Requires installing a custom CA certificate",
                        checked = s.mitmDecryptTls.value,
                        onCheckedChange = {
                            s.mitmDecryptTls.value = it; s.saveBoolean("mitm_decrypt_tls", it)
                        }
                    )
                }
                item {
                    SettingsClickable(
                        title = "Install CA Certificate",
                        subtitle = if (s.mitmCertInstalled.value) "Certificate installed ✓" else "Tap to install",
                        onClick = { onNavigateToDetail("CA Certificate") }
                    )
                }
            }

            // ━━━━━ DISPLAY / PARSER SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.Visibility, title = "Display & Parser") }

            item {
                SettingsToggle(
                    title = "Resolve hostnames",
                    subtitle = "Show domain names instead of IP addresses (slower)",
                    checked = s.resolveHostnames.value,
                    onCheckedChange = {
                        s.resolveHostnames.value = it; s.saveBoolean("resolve_hostnames", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Absolute timestamps",
                    subtitle = "Show wall-clock time vs. relative time since capture start",
                    checked = s.showAbsoluteTimestamps.value,
                    onCheckedChange = {
                        s.showAbsoluteTimestamps.value = it; s.saveBoolean("show_absolute_ts", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Hex dump payload",
                    subtitle = "Show packet payload as hexadecimal",
                    checked = s.hexDumpPayload.value,
                    onCheckedChange = {
                        s.hexDumpPayload.value = it; s.saveBoolean("hex_dump_payload", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Color-code protocols",
                    subtitle = "Use different colors for TCP, UDP, ICMP, etc.",
                    checked = s.colorCodeProtocols.value,
                    onCheckedChange = {
                        s.colorCodeProtocols.value = it; s.saveBoolean("color_protocols", it)
                    }
                )
            }
            item {
                SettingsSlider(
                    title = "Font size",
                    subtitle = "Packet list text size",
                    value = s.packetListFontSize.value.toFloat(),
                    valueRange = 8f..24f,
                    steps = 15,
                    valueLabel = "${s.packetListFontSize.value} sp",
                    onValueChange = {
                        val v = it.toInt()
                        s.packetListFontSize.value = v
                        s.saveInt("font_size", v)
                    }
                )
            }

            // ━━━━━ EXPORT / STORAGE SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.Save, title = "Export & Storage") }

            item {
                SettingsDropdown(
                    title = "Export format",
                    subtitle = "File format for saved captures",
                    selected = s.exportFormat.value,
                    options = listOf("pcap", "pcapng", "csv", "json"),
                    onSelected = {
                        s.exportFormat.value = it; s.saveString("export_format", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Auto-save captures",
                    subtitle = "Automatically save when capture stops",
                    checked = s.autoSave.value,
                    onCheckedChange = {
                        s.autoSave.value = it; s.saveBoolean("auto_save", it)
                    }
                )
            }
            item {
                SettingsNumberField(
                    title = "Max capture files",
                    subtitle = "Oldest files deleted when limit reached",
                    value = s.maxCaptureFiles.value,
                    onValueChange = {
                        s.maxCaptureFiles.value = it; s.saveInt("max_capture_files", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Compress exports",
                    subtitle = "Gzip-compress saved capture files",
                    checked = s.compressExports.value,
                    onCheckedChange = {
                        s.compressExports.value = it; s.saveBoolean("compress_exports", it)
                    }
                )
            }

            // ━━━━━ NOTIFICATION SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.Notifications, title = "Notifications") }

            item {
                SettingsToggle(
                    title = "Show notification",
                    subtitle = "Display persistent notification during capture",
                    checked = s.showNotification.value,
                    onCheckedChange = {
                        s.showNotification.value = it; s.saveBoolean("show_notification", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Show packet count",
                    subtitle = "Display live packet count in notification",
                    checked = s.showPacketCount.value,
                    onCheckedChange = {
                        s.showPacketCount.value = it; s.saveBoolean("show_packet_count", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Vibrate on first capture",
                    subtitle = "Vibrate once when the first packet is captured",
                    checked = s.vibrateOnCapture.value,
                    onCheckedChange = {
                        s.vibrateOnCapture.value = it; s.saveBoolean("vibrate_on_capture", it)
                    }
                )
            }

            // ━━━━━ ADVANCED SETTINGS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.Build, title = "Advanced") }

            item {
                SettingsSlider(
                    title = "Buffer size",
                    subtitle = "Kernel read buffer size",
                    value = s.bufferSizeKb.value.toFloat(),
                    valueRange = 16f..512f,
                    steps = 30,
                    valueLabel = "${s.bufferSizeKb.value} KB",
                    onValueChange = {
                        val v = it.toInt()
                        s.bufferSizeKb.value = v
                        s.saveInt("buffer_size_kb", v)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Enable IPv6",
                    subtitle = "Capture IPv6 traffic (experimental)",
                    checked = s.enableIpv6.value,
                    onCheckedChange = {
                        s.enableIpv6.value = it; s.saveBoolean("enable_ipv6", it)
                    }
                )
            }
            item {
                SettingsDropdown(
                    title = "Log level",
                    subtitle = "Verbosity of internal logging",
                    selected = s.logLevel.value,
                    options = listOf("Verbose", "Debug", "Info", "Warning", "Error"),
                    onSelected = {
                        s.logLevel.value = it; s.saveString("log_level", it)
                    }
                )
            }
            item {
                SettingsToggle(
                    title = "Keep screen on",
                    subtitle = "Prevent screen timeout while capturing",
                    checked = s.keepScreenOn.value,
                    onCheckedChange = {
                        s.keepScreenOn.value = it; s.saveBoolean("keep_screen_on", it)
                    }
                )
            }

            // ━━━━━ DANGER ZONE ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            item { SectionHeader(icon = Icons.Default.Warning, title = "Danger Zone", color = Color.Red) }

            item {
                var showResetDialog by remember { mutableStateOf(false) }
                SettingsClickable(
                    title = "Reset all settings",
                    subtitle = "Restore every option to its default value",
                    titleColor = Color.Red,
                    onClick = { showResetDialog = true }
                )
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset Settings?") },
                        text = { Text("All settings will be restored to defaults. This cannot be undone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                s.resetAll()
                                showResetDialog = false
                            }) { Text("Reset", color = Color.Red) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }
            item {
                var showDeleteDialog by remember { mutableStateOf(false) }
                SettingsClickable(
                    title = "Delete all captures",
                    subtitle = "Remove all saved .pcap files from storage",
                    titleColor = Color.Red,
                    onClick = { showDeleteDialog = true }
                )
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete All Captures?") },
                        text = { Text("Every saved capture file will be permanently deleted.") },
                        confirmButton = {
                            TextButton(onClick = {
                                // TODO: delete capture files from storage
                                showDeleteDialog = false
                            }) { Text("Delete", color = Color.Red) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── Reusable Setting Components ──────────────────────────────────────────

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    color: Color = Color.Black
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(text = title, color = Color.Black) },
        supportingContent = { Text(text = subtitle, color = Color.Gray, fontSize = 12.sp) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.Black
                )
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.White)
    )
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}

@Composable
fun SettingsTextField(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = title, color = Color.Black, fontWeight = FontWeight.Medium)
        Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}

@Composable
fun SettingsNumberField(
    title: String,
    subtitle: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = title, color = Color.Black, fontWeight = FontWeight.Medium)
        Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { text ->
                text.toIntOrNull()?.let { onValueChange(it) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp),
            shape = RoundedCornerShape(8.dp)
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}

@Composable
fun SettingsSlider(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.Black, fontWeight = FontWeight.Medium)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Text(
                text = valueLabel,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color.Black,
                activeTrackColor = Color.Black
            )
        )
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}

@Composable
fun SettingsDropdown(
    title: String,
    subtitle: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = title, color = Color.Black, fontWeight = FontWeight.Medium)
        Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(text = selected)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}

@Composable
fun SettingsClickable(
    title: String,
    subtitle: String,
    titleColor: Color = Color.Black,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = title, color = titleColor) },
        supportingContent = { Text(text = subtitle, color = Color.Gray, fontSize = 12.sp) },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.White)
    )
    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
}
