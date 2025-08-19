package com.rolandmit.so2aircodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List as ListIcon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** Main activity hosting all screens. */
class MainActivity : ComponentActivity() {
    private val vm: BleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(vm) }
    }
}

@Composable
fun App(vm: BleViewModel) {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(nav) }
    ) { padding ->
        NavHost(nav, startDestination = "scan", modifier = Modifier.padding(padding)) {
            composable("scan") { ScanScreen(vm) }
            composable("control") { ControlScreen(vm) }
            composable("settings") { SettingsScreen() }
            composable("logs") { LogsScreen(vm) }
        }
    }
}

@Composable
fun BottomBar(nav: NavHostController) {
    NavigationBar {
        NavigationBarItem(selected = false, onClick = { nav.navigate("scan") }, label = { Text("Scan") }, icon = { Icon(Icons.Filled.Bluetooth, null) })
        NavigationBarItem(selected = false, onClick = { nav.navigate("control") }, label = { Text("Control") }, icon = { Icon(Icons.Filled.Build, null) })
        NavigationBarItem(selected = false, onClick = { nav.navigate("settings") }, label = { Text("Settings") }, icon = { Icon(Icons.Filled.Settings, null) })
        NavigationBarItem(selected = false, onClick = { nav.navigate("logs") }, label = { Text("Logs") }, icon = { Icon(ListIcon, null) })
    }
}

@Composable
fun ScanScreen(vm: BleViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = state.name)
        Button(onClick = { vm.scanAndConnect() }) { Text("Scan & Connect") }
        if (state == BleState.READY) {
            Button(onClick = { vm.disconnect() }) { Text("Disconnect") }
        }
    }
}

@Composable
fun ControlScreen(vm: BleViewModel) {
    var warn by remember { mutableStateOf(false) }
    if (warn) {
        AlertDialog(
            onDismissRequest = { warn = false },
            confirmButton = {
                TextButton(onClick = { warn = false; vm.set27() }) { Text(stringResource(R.string.warning_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { warn = false }) { Text(stringResource(R.string.warning_cancel)) }
            },
            title = { Text(stringResource(R.string.warning_title)) },
            text = { Text(stringResource(R.string.warning_message)) }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Button(onClick = { vm.set20() }) { Text("20 km/h") }
        Button(onClick = { warn = true }) { Text("27 km/h") }
        Button(onClick = { vm.eco() }) { Text("ECO") }
        Button(onClick = { vm.normal() }) { Text("NORMAL") }
        Button(onClick = { vm.sport() }) { Text("SPORT") }
        Button(onClick = { vm.lock() }) { Text("LOCK") }
        Button(onClick = { vm.unlock() }) { Text("UNLOCK") }
    }
}

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Settings coming soon")
    }
}

@Composable
fun LogsScreen(vm: BleViewModel) {
    val logs by vm.logs.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(logs) { log ->
                Text("${'$'}{log.timestamp}: ${'$'}{log.command}", modifier = Modifier.padding(8.dp))
            }
        }
        Button(onClick = { vm.exportLogs(vm.getApplication()) }, modifier = Modifier.padding(8.dp)) {
            Text("Export")
        }
    }
}
