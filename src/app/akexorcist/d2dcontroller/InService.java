package app.akexorcist.d2dcontroller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

// AsyncTask class for get command from controller device
public class InService extends AsyncTask<Void, Void, Void> {
	private ServerSocket ss;
	int TCP_SERVER_PORT;
	Boolean TASK_STATE = true;
	Boolean LED1_STATE = false, LED2_STATE = false
			, LED3_STATE = false, LED4_STATE = false;

    public InService(int port) {
    	TCP_SERVER_PORT = port;
    }
    
    // Close task
    public void killTask() {
    	TASK_STATE = false;
    }
    
    // Get LED state 
    public boolean getState(int index) {
    	if(index == 1) {
    		return LED1_STATE;
    	} else if(index == 2) {
    		return LED2_STATE;
    	} else if(index == 3) {
    		return LED3_STATE;
    	} else if(index == 4) {
    		return LED4_STATE;
    	}
		return false;
    }
    
    // Background thread to get command from controller device
    // This thread will work until main activity (IOIO.java) has finish or close
	protected Void doInBackground(Void... params) {  
		try {
			ss = new ServerSocket(TCP_SERVER_PORT);
			ss.setSoTimeout(300);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(TASK_STATE) {
			try {
				Socket s = ss.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				final String incomingMsg = in.readLine();
				Log.i("Message Incoming", incomingMsg);

				if(incomingMsg.equals("LED1-ON")) {
					LED1_STATE = true;
				} else if(incomingMsg.equals("LED1-OFF")) {
					LED1_STATE = false;
				} else if(incomingMsg.equals("LED2-ON")) {
					LED2_STATE = true;
				} else if(incomingMsg.equals("LED2-OFF")) {
					LED2_STATE = false;
				} else if(incomingMsg.equals("LED3-ON")) {
					LED3_STATE = true;
				} else if(incomingMsg.equals("LED3-OFF")) {
					LED3_STATE = false;
				} else if(incomingMsg.equals("LED4-ON")) {
					LED4_STATE = true;
				} else if(incomingMsg.equals("LED4-OFF")) {
					LED4_STATE = false;
				} else if(incomingMsg.equals("UPDATE")) {
					// Send all LED state to controller device
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));	
					String outgoingMsg = String.valueOf(LED1_STATE) + "," 
										+ String.valueOf(LED2_STATE) + ","
										+ String.valueOf(LED3_STATE) + ","
										+ String.valueOf(LED4_STATE);
					out.write(outgoingMsg);
					out.flush();
					out.close();
				}
				s.close();
			} catch (IOException e) { }
		}
		try {
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
