package com.rolandmit.so2aircodex

/** Possible connection states. */
enum class BleState {
    DISCONNECTED,
    CONNECTING,
    HANDSHAKING,
    READY,
    ERROR
}

/** Simple log entry for hex commands. */
data class LogEntry(val timestamp: Long, val command: String)
