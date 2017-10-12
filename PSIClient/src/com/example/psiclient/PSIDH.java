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
import java.security.Security;
import java.util.Arrays;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECPoint;

import android.annotation.SuppressLint;
import android.os.Environment;

@SuppressLint("NewApi")
public class PSIDH implements PSI {

	public PSIDH() {
		DB = new File(Environment.getExternalStorageDirectory(), "/MalwareDB");
		DB_size = 0;
		generateKey();
	}
	
	public long getDBSize() {
		return DB_size;
	}
	
	public void downloadDB(Socket socket) {        
		try {
			startTime = System.currentTimeMillis();
			DataInputStream d_in = new DataInputStream(socket.getInputStream());
			
			DB_size = Utils.receiveInteger(d_in);
			File tmp = new File(Environment.getExternalStorageDirectory(), "/tmp");
			Utils.receiveFile(d_in, tmp, DB_size);
			System.out.println("-----------------Download done!-------------");
				
			FileInputStream f_in = new FileInputStream(tmp);	
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(DB));
			byte[] buffer = new byte[4160];
			byte[] sub = new byte[65];
			int read = 0;
			int total = 0;
			while ((read = f_in.read(buffer)) > 0) {
				total = total + read;
				int n = read / 65;
				for (int i = 0; i < n; i ++) {
					System.arraycopy(buffer, i*65, sub, 0, 65);
					sub = bytesToECPoint(sub).multiply(key).getEncoded(false);
					System.arraycopy(sub, 0, buffer, i*65, 65);
					System.out.println("--------------" + Utils.bytesToBigInteger(sub, 0, sub.length).toString() + "-------------");
				}
				out.write(buffer);
			}
			System.out.println("DBresult------------------Time used:" + (System.currentTimeMillis() - startTime));
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
		byte[] query = G.multiply(I).multiply(key).getEncoded(false);
		
		Utils.sendBytes(socket, query);
		
		return receiveAndParseResult(socket);
	}
	
	public boolean receiveAndParseResult(Socket socket) {
		byte[] ret = Utils.receiveBytes(socket);			
    	
    	System.out.println("result------------------Time used:" + (System.currentTimeMillis() - startTime));
    	return Utils.readFileAndTest(ret, DB, 65);   
	}
	
	private File DB;
	private long DB_size = 0;
	private long startTime;
	private BigInteger key;
	private ECPoint G;
	
	private void generateKey() {		
		ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");	
		BigInteger n = ecSpec.getN();
		G = ecSpec.getG();
		key = org.spongycastle.util.BigIntegers.createRandomInRange(BigInteger.ONE, 
    			n.subtract(BigInteger.ONE), new SecureRandom());
	}
	
	private ECPoint bytesToECPoint(byte[] bytes) {
		ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
		ECPoint point = ecSpec.getCurve().decodePoint(bytes);	
		
		return point;
	}
}
