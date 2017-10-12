import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

public class PSIDH2 implements PSI {
	public PSIDH2(int size) {
		
		generateKey();
		DB = new File("./DB_DH");	
		long startTime = System.currentTimeMillis();
		generateDB(size);
	
		System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));
	}
	
	public void sendDB(Socket socket) {
		Utils.sendBytes(socket, Utils.bigIntegerToBytes(N, false));
		try {
			DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());

			Utils.sendInteger(d_out, DB.length());
			Utils.sendFile(d_out, DB);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void answerQuery(Socket socket) {
		byte[] query = Utils.receiveBytes(socket);		
		byte[] result = Utils.bigIntegerToBytes(Utils.bytesToBigInteger(query, 0, query.length).modPow(key, N), false);
				
		Utils.sendBytes(socket, result);
	}
	
	private BigInteger key;
	private BigInteger N; //modular
	private BigInteger phi_N; 
	private File DB;
	private int mod_size = 2048;
	
	private void generateKey() {
		N = generatePrime(mod_size);
		phi_N = N.subtract(new BigInteger("1")); 
		key = new BigInteger(256, new SecureRandom());
		key = key.mod(phi_N);
		
		System.out.println(N.toString(16));
		System.out.println(key.toString(16));
	}
	
	private void generateDB(int size) {
		System.out.println("building DB...");		
		for (int i = 0; i < size; i ++) {
			BigInteger I = Utils.stringToBigInteger(i+"");
	//		System.out.println(I.modPow(key, N).toString(16));
			byte[] result = Utils.bigIntegerToBytes(I.modPow(key, N), false);
			if (result.length < 128) {
				Utils.padBytes(result, 128);
			}
			Utils.writeLineToFile(DB, result, i, size);			
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
}
