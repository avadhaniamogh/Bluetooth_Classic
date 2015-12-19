package com.android.bluetooth.classic.threads;

import java.io.IOException;

import com.android.bluetooth.classic.BluetoothClassicActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class ConnectThread extends Thread {

	private final BluetoothSocket mmSocket;
	private final BluetoothDevice mmDevice;
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	private Handler mHandler;
	private BluetoothClassicActivity mAtomActivity;

	public ConnectThread(BluetoothDevice device, Handler handler) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		this.mHandler = handler;
		BluetoothSocket tmp = null;
		mAtomActivity = new BluetoothClassicActivity();
		mmDevice = device;
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code
			tmp = device.createRfcommSocketToServiceRecord(mAtomActivity.getMyUUID());
		} catch (IOException e) { 

		}
		mmSocket = tmp;
	}

	public void run() {
		// Cancel discovery because it will slow down the connection
		mAtomActivity.getBluetoothAdapter().cancelDiscovery();
		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			mmSocket.connect();
		} catch (IOException connectException) {	
			// Unable to connect; close the socket and get out
			try {
				mmSocket.close();
			} catch (IOException closeException) { }
			return;
		}

		// Do work to manage the connection (in a separate thread)

		mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
	}
	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) { }
	}


}
