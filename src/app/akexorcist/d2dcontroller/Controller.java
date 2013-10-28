package app.akexorcist.d2dcontroller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Controller extends Activity {
    public static final int TCP_SERVER_PORT = 21111;
    
    CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
	Button buttonFind, buttonMode;
	EditText editTextIP;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.controller);
		overridePendingTransition(0, 0);
        
		// Text input filter for editTextIP
        InputFilter filter = new InputFilter() { 
			public CharSequence filter(CharSequence arg0, int arg1, int arg2,
					Spanned arg3, int arg4, int arg5) {
				Log.i("Check", String.valueOf(arg0));
				for (int i = arg1; i < arg2; i++) { 
					if (!Character.isDigit(arg0.charAt(i)) 
							&& !String.valueOf(arg0.charAt(i)).equals(".")) { 
	                    return ""; 
	                }
				} 
				return arg0;
			} 
	    }; 
	    
	    // Edit text for input target device IP address
        editTextIP = (EditText)findViewById(R.id.editTextIP);
	    editTextIP.setFilters(new InputFilter[]{filter}); 
	    SharedPreferences settings = getSharedPreferences("Pref", 0);
        editTextIP.setText(settings.getString("IP", ""));
		editTextIP.setSelection(editTextIP.getText().toString().length());
        
		// Button for switch to IOIO mode 
		// For device which connected with IOIO (Target device)
	    buttonMode = (Button)findViewById(R.id.buttonMode);
	    buttonMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent go = new Intent(getApplicationContext(), IOIO.class);
				startActivity(go);
				Controller.this.finish();
			}
	    });
	    
	    Button buttonRefresh = (Button)findViewById(R.id.buttonRefresh);
	    buttonRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendMessage(editTextIP.getText().toString(), "UPDATE");
			}
        });
	    
	    // Button for scan other device's IP Address on wlan network
	    // (Or you can input IP Address directly into Edit Text)
        buttonFind = (Button)findViewById(R.id.buttonFind);
        buttonFind.setOnClickListener(new OnClickListener() {
        	Button buttonScan;
        	IPDiscover ipd;
        	public void onClick(View v) {
        		// Create IP address viewer dialog
        		final Dialog dialog = new Dialog(Controller.this);
        	    dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
        	    dialog.setContentView(R.layout.dialog_ip);
        	    dialog.setCancelable(true);
        	    
        	    // Button for close dialog
        	    Button buttonClose = (Button) dialog.findViewById(R.id.buttonClose);
        	    buttonClose.setOnClickListener(new OnClickListener() {
        	        public void onClick(View v) {
        	        	dialog.cancel();
        	        }
        	    });

        	    // List view to show list of other device's IP Address on wlan network
        	    ListView listViewIP = (ListView)dialog.findViewById(R.id.listViewIP);
        	    listViewIP.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						editTextIP.setText(ipd.get(arg2));
						editTextIP.setSelection(editTextIP.getText().toString().length());
						dialog.cancel();
					}
        	    });
        	    
        	    // Progress bar for busy status while scanning
        	    ProgressBar progressBarLoad = (ProgressBar)dialog.findViewById(R.id.progressBarLoad);
                progressBarLoad.setVisibility(View.INVISIBLE);

                // Button for scan other device's IP Address on wlan network
                buttonScan = (Button)dialog.findViewById(R.id.buttonScan);
                buttonScan.setOnClickListener(new OnClickListener() {
                	public void onClick(View v) {
                		buttonScan.setVisibility(View.INVISIBLE);
            	        ipd.getConnectedDevices();	
                	}
                });

                // Create object from IPDiscover class
                ipd = new IPDiscover(getApplicationContext()
                		, listViewIP, buttonScan, progressBarLoad);
                
                // Show IP address viewer dialog
        	    dialog.show();
        	}
        });

        // Check box to control 1st LED
        checkBox1 = (CheckBox)findViewById(R.id.checkBox1);
        checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1) {
					// Send commmand to target device
					sendMessage(editTextIP.getText().toString(), "LED1-ON");
				} else {
					sendMessage(editTextIP.getText().toString(), "LED1-OFF");
				}
			}
        });

        // Check box to control 2nd LED
        checkBox2 = (CheckBox)findViewById(R.id.checkBox2);
        checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1) {
					sendMessage(editTextIP.getText().toString(), "LED2-ON");
				} else {
					sendMessage(editTextIP.getText().toString(), "LED2-OFF");
				}
			}
        });

        // Check box to control 3rd LED
        checkBox3 = (CheckBox)findViewById(R.id.checkBox3);
        checkBox3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1) {
					sendMessage(editTextIP.getText().toString(), "LED3-ON");
				} else {
					sendMessage(editTextIP.getText().toString(), "LED3-OFF");
				}
			}
        });
        
        // Check box to control 4th LED
        checkBox4 = (CheckBox)findViewById(R.id.checkBox4);
        checkBox4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1) {
					sendMessage(editTextIP.getText().toString(), "LED4-ON");
				} else {
					sendMessage(editTextIP.getText().toString(), "LED4-OFF");
				}
			}
        });
    }
    
    public void sendMessage(String ip, String message) {
    	// Target device IP address
    	final String IP_ADDRESS = ip;
    	
    	// String command to be sent
    	final String MESSAGE = message;

    	// Create runnable for send command to target device
    	Runnable runSend = new Runnable() {
			public void run() {
				try {
					// Sent command to target device
					Socket s = new Socket(IP_ADDRESS, TCP_SERVER_PORT);
					s.setSoTimeout(5000);
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
					String outgoingMsg = MESSAGE + System.getProperty("line.separator"); 
					out.write(outgoingMsg);
					out.flush();
					
					// Get LED state from target device
					if(MESSAGE.equals("UPDATE")) {
						BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
						final String inMsg = in.readLine();
						Handler refresh = new Handler(Looper.getMainLooper());
						refresh.post(new Runnable() {
							public void run() {
								String[] strState = inMsg.split(",");
								checkBox1.setChecked(Boolean.valueOf(strState[0]));
								checkBox2.setChecked(Boolean.valueOf(strState[1]));
								checkBox3.setChecked(Boolean.valueOf(strState[2]));
								checkBox4.setChecked(Boolean.valueOf(strState[3]));
								Toast.makeText(getApplicationContext()
										, "Update!", Toast.LENGTH_SHORT).show();
							}
						});
						in.close();
					}
					s.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
					Handler refresh = new Handler(Looper.getMainLooper());
					refresh.post(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext()
									, "No device on this IP address."
									, Toast.LENGTH_SHORT).show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					Handler refresh = new Handler(Looper.getMainLooper());
					refresh.post(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext()
									, "Connection failed. Please try again."
									, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		};
		new Thread(runSend).start();
    }
    
    public void onPause() {
    	super.onPause();
    	
    	// Save IP Address to use the next time
    	SharedPreferences settings = getSharedPreferences("Pref", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("IP", editTextIP.getText().toString());
        editor.commit();
    }
}
