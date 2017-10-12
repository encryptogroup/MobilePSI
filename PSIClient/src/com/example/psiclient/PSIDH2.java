package com.example.psiclient;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import android.annotation.SuppressLint;
import android.os.Environment;

@SuppressLint("NewApi")
public class PSIDH2 implements PSI {

	public PSIDH2() {
		DB = new File(Environment.getExternalStorageDirectory(), "/MalwareDB");
		DB_size = 0;
		generateKey();
	}
	
	public long getDBSize() {
		return DB_size;
	}
	
	public void downloadDB(Socket socket) {   		
		generateKey();
		
		byte[] ret = Utils.receiveBytes(socket);
		N = Utils.bytesToBigInteger(ret, 0, ret.length);
		phi_N = N.subtract(new BigInteger("1")); 
		key = key.mod(phi_N);
		
		try {
			startTime = System.currentTimeMillis();
			DataInputStream d_in = new DataInputStream(socket.getInputStream());
			
			DB_size = Utils.receiveInteger(d_in);
			File tmp = new File(Environment.getExternalStorageDirectory(), "/tmp");
			Utils.receiveFile(d_in, tmp, DB_size);
			System.out.println("-----------------Download done!-------------");
				
			FileInputStream f_in = new FileInputStream(tmp);	
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(DB));
			byte[] buffer = new byte[4096];
			int item_size = mod_size / 8; 
			byte[] sub = new byte[item_size];
			int read = 0;
			int total = 0;
			while ((read = f_in.read(buffer)) > 0) {
				System.out.println(read);
				total = total + read;
				int n = read / item_size;
				for (int i = 0; i < n; i ++) {
//					System.arraycopy(buffer, i*item_size, sub, 0, item_size);
					sub = Utils.bigIntegerToBytes(Utils.bytesToBigInteger(sub, 0, sub.length).modPow(key, N), false);
//					System.arraycopy(sub, 0, buffer, i*item_size, item_size);
			//		System.out.println("--------------" + Utils.bytesToBigInteger(sub, 0, sub.length).toString() + "-------------");
				}
				out.write(buffer);
			}
			System.out.println("---------DBresult---------Time used:" + (System.currentTimeMillis() - startTime));
			f_in.close();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean sendQuery(String s, Socket socket) {
		startTime = System.currentTimeMillis();
		
		BigInteger I = Utils.stringToBigInteger(s);
		byte[] query = Utils.bigIntegerToBytes(I.modPow(key, N), false);
		
		Utils.sendBytes(socket, query);
		
		return receiveAndParseResult(socket);
	}
	
	public boolean receiveAndParseResult(Socket socket) {
		byte[] ret = Utils.receiveBytes(socket);			
    	
    	System.out.println("------------------Time used:" + (System.currentTimeMillis() - startTime));
    	return Utils.readFileAndTest(ret, DB, mod_size/8);   
	}
	
	private File DB;
	private long DB_size = 0;
	private long startTime;
	private BigInteger key;
	private BigInteger N;
	private BigInteger phi_N;
	private int mod_size = 2048;
	
	private void generateKey() {		
		key = new BigInteger(256, new SecureRandom());
	//	key = key.mod(phi_N);
	}
}
