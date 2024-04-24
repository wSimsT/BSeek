package edu.temple.bseek

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothService(adapter: BluetoothAdapter, mHandler : Handler) {
    private val bluetoothAdapter = adapter
    private val handler = mHandler
    private lateinit var sendReceiver: SendReceive

    @SuppressLint("MissingPermission")
    inner class ServerClass: Thread() {
        private var serverSocket : BluetoothServerSocket? = null

        override fun run() {
            super.run()
            var socket: BluetoothSocket? = null
            while (socket==null) {
                try {
                    val message = Message.obtain()
                    message.what = bStates.STATE_CONNECTING
                    handler.sendMessage(message)
                    socket = serverSocket!!.accept()
                } catch(e:IOException) {
                    e.printStackTrace()
                    val message = Message.obtain()
                    message.what = bStates.STATE_FAILED
                    handler.sendMessage(message)

                }

                if (socket!=null) {
                    val message = Message.obtain()
                    message.what = bStates.STATE_CONNECTED
                    handler.sendMessage(message)
                    sendReceiver = SendReceive(socket)
                    sendReceiver.start()
                    break

                }
            }
        }

        init {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BSeek", UUID.fromString(bStates.UUID))
            } catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }

    inner class SendReceive(private val bluetoothSocket: BluetoothSocket) : Thread() {
        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null


        override fun run() {
            super.run()
            val buffer = ByteArray(1024)
            var bytes : Int? = null

            while(true) {
                try {
                    bytes = (inputStream?.read(buffer) ?: bytes?.let {
                        handler.obtainMessage(bStates.MSG_RECEIVED,
                            it,-1, buffer).sendToTarget()
                    }) as Int
                }
                catch(e:IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun write(byte: ByteArray) {
            try {
                outputStream?.write(byte)
            } catch (e:IOException) {
                e.printStackTrace()
            }
        }

        init {
            var tempIn: InputStream? = null
            var tempOut: OutputStream? = null

            try {
                tempIn = bluetoothSocket.inputStream
                tempOut = bluetoothSocket.outputStream
            } catch (e:IOException) {
                e.printStackTrace()
            }

            if (tempIn!=null) {
                inputStream = tempIn
            }
            if (tempOut!=null) {
                outputStream = tempOut
            }
        }
    }

    @SuppressLint("MissingPermission")
    inner class ClientClass(private val device: BluetoothDevice): Thread(){
        private var socket: BluetoothSocket? = null
        @SuppressLint("MissingPermission")
        override fun run() {
            super.run()

            try {
                socket?.connect()
                val message = Message.obtain()
                message.what = bStates.STATE_CONNECTED
                handler.sendMessage(message)
                sendReceiver = socket?.let { SendReceive(it) }!!
                sendReceiver.start()
            } catch (e: IOException) {
                e.printStackTrace()
                val message = Message.obtain()
                message.what = bStates.STATE_FAILED
                handler.sendMessage(message)
            }
        }

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(bStates.UUID))
            } catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }
}