package com.example.agarbattidryer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.agarbattidryer.ui.components.BigActionButton
import com.example.agarbattidryer.ui.components.ConnectionStatusBar
import com.example.agarbattidryer.ui.components.SensorMetricCard
import com.example.agarbattidryer.ui.components.SensorType
import com.example.agarbattidryer.ui.components.StatusBadge
import com.example.agarbattidryer.ui.components.TimerDisplay
import com.example.agarbattidryer.ui.theme.SolarAmber

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onConnectDeviceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: App Title & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = SolarAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AGARBATTI DRYER",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Smart Solar Drying System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Machine Status Badge
            StatusBadge(
                status = uiState.deviceStatus,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Main Drying Timer
            TimerDisplay(
                durationSeconds = uiState.dryingDurationSeconds,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Real-time Temperature & Humidity Sensor Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SensorMetricCard(
                    type = SensorType.TEMPERATURE,
                    value = uiState.temperature,
                    modifier = Modifier.weight(1f)
                )
                SensorMetricCard(
                    type = SensorType.HUMIDITY,
                    value = uiState.humidity,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.activeDeviceName != null || uiState.connectionState == com.example.agarbattidryer.data.model.ConnectionState.CONNECTED) {
                // Large Action Button: START DRYING / STOP DRYING
                BigActionButton(
                    isDrying = uiState.isDrying,
                    enabled = if (uiState.isDrying) uiState.canStop else (uiState.canStart && uiState.connectionState == com.example.agarbattidryer.data.model.ConnectionState.CONNECTED),
                    onClick = {
                        if (uiState.isDrying) {
                            viewModel.onStopDrying()
                        } else {
                            viewModel.onStartDrying()
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                androidx.compose.material3.Button(
                    onClick = onConnectDeviceClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Rounded.WbSunny, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("CONNECT DEVICE", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            // Connection Status Footer Chip
            ConnectionStatusBar(
                connectionState = uiState.connectionState,
                deviceName = uiState.activeDeviceName,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )

            if (uiState.activeDeviceName != null || uiState.connectionState == com.example.agarbattidryer.data.model.ConnectionState.CONNECTED) {
                androidx.compose.material3.TextButton(
                    onClick = onConnectDeviceClick,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        "MANAGE DEVICE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
