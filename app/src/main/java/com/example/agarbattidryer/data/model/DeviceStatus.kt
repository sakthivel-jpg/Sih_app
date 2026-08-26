package com.example.agarbattidryer.data.model

/**
 * Represents the operational state of the Agarbatti Dryer machine.
 * Designed to be mapped directly to simple, clear text for rural artisans.
 */
enum class DeviceStatus {
    READY,
    DRYING,
    COMPLETED,
    STOPPED,
    DISCONNECTED,
    ERROR;

    val displayTitle: String
        get() = when (this) {
            READY -> "READY"
            DRYING -> "DRYING"
            COMPLETED -> "COMPLETED"
            STOPPED -> "STOPPED"
            DISCONNECTED -> "DISCONNECTED"
            ERROR -> "ERROR"
        }

    val displaySubtitle: String
        get() = when (this) {
            READY -> "Machine is ready. Press Start."
            DRYING -> "Drying in progress..."
            COMPLETED -> "Drying completed! Batch is ready."
            STOPPED -> "Drying stopped by user."
            DISCONNECTED -> "Machine connection lost."
            ERROR -> "Check machine connection."
        }
}
