package com.example.psiclient;
import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;

@SuppressLint("NewApi")
public class PSIRSA implements PSI {	
	
	public PSIRSA() {
		DB = new File(Environment.getExternalStorageDirectory(), "/MalwareDB");
		DB_size = 0;

	}
	
	public long getDBSize() {
		return DB_size;
	}
	
	public void downloadDB(Socket socket) {
/*		System.out.println("---------bloom filter1--------");
		startTime = System.currentTimeMillis();
		int n = 32768;
		int m = 6281664;
		int k = 133;		
		int bf[] = new int[m/4+1]; 
		for (int i = 0; i < m/4 + 1; i ++) 
		{
			bf[i] = 0;
		}
		for (int i = 0; i < n; i ++)
		{
			for (int j = 0; j < k; j++)
			{
		  		int r = (int)(Math.random() * m);
		  		int a = (int)(r / 4);
		  		int b = (int)(r % 4);	
		  		int temp = bf[a] % (int)(Math.pow(2,b+1));
		  		if (temp < Math.pow(2,b))
		  			bf[a] = bf[a] + (int)Math.pow(2,b);
			}
		}
		System.out.println("---------bloom filter--------Time used:" + (System.currentTimeMillis() - startTime));

*/		
/*		try {
			byte[][] pk = Utils.receive2DBytes(socket);					
			e = Utils.bytesToBigInteger(pk[0], 0, pk[0].length);
			N = Utils.bytesToBigInteger(pk[1], 0, pk[1].length);	
			// Log.v("", e.toString());
			// Log.v("", N.toString());		
			DataInputStream d_in = new DataInputStream(socket.getInputStream());
			DB_size = Utils.receiveInteger(d_in);
			Utils.receiveFile(d_in, DB, DB_size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		startTime = System.currentTimeMillis();
		String s = Utils.receiveString(socket);
		System.out.println("PSIupdate:" + (System.currentTimeMillis() - startTime));
		System.out.println(s);
	/*	
		startTime = System.currentTimeMillis();
		for (int i=0; i<1024; i++) 
		{
			generateBlindingFactor();
			blindFactor.modInverse(N);
			blindFactor.modPow(e, N);
		}
		System.out.println("----Precomputation Time used:" + (System.currentTimeMillis() - startTime));*/
	}
	
	public boolean sendQuery(String s, Socket socket) {	
		startTime = System.currentTimeMillis();
		
		BigInteger h = Utils.stringToBigInteger(s);		
		generateBlindingFactor();
		BigInteger x = h.multiply(blindFactor.modPow(e, N)).mod(N);
		//	BigInteger x = h.multiply(N).mod(N);
		byte[] query = Utils.bigIntegerToBytes(x, false);
		
		Utils.sendBytes(socket, query);
		
		return receiveAndParseResult(socket);
	}
	
	public boolean receiveAndParseResult(Socket socket) {
		byte[] ret = Utils.receiveBytes(socket);			
		BigInteger y = Utils.bytesToBigInteger(ret, 0, ret.length);
    	BigInteger z = y.multiply(blindFactor.modInverse(N)).mod(N);  
    	// BigInteger z = y.multiply(N).mod(N);  
    	//System.out.println(z.toString());
    	byte[] result = Utils.bigIntegerToBytes(z, false);
    	
    	System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));
    	return Utils.readFileAndTest(result, DB, 128);    		
	}
	
	private BigInteger e;
	private BigInteger N;
	private BigInteger blindFactor;
	private File DB;
	private long DB_size = 0;
	private long startTime;
		
	private void generateBlindingFactor() {
		BigInteger ZERO = BigInteger.valueOf(0);
		BigInteger ONE = BigInteger.valueOf(1);
        int length = N.bitLength() - 1; 
        BigInteger gcd;
        do
        {
        	blindFactor = new BigInteger(length, new SecureRandom());
            gcd = blindFactor.gcd(N);
        }
        while (blindFactor.equals(ZERO) || blindFactor.equals(ONE) || !gcd.equals(ONE));
	}			
}
