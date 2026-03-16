package com.example.firstrealapplication

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firstrealapplication.ui.SettingsScreen
import com.example.firstrealapplication.ui.SettingsState
import com.example.firstrealapplication.ui.components.AppHeader
import com.example.firstrealapplication.ui.theme.FirstRealApplicationTheme
import com.example.firstrealapplication.vpn.LocalVpnService
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    // Captured packet lines — shared mutable state for the UI
    private val _packetLog = mutableStateOf("")
    private val _isCapturing = mutableStateOf(false)
    private val _filterText = mutableStateOf("")
    private val _saveMessage = mutableStateOf<String?>(null)
    private val _captureStatus = mutableStateOf<String?>(null)
    private lateinit var settingsState: SettingsState
    private var hasHandledAutoStart = false
    private var pendingAutoStartPermission = false
    private var currentStartWasAuto = false

    // SAF file picker to export the current pcap
    private val saveLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val pcapFile = LocalVpnService.instance?.getCurrentPcapFile()
            if (pcapFile != null && pcapFile.exists()) {
                contentResolver.openOutputStream(uri)?.use { out ->
                    pcapFile.inputStream().use { it.copyTo(out) }
                }
                _saveMessage.value = "Saved to ${uri.lastPathSegment}"
            } else {
                // No active pcap — export the packet log as a text file
                contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(_packetLog.value.toByteArray())
                }
                _saveMessage.value = "Log exported to ${uri.lastPathSegment}"
            }
        }
    }

    // SAF file picker to open/import a capture file
    private val openLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (content != null) {
                    _packetLog.value = content
                    _saveMessage.value = "Loaded ${uri.lastPathSegment}"
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to open file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startCaptureService()
        } else {
            _captureStatus.value = if (pendingAutoStartPermission) {
                "Auto-Start is waiting for VPN permission"
            } else {
                "VPN access has not been granted"
            }
        }
        pendingAutoStartPermission = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsState = SettingsState(applicationContext)
        enableEdgeToEdge()
        setContent {
            FirstRealApplicationTheme {
                val settingsLoaded by settingsState.isLoaded
                val captureOnStart by settingsState.captureOnStart
                val packetLog by _packetLog
                val isCapturing by _isCapturing
                val filterText by _filterText
                val saveMessage by _saveMessage
                val captureStatus by _captureStatus

                LaunchedEffect(settingsLoaded, captureOnStart) {
                    if (settingsLoaded && captureOnStart && !hasHandledAutoStart && !_isCapturing.value) {
                        hasHandledAutoStart = true
                        requestVpnAndStart(autoStart = true)
                    }
                }

                if (!settingsLoaded) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Lade Einstellungen...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                packetLog = packetLog,
                                isCapturing = isCapturing,
                                filterText = filterText,
                                captureStatus = captureStatus,
                                onPlay = { requestVpnAndStart() },
                                onStop = { stopCapture() },
                                onFilterChanged = { _filterText.value = it },
                                onSave = { saveCapture() },
                                saveMessage = saveMessage,
                                onDismissSaveMessage = { _saveMessage.value = null },
                                onNew = { clearCapture() },
                                onOpen = { openFile() },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onPacketClick = { line ->
                                    val encoded = URLEncoder.encode(line, "UTF-8")
                                    navController.navigate("packetDetail/$encoded")
                                }
                            )
                        }
                        composable("packetDetail/{packetLine}") { backStackEntry ->
                            val encoded = backStackEntry.arguments?.getString("packetLine") ?: ""
                            val line = URLDecoder.decode(encoded, "UTF-8")
                            PacketDetailScreen(
                                packetLine = line,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToDetail = { detail: String ->
                                    navController.navigate("settings/$detail")
                                },
                                settingsState = settingsState
                            )
                        }
                        composable("settings/{detail}") { backStackEntry ->
                            val detail = backStackEntry.arguments?.getString("detail") ?: ""
                            SettingsDetailScreen(detail = detail, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    private fun requestVpnAndStart(autoStart: Boolean = false) {
        currentStartWasAuto = autoStart
        val intent = VpnService.prepare(this)
        if (intent != null) {
            pendingAutoStartPermission = autoStart
            _captureStatus.value = if (autoStart) {
                "Auto-Start is waiting for VPN permission"
            } else {
                "Waiting for VPN permission"
            }
            vpnPermissionLauncher.launch(intent)
        } else {
            _captureStatus.value = if (autoStart) {
                "Auto-Start is initializing capture"
            } else {
                "Capture is starting"
            }
            startCaptureService()
        }
    }

    private fun startCaptureService() {
        _packetLog.value = ""
        _isCapturing.value = true
        _captureStatus.value = if (currentStartWasAuto) {
            "Capture active (Auto-Start)"
        } else {
            "Capture active"
        }

        // Register callback for incoming packets
        LocalVpnService.onPacketCaptured = { summary ->
            // Append to the log (only latest 500 lines to avoid OOM)
            val current = _packetLog.value
            val lines = current.lines().takeLast(499)
            _packetLog.value = (lines + summary).joinToString("\n")
        }

        val serviceIntent = Intent(this, LocalVpnService::class.java).apply {
            action = LocalVpnService.ACTION_START
        }
        startService(serviceIntent)
    }

    private fun stopCapture() {
        _isCapturing.value = false
        pendingAutoStartPermission = false
        currentStartWasAuto = false
        _captureStatus.value = "Capture stopped"
        LocalVpnService.onPacketCaptured = null

        val serviceIntent = Intent(this, LocalVpnService::class.java).apply {
            action = LocalVpnService.ACTION_STOP
        }
        startService(serviceIntent)
    }

    private fun saveCapture() {
        val pcapFile = LocalVpnService.instance?.getCurrentPcapFile()
        val defaultName = pcapFile?.name ?: "capture.pcap"
        saveLauncher.launch(defaultName)
    }

    private fun clearCapture() {
        _packetLog.value = ""
        _filterText.value = ""
        _saveMessage.value = "Capture cleared"
    }

    private fun openFile() {
        openLauncher.launch(arrayOf("*/*"))
    }

    override fun onDestroy() {
        LocalVpnService.onPacketCaptured = null
        super.onDestroy()
    }
}

@Composable
fun HomeScreen(
    packetLog: String,
    isCapturing: Boolean,
    filterText: String,
    captureStatus: String?,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onFilterChanged: (String) -> Unit,
    onSave: () -> Unit,
    saveMessage: String?,
    onDismissSaveMessage: () -> Unit,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onPacketClick: (String) -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when save completes
    LaunchedEffect(saveMessage) {
        if (saveMessage != null) {
            snackbarHostState.showSnackbar(saveMessage)
            onDismissSaveMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppHeader(
                title = "TrafficMobile",
                onSettingsClick = onNavigateToSettings,
                onNewClick = onNew,
                onOpenClick = onOpen
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.PlayArrow,
                    label = "Play",
                    enabled = !isCapturing,
                    onClick = onPlay
                )
                ActionButton(
                    icon = Icons.Default.Close,
                    label = "Stop",
                    enabled = isCapturing,
                    onClick = onStop
                )
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Filter",
                    onClick = { showFilterDialog = true }
                )
                ActionButton(
                    icon = Icons.Default.Check,
                    label = "Save",
                    onClick = onSave
                )
            }

            if (!captureStatus.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color(0xFFF5F7FA),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = captureStatus,
                        color = Color(0xFF455A64),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            // Filter indicator
            if (filterText.isNotBlank()) {
                Text(
                    text = "Filter: $filterText",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Build the filtered line list
            val lines = remember(packetLog, filterText) {
                val allLines = packetLog.lines().filter { it.isNotBlank() }
                if (filterText.isBlank()) allLines
                else allLines.filter { it.contains(filterText, ignoreCase = true) }
            }

            // Packet list — each line is clickable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .border(1.dp, Color.LightGray)
            ) {
                if (lines.isEmpty()) {
                    Text(
                        text = if (isCapturing) "Waiting for packets…" else "Press Play to start capturing",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(lines) { index, line ->
                            val bgColor = if (index % 2 == 0) Color.White else Color(0xFFF9F9F9)
                            // Determine protocol color hint
                            val lineColor = when {
                                line.startsWith("TCP") -> Color(0xFF1565C0)
                                line.startsWith("UDP") -> Color(0xFF2E7D32)
                                line.startsWith("ICMP") -> Color(0xFFE65100)
                                else -> Color.Black
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPacketClick(line) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Text(
                                        text = line,
                                        color = lineColor,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Open details",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (index < lines.lastIndex) {
                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter dialog
    if (showFilterDialog) {
        var tempFilter by remember { mutableStateOf(filterText) }
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Packet Filter") },
            text = {
                OutlinedTextField(
                    value = tempFilter,
                    onValueChange = { tempFilter = it },
                    label = { Text("Filter expression (e.g. TCP, 192.168)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onFilterChanged(tempFilter)
                    showFilterDialog = false
                }) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onFilterChanged("")
                    showFilterDialog = false
                }) { Text("Clear") }
            }
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(50.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFF5F5F5),
            disabledContentColor = Color.LightGray
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.7f
            )
        }
    }
}

@Composable
fun PacketDetailScreen(packetLine: String, onBack: () -> Unit) {
    // Parse the summary line back into structured fields
    // Format: "PROTO  src:port → :dstport [FLAGS]  len=N"
    val protocol = packetLine.substringBefore(" ").trim()
    val hasArrow = "→" in packetLine
    val srcFull = if (hasArrow) {
        packetLine.substringAfter("  ").substringBefore(" →").trim()
    } else {
        packetLine.substringAfter("  ").substringBefore("  len=").trim()
    }
    val dstFull = if (hasArrow) {
        packetLine.substringAfter("→ ").substringBefore("  ").trim()
    } else ""
    val flags = if ("[" in packetLine) {
        packetLine.substringAfter("[").substringBefore("]")
    } else ""
    val length = if ("len=" in packetLine) {
        packetLine.substringAfter("len=").trim()
    } else "?"

    val srcIp = srcFull.substringBefore(":").removePrefix(":")
    val srcPort = if (":" in srcFull) srcFull.substringAfter(":") else ""
    val dstIp = dstFull.substringBefore(":").removePrefix(":")
    val dstPort = if (":" in dstFull) dstFull.substringAfter(":") else ""

    Scaffold(
        topBar = {
            AppHeader(
                title = "Packet Detail",
                onSettingsClick = {},
                onNewClick = null,
                onOpenClick = null
            )
        },
        containerColor = Color.White,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Back") }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Overview Card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = packetLine,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // ── Protocol Section ──
            DetailSection(title = "Protocol") {
                DetailRow("Type", protocol)
                DetailRow("IP Version", "IPv4")
                if (flags.isNotBlank()) {
                    DetailRow("Flags", flags)
                }
            }

            // ── Source Section ──
            DetailSection(title = "Source") {
                DetailRow("IP Address", srcIp.ifBlank { "N/A" })
                if (srcPort.isNotBlank()) {
                    DetailRow("Port", srcPort)
                    DetailRow("Service", guessService(srcPort.toIntOrNull()))
                }
            }

            // ── Destination Section ──
            if (dstIp.isNotBlank()) {
                DetailSection(title = "Destination") {
                    DetailRow("IP Address", dstIp)
                    if (dstPort.isNotBlank()) {
                        DetailRow("Port", dstPort)
                        DetailRow("Service", guessService(dstPort.toIntOrNull()))
                    }
                }
            }

            // ── Payload Section ──
            DetailSection(title = "Payload") {
                DetailRow("Length", "$length bytes")
            }

            // ── TCP Flags breakdown ──
            if (protocol == "TCP" && flags.isNotBlank()) {
                DetailSection(title = "TCP Flags") {
                    val flagList = flags.split(",").map { it.trim() }
                    DetailRow("SYN", if ("SYN" in flagList) "✓ Set" else "✗ Not set")
                    DetailRow("ACK", if ("ACK" in flagList) "✓ Set" else "✗ Not set")
                    DetailRow("FIN", if ("FIN" in flagList) "✓ Set" else "✗ Not set")
                    DetailRow("RST", if ("RST" in flagList) "✓ Set" else "✗ Not set")
                    DetailRow("PSH", if ("PSH" in flagList) "✓ Set" else "✗ Not set")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(
            text = value,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

fun guessService(port: Int?): String = when (port) {
    21 -> "FTP"
    22 -> "SSH"
    23 -> "Telnet"
    25 -> "SMTP"
    53 -> "DNS"
    80 -> "HTTP"
    110 -> "POP3"
    143 -> "IMAP"
    443 -> "HTTPS"
    993 -> "IMAPS"
    995 -> "POP3S"
    3306 -> "MySQL"
    5432 -> "PostgreSQL"
    8080 -> "HTTP Alt"
    8443 -> "HTTPS Alt"
    else -> if (port != null && port < 1024) "Well-known" else "—"
}

@Composable
fun SettingsDetailScreen(detail: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            AppHeader(
                title = detail, 
                onSettingsClick = {}
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Configuration for $detail", color = Color.Black)
        }
    }
}
