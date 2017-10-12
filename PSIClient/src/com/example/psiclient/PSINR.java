package com.example.psiclient;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.util.EvaRunnable;
import com.oblivm.backend.util.InitOTEvaRunnable;

import android.os.Environment;

public class PSINR implements PSI {
	public PSINR(String ip) {
		DB = new File(Environment.getExternalStorageDirectory(), "/MalwareDB");
		DB_size = 0;
		host = ip;
	}
	
	public long getDBSize() {
		return DB_size;
	}
	
	public void downloadDB(Socket socket) {
		try {
			byte[] ret = Utils.receiveBytes(socket);			
			N = Utils.bytesToBigInteger(ret, 0, ret.length);
			phi_N = N.subtract(new BigInteger("1"));
			DataInputStream d_in = new DataInputStream(socket.getInputStream());
			DB_size = Utils.receiveInteger(d_in);
			Utils.receiveFile(d_in, DB, DB_size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean sendQuery(String s, Socket socket) {	
		long startTime = System.currentTimeMillis();
//		runInitOT();
		
		
		byte[] ret = Utils.receiveBytes(socket);
		BigInteger G = Utils.bytesToBigInteger(ret, 0, ret.length);
				
//		byte[][] keys = runOT(s);
		
		byte[][] keys = Utils.receive2DBytes(socket);
		BigInteger product = new BigInteger("1");
		for (int i = 0; i < input_size; i ++) {
			product = product.multiply(Utils.bytesToBigInteger(keys[i], 0, keys[i].length)).mod(phi_N);
		//	System.out.println(Utils.bytesToBigInteger(keys[i], 0, keys[i].length).toString(16));
		}
		BigInteger enc = G.modPow(product, N);
		System.out.println(enc.toString(16));
		
		byte[] bytes = new byte[256];
		byte[] tmp = Utils.bigIntegerToBytes(enc, false);
		System.arraycopy(tmp, 0, bytes, 0, tmp.length);	
		

		boolean result = Utils.readFileAndTest(bytes, DB, 128);  
		System.out.println("----------------Time used:" + (System.currentTimeMillis() - startTime));
		System.out.println("************************************************");
		System.out.println(result);
		
		return result;
	}
	
	private File DB;
	private long DB_size = 0;
	private BigInteger N;
	private BigInteger phi_N;
	private BigInteger g = new BigInteger("5");
	private int port = 1707;
	private String host;
	private int input_size = 32;
	
	private void runInitOT() {
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
	}
	
	private byte[][] runOT(String s) {
		try {
			String hash = Utils.bytesToBinaryString(Utils.sha1(s, input_size));
		//	System.out.println(hash);
		//	String hash = "11111101111001001111101110101110010010100000100111100000001000001110111111110111001000101001011010011111100000111000001100101011";
	//		hash =        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
			for( int i = 0; i < Flag.repeat; ++i ){
				Class<?> clazz = Class.forName("com.oblivm.backend.example.AES" + "$Evaluator");
				EvaRunnable run = (EvaRunnable) clazz.newInstance();
				run.setParameter(Mode.getMode("OPT"), host, port, hash, null, null);
				run.port += 2*i;
				run.runOTOnly(input_size);
			
				if(Flag.CountTime)
					Flag.sw.print();
				if(Flag.countIO)
					run.printStatistic();
				return run.getInputsBob();
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
		return null;
	}
}
