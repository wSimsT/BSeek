package edu.temple.bseek

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
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
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 50
    private val PERMISSION_CODE = 100
    private val DISCOVER_CODE = 150
    private val BLUETOOTH_CONNECT_PERMISSION = 200
    val REQUEST_BLUETOOTH_CONNECT = 1
    val REQUEST_ACCESS_COURSE_LOCATION = 101
    val REQUEST_BLUETOOTH_SCAN = 250

    private lateinit var bondedArray: Array<String>
    private var btDevices = arrayOfNulls<BluetoothDevice>(3)


    private var bluetoothPermissionGiven = false

    lateinit var createSessionButton: Button
    lateinit var joinSessionButton: Button
    lateinit var pairedSessionButton: Button
    lateinit var discoverDeviceButton: Button
    lateinit var bondedRecyclerView: RecyclerView
    lateinit var bondedTextView1 : TextView
    lateinit var bondedTextView2 : TextView
    lateinit var bondedTextView3 : TextView

    // create vars for sensorManager, light sensor and sensorEventListener
    lateinit var sensorManager: SensorManager
    var lightSensor: Sensor? = null
    lateinit var sensorEventListener: SensorEventListener

    // get Bluetooth adapter for enabling, discovery, handling
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothDevice: BluetoothDevice
    lateinit var receiverBT: BluetoothReceiver
    lateinit var receiverDisc: Discoverability

    // UUID variable to uniquely identify device for Bluetooth connections
    val MY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    // Define a constant for the permission request code
    private val REQUEST_BLUETOOTH_PERMISSION = 123

    private val discoverReceiver = object: BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.d("discoveryReceiver", "code started running")
            var action = ""
            if(intent!=null) {
                action = intent.action.toString()
            }
            when(action){
                BluetoothAdapter.ACTION_STATE_CHANGED ->{
                    Log.d("discoverReceiver", "STATE CHANGED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("discoverReceiver", "DISCOVERY STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->{
                    Log.d("discoverReceiver", "DISCOVERY FINISHED")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d("BluetoothDevice Action_Found","Action Found")
                    val device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device!=null) {
                        Log.d("discoverReceiver", "${device.name} ${device.uuids}")

                    }


                }

            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize buttons
        createSessionButton = findViewById(R.id.createSessionButton)
        joinSessionButton = findViewById(R.id.joinSessionButton)
        pairedSessionButton = findViewById(R.id.pairSessionButton)
        discoverDeviceButton = findViewById(R.id.discoverDevicesButton)

        // get BluetoothManager and adapter to perform BT tasks
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        //test2



        /*
        val BTpermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val runtimePermissions = mutableListOf<String>()
        for(perms in runtimePermissions) {
            if (ContextCompat.checkSelfPermission(this, perms)
                != PackageManager.PERMISSION_GRANTED
            ) {
                runtimePermissions.add(perms)
            }
        }
        if (runtimePermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                runtimePermissions.toTypedArray(),
                BLUETOOTH_CONNECT_PERMISSION
            )
        } else {
            Log.d("Permissions", "Permissions already granted onCreate")
        }*/



        // initialize receivers
        receiverBT = BluetoothReceiver()
        receiverDisc = Discoverability()


        // gets paired Bt devices and lists them to the user
        pairedSessionButton.setOnClickListener{
            var increment = 0

            getPairedBT()
            val pairedDevices = bluetoothAdapter.bondedDevices

            bondedArray = pairedDevices?.map { device ->
                "${device.name}\n${device.bondState}"
            }?.toTypedArray()!!

            val names = arrayOfNulls<String>(pairedDevices.size)
            //btDevices = arrayOfNulls<BluetoothDevice>(pairedDevices.size)

            increment = 0

            if (pairedDevices.size > 0) {
                for (device in pairedDevices) {
                    btDevices[increment] = device
                    Log.d("pairedSession button", btDevices[increment].toString())
                    if (device != null) {
                        names[increment] = device.name
                    }
                    increment++
                }
            }

            Log.d("btDevices with enableDiscovery", btDevices[0].toString())
            btDevices[0]?.let { enableDiscover(it) }
            bluetoothDevice = btDevices[0]!!

            // initialize text views
            bondedTextView1 = findViewById(R.id.pairedDevice1Text)
            bondedTextView2 = findViewById(R.id.pairedDevice2Text)
            bondedTextView3 = findViewById(R.id.pairedDevice3Text)

            var deviceAmount = 0
            // assign bonded devices to main activity text views
            if (bondedArray.isNotEmpty()) {

                if (bondedArray[0]!=null&&(bondedArray.size > 0)) {
                    bondedTextView1.text = bondedArray[0]
                    bondedTextView1.setOnClickListener {

                    }

                }


            }


            //setContentView(R.layout.bonded_list)
        }

        // enable bluetooth
        enableBT()

        discoverDeviceButton.setOnClickListener{
            // Check if the Bluetooth permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request the permission

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    REQUEST_BLUETOOTH_SCAN)
            } else {
                        // Permission is already granted
                        // You can perform Bluetooth operations here
            }

                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                        when(ContextCompat.checkSelfPermission(
                            baseContext,Manifest.permission.ACCESS_COARSE_LOCATION
                        )){
                            PackageManager.PERMISSION_DENIED-> AlertDialog.Builder(this)
                                .setTitle("Runtime Permission")
                                .setMessage("Give Permission")
                                .setNeutralButton("Okay",DialogInterface.OnClickListener{dialog, which ->
                                    if(ContextCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_COARSE_LOCATION)!=
                                        PackageManager.PERMISSION_GRANTED){
                                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_ACCESS_COURSE_LOCATION)
                                    }
                                }).show()
                            //.findViewById<TextView>(R.id.message)!!.movementMethod = LinkMovementMethod.getInstance()

                            PackageManager.PERMISSION_GRANTED ->{
                                discoverDevices()
                                Log.d("discoverDevices", "Permission Granted")

                            }
                        }
                    }

                }







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


    }

    // Override onRequestPermissionsResult to handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if the request code matches the Bluetooth permission request code
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            // Check if the permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with Bluetooth-related tasks
                //enableBT()
                bluetoothPermissionGiven = true
                Log.d("onRequestPermissionResult", "request bluetooth enableBT")
            } else {
                // Permission is denied, handle this situation (e.g., show a message to the user)
                // Optionally, you can disable or hide Bluetooth-related functionality
            }
        } else if (requestCode == REQUEST_BLUETOOTH_SCAN) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("onRequestPermissionResult", "request bluetooth scan")
            } else {

            }
        } else if (requestCode == REQUEST_ACCESS_COURSE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //discoverDevices()
                Log.d("onRequestPermissionResult", "request access coarse and discover devices function ran")
            } else {

            }
        }
    }


    // starts looking for devices using an intent filter that is passed to a broadcast receiver
    @SuppressLint("MissingPermission")
    private fun discoverDevices() {
        Log.d("discoverDevices", "function is running")
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoverReceiver, filter)


        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
        Log.d("discoverDevices", "discovery started.")
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
                Log.d("onActivityResult", "bluetoothPermission given is true")
            } else {
                // failed/rejected enable attempt
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
        //unregisterReceiver(receiver)
        unregisterReceiver(receiverBT)
        unregisterReceiver(receiverDisc)
    }

    private fun enableBT() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED->{

            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_CONNECT)->{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 101)
            }
        }


        joinSessionButton.setOnClickListener {

            Log.d("joinSessionButton", "button clicked")



            if(!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(intent)

                val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                registerReceiver(receiverBT, intentFilter)
            }

            // start the hosting session for the game
            val joinIntent = Intent(this, JoinSeek::class.java)
            joinIntent.putExtra("DEVICE", bondedTextView1.text)
            joinIntent.putExtra("DEVICE_OBJ", bluetoothDevice)
            startActivity(joinIntent)

            Log.d("join button","enabled BT")

        }

    }

    // get the devices that are already bonded to the users devices through Bluetooth
    @SuppressLint("MissingPermission")
    private fun getPairedBT() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED->{

            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_SCAN)->{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_SCAN), PERMISSION_CODE)
            }
        }
        val bondedDev = bluetoothAdapter.bondedDevices
        Log.d("getPairedBT", bondedDev.size.toString())
        Log.d("getPairedBT", bondedDev.toString())

        for (device in bondedDev) {
            Log.d("getPairedBT devices", device.name)
        }
    }

    private fun enableDiscover(btDevice: BluetoothDevice) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED->{

            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_CONNECT)->{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 101)
            }
        }

        createSessionButton.setOnClickListener {
            Log.d("createSessionButton", "button clicked")
            // make local device discoverable to other devices
            //startActivityForResult(requestBtIntent, DISCOVER_CODE)
            // prompt the user for a session key

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request the permission

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_CONNECT)
            } else {
                // Permission is already granted
                // You can perform Bluetooth operations here
            }
            val discoverIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 20)
            startActivity(discoverIntent)

            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            registerReceiver(receiverDisc, intentFilter)
            Log.d("enableDiscovery","registered receiver for BT")

            //val clientClass = BluetoothService(bluetoothAdapter, msgHandler).ServerClass()

            // start the hosting session for the game
            val hostIntent = Intent(this, HostSeek::class.java)
            hostIntent.putExtra("DEVICE", bondedTextView1.text)
            hostIntent.putExtra("DEVICE_OBJ", btDevice)
            startActivity(hostIntent)

        }
    }



}

