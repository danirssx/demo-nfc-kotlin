package com.example.nfc_kotlin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.nfc_kotlin.nfc.NfcModule
import com.example.nfc_kotlin.api.server.KtorServer
import com.example.nfc_kotlin.ui.theme.NfckotlinTheme

data class LogEntry(
    val timestamp: String,
    val message: String,
    val type: LogType
)

enum class LogType {
    NFC_DETECTED, API_SUCCESS, API_ERROR, SERVER_INFO
}

class MainActivity : ComponentActivity() {
    
    private lateinit var nfcModule: NfcModule
    private lateinit var ktorServer: KtorServer
    private val logs = mutableStateListOf<LogEntry>()
    
    private fun addLog(message: String, type: LogType) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        logs.add(0, LogEntry(timestamp, message, type))
        // Keep only last 50 logs
        if (logs.size > 50) {
            logs.removeAt(logs.size - 1)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcModule = NfcModule(this)
        ktorServer = KtorServer()
        
        // Set up NFC callback for real-time logging
        nfcModule.setCallback(object : NfcModule.NfcCallback {
            override fun onTagDetected(tagInfo: String) {
                addLog("NFC TAG DETECTED: $tagInfo", LogType.NFC_DETECTED)
            }
            
            override fun onApiResponse(success: Boolean, message: String) {
                if (success) {
                    addLog("API SUCCESS: $message", LogType.API_SUCCESS)
                } else {
                    addLog("API ERROR: $message", LogType.API_ERROR)
                }
            }
        })
        
        // Start KTOR server
        if (ktorServer.startServer(3000)) {
            android.util.Log.d("MainActivity", "KTOR server started successfully on port 3000")
            addLog("KTOR server started on port 3000", LogType.SERVER_INFO)
        } else {
            android.util.Log.e("MainActivity", "Failed to start KTOR server")
            addLog("Failed to start KTOR server", LogType.API_ERROR)
        }
        
        enableEdgeToEdge()
        setContent {
            NfckotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NfcLoggingScreen(
                        logs = logs,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        nfcModule.startReading()
        addLog("NFC reading started", LogType.SERVER_INFO)
    }
    
    override fun onPause() {
        super.onPause()
        nfcModule.stopReading()
        addLog("NFC reading paused", LogType.SERVER_INFO)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        nfcModule.handleNewIntent(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ktorServer.stopServer()
        addLog("KTOR server stopped", LogType.SERVER_INFO)
    }
}

@Composable
fun NfcLoggingScreen(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto scroll to top when new log is added
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NFC KTOR API Monitor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time NFC detection and API communication",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "Server: localhost:3000",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Status indicator
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity Log (${logs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color.Green.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "🟢 ACTIVE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Logs list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Waiting for NFC tags...\nHold an NFC tag near your device to see instant logging.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(logs) { log ->
                    LogEntryCard(log)
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(log: LogEntry) {
    val backgroundColor = when (log.type) {
        LogType.NFC_DETECTED -> Color.Blue.copy(alpha = 0.1f)
        LogType.API_SUCCESS -> Color.Green.copy(alpha = 0.1f)  
        LogType.API_ERROR -> Color.Red.copy(alpha = 0.1f)
        LogType.SERVER_INFO -> Color.Gray.copy(alpha = 0.1f)
    }
    
    val iconEmoji = when (log.type) {
        LogType.NFC_DETECTED -> "📡"
        LogType.API_SUCCESS -> "✅"
        LogType.API_ERROR -> "❌"
        LogType.SERVER_INFO -> "ℹ️"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = iconEmoji,
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 16.sp
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NfcLoggingPreview() {
    NfckotlinTheme {
        NfcLoggingScreen(
            logs = listOf(
                LogEntry("12:34:56", "NFC TAG DETECTED: Type: NfcA, Serial: 04A1B2C3, Time: 2024-01-15 12:34:56", LogType.NFC_DETECTED),
                LogEntry("12:34:57", "API SUCCESS: Tag sent to API successfully", LogType.API_SUCCESS),
                LogEntry("12:34:55", "KTOR server started on port 3000", LogType.SERVER_INFO)
            )
        )
    }
}