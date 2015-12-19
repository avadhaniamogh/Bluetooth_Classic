package com.android.bluetooth.classic.fragment;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import com.android.bluetooth.classic.BluetoothClassicActivity;
import com.android.bluetooth.classic.R;
import com.android.bluetooth.classic.threads.ConnectThread;
import com.android.bluetooth.classic.threads.ConnectedThread;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothFragment extends Fragment {

	private ArrayAdapter<String> mBluetoothPairedArrayAdapter;
	private ArrayAdapter<String> mBluetoothAvailableArrayAdapter;
	private BluetoothClassicActivity mBTClassicActivity = new BluetoothClassicActivity();
	ListView mPairedDeviceListView;
	ListView mAvailableDeviceListView;
	ArrayList<BluetoothDevice> mBluetoothAvailableArrayList;
	ArrayList<BluetoothDevice> mBluetoothPairedArrayList;
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	ConnectThread mConnectThread;
	ConnectedThread mConnectedThread;
	String TAG = "BLUETOOTH";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bluetooth_fragment, parentViewGroup, false);
		Button bluetoothOn = (Button) rootView.findViewById(R.id.btn_bluetooth_on);
		Button bluetoothOff = (Button) rootView.findViewById(R.id.btn_bluetooth_off);
		Button bluetoothSearch = (Button) rootView.findViewById(R.id.btn_bluetooth_search);
		mAvailableDeviceListView = (ListView) rootView.findViewById(R.id.available_device_list);
		mPairedDeviceListView = (ListView) rootView.findViewById(R.id.paired_device_list);

		mBluetoothPairedArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAvailableArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAvailableArrayList = new ArrayList<BluetoothDevice>();
		mBluetoothPairedArrayList = new ArrayList<BluetoothDevice>();

		if(mBTClassicActivity.getBluetoothAdapter() == null) {
			Toast.makeText(getActivity(),"Device does not support Bluetooth"
					,Toast.LENGTH_LONG).show();
			return rootView;
		}

		if(mBTClassicActivity.getBluetoothAdapter().isEnabled()) {
			displayPairedDevices();
		} else {
			clearPairedDevices();
		}
		OnClickListener bluetoothOnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mBTClassicActivity.getBluetoothAdapter().isEnabled()) {
					Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(turnOn, 0);
					Toast.makeText(getActivity(),"Bluetooth turned on"
							,Toast.LENGTH_LONG).show();
				}
				else{
					Toast.makeText(getActivity(),"Bluetooth already on",
							Toast.LENGTH_LONG).show();
				}
			}
		};
		bluetoothOn.setOnClickListener(bluetoothOnListener);

		OnClickListener bluetoothOffListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBTClassicActivity.getBluetoothAdapter().disable();
				Toast.makeText(getActivity(),"Bluetooth turned off" ,
						Toast.LENGTH_LONG).show();
			}
		};
		bluetoothOff.setOnClickListener(bluetoothOffListener);

		OnClickListener bluetoothSearchListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mBTClassicActivity.getBluetoothAdapter().isDiscovering()) {
					mBTClassicActivity.getBluetoothAdapter().cancelDiscovery();
				}
				clearAvailableDevices();
				mBTClassicActivity.getBluetoothAdapter().startDiscovery();
			}
		};
		bluetoothSearch.setOnClickListener(bluetoothSearchListener);
		mPairedDeviceListView.setAdapter(mBluetoothPairedArrayAdapter);
		mAvailableDeviceListView.setAdapter(mBluetoothAvailableArrayAdapter);

		mAvailableDeviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(),"Click" ,
						Toast.LENGTH_LONG).show();
				final int devicePosition = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure");
				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mBTClassicActivity.getBluetoothAdapter().isDiscovering()) {
							mBTClassicActivity.getBluetoothAdapter().cancelDiscovery();
						}
						BluetoothDevice bluetoothDevice = mBluetoothAvailableArrayList.get(devicePosition);
						Boolean isPaired = doPairing(bluetoothDevice);
						if(isPaired) {
							//mBluetoothPairedArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
							Log.d(TAG, "createBond called successfully");
						}
					}
				});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				AlertDialog alert11 = builder.create();
				alert11.show();
			}
		});

		mPairedDeviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				final int devicePosition = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure");
				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						BluetoothDevice bluetoothDevice = mBluetoothPairedArrayList.get(devicePosition);
						/*Boolean isRemoved = doRemove(bluetoothDevice);
						if(isRemoved) {
							Log.d(TAG, "removeBond called successfully");
						}*/
						doConnectDevice(bluetoothDevice);
					}
				});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				AlertDialog alert11 = builder.create();
				alert11.show();
			}

		});

		return rootView;
	}
	
	private void doConnectDevice(BluetoothDevice bluetoothDevice) {
		if(mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if(mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mConnectThread = new ConnectThread(bluetoothDevice, mHandler);
		mConnectThread.start();
	}

	private Boolean doPairing(BluetoothDevice bluetoothDevice) {
		// TODO Auto-generated method stub
		try {
			Class<?> class1 = Class.forName("android.bluetooth.BluetoothDevice");
			Method createBondMethod = class1.getMethod("createBond");  
			Boolean returnValue = (Boolean) createBondMethod.invoke(bluetoothDevice);  
			return returnValue.booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private Boolean doRemove(BluetoothDevice bluetoothDevice) {
		try {
			Class<?> class1 = Class.forName("android.bluetooth.BluetoothDevice");
			Method removeBondMethod = class1.getMethod("removeBond");  
			Boolean returnValue = (Boolean) removeBondMethod.invoke(bluetoothDevice);  
			return returnValue.booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void clearPairedDevices() {
		// TODO Auto-generated method stub
		mBluetoothPairedArrayAdapter.clear();
		mBluetoothPairedArrayList.clear();
		mBluetoothPairedArrayAdapter.notifyDataSetChanged();
	}

	public void displayPairedDevices() {
		// TODO Auto-generated method stub
		Set<BluetoothDevice> pairedDevices;
		pairedDevices = mBTClassicActivity.getBluetoothAdapter().getBondedDevices();
		mBluetoothPairedArrayAdapter.clear();
		mBluetoothPairedArrayList.clear();
		for(BluetoothDevice device : pairedDevices) {
			mBluetoothPairedArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
			mBluetoothPairedArrayList.add(device);
		}
		mBluetoothPairedArrayAdapter.notifyDataSetChanged();
	}

	public void clearAvailableDevices() {
		mBluetoothAvailableArrayAdapter.clear();
		if(mBluetoothAvailableArrayList != null)
			mBluetoothAvailableArrayList.clear();
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}

	public void addDeviceAndDisplayAvailableDevices(BluetoothDevice device) {
		mBluetoothAvailableArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		mBluetoothAvailableArrayList.add(device);
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}

	public void removeDeviceAndDisplayAvailableDevices(BluetoothDevice device) {
		mBluetoothAvailableArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
		mBluetoothAvailableArrayList.remove(device);
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}

	private class AcceptThread extends Thread {
		private BluetoothClassicActivity mBTClassicActivity = new BluetoothClassicActivity();
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mBTClassicActivity.getBluetoothAdapter().listenUsingRfcommWithServiceRecord("BT", mBTClassicActivity.getMyUUID());
			} catch (IOException e) { }
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					//	                manageConnectedSocket(socket);
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) { }
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	}
	
	MyBluetoothHandler mHandler = new MyBluetoothHandler(this);
	static class MyBluetoothHandler extends Handler{
        WeakReference<BluetoothFragment> mFragment;

        MyBluetoothHandler(BluetoothFragment aFragment) {
            mFragment = new WeakReference<BluetoothFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
            BluetoothFragment fragment = mFragment.get();
            switch(msg.what){
			case SUCCESS_CONNECT:
				if(fragment.mConnectedThread != null) {
					fragment.mConnectedThread.cancel();
					fragment.mConnectedThread = null;
				}
				fragment.mConnectedThread = new ConnectedThread((BluetoothSocket)msg.obj, fragment.mHandler);
				Toast.makeText(fragment.getActivity(), "CONNECT", Toast.LENGTH_SHORT).show();
				String s = "successfully connected ";
				fragment.mConnectedThread.start();
				for(int i = 0; i < 10; i++) {
					fragment.mConnectedThread.write(s.getBytes());
				}
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[])msg.obj;
				String string = new String(readBuf);
				if(!fragment.mBTClassicActivity.getBluetoothAdapter().isEnabled()) {
					if(fragment.mConnectedThread != null) {
						fragment.mConnectedThread.cancel();
						fragment.mConnectedThread = null;
					}
					Log.d(fragment.TAG, "00-> Bluetooth returned");
					return;
				}
				Toast.makeText(fragment.getActivity(), string, Toast.LENGTH_SHORT).show();
				Log.d(fragment.TAG, "Bluetooth Received: " + string);
				break;
			}
            
        }
    }
}
