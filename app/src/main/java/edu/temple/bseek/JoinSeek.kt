package edu.temple.bseek

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.util.UUID

class JoinSeek : AppCompatActivity() {
    // create vars for sensorManager, light sensor and sensorEventListener
    lateinit var sensorManager: SensorManager
    var lightSensor: Sensor? = null
    lateinit var sensorEventListener: SensorEventListener
    lateinit var sender: BluetoothService.SendReceive

    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothSocket: BluetoothSocket

    /*val stopSeek = findViewById<Button>(R.id.stopSeekButton)
    val beginSeek = findViewById<Button>(R.id.beginSeekButton)
    val restartSeek = findViewById<Button>(R.id.restartSeekButton)*/

    lateinit var gameStatusTextView : TextView
    lateinit var timerText : TextView
    lateinit var handlerInfo : TextView
    lateinit var pairedDeviceTextView: TextView
    lateinit var brightnessValue : TextView

    val timer = object: CountDownTimer(300000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            gameStatusTextView.text = "Active"
            timerText.text = (millisUntilFinished / 1000).toString()
        }

        override fun onFinish() {
            gameStatusTextView.text = "Inactive"
        }
    }
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seek_session_join)

        // get BluetoothManager and adapter to perform BT tasks
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // initialize views
        val gameStatusTextView = findViewById<TextView>(R.id.gameStatusSeekTextView)
        val timerText = findViewById<TextView>(R.id.timerTextView)
        val handlerInfo = findViewById<TextView>(R.id.handlerInfo)
        val pairedDeviceText = findViewById<TextView>(R.id.pairedDeviceTextView)
        val brightnessView = findViewById<TextView>(R.id.brightnessValueTextView)

        val connectedDevice = intent.getStringExtra("DEVICE")
        val device = intent.getParcelableExtra<BluetoothDevice>("DEVICE_OBJ")
        if (device != null) {
            pairedDeviceText.text = device.name
        }
        if (device != null) {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(bStates.UUID))
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
                        var lightValue = p0.values[0].toString()
                        Log.d("Light Sensor Event", lightValue)
                        sender = BluetoothService(bluetoothAdapter, msgHandler).SendReceive(bluetoothSocket)
                        //sender.write(lightValue.toByteArray())
                        if (p0.values[0] <= 1) {
                            brightnessView.text = "User's device is somewhere dark"
                        } else if (p0.values[0] > 250) {
                            brightnessView.text = "User's device is somewhere bright"
                        }
                    }
                }

            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                Log.d("Accuracy Change", "Accuracy Change")
            }

        }

        // register for sensor updates(will persist as long as the activity does)
        sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)

        //pairedDeviceTextView.text = connectedDevice





        val clientClass = device?.let {
            Log.d("client class", device.name.toString())
            BluetoothService(bluetoothAdapter, msgHandler).ClientClass(
                it
            )
        }

    }

    // handler for the changes in connection state
    private val msgHandler : Handler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                bStates.STATE_LISTENING -> {
                    handlerInfo.text = "Listening"
                }

                bStates.STATE_CONNECTING -> {
                    handlerInfo.text = "Connecting..."
                }

                bStates.STATE_CONNECTED -> {
                    handlerInfo.text = "Connected"
                }

                bStates.STATE_FAILED -> {
                    handlerInfo.text = "Connection Failed"
                }

                bStates.MSG_RECEIVED -> {
                    val readMsg = msg.obj as ByteArray
                    val tempMsg = String(readMsg, 0, msg.arg1)
                    handlerInfo.text = tempMsg
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }
}