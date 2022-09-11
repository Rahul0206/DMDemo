package com.example.bluetooth_connection.activity

import CharAdapter
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth_connection.R
import com.example.bluetooth_connection.databinding.ActivityDeviceDetailBinding
import com.example.bluetooth_connection.service.BluetoothLeService
import com.example.bluetooth_connection.utils.ByteUtils


class DeviceDetailActivity : AppCompatActivity() {
    /**
     * xml reference [binding] from layout
     * device detail [result] gating form ScanResultAdapter.
     * mac address [deviceAddress]
     *
     * @https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview
     */

    lateinit var binding: ActivityDeviceDetailBinding
    private lateinit var deviceAddress: String
    private lateinit var result: ScanResult

    private var isConnected = false

    private lateinit var myAdapter: CharAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_device_detail)

        binding.gatingData = true

        // initialize recycler view
        setVerticalRecyclerView(this@DeviceDetailActivity, findViewById(R.id.characteristic_rl))

        // get device
        result = intent.getParcelableExtra<ScanResult>("model")!!

        // set data for xml
        deviceAddress = result.device.address
        binding.btnText = "CONNECT"
        binding.address = deviceAddress
        binding.rssi = result.rssi.toString()
        if (result.scanRecord != null) {
            binding.scanRecord = ByteUtils.bytesToHex(result.scanRecord!!.bytes)
            binding.advertiseFlags = result.scanRecord!!.advertiseFlags.toString()
            binding.manufacturerSpecificData =
                result.scanRecord!!.manufacturerSpecificData.toString()
        }

        binding.gatingData = false

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        // call when click connect button
        binding.btnConnect.setOnClickListener {
            // check if service not initialize
            if (bluetoothService != null) {
                if (!isConnected) {
                    binding.isConnecting = true
                    // connect device
                    bluetoothService!!.connect(deviceAddress)
                } else {
                    binding.isConnecting = true
                    bluetoothService!!.disConnect()
                }
            } else {
                Toast.makeText(this, "Is null", Toast.LENGTH_LONG).show()
            }
        }
    }

    // characteristic list
    private val characteristic = ArrayList<BluetoothGattCharacteristic>()

    // initialize recycler view and set characteristic list
    @SuppressLint("NotifyDataSetChanged")
    private fun setVerticalRecyclerView(context: Context, recyclerView: RecyclerView) {
        myAdapter = CharAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            adapter = myAdapter
        }
        myAdapter.submitList(characteristic)
    }

    // Broadcast Receiver { BroadcastReceiver } , call when fire intent
    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    isConnected = true
                    binding.gatingData = true
                    binding.isConnecting = false
                    binding.btnText = "DISCONNECTED"
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    isConnected = false
                    binding.isConnecting = false
                    binding.btnText = "CONNECT"
                    showToast("Connection Failed")
                    characteristic.clear()
                    myAdapter.notifyDataSetChanged()
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    //
                    displayGattServices(bluetoothService?.getSupportedGattServices())
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun displayGattServices(gattServices: List<BluetoothGattService?>?) {
        Log.e(myTag, "characteristic! =++++ :1:}")
        if (gattServices == null) return
        characteristic.clear()
        myAdapter.notifyDataSetChanged()

        Log.e(myTag, "characteristic! =++++ :2:}")

        gattServices.forEach { gattService ->
            binding.deviceUid = gattService?.uuid.toString()
            gattService?.characteristics?.forEach { gattCharacteristic ->
                characteristic.add(gattCharacteristic)
                Log.e(myTag, "characteristic! =++++ :3: ${gattCharacteristic.service}}")
            }
        }

        myAdapter.notifyDataSetChanged()

        binding.gatingData = false
    }


    private var bluetoothService: BluetoothLeService? = null

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(myTag, "Unable to initialize Bluetooth")
                    finish()
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    fun showToast(string: String) {
        Toast.makeText(this@DeviceDetailActivity, string, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        Log.e(myTag, "OK!! registerReceiver")
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gattUpdateReceiver)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
    }
}