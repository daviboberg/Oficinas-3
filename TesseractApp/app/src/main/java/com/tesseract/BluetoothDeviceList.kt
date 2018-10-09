package com.tesseract


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast

class BluetoothDeviceList : Fragment() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mPairedDevices: Set<BluetoothDevice>
    private var bluetoothService: BluetoothService? = null

    override fun onStart() {
        super.onStart()
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            this.activity!!.startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        bluetoothService = BluetoothService(mHandler)
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService!!.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                bluetoothService!!.start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            val activity = activity
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            activity!!.finish()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_bluetooth_device_list, container, false)

        if (this.mBluetoothAdapter == null) {
            Toast.makeText(this.context, "This device does not support bluetooth", Toast.LENGTH_SHORT).show()
            return view
        }

//        if (!this.mBluetoothAdapter!!.isEnabled) {
//            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
//        }

        val scanButton: Button = view.findViewById(R.id.bluetooth_button_refresh)
        scanButton.setOnClickListener {
            doDiscovery()
            pairedDevicesList()
        }

        return view
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val activity = activity
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    BluetoothService.STATE_CONNECTED -> {
//                        setStatus(getString(R.string.title_connected_to, mConnectedDeviceName))
//                        mConversationArrayAdapter!!.clear()
                        Log.i(TAG, "MESSAGE_STATE_CHANGE, bluetooth state connected")
                    }
                    BluetoothService.STATE_CONNECTING -> {
                        Log.i(TAG, "Bluetooth state connecting")
                    }
                    BluetoothService.STATE_LISTEN, BluetoothService.STATE_NONE -> {
                        Log.i(TAG, "BLuetooth state Listen or None")
                    }

                }
                MESSAGE_WRITE -> {
//                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
//                    val writeMessage = String(writeBuf)
//                    mConversationArrayAdapter!!.add("Me:  $writeMessage")
                    Log.i(TAG, "MESSAGE WRITE")
                }
                MESSAGE_READ -> {
                    Log.i(TAG, "MESSAGE_READ")
//                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
//                    val readMessage = String(readBuf, 0, msg.arg1)
//                    mConversationArrayAdapter!!.add("$mConnectedDeviceName:  $readMessage")
                }
                MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME)
//                    if (null != activity) {
//                        Toast.makeText(activity, "Connected to " + mConnectedDeviceName!!, Toast.LENGTH_SHORT).show()
//                    }
                    Log.i(TAG, "MESSAGE_DEVICE_NAME")
                }
                MESSAGE_TOAST -> if (null != activity) {
                    Toast.makeText(activity, msg.data.getString(TOAST),
                            Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "MESSAGE_TOAST")

                }
            }
        }
    }

    private fun pairedDevicesList() {
        this.mPairedDevices = this.mBluetoothAdapter!!.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()

        if (!this.mPairedDevices.isEmpty()) {
            for (device: BluetoothDevice in mPairedDevices) {
                list.add(device)
                Log.i("device", ""+device)
            }
        } else {
            Toast.makeText(this.context, "No paired device", Toast.LENGTH_SHORT).show()
        }

        val deviceList: ListView = view!!.findViewById(R.id.bluetooth_device_list)
        val adapter = ArrayAdapter(this.context!!, android.R.layout.simple_list_item_1, list)
        deviceList.adapter = adapter
        deviceList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
            mBluetoothAdapter!!.cancelDiscovery()
            connectDevice(address, true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (mBluetoothAdapter!!.isEnabled) {
                        Toast.makeText(this.context, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this.context, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this.context, "Bluetooth enabling has been canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An [Intent] with [DeviceListActivity.EXTRA_DEVICE_ADDRESS] extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private fun connectDevice(address: String, secure: Boolean) {
        val device: BluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(address)
        bluetoothService!!.connect(device, secure)
    }

    private fun doDiscovery() {
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
        }

        mBluetoothAdapter!!.startDiscovery()
    }

    companion object {
        private val TAG = "BluetoothFragment"

        private const val REQUEST_CONNECT_DEVICE_SECURE = 1
        private const val REQUEST_CONNECT_DEVICE_INSECURE = 2
        private const val REQUEST_ENABLE_BLUETOOTH = 3
        private const val EXTRA_DEVICE_ADDRESS = "device_address"

        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        const val TOAST = "toast"
    }
}