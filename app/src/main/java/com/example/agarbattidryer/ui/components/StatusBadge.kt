package com.example.agarbattidryer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agarbattidryer.data.model.DeviceStatus
import com.example.agarbattidryer.ui.theme.DisconnectedGray
import com.example.agarbattidryer.ui.theme.DisconnectedGrayLight
import com.example.agarbattidryer.ui.theme.DryingOrange
import com.example.agarbattidryer.ui.theme.DryingOrangeLight
import com.example.agarbattidryer.ui.theme.ReadyGreen
import com.example.agarbattidryer.ui.theme.ReadyGreenLight
import com.example.agarbattidryer.ui.theme.StopRed
import com.example.agarbattidryer.ui.theme.StopRedLight

@Composable
fun StatusBadge(
    status: DeviceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        DeviceStatus.READY -> Triple(ReadyGreenLight, ReadyGreen, ReadyGreen)
        DeviceStatus.DRYING -> Triple(DryingOrangeLight, DryingOrange, DryingOrange)
        DeviceStatus.COMPLETED -> Triple(ReadyGreenLight, ReadyGreen, ReadyGreen)
        DeviceStatus.STOPPED -> Triple(StopRedLight, StopRed, StopRed)
        DeviceStatus.DISCONNECTED, DeviceStatus.ERROR -> Triple(DisconnectedGrayLight, DisconnectedGray, DisconnectedGray)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = if (status == DeviceStatus.DRYING) 0.3f else 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .border(1.5.dp, dotColor.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .alpha(if (status == DeviceStatus.DRYING) dotAlpha else 1.0f)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = status.displayTitle,
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}
