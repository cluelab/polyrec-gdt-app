package it.unisa.di.cluelab.polyrec.bluetooth.app;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import it.unisa.di.cluelab.polyrec.TPoint;


public class ClientSocketActivity extends Activity {
	private static final String TAG = ClientSocketActivity.class.getSimpleName();
	private static final int REQUEST_DISCOVERY = 0x1;;
	private Handler _handler = new Handler();
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

	private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

	private BluetoothSocket socket = null;
	protected ObjectOutputStream oos;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		setContentView(R.layout.client_socket);
		if (!_bluetooth.isEnabled()) {
			finish();
			return;
		}
		Intent intent = new Intent(this, DiscoveryActivity.class);
	

		/* Prompted to select a server to connect */
		

		/* Select device for list */
		startActivityForResult(intent, REQUEST_DISCOVERY);
	}

	/* after select, connect to device */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode != REQUEST_DISCOVERY) {

			return;
		}
		if (resultCode != RESULT_OK) {

			return;
		}
		final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		//Toast.makeText(this, device.getName(), Toast.LENGTH_SHORT).show();

		
			if (socket ==null || socket!= null && !socket.isConnected()) {
			try {
				socket = device.createRfcommSocketToServiceRecord(MY_UUID);
			
				socket.connect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (socket != null)
					try {
						Toast.makeText(this, "UNABLE TO CONNECT TO SELECTED DEVICE\n(check you have select 'draw with smartphone' in Polyrec Application)", Toast.LENGTH_LONG).show();
						
						socket.close();
						_bluetooth.disable();
						  Intent i = new Intent(getApplicationContext(), Activity01.class);
			               startActivity(i);
					} catch (IOException e1) {
					
						e1.printStackTrace();
					}
			}}
			if (socket.isConnected()) {
				Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
			/*
			 * Intent intent = new Intent(this, CanvasActivity.class);
			 * startActivity(intent);
			 */
				LinearLayout layout = (LinearLayout) findViewById(R.id.myView);

				// single touch (prima versione)s
				//final SingleTouchEventView canvas = new SingleTouchEventView(this, this, null);
				//multituuch test		
				final MultiTouchPaintView canvas = new MultiTouchPaintView(this, this,null);//quello migliore

			
				// larghezza e altezza uguali al canvas sul PC
				canvas.setLayoutParams(new LayoutParams(800, 800));
				layout.addView(canvas);
				Button clearButton = new Button(this);
				
				clearButton.setText("Clear");
				clearButton.setLayoutParams(new LayoutParams(
			        ViewGroup.LayoutParams.MATCH_PARENT,
			            ViewGroup.LayoutParams.WRAP_CONTENT));
				layout.addView(clearButton);
				clearButton.setOnClickListener(new View.OnClickListener() {

			        @Override
			        public void onClick(View v) {
			        	canvas.clearCanvas();
			        	try {
			        		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
							oos.writeObject(new TPoint(-2,-2,-2));

						} catch (IOException e1) {
							
							e1.printStackTrace();
						}
			        }
			    }); 
				Button okButton = new Button(this);
				okButton.setText("Close");
				okButton.setLayoutParams(new LayoutParams(
			        ViewGroup.LayoutParams.MATCH_PARENT,
			            ViewGroup.LayoutParams.WRAP_CONTENT));
				layout.addView(okButton);
				okButton.setOnClickListener(new View.OnClickListener() {

			        @Override
			        public void onClick(View v) {
			        	try {
			        		oos = new ObjectOutputStream(socket.getOutputStream());
							oos.writeObject(new TPoint(-3,-3,-3));
							oos.flush();
							oos.close();
						} catch (IOException e1) {
							
							e1.printStackTrace();
						}
			                Intent i = new Intent(getApplicationContext(), Activity01.class);
			                startActivity(i);
			        }
			    }); 
				
				
			} else{
				
				
				/*enabler = new Intent(this, ClientSocketActivity.class);
				startActivity(enabler);*/
				}
		
		/*
		 * new Thread() { public void run() { connect(device); }; }.start();
		 */
	}

	public BluetoothSocket getSocket() {
		return socket;
	}
	
	public void onBackPressed(){
		
		try {
			if (oos!=null)
			
			oos.close();
			if (socket!=null)
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		Intent i = new Intent(getApplicationContext(), Activity01.class);
        startActivity(i);
	}
}
