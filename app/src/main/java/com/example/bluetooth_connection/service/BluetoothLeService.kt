package com.example.bluetooth_connection.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.bluetooth_connection.activity.myTag


class BluetoothLeService : Service() {

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(myTag, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    private var bluetoothGatt: BluetoothGatt? = null

    fun connect(address: String): Boolean {
        /**
         * connect to the GATT server on the device
         * call from device detail activity
         *
         * @bluetoothGattCallback  call back function
         *
         *
         */

        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // check permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }

                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                Log.e(myTag, "OK!! ${bluetoothGatt?.device?.address}")
                return true
            } catch (exception: IllegalArgumentException) {
                Log.e(myTag, "Device not found with provided address.  Unable to connect.")
                return false
            }
        } ?: run {
            Log.e(myTag, "BluetoothAdapter not initialized")
            return false
        }
    }

    fun disConnect() {
        if (bluetoothGatt == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(myTag, "checkSelfPermission")
                return
            }
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt = null
        broadcastUpdate(ACTION_GATT_DISCONNECTED)
        Log.e(myTag, "broadcastUpdate")
    }

    // call when connection status change
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.e(myTag, "OK!! STATE_CONNECTED")
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    Log.e(myTag, "OK!! STATE_DISCONNECTED")
                }
                else -> {
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    Log.e(myTag, "OK!! ACTION_GATT_DISCONNECTED")
                }
            }
        }

        // call when connection connected
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.e(myTag, "onServicesDiscovered received: $status / 1")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.e(myTag, "onServicesDiscovered received: $status / 3")
            }
        }

    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    fun getSupportedGatt(): BluetoothGatt? {
        return bluetoothGatt
    }

    // update connection status via broadcast receiver
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // close service
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(myTag, "checkSelfPermission")
                    return
                }
            }
            gatt.close()
            bluetoothGatt = null
        }
    }

    companion object {
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth_connection.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth_connection.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth_connection.ACTION_GATT_SERVICES_DISCOVERED"
    }
}

