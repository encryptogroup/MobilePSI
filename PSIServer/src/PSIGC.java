import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.util.GenPreRunnable;
import com.oblivm.backend.util.GenRunnable;
import com.oblivm.backend.util.InitOTEvaRunnable;
import com.oblivm.backend.util.InitOTGenRunnable;

public class PSIGC implements PSI {
	public PSIGC(int size) {
		
		key = new byte[16];
		Arrays.fill( key, (byte) 0 );
		iv = new byte[16];
		Arrays.fill( iv, (byte) 0 );
		
		DB = new File("./DB_GC");
		generateDB(size);	
		
		long startTime = System.currentTimeMillis();
		generateGCTableAndClientKeyPair();
		
		System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));
	}
	
	public void sendDB(Socket socket) {
		try {
			DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());
			
			File GCTable = new File("test.txt"); //!!!!! There maybe problems here
			File res = new File("res.txt");
			File circuit_file = new File("AES-SHDL.txt");
			
			Utils.sendInteger(d_out, GCTable.length());
			Utils.sendInteger(d_out, circuit_file.length());
			Utils.sendInteger(d_out, res.length());
			Utils.sendInteger(d_out, DB.length());
					
			Utils.sendFile(d_out, GCTable);																				
			Utils.sendFile(d_out, circuit_file);
			Utils.sendFile(d_out, res);	
			Utils.sendFile(d_out, DB);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void answerQuery(Socket socket) {
		// String client_ip = receiveIP(socket); //for emulator
		//String client_ip = socket.getRemoteSocketAddress().toString().substring(1).split(":")[0]; //for real device
		// System.out.println(client_ip);

		runInitOT();
		runGC();
	}
	
	private File DB;
	private int port = 1707;
	private byte[] key;
	private byte[] iv;
	private int input_size = 128;
	
	private void generateDB(int size) {				
		System.out.println("building DB...");		
		for (int i = 0; i < size; i ++) {
			try {
				Utils.writeLineToFile(DB, encrypt(Utils.sha1(i + "", input_size)), i, size);	
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}								
		}	
/*		try {
			System.out.println(Utils.bytesToBinaryString(encrypt(Utils.sha1("abc"))));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@SuppressWarnings("rawtypes")
	private void generateGCTableAndClientKeyPair() {
		try {
			Class<?> clazz = Class.forName("com.oblivm.backend.example.AES" + "$PreGenerator");
			GenPreRunnable run = (GenPreRunnable) clazz.newInstance();
			run.run();
			if(Flag.CountTime)
				Flag.sw.print();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] encrypt(byte[] data) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
	    
	    Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

	    byte[] encrypted = cipher.doFinal(data);
	    
        return Arrays.copyOfRange(encrypted, 0, 16);
	}
			
	private void runInitOT() {
		for( int i = 0; i < Flag.repeat; ++i ){
			Class<?> clazz;
			try {
				clazz = Class.forName("com.oblivm.backend.example.AES" + "$InitOTGenerator");
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
	
	private void runGC() {
		for( int i = 0; i < Flag.repeat; ++i ){
			try {
				Class<?> clazz2 = Class.forName("com.oblivm.backend.example.AES" + "$Generator");
				GenRunnable run2 = (GenRunnable) clazz2.newInstance();
				run2.setParameter(port);
				run2.port += 2*i;
				run2.run();
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
	
	private String receiveIP(Socket socket) {
		return Utils.receiveString(socket);
	}
}
