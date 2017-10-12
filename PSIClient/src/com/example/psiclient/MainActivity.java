package com.example.psiclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private final String IP = "10.100.31.171";
	private final int PORT = 8000;
	private Socket socket = null;
    private InetAddress inetAddress;
    
	private PSI psi;
	
	private Handler handler;
	private final int TOAST_DB_loaded = 1;
	private final int TOAST_APP_empty = 2;
	private final int TOAST_DB_empty = 3;
	private final int TOAST_Malware = 4;
	private final int TOAST_NOT_Malware = 5;
	
	private void callMainThread (int arg) {
		Message msg = handler.obtainMessage();
        msg.arg1 = arg;
        handler.sendMessage(msg);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addButtonClickListener();
		
		psi = new PSIRSA();
//		psi = new PSIDH();
//		psi = new PSIGC(IP);
//		psi = new PSINR(IP);
//		psi = new PSIDH2();
		
		handler = new Handler(Looper.getMainLooper()) {
		    @Override
		    public void handleMessage(Message msg) {
		    	if (msg.arg1 == TOAST_DB_loaded) {
		    		Toast.makeText(MainActivity.this, "DB has been downloaded!", Toast.LENGTH_LONG).show();
		    	} else if (msg.arg1 == TOAST_APP_empty) {
		    		Toast.makeText(MainActivity.this, "Please input an App!", Toast.LENGTH_LONG).show();
		    	} else if (msg.arg1 == TOAST_DB_empty) {
		    		Toast.makeText(MainActivity.this, "Please load DB first!", Toast.LENGTH_LONG).show();
		    	} else if (msg.arg1 == TOAST_Malware) {
		    		Toast.makeText(MainActivity.this, "This is a malware!!!", Toast.LENGTH_LONG).show();
		    	} else if (msg.arg1 == TOAST_NOT_Malware) {
		    		Toast.makeText(MainActivity.this, "This is a secure APP!", Toast.LENGTH_LONG).show();
		    	} 
		    }
		};
	}
	
	@SuppressLint("NewApi")
	public void addButtonClickListener() {
		Button btnSubmit = (Button)findViewById(R.id.button1);
		btnSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new Thread(new Runnable(){  
	                @SuppressLint("NewApi")
					@Override  
	                public void run() {  
	    				EditText txt = (EditText)findViewById(R.id.editText1);
	    				String app = txt.getText().toString();
	    				if (psi.getDBSize() == 0) {
	    					callMainThread(TOAST_DB_empty);	    					
	    				} else if (app.isEmpty()) {
	    					callMainThread(TOAST_APP_empty);
		    			}  else {
	    					callServer(app, "QUERY");
	    				}
	                }  
	            }).start();  
			}
		});
		
		Button btnUpdate = (Button)findViewById(R.id.button2);
		btnUpdate.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new Thread(new Runnable(){  
	                @Override  
	                public void run() {  				
	                	callServer("", "DB");
	                }  
	            }).start();  
			}
		});
	}
	
	public void callServer(String app, String type) {	    
		try {
			inetAddress = InetAddress.getByName(IP);  						
			socket = new Socket(inetAddress, PORT);            			
            
			
			Utils.sendString(socket, type);               
            if (type.equals("DB")) {  
        //     	Utils.sendString(socket, type);  
            	callMainThread(TOAST_DB_loaded);
            	psi.downloadDB(socket);           	            	  	            
	            callMainThread(TOAST_DB_loaded);
            } else if (type.equals("QUERY")) {  
            	if (psi.sendQuery(app, socket)) {
            		callMainThread(TOAST_Malware);
            	} else {
            		callMainThread(TOAST_NOT_Malware);
            	}  
            }   
            Utils.sendString(socket, "END");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if( socket != null){
				try {
					socket.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		}
	}
}
