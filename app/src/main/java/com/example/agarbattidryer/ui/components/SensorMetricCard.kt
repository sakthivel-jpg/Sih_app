package com.example.agarbattidryer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agarbattidryer.ui.theme.HumidityAccent
import com.example.agarbattidryer.ui.theme.HumidityAccentBg
import com.example.agarbattidryer.ui.theme.TempAccent
import com.example.agarbattidryer.ui.theme.TempAccentBg
import java.util.Locale

enum class SensorType {
    TEMPERATURE,
    HUMIDITY
}

@Composable
fun SensorMetricCard(
    type: SensorType,
    value: Float?,
    modifier: Modifier = Modifier
) {
    val (label, displayValue, icon, accentColor, iconBg) = when (type) {
        SensorType.TEMPERATURE -> MetricConfig(
            label = "TEMPERATURE",
            displayValue = if (value != null) String.format(Locale.US, "%.1f°C", value) else "--",
            icon = Icons.Rounded.DeviceThermostat,
            accentColor = TempAccent,
            iconBg = TempAccentBg
        )
        SensorType.HUMIDITY -> MetricConfig(
            label = "HUMIDITY",
            displayValue = if (value != null) String.format(Locale.US, "%.0f%%", value) else "--",
            icon = Icons.Rounded.WaterDrop,
            accentColor = HumidityAccent,
            iconBg = HumidityAccentBg
        )
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = displayValue,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp
        )
    }
}

private data class MetricConfig(
    val label: String,
    val displayValue: String,
    val icon: ImageVector,
    val accentColor: Color,
    val iconBg: Color
)
