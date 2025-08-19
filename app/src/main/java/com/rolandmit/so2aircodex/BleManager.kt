package com.rolandmit.so2aircodex

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Handles BLE scanning, connection and command sending.
 * All methods log hex commands and update connection state via callback.
 */
class BleManager(
    private val context: Context,
    private val stateCallback: (BleState) -> Unit,
    private val logCallback: (String) -> Unit
) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scope = CoroutineScope(Dispatchers.IO)

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null

    private val serviceUuid = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
    private val writeUuid = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")

    private val scanner get() = adapter?.bluetoothLeScanner

    /** Start scan and connect to the first matching device. */
    fun scanAndConnect() {
        if (adapter == null || !adapter.isEnabled) return
        stateCallback(BleState.CONNECTING)
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build()
        val settings = ScanSettings.Builder().build()
        scanner?.startScan(listOf(filter), settings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.connectGatt(context, false, gattCallback)
            scanner?.stopScan(this)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                this@BleManager.gatt = gatt
                gatt.requestMtu(247)
                gatt.discoverServices()
            } else {
                stateCallback(BleState.DISCONNECTED)
                // automatic reconnect
                scope.launch {
                    delay(1000)
                    scanAndConnect()
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            writeChar = gatt.getService(serviceUuid)?.getCharacteristic(writeUuid)
            if (writeChar == null) {
                stateCallback(BleState.ERROR)
                return
            }
            stateCallback(BleState.HANDSHAKING)
            scope.launch { handshake() }
        }
    }

    /** Perform secret and handshake sequence. */
    private suspend fun handshake() {
        executeFlow(Commands.SECRET, Commands.HANDSHAKE)
        stateCallback(BleState.READY)
    }

    /** Execute a series of hex commands with default delay and retries. */
    suspend fun executeFlow(vararg hex: String) {
        hex.forEach { sendCommand(it) }
    }

    /** Send single hex command with retries. */
    suspend fun sendCommand(hex: String) {
        val characteristic = writeChar ?: return
        var attempt = 0
        var backoff = 250L
        val bytes = hexStringToByteArray(hex)
        while (attempt <= 2) {
            val ok = characteristic.run {
                value = bytes
                writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                gatt?.writeCharacteristic(this) ?: false
            }
            logCallback(hex)
            if (ok) return
            delay(backoff)
            backoff += 250
            attempt++
        }
    }

    fun disconnect() {
        gatt?.close()
        gatt = null
        stateCallback(BleState.DISCONNECTED)
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((s[i].digitToInt(16) shl 4) + s[i + 1].digitToInt(16)).toByte()
            i += 2
        }
        return data
    }
}
