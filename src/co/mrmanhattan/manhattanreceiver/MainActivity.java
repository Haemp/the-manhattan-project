package co.mrmanhattan.manhattanreceiver;

import java.util.ArrayList;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// TODO: Get a list of all the available bluetooth devices and show them in a list
public class MainActivity extends Activity {

	private ArrayList<String> deviceNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.deviceNames = new ArrayList<String>();
		
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		
		// check if the bluetooth is enabled
		if( !bluetooth.isEnabled() ){
			Intent startBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(startBluetooth, 1);
		}else{
			
			Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
			
			for (BluetoothDevice device : devices) {
				this.deviceNames.add(device.getName() + "\n" + device.getAddress() + device.getUuids()[0].toString());
				//device.createRfcommSocketToServiceRecord(uuid)
			}
			
			ArrayAdapter<String> devicesArray = new ArrayAdapter<String>(this, R.layout.bluetooth_device_view, this.deviceNames);
			
			ListView list = (ListView) findViewById(R.id.bluetoothList);
			list.setAdapter(devicesArray);
			
		}
				
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
