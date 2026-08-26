package com.example.agarbattidryer.data.model

/**
 * Represents the connectivity state with the Agarbatti Dryer hardware.
 */
enum class ConnectionState {
    DISCONNECTED,
    SEARCHING,
    CONNECTING,
    CONNECTED;

    val displayLabel: String
        get() = when (this) {
            DISCONNECTED -> "Disconnected"
            SEARCHING -> "Searching..."
            CONNECTING -> "Connecting..."
            CONNECTED -> "Connected"
        }
}
