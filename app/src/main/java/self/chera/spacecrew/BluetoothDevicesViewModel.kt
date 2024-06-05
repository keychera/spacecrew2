package self.chera.spacecrew

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel


enum class BluetoothDevicesState { IDLE, SCANNING, CONNECTING, FINISHED }

data class Device(
    val address: String,
    val name: String = "",
)

fun BluetoothDevice.asDevice(): Device {
    return Device(this.address, this.address)
}

class BluetoothDevicesViewModel : ViewModel() {
    private val _deviceList = mutableStateListOf<Device>()
    private val _state = mutableStateOf(BluetoothDevicesState.IDLE)
    val deviceList get() = _deviceList

    fun scanDevices(context: Context) {
        _deviceList.clear()
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            System.err.println("Bluetooth SCAN not allowed")
            return
        }
        _deviceList.addAll(adapter.bondedDevices.map { it.asDevice() })

        adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action.toString()
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _state.value = BluetoothDevicesState.SCANNING
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                    device?.let { _deviceList.add(it.asDevice()) }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _state.value = BluetoothDevicesState.FINISHED
                }
            }
        }
    }
}