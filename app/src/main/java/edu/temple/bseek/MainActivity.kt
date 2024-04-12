package edu.temple.bseek

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.lang.UProperty.NAME
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 50
    private val PERMISSION_CODE = 100
    private val DISCOVER_CODE = 150
    private val BLUETOOTH_CONNECT_PERMISSION = 200

    private var bluetoothPermissionGiven = false

    lateinit var createSessionButton: Button
    lateinit var joinSessionButton: Button

    // create vars for sensorManager, light sensor and sensorEventListener
    lateinit var sensorManager: SensorManager
    var lightSensor: Sensor? = null
    lateinit var sensorEventListener: SensorEventListener

    // get Bluetooth adapter for enabling, discovery, handling
    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null

    // UUID variable to uniquely identify device for Bluetooth connections
    val MY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")








    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, HostSeek::class.java)
        startActivity(intent)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter

        // Ask for fine location access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(baseContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_CODE)
            }
        }

        // check for BLUETOOTH_CONNECT permission
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                BLUETOOTH_CONNECT_PERMISSION
            )
        } else {
            Log.d("BLUETOOTH_CONNECT Permission denied", "BLUETOOTH_CONNECT Permission denied")
        }*/

        // get light sensor
        sensorManager = getSystemService(SensorManager::class.java)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val requestBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)


        // sensor event listener for ambient light sensor
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(p0: SensorEvent?) {
                p0?.run {
                    if (p0.sensor.type == Sensor.TYPE_LIGHT) {
                        Log.d("Light Sensor Event", p0.values[0].toString())
                    }
                }

            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                Log.d("Accuracy Change", "Accuracy Change")
            }

        }

        // register for sensor updates(will persist as long as the activity does)
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)

        createSessionButton = findViewById(R.id.startSessionButton)
        joinSessionButton = findViewById(R.id.joinSessionButton)




        // check if device has bluetooth feature
        if (bluetoothAdapter == null) {
            Log.d("No device Bluetooth", "Device has no Bluetooth.")
        } else {
            // do everything involving bluetooth
            // check if Bluetooth is enabled, creates dialog pop up
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)


            } else {
                    /*
                    Log.d("Querying Paired Devices","query all paired devices")
                    // query all paired devices and get the name and MAC address of each device
                    val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                    pairedDevices?.forEach { device ->
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address

                        val hashPairedDevices = HashMap<String, String>()
                        hashPairedDevices[device.name] = device.address


                        // Register for broadcasts when a device is discovered.
                        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                        registerReceiver(receiver, filter)

                }

                     */

            }
        }







        // TODO code automatically goes to denied. But when you run thread code after, it needs the permissiion?
        createSessionButton.setOnClickListener {
            // start a session using an intent
            /*val connectThread = ConnectThread()
            connectThread.start()*/
            // check for BLUETOOTH_CONNECT permission
            // Register for broadcasts when a device is discovered.
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)

        }



        joinSessionButton.setOnClickListener {
            // make local device discoverable to other devices
            startActivityForResult(requestBtIntent, DISCOVER_CODE)
            // prompt the user for a session key
        }

    }



    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.d("onReceive() Broadcast Receiver Action_FOUND", "onReceive() Broadcast Receiver Action_FOUND")
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d("onReceive Permission not granted","BLUETOOTH_CONNECT permission not granted.")
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    } else {

                    }
                    device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                }
            }
        }
    }





    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.appcompat.app.AppCompatActivity"
    )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // change global variable
                bluetoothPermissionGiven = true
            } else {
                // handle the failed/rejected enable attempt
            }
        }
    }

    // Thread that turns the users device into a server to host the incoming Bluetooth connections
    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {


        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME.toString(), MY_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            Log.d("accept thread run()", "accept thread is running.")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }

        fun manageMyConnectedSocket(socket: BluetoothSocket?) {
            //
            Log.d("manageMyConnectSocket", "Connected to client device")
        }
    }


    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageOtherDeviceSocket(socket)
            }
        }

        private fun manageOtherDeviceSocket(socket: BluetoothSocket?) {
            // do something with the socket
            Log.d("manageOtherDeviceSocket", "Connected to server device")
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // un register listener for light sensor
        sensorManager.unregisterListener(sensorEventListener)
        unregisterReceiver(receiver)
    }
}

