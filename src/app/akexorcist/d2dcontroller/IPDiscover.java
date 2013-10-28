package app.akexorcist.d2dcontroller;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// IP address scanner class
public class IPDiscover {
	private Context mContext;
	private int count = 0;
	public boolean getIPState = false;
	public String DEVICE_IP_ADDRESS;
	private String ipTrim;
	private ArrayList<String> arr_ip;
	private ListView mListIP;
	private View mNormal, mLoad;
	
	public IPDiscover(Context context, ListView listIP, View normal, View load) {
		mContext = context;
		getDeviceIP();
		mListIP = listIP;
		mNormal = normal;
		mLoad = load;
	}
	
	// Get class state 
	// true = is scanning IP address (Busy)
	// false =  isn't scanning or finished (Idle)
	public boolean isDiscovered() {
		return getIPState;
	}
	
	// Get each other device's IP Address on wlan network
	public String get(int index) {
		return arr_ip.get(index);
	}
	
	// Get all other device's IP Address on wlan network
	public ArrayList<String> getIPList() {
		return arr_ip;
	}

    // Get device IP address
	private void getDeviceIP() {
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	int ipAddress = wifiInfo.getIpAddress();
    	ipTrim = (ipAddress & 0xFF) + "."
    			+ ((ipAddress >> 8 ) & 0xFF) + "."
    	    	+ ((ipAddress >> 16 ) & 0xFF);
    	DEVICE_IP_ADDRESS = (ipAddress & 0xFF) + "."
    			+ ((ipAddress >> 8 ) & 0xFF) + "."
    	    	+ ((ipAddress >> 16 ) & 0xFF) + "."
    	    	+ ((ipAddress >> 24 ) & 0xFF);
    	Log.i("IP Discoverage",  "Device IP : " + DEVICE_IP_ADDRESS);
	}
	
	// Find other device's IP Address on wlan network 
	public void getConnectedDevices() {
		// Busy state
		getIPState = false;
		
		Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
        	public void run() {
        		mNormal.setVisibility(View.INVISIBLE);
        		mLoad.setVisibility(View.VISIBLE);
        	}
        });
        
    	arr_ip = new ArrayList<String>();
		mListIP.setAdapter(new ArrayAdapter<String>(mContext
        		, android.R.layout.simple_list_item_1, arr_ip));	
		
		// Scan from xxx.xxx.xxx.0 to xxx.xxx.xxx.255
        for (int i = 0; i <= 255; i++) {
        	final int j = i;
            Runnable runnable = new Runnable() {
    			public void run() {
		            try {
		            	// Execute "ping" command
		            	Process proc = Runtime.getRuntime().exec("ping -c 2 " + ipTrim + "." + String.valueOf(j));
			            proc.waitFor();
			            int exit = proc.exitValue();
			            proc.destroy();
			            
			            // Get ping response and not itself IP address
			            if (exit == 0 && !(ipTrim + "." + String.valueOf(j)).equals(DEVICE_IP_ADDRESS)) {
			            	count++;
			                arr_ip.add(ipTrim + "." + String.valueOf(j));
			            } else {
			            	count++;
			            }
			            
			            // When scanning has finish, will show IP address list on list view
			            if(count >= 255) {
			        		Handler refresh = new Handler(Looper.getMainLooper());
			        		refresh.post(new Runnable() {
			        			public void run() {
			        				mListIP.setAdapter(new ArrayAdapter<String>(mContext
			        		        		, android.R.layout.simple_list_item_1, arr_ip));	
									mNormal.setVisibility(View.VISIBLE);
									mLoad.setVisibility(View.INVISIBLE);
			        			}
			        		});
			        		
			        		// Idle state
			        		getIPState = true;	
		            		count = 0;
		            	}
			        } catch (IOException e) {
					} catch (InterruptedException e) { }
    			}
    		};
    		new Thread(runnable).start();
        }
    }
}
