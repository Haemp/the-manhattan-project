package co.mrmanhattan.manhattanreceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// TODO: Get a list of all the available bluetooth devices and show them in a list
public class MainActivity extends Activity {

	private ArrayList<String> deviceNames;
	private BluetoothAdapter bluetooth; 
	private ConnectThread thread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.deviceNames = new ArrayList<String>();
		
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		BluetoothSocket socket = null;
		// check if the bluetooth is enabled
		BluetoothDevice device = getDeviceByName("HC-06");
		
		// send this device to the thread to handle the 
		// socket connection
		this.thread = new ConnectThread(device);
		this.thread.run();
	}
	
	
	private BluetoothDevice getDeviceByName( String name ){
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		
		// check if the bluetooth is enabled
		if( !bluetooth.isEnabled() ){
			Intent startBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(startBluetooth, 1);
		}else{
			
			Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
			
			for (BluetoothDevice device : devices) {
				if( device.getName().equals(name) ){
					return device;
				}
			}
			
			//ArrayAdapter<String> devicesArray = new ArrayAdapter<String>(this, R.layout.bluetooth_device_view, this.deviceNames);
			
			//ListView list = (ListView) findViewById(R.id.bluetoothList);
			//list.setAdapter(devicesArray);
		}
		return null;
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	        
	        // TODO: This could be totally wrong UUID
	        ParcelUuid uuid = device.getUuids()[0];
	        
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(uuid.getUuid());
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        bluetooth.cancelDiscovery();
	 
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
	        //manageConnectedSocket(mmSocket);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}

}


