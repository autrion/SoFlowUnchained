package com.rolandmit.so2aircodex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel bridging BLE manager and UI. */
class BleViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(BleState.DISCONNECTED)
    val state: StateFlow<BleState> = _state.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val manager = BleManager(
        app,
        { _state.value = it },
        { log -> addLog(log) }
    )

    fun scanAndConnect() = manager.scanAndConnect()

    fun disconnect() = manager.disconnect()

    private fun addLog(hex: String) {
        val entry = LogEntry(System.currentTimeMillis(), hex)
        _logs.value = _logs.value + entry
    }

    fun exportLogs(context: Application) {
        // simple export via share intent
        val text = _logs.value.joinToString("\n") { "${'$'}{it.timestamp}:${'$'}{it.command}" }
        val file = java.io.File(context.cacheDir, "logs.txt")
        file.writeText(text)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun set20() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.UNLOCK, Commands.SPEED_20_PREF)
    fun set27() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.UNLOCK, Commands.SPEED_27)
    fun eco() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.ECO)
    fun normal() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.NORMAL)
    fun sport() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.SPORT)
    fun lock() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.LOCK)
    fun unlock() = sendFlow(Commands.SECRET, Commands.HANDSHAKE, Commands.UNLOCK)

    private fun sendFlow(vararg cmds: String) {
        viewModelScope.launch { manager.executeFlow(*cmds) }
    }
}
