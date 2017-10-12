package com.example.psiclient;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.util.EvaRunnable;
import com.oblivm.backend.util.InitOTEvaRunnable;
import com.oblivm.backend.util.InitOTGenRunnable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.format.Formatter;

@SuppressLint("NewApi")
public class PSIGC implements PSI {
	
	public PSIGC(String ip) {
		host = ip;
		GCTable = new File(Environment.getExternalStorageDirectory(), "/test.txt");
		res = new File(Environment.getExternalStorageDirectory(), "/res.txt");
		DB = new File(Environment.getExternalStorageDirectory(), "/MalwareDB");
	}
	
	public long getDBSize() {
		return DB_size;
	}
	
	public void downloadDB(Socket socket) {
		try {
			DataInputStream d_in = new DataInputStream(socket.getInputStream());
			long size1 = Utils.receiveInteger(d_in);
			long size2 = Utils.receiveInteger(d_in);
			long size3 = Utils.receiveInteger(d_in);
			DB_size = Utils.receiveInteger(d_in);
						
			Utils.receiveFile(d_in, GCTable, size1);
				
			File circuit_file = new File(Environment.getExternalStorageDirectory(), "/AES-SHDL.txt");
			Utils.receiveFile(d_in, circuit_file, size2);
			
			Utils.receiveFile(d_in, res, size3);
			
			Utils.receiveFile(d_in, DB, DB_size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean sendQuery(String s, Socket socket) {	
		// sendIP(socket); //for emulator
		runInitOT();
		return runGC(s);
	}
	
	private File DB;
	private long DB_size = 0;
	private File GCTable;
	private File res;
	private int port = 1707;
	private String host;
	private int input_size = 128;
	
	
	//run the initial phase of OT extension
		//server acts as a receiver
		private void runInitOT() {
			long startTime = System.currentTimeMillis();
			for( int i = 0; i < Flag.repeat; ++i ){
				Class<?> clazz;
				try {
					clazz = Class.forName("com.oblivm.backend.example.AES" + "$InitOTEvaluator");
					InitOTEvaRunnable run;
					run = (InitOTEvaRunnable) clazz.newInstance();
					run.setParameter(Mode.getMode("OPT"), host, port);
					run.port += i;
					run.run();
					if(Flag.CountTime)
						Flag.sw.print();
					if(Flag.countIO)
						run.printStatistic();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("-----------------Time used for InitOT:" + (System.currentTimeMillis() - startTime));
		}
		
	private boolean runGC(String s) {
		try {
			long startTime = System.currentTimeMillis();
			String hash = Utils.bytesToBinaryString(Utils.sha1(s, input_size));
		//	System.out.println(hash);
		//	String hash = "11111101111001001111101110101110010010100000100111100000001000001110111111110111001000101001011010011111100000111000001100101011";
			for( int i = 0; i < Flag.repeat; ++i ){
				Class<?> clazz = Class.forName("com.oblivm.backend.example.AES" + "$Evaluator");
				EvaRunnable run = (EvaRunnable) clazz.newInstance();
				run.setParameter(Mode.getMode("OPT"), host, port, hash, GCTable, res);
				run.port += 2*i;
				run.run();
				if(Flag.CountTime)
					Flag.sw.print();
				if(Flag.countIO)
					run.printStatistic();
				
	/*			byte[][] ret = run.getInputsBob();
				for (int j = 0; j < 128; j ++) {
					System.out.println(Utils.bytesToBigInteger(ret[j], 0, ret[j].length).toString(16));
				}*/
				
				System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));
				
				boolean result = Utils.readFileAndTest(run.getResult(), DB, 16);  
				System.out.println("************************************************");
				System.out.println(result);
			}
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private void sendIP(Socket socket) {
		String ip = Utils.getIPAddress(true);
		Utils.sendString(socket, ip);
	}
}