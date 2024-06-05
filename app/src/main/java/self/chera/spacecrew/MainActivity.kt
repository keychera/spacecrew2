@file:OptIn(ExperimentalPermissionsApi::class)

package self.chera.spacecrew

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    Root(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Root(modifier: Modifier = Modifier) {
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
        Navigation(modifier)
    } else {
        AskForBluetooth(permissionStates = bluetoothPermissionState, modifier)
    }
}
@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") {
            ConnectedDevices(
                onClickScanForDevice = { /* TODO */ },
                modifier = modifier
            )
        }
    }
}

@Composable
fun ConnectedDevices(
    onClickScanForDevice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        val devices = (1..8).map { Device("devices #$it") }.toList()
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

data class Device(
    val name: String
) 

@Composable
fun DeviceList(devices: List<Device>, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(4.dp),
    ) {
        if (devices.isNotEmpty()) {
            LazyColumn() {
                items(devices) {
                    DeviceCard(deviceName = it)
                }
            }
        } else {
            Text("No device found", color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun DeviceCard(deviceName: Device) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = deviceName.name)
    }
}

@Preview(showBackground = true, heightDp = 360, widthDp = 360)
@Composable
fun GreetingPreview() {
    Spacecrew2Theme {
        ConnectedDevices({})
    }
}