package co.mrmanhattan.manhattanreceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

import java.sql.Date;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import javax.crypto.AEADBadTagException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

// TODO: Get a list of all the available bluetooth devices and show them in a list
public class MainActivity extends Activity {

	private ArrayList<String> deviceNames;
	private BluetoothAdapter bluetooth; 
	private ConnectThread thread;
	public TextToSpeech ttobj; 
	public TextView text;
	
	Handler mHandler = new Handler(){
		
		@Override
	    public void handleMessage(Message msg) {
			byte[] a = (byte[]) msg.obj;
			byte b = a[0];
			String txt = b+"";
			
			if(txt.equals("1")){
				ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
					   @Override
					   
					   
					   public void onInit(int status) {
						   ttobj.setLanguage(Locale.UK);
						   Calendar c = Calendar.getInstance(); 
							SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
							String formatted = format1.format(c.getTime());

						   speakText(formatted);
					   }
					}
					);
			}
			
			text.setText(txt);
			
			// get current time
			
			
			System.out.println("Message" +msg.toString());
		}
	};
	
	/*
	 * Method that takes a string and outputs it as sound 
	 */
   public void speakText(String toSpeak){
	      ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
	   }
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		this.deviceNames = new ArrayList<String>();
		text = (TextView) findViewById(R.id.signal);
		text.setText("Initated");
		


		if(true){
			ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
				   @Override
				   public void onInit(int status) {
					   ttobj.setLanguage(Locale.UK);
						Calendar c = Calendar.getInstance(); 
						SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
						String formatted = format1.format(c.getTime());

					   speakText(formatted);
				   }
				}
			
				);
		}

		
		// bluetooth logic
			
			bluetooth = BluetoothAdapter.getDefaultAdapter();
		if( bluetooth != null ){
			BluetoothSocket socket = null;
			// check if the bluetooth is enabled
			BluetoothDevice device = getDeviceByName("DKBLE113 thermometer");
			
			if(device != null){
				// send this device to the thread to handle the 
				// socket connection
				this.thread = new ConnectThread(device);
				this.thread.start();			
			}
		}
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
	        Thread cThread = new ConnectedThread(mmSocket);
	        cThread.start();
	        
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	
	
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	        
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	            	Thread.sleep(1);
	                bytes = mmInStream.read(buffer);
	                
	                // Send the obtained bytes to the UI activity
	                mHandler.sendMessage(Message.obtain(mHandler, 9001, buffer));
	                // mHandler.obtainMessage(9999, bytes)
	                //       .sendToTarget();
	            } catch (IOException e) {
	                break;
	            } catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}

}


