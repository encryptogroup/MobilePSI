import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.gc.GCSignal;
import com.oblivm.backend.util.GenRunnable;
import com.oblivm.backend.util.InitOTGenRunnable;

public class PSINR implements PSI {
	public PSINR(int size) {		
		long startTime = System.currentTimeMillis();
		N = generatePrime(mod_size);
		phi_N = N.subtract(new BigInteger("1")); 
		generateKeys(key_size); //generateKeys(128);
		DB = new File("./DB_NR");
		/*base phase*/
		BigInteger a = new BigInteger(2048, new SecureRandom());
		BigInteger b = new BigInteger(128, new SecureRandom());
		
		  for (int i = 0; i < 1024; i ++)
		  {
			System.out.println(i);
		    for (int j = 0; j < 32; j ++) {
		    	a = a.multiply(b).mod(phi_N);
		    }
		    a = g.modPow(a, N);
		  }		  
		System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));
		
		
	//	generateDB(size);
		
		
	}
	
	public void sendDB(Socket socket) {
		try {
			Utils.sendBytes(socket, Utils.bigIntegerToBytes(N, false));
			DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());
			Utils.sendInteger(d_out, DB.length());
			Utils.sendFile(d_out, DB);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void answerQuery(Socket socket) {
//		runInitOT();
		
		byte[][] keys0 = new byte[input_size][];
		byte[][] keys1 = new byte[input_size][];
		BigInteger[] r_s = new BigInteger[input_size];
		int bytes_length = key_size / 8 * 2;
		for (int i = 0; i < input_size; i ++) {
			r_s[i] = generatePrime(key_size); //new BigInteger(key_size, new SecureRandom());
			
			byte[] tmp = Utils.bigIntegerToBytes(keys[0][i].multiply(r_s[i]), false);
			if (tmp.length < bytes_length) {
				tmp = Utils.padBytes(tmp, bytes_length);
			}
			keys0[i] = tmp;
			
			tmp = Utils.bigIntegerToBytes(keys[1][i].multiply(r_s[i]), false);
			if (tmp.length < bytes_length) {
				tmp = Utils.padBytes(tmp, bytes_length);
			}
			keys1[i] = tmp;
		}
		
		BigInteger r = new BigInteger("1");
		for (int i = 0; i < input_size; i ++) {
			r = r.multiply(r_s[i]).mod(phi_N);
		}		
		BigInteger r_inverse = r.modInverse(phi_N);
		BigInteger G = g.modPow(r_inverse, N);
		
		Utils.sendBytes(socket, Utils.bigIntegerToBytes(G, false));
		
		Utils.send2DBytes(socket, keys0);
//		runOT(keys0, keys1);
	}
	
	private int input_size = 32;
	private int key_size = 40;
	private int mod_size = 2048;
	private BigInteger g = new BigInteger("5");
	private BigInteger N; //modular
	private BigInteger phi_N; 
	private BigInteger[][] keys = new BigInteger[2][128];
	private File DB;
	private int port = 1707;
	
	private void generateKeys(int keySize) {
		for (int i = 0; i < input_size; i ++) {
			keys[0][i] = new BigInteger(keySize, new SecureRandom());
			
		//	System.out.println(keys[0][i].toString(16));
			keys[1][i] = new BigInteger(keySize, new SecureRandom());
		}
	
		/* test*/
/*		BigInteger a = new BigInteger(keySize, new SecureRandom());
		System.out.println(Utils.bytesToBinaryString(Utils.bigIntegerToBytes(a, false)));
		System.out.println(Utils.bytesToBinaryString(a.toByteArray()));*/
	}
	
	private void generateDB(int size) {
		System.out.println("building DB...");		
		for (int i = 0; i < size; i ++) {
			try {
				String hash = Utils.bytesToBinaryString(Utils.sha1(i + "", input_size));
				BigInteger product = new BigInteger("1");
				for (int j = 0; j < input_size; j ++) {
					if(hash.charAt(j) == '0') {
						//TODO
				//		BigInteger k0 = Utils.bytesToBigInteger(Utils.bigIntegerToBytes(keys[0][j], false), 0, 10);
						product = product.multiply(keys[0][j]).mod(phi_N);
				//		if(i == 0) {
				//			System.out.println(k0.toString(16));
				//		}
					} else if(hash.charAt(j) == '1') {
						//TODO
				//		BigInteger k1 = Utils.bytesToBigInteger(Utils.bigIntegerToBytes(keys[1][j], false), 0, 10);
						product = product.multiply(keys[1][j]).mod(phi_N);
				//		if(i == 0) {
				//			System.out.println(k1.toString(16));
				//		}
					}
				}
				BigInteger result = g.modPow(product, N);
			//	if(i == 0) {	
			//		System.out.println(result.toString(16));
			//	}
				byte[] bytes = new byte[256];
				byte[] tmp = Utils.bigIntegerToBytes(result, false);
				System.arraycopy(tmp, 0, bytes, 0, tmp.length);				
				Utils.writeLineToFile(DB, bytes, i, size);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	private void runInitOT() {
		for( int i = 0; i < Flag.repeat; ++i ){
			try {
				Class<?> clazz = Class.forName("com.oblivm.backend.example.AES" + "$InitOTGenerator");
				InitOTGenRunnable run;
				run = (InitOTGenRunnable) clazz.newInstance();
				run.setParameter(port);
				run.port += i;
				run.run();
				if(Flag.CountTime)
					Flag.sw.print();
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
	
	private BigInteger generatePrime(int keySize)
	{
		BigInteger c = new BigInteger(keySize, new SecureRandom());

	    for (; ; )
	    {
	        if (c.isProbablePrime(1) == true) break;
	        c = c.subtract(new BigInteger("1"));
	    }
	    return (c);
	}
	
	private void runOT(byte[][] keys0, byte[][] keys1) {		
		for( int i = 0; i < Flag.repeat; ++i ){
			try {
				Class<?> clazz2 = Class.forName("com.oblivm.backend.example.AES" + "$Generator");
				GenRunnable run2 = (GenRunnable) clazz2.newInstance();
				run2.setParameter(port);
				run2.port += 2*i;
				//make sure the inputs to OT is exactly 80 bits
				run2.runOTOnly(keys0, keys1, input_size);
				if(Flag.CountTime)
					Flag.sw.print();
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
}
