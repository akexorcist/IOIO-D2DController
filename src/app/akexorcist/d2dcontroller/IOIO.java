package app.akexorcist.d2dcontroller;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class IOIO extends IOIOActivity {
    public static final int TCP_SERVER_PORT = 21111;
	Button buttonMode;
	InService inTask = null;
	CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ioio);
		overridePendingTransition(0, 0);

		// Create object from InService class
		// For get command from controller device
        inTask = new InService(TCP_SERVER_PORT);
        inTask.execute();
        
        // Text view to show device IP address
        TextView textViewIP = (TextView)findViewById(R.id.textViewIP);
        textViewIP.setText(getIP());
        
        // Check box for show LED state 
        // Disable it because on IOIO mode isn't use for control
        checkBox1 = (CheckBox)findViewById(R.id.checkBox1);
        checkBox1.setEnabled(false);
        checkBox2 = (CheckBox)findViewById(R.id.checkBox2);
        checkBox2.setEnabled(false);
        checkBox3 = (CheckBox)findViewById(R.id.checkBox3);
        checkBox3.setEnabled(false);
        checkBox4 = (CheckBox)findViewById(R.id.checkBox4);
        checkBox4.setEnabled(false);
        

		// Button for switch to controller mode 
		// For device to control device which connected with IOIO
	    buttonMode = (Button)findViewById(R.id.buttonMode);
	    buttonMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent go = new Intent(getApplicationContext(), Controller.class);
				startActivity(go);
				IOIO.this.finish();
			}
	    });
	    
    }
    
    // Get device IP address
    public String getIP() {
    	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	int ipAddress = wifiInfo.getIpAddress();
    	String ip = (ipAddress & 0xFF) + "." +
    			((ipAddress >> 8 ) & 0xFF) + "." +
    			((ipAddress >> 16 ) & 0xFF) + "." +
                ((ipAddress >> 24 ) & 0xFF );
    	if(ip.equals("0.0.0.0"))
    		ip = "Please connect WIFI";
    	return ip;
	}
    
    // IOIO class
    class Looper extends BaseIOIOLooper {
    	DigitalOutput dio1, dio2, dio3, dio4;
    	
    	protected void setup() throws ConnectionLostException, InterruptedException {
    		// Assign digital output object with each port on IOIO board 
    		// and initial to LOW
    		dio1 = ioio_.openDigitalOutput(1, false);
    		dio2 = ioio_.openDigitalOutput(2, false);
    		dio3 = ioio_.openDigitalOutput(3, false);
    		dio4 = ioio_.openDigitalOutput(4, false);
            
    		runOnUiThread(new Runnable() {
    			public void run() {
    				Toast.makeText(getApplicationContext()
    						, "Connected!", Toast.LENGTH_SHORT).show();
    			}
    		});
    	}
    	
    	public void loop() throws ConnectionLostException, InterruptedException {
    		// Set LED state
    		dio1.write(inTask.getState(1));
    		dio2.write(inTask.getState(2));
    		dio3.write(inTask.getState(3));
    		dio4.write(inTask.getState(4));
    		runOnUiThread(new Runnable() {
    			public void run() {
    				// Set check box state
    				checkBox1.setChecked(inTask.getState(1));
    				checkBox2.setChecked(inTask.getState(2));
    				checkBox3.setChecked(inTask.getState(3));
    				checkBox4.setChecked(inTask.getState(4));
    			}
    		});
    		Thread.sleep(100);
    	}
    }
    
    public IOIOLooper createIOIOLooper() {
        return new Looper();
    } 
    
    public void onPause() {
    	super.onPause();
    	// Close inTask if exist
    	if(inTask != null)
    		inTask.killTask();
    }
}
