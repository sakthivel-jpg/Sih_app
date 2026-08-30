package com.example.agarbattidryer.ui.device

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agarbattidryer.data.model.DeviceIdentity
import com.example.agarbattidryer.device.ProvisioningResult
import com.example.agarbattidryer.device.WifiProvisioningService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiProvisioningScreen(
    deviceIdentity: DeviceIdentity,
    onProvisioned: (String, String) -> Unit, // ip, port
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isProvisioning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConnectedToSoftAP by remember { mutableStateOf(false) }
    var isCheckingSoftAP by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val setupSsid = "${deviceIdentity.deviceId}-SETUP"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETUP NETWORK", fontWeight = FontWeight.Black) },
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
                .padding(24.dp)
        ) {
            if (!isConnectedToSoftAP) {
                Text(
                    text = "Connect to Dryer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Please go to your phone's Wi-Fi settings and connect to the following network:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Network Name (SSID):", fontSize = 12.sp)
                        Text(text = setupSsid, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        isCheckingSoftAP = true
                        errorMessage = null
                        coroutineScope.launch {
                            val isConnected = WifiProvisioningService.checkProvisioningStatus()
                            isConnectedToSoftAP = isConnected
                            if (!isConnected) {
                                errorMessage = "Could not reach dryer. Make sure you are connected to $setupSsid."
                            }
                            isCheckingSoftAP = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingSoftAP
                ) {
                    Text(if (isCheckingSoftAP) "CHECKING CONNECTION..." else "I'M CONNECTED")
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(
                    text = "Configure Wi-Fi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter the Wi-Fi credentials that the dryer should connect to.")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Wi-Fi Network Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Wi-Fi Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        isProvisioning = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = WifiProvisioningService.provisionWifi(ssid, password)
                            isProvisioning = false
                            when (result) {
                                is ProvisioningResult.Success -> {
                                    onProvisioned(result.newIp, "80")
                                }
                                is ProvisioningResult.Error -> {
                                    errorMessage = result.message
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProvisioning && ssid.isNotBlank()
                ) {
                    Text(if (isProvisioning) "CONNECTING DRYER..." else "CONNECT DRYER TO WI-FI")
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
