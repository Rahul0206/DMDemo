package com.example.bluetooth_connection.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth_connection.R
import com.example.bluetooth_connection.adapter.ScanResultAdapter
import java.util.*


const val myTag: String = ""

class MainActivity : AppCompatActivity() {

    /**
     *
     * @scanButton  => Instance of scanning button
     * @devicesRl   => Instance of RecyclerView
     * @myAdapter   => Recycler Adapter
     *
     */

    lateinit var scanButton: AppCompatButton
    lateinit var devicesRl: RecyclerView
    private lateinit var myAdapter: ScanResultAdapter

    val DEVICE_UUID = UUID.fromString("4576d562-7e68-11ec-90d6-0242ac120003")

    private val filters = ArrayList<ScanFilter>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanButton = findViewById(R.id.scan_btn)
        devicesRl = findViewById(R.id.devices_rl)
        setVerticalRecyclerView(this@MainActivity, findViewById(R.id.devices_rl))


        /// click BLE scan button event
        scanButton.setOnClickListener {
            getData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setVerticalRecyclerView(context: Context, recyclerView: RecyclerView) {
        myAdapter = ScanResultAdapter(context)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            adapter = myAdapter
        }
        myAdapter.submitList(arrayList)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        /// check LOCATION permission
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        /// check scanning permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationPermission()
                return
            }
        }

        /// check bluetooth on/off
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

            if (bluetoothAdapter?.isEnabled == false) {
                Toast.makeText(this, "Please on bluetooth", Toast.LENGTH_LONG).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
                return
            }

            /// create bluetoothLeScanner instance
            val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

            /// create handler for scanning duration
            val handler = Handler()


            if (!scanning) { // if not scanning
                // clear adapter item
                arrayList.clear()
                myAdapter.notifyDataSetChanged()

                handler.postDelayed({
                    // call ofter 5000ms
                    scanning = false
                    bluetoothLeScanner?.stopScan(leScanCallback)
                }, 5000)
                scanning = true
                // start scanning
                bluetoothLeScanner?.startScan(filters, scanSettings.build(), leScanCallback)
            } else { // if scanning
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
            }
        }
    }

    // BLE SCANNING SETTING
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)


    private val arrayList = ArrayList<ScanResult>()

    //  // BLE SCANNING response callback function
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("NotifyDataSetChanged", "MissingPermission", "NewApi")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            // check if already have a device in arrayList
            val indexQuery = arrayList.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) {
                //  if device already added in list then update item in adapter
                arrayList[indexQuery] = result
                myAdapter.notifyItemChanged(indexQuery)
            } else {
                //  added new device in list then update adapter
                arrayList.add(result)
                myAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(myTag, "onScanFailed: code $errorCode")
        }
    }


    //  scanning status variable
    private var scanning = false
        set(value) {
            field = value
            runOnUiThread { scanButton.text = if (value) "Stop Scan" else "Start Scan" }
        }


    //  Request for user all permission
    private fun requestLocationPermission() {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            } else {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }


    //  call when bluetooth on/off
    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(myTag, "${result.data} = ${result.resultCode}")
        }

    //  call when accept/deny permission
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.e(myTag, "${it.key} = ${it.value}")
            }
        }
}

