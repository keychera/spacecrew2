@file:OptIn(ExperimentalPermissionsApi::class)

package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import self.chera.spacecrew.ui.theme.Spacecrew2Theme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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

@RequiresApi(Build.VERSION_CODES.S)
@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun Root(activity: ComponentActivity, modifier: Modifier = Modifier) {
    BluetoothLayer(activity, ifGranted = {
        Navigation(activity, modifier)
    })
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BluetoothLayer(
    activity: ComponentActivity,
    modifier: Modifier = Modifier,
    ifGranted: @Composable () -> Unit,
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

    BluetoothPermissionLayer(activity = activity,
        bluetoothPermissionState = bluetoothPermissionState,
        ifGranted = {
            BluetoothToggleLayer(
                activity = activity,
                ifBluetoothOn = { ifGranted() },
                ifBluetoothOff = { toggleBluetoothOn ->
                    AskForBluetoothToggle(toggleBluetoothOn, modifier)
                })
        },
        ifNotGranted = { permissionState ->
            AskForBluetoothPermission(
                askForPermission = { permissionState.launchMultiplePermissionRequest() },
                modifier = modifier
            )
        })
}

@Composable
fun BluetoothPermissionLayer(
    activity: ComponentActivity,
    bluetoothPermissionState: MultiplePermissionsState,
    ifGranted: @Composable () -> Unit,
    ifNotGranted: @Composable (MultiplePermissionsState) -> Unit,
) {
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
        ifNotGranted(bluetoothPermissionState)
    }
}

@Composable
fun BluetoothToggleLayer(
    activity: ComponentActivity,
    ifBluetoothOn: @Composable () -> Unit,
    ifBluetoothOff: @Composable (() -> Unit) -> Unit,
) {
    val manager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetooth = manager.adapter

    var isBluetoothOn by remember { mutableStateOf(bluetooth.isEnabled) }

    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    val toggleBT = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isBluetoothOn = bluetooth.isEnabled }

    if (isBluetoothOn) {
        ifBluetoothOn()
    } else {
        ifBluetoothOff { toggleBT.launch(enableIntent) }
    }
}


@Composable
fun AskForBluetoothPermission(
    askForPermission: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Bluetooth is needed")
        Button(onClick = askForPermission) {
            Text("Grant permission")
        }
    }
}


@Composable
fun AskForBluetoothToggle(
    toggleBluetoothOn: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Bluetooth is turned off")
        Button(
            onClick = { toggleBluetoothOn() }
        ) {
            Text(text = "Turn ON")
        }
    }
}

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun Navigation(activity: ComponentActivity, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") {
            ConnectedDevices(activity = activity, modifier = modifier)
        }
    }
}

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun ConnectedDevices(activity: ComponentActivity, modifier: Modifier = Modifier) {
    val btDevicesViewModel: BluetoothDevicesViewModel = viewModel(activity)
    ConnectedDevices(
        activity = activity,
        devices = btDevicesViewModel.deviceList,
        onClickScanForDevice = { btDevicesViewModel.scanDevices(activity) },
        modifier = modifier
    )
}

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun ConnectedDevices(
    activity: ComponentActivity,
    devices: List<Device>,
    onClickScanForDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        DeviceList(activity = activity, devices = devices, modifier.weight(1.0F))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.weight(1.0F)) {
                Text(text = "found ${devices.size} devices")
                Text(text = "connected to (0) devices")
            }
            Button(onClick = onClickScanForDevice) {
                Text(text = "Scan")
            }
        }
    }
}

@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun DeviceList(activity: ComponentActivity, devices: List<Device>, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(4.dp)
            .padding(start = 8.dp),
    ) {
        if (devices.isNotEmpty()) {
            LazyColumn {
                items(devices) { device ->
                    val btDevicesViewModel: BluetoothDevicesViewModel = viewModel(activity)
                    val index = btDevicesViewModel.deviceList.indexOf(device)
                    DeviceCard(btDevicesViewModel = btDevicesViewModel, deviceIdx = index)
                }
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
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun DeviceCard(btDevicesViewModel: BluetoothDevicesViewModel, deviceIdx: Int) {
    val device = btDevicesViewModel.deviceList[deviceIdx]
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (device.isRetrievingName) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            LaunchedEffect(key1 = device.address) {
                CoroutineScope(Dispatchers.Default).launch {
                    val retrievedName = withTimeoutOrNull(10000) {
                        var retrieving: String? = null
                        while (retrieving == null) {
                            delay(1000)
                            retrieving = device.source.name
                        }
                        retrieving
                    }
                    val updatedDevice = device.copy().apply {
                        name = retrievedName ?: "[no name available]"
                        isRetrievingName = false
                    }
                    btDevicesViewModel.deviceList[deviceIdx] = updatedDevice
                }
            }
        }
        Text(
            text = device.name, modifier = Modifier
                .weight(1.0F)
                .padding(start = 8.dp)
        )
        Button(onClick = { /*TODO*/ }) {
            Text(text = "Connect")
        }
    }
}

@Preview(showBackground = true, heightDp = 360, widthDp = 360)
@Composable
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun GreetingPreview() {
    Spacecrew2Theme {

    }
}