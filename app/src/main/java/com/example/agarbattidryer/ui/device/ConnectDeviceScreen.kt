package com.example.agarbattidryer.ui.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.device.DynamicDeviceService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectDeviceScreen(
    deviceService: DynamicDeviceService,
    onBack: () -> Unit,
    onScanQrCode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val connectionState by deviceService.connectionState.collectAsState()
    
    // IP input state for Wi-Fi
    var wifiIp by remember { mutableStateOf("192.168.1.50") }
    var wifiPort by remember { mutableStateOf("80") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CONNECT DEVICE", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "WI-FI",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val activeDevice by deviceService.activeDevice.collectAsState()
            if (activeDevice != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ACTIVE DEVICE",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "● ${connectionState.displayLabel.uppercase()}",
                        fontWeight = FontWeight.Bold,
                        color = if (connectionState == ConnectionState.CONNECTED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(activeDevice?.name ?: "Unknown Device", fontWeight = FontWeight.Bold)
                    Text("IP: ${activeDevice?.id ?: "Unknown"}")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                deviceService.disconnect()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("DISCONNECT DEVICE")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                deviceService.disconnect()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CHANGE DEVICE")
                    }
                }
            } else {
                Button(
                    onClick = { onScanQrCode?.invoke() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SCAN DRYER QR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "ADVANCED / MANUAL SETUP",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wifiIp,
                    onValueChange = { wifiIp = it },
                    label = { Text("ESP32 IP Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = wifiPort,
                    onValueChange = { wifiPort = it },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            deviceService.connect("$wifiIp:$wifiPort")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (connectionState == ConnectionState.CONNECTING) "CONNECTING..." else "CONNECT MANUALLY")
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (connectionState == ConnectionState.CONNECTED) {
                Text(
                    text = "Connected Successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RETURN TO HOME")
                }
            }
        }
    }
}
