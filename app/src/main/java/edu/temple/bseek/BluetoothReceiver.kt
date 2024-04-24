package edu.temple.bseek

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BluetoothReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (action==BluetoothAdapter.ACTION_STATE_CHANGED) {
            Log.d("BluetoothReceiver","Action state changed")
            when(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)){
                BluetoothAdapter.STATE_ON->{
                    Log.d("BT Receiver", "State on")
                }
                BluetoothAdapter.STATE_OFF->{
                    Log.d("BT Receiver", "State off")
                }
                BluetoothAdapter.STATE_TURNING_OFF->{
                    Log.d("BT Receiver", "State turning off")
                }
                BluetoothAdapter.STATE_TURNING_ON->{
                    Log.d("BT Receiver", "State turning on")
                }
            }
        }
    }
}


