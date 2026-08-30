package com.example.agarbattidryer.device

import com.example.agarbattidryer.data.model.DeviceIdentity

object QrDeviceParser {
    
    fun parse(payload: String): DeviceIdentity? {
        if (payload.isBlank()) return null
        
        // Typical format: AGARBATTI-DRYER-001|PAIR-7F3A92
        // Or just: AGARBATTI-DRYER-001
        val parts = payload.split("|")
        val deviceId = parts[0].trim()
        
        // Basic validation
        if (deviceId.isEmpty() || !deviceId.startsWith("AGARBATTI-DRYER-")) {
            return null
        }
        
        val pairToken = if (parts.size > 1) parts[1].trim() else null
        
        return DeviceIdentity(deviceId, pairToken)
    }
}
