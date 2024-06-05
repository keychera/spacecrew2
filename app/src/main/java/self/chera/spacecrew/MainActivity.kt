@file:OptIn(ExperimentalPermissionsApi::class)

package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import self.chera.spacecrew.ui.theme.Spacecrew2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Spacecrew2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Root(this, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Root(activity: ComponentActivity, modifier: Modifier = Modifier) {
    BluetoothLayer(activity, ifGranted = {
        Navigation(activity, modifier)
    })
}

@Composable
fun BluetoothLayer(
    activity: ComponentActivity,
    ifGranted: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val bluetoothPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )

    if (bluetoothPermissionState.allPermissionsGranted) {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        val btDevicesViewModel: BluetoothDevicesViewModel = viewModel(activity)
        activity.registerReceiver(btDevicesViewModel.receiver, filter)

        val lifecycleOwner = LocalLifecycleOwner.current
        val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

        LaunchedEffect(lifecycleState) {
            if (lifecycleState == Lifecycle.State.DESTROYED) {
                activity.unregisterReceiver(btDevicesViewModel.receiver)
            }
        }

        ifGranted()
    } else {
        AskForBluetooth(permissionStates = bluetoothPermissionState, modifier)
    }
}

@Composable
fun Navigation(activity: ComponentActivity, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") {
            ConnectedDevices(activity = activity, modifier = modifier)
        }
    }
}

@Composable
fun ConnectedDevices(activity: ComponentActivity, modifier: Modifier = Modifier) {
    val btDevicesViewModel: BluetoothDevicesViewModel = viewModel(activity)
    ConnectedDevices(
        devices = btDevicesViewModel.deviceList,
        onClickScanForDevice = { btDevicesViewModel.scanDevices(activity) },
        modifier = modifier
    )
}

@Composable
fun ConnectedDevices(
    devices: List<Device>,
    onClickScanForDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        DeviceList(devices = devices, modifier.weight(1.0F))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.weight(1.0F)) {
                Text(text = "found (0) devices")
                Text(text = "connected to (0) devices")
            }
            Button(onClick = onClickScanForDevice) {
                Text(text = "Scan")
            }
        }
    }
}

@Composable
fun AskForBluetooth(
    permissionStates: MultiplePermissionsState,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Bluetooth is needed")
        Button(onClick = { permissionStates.launchMultiplePermissionRequest() }) {
            Text("Grant permission")
        }
    }
}

@Composable
fun DeviceList(devices: List<Device>, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(4.dp)
            .padding(start = 8.dp),
    ) {
        if (devices.isNotEmpty()) {
            LazyColumn() {
                items(devices) { DeviceCard(deviceName = it) }
            }
        } else {
            Text(
                "No device found",
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun DeviceCard(deviceName: Device) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
//        CircularProgressIndicator(modifier = Modifier.size(16.dp))
        Text(
            text = deviceName.name, modifier = Modifier
                .weight(1.0F)
                .padding(start = 8.dp), color = MaterialTheme.colorScheme.tertiary
        )
        Button(onClick = { /*TODO*/ }) {
            Text(text = "Connect")
        }
    }
}

@Preview(showBackground = true, heightDp = 360, widthDp = 360)
@Composable
fun GreetingPreview() {
    Spacecrew2Theme {
        val devices = (1..2).map { Device("devices #$it") }.toList()
        ConnectedDevices(devices, {})
    }
}