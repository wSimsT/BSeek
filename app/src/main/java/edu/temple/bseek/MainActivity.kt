package edu.temple.bseek

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 100

    lateinit var createSessionButton: Button
    lateinit var joinSessionButton: Button

    // create vars for sensorManager, light sensor and sensorEventListener
    lateinit var sensorManager: SensorManager
    var lightSensor: Sensor? = null
    lateinit var sensorEventListener: SensorEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // get light sensor
        sensorManager = getSystemService(SensorManager::class.java)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)


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

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.d("No device Bluetooth", "Device has no Bluetooth.")
        } else {
            // do everything involving bluetooth
            // check if Bluetooth is enabled
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                val REQUEST_ENABLE_BT = 0
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }




        createSessionButton.setOnClickListener {
            // start a session using an intent
        }

        joinSessionButton.setOnClickListener {
            // prompt the user for a session key
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        // un register listener for light sensor
        sensorManager.unregisterListener(sensorEventListener)
    }
}