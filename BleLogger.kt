package com.soflow.ble

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for logging raw BLE messages to a text file.
 * Each log entry includes a timestamp and the message bytes formatted as hex.
 */
object BleLogger {
    private const val LOG_FILE_NAME = "ble_log.txt"
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    /**
     * Appends the provided [data] to the log file. The file is stored in the app's
     * external files directory when available, falling back to internal storage.
     */
    fun log(context: Context, data: ByteArray) {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val logFile = File(dir, LOG_FILE_NAME)
        val timestamp = timeFormat.format(Date())
        val hex = data.joinToString(separator = " ") { String.format("%02X", it) }
        logFile.appendText("$timestamp: $hex\n")
    }
}
