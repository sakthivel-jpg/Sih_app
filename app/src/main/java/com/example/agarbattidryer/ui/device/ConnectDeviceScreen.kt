package com.example.agarbattidryer.ui.device

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.device.DeviceMode
import com.example.agarbattidryer.device.DynamicDeviceService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectDeviceScreen(
    deviceService: DynamicDeviceService,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentMode by deviceService.currentMode.collectAsState()
    val connectionState by deviceService.connectionState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(currentMode) }
    
    // IP input state for Wi-Fi
    var wifiIp by remember { mutableStateOf("192.168.4.1") }
    
    // Scanned devices state
    var discoveredDevices by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedTab) {
        if (selectedTab != currentMode) {
            deviceService.setMode(selectedTab)
        }
    }

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
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = selectedTab == DeviceMode.BLE,
                    onClick = { selectedTab = DeviceMode.BLE },
                    label = { Text("BLUETOOTH") },
                    leadingIcon = { Icon(Icons.Rounded.Bluetooth, null) }
                )
                FilterChip(
                    selected = selectedTab == DeviceMode.WIFI,
                    onClick = { selectedTab = DeviceMode.WIFI },
                    label = { Text("WI-FI") },
                    leadingIcon = { Icon(Icons.Rounded.Wifi, null) }
                )
                FilterChip(
                    selected = selectedTab == DeviceMode.MOCK,
                    onClick = { selectedTab = DeviceMode.MOCK },
                    label = { Text("MOCK") },
                    leadingIcon = { Icon(Icons.Rounded.DeveloperMode, null) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when (selectedTab) {
                DeviceMode.BLE -> {
                    Button(
                        onClick = {
                            isScanning = true
                            coroutineScope.launch {
                                discoveredDevices = deviceService.getAvailableDevices()
                                isScanning = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isScanning) "SCANNING..." else "SCAN FOR ESP32")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn {
                        items(discoveredDevices) { device ->
                            DeviceListItem(
                                device = device,
                                onConnect = {
                                    coroutineScope.launch {
                                        deviceService.connect(device.id)
                                    }
                                },
                                isConnecting = connectionState == ConnectionState.CONNECTING
                            )
                        }
                    }
                }
                DeviceMode.WIFI -> {
                    OutlinedTextField(
                        value = wifiIp,
                        onValueChange = { wifiIp = it },
                        label = { Text("ESP32 IP Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = "80",
                        onValueChange = { },
                        label = { Text("Port (Fixed for now)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                deviceService.connect(wifiIp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (connectionState == ConnectionState.CONNECTING) "CONNECTING..." else "CONNECT")
                    }
                }
                DeviceMode.MOCK -> {
                    Text("Mock Mode simulates hardware.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                deviceService.connect("MOCK_ID")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CONNECT MOCK")
                    }
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

@Composable
fun DeviceListItem(
    device: DeviceInfo,
    onConnect: () -> Unit,
    isConnecting: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(device.name, fontWeight = FontWeight.Bold)
            Text(device.id, fontSize = 12.sp)
        }
        Button(onClick = onConnect, enabled = !isConnecting) {
            Text("CONNECT")
        }
    }
}
