package edu.temple.bseek

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    lateinit var createSessionButton: Button
    lateinit var joinSessionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createSessionButton = findViewById(R.id.startSessionButton)
        joinSessionButton = findViewById(R.id.joinSessionButton)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.d("No device Bluetooth", "Device has no Bluetooth.")
        }


        createSessionButton.setOnClickListener {
            // start a session using an intent
        }

        joinSessionButton.setOnClickListener {
            // prompt the user for a session key
        }

    }
}