import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

public class PSIRSA implements PSI {
	private String s = "";
	public PSIRSA(int size) {	
		for (int i = 0; i < 120; i ++)
			s += "a";
		
	/*	generateKeys(2048);
		
		RSAKeyParameters pk = (RSAKeyParameters)keyPair.getPublic();
		BigInteger e = pk.getExponent();	
		BigInteger N = pk.getModulus();
//		System.out.println(e.toString());
//		System.out.println(N.toString());
		
		DB = new File("./DB_RSA");
		long startTime = System.currentTimeMillis();
		generateDB(size);		
		
		System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));*/
	}
	
	public void sendDB(Socket socket) {	
		/*	DataOutputStream d_out = new DataOutputStream(socket.getOutputStream());
		
		Utils.send2DBytes(socket, getPK());
		Utils.sendInteger(d_out, DB.length());
		Utils.sendFile(d_out, DB);*/
		Utils.sendString(socket, s);			
	}
	
	public void answerQuery(Socket socket) {
		byte[] query = Utils.receiveBytes(socket);
		byte[] result = sign(query);					

		Utils.sendBytes(socket, result);
	}
	
	private AsymmetricCipherKeyPair keyPair;
	private File DB;
	
	private byte[][] getPK() {
		byte[][] ret = new byte[2][];
		RSAKeyParameters pk = (RSAKeyParameters)keyPair.getPublic();
		BigInteger e = pk.getExponent();	
		BigInteger N = pk.getModulus();
		//System.out.println(e.toString());
		//System.out.println(N.toString());
		ret[0] = Utils.bigIntegerToBytes(e, false);
		ret[1] = Utils.bigIntegerToBytes(N, false);			
						
		return ret;
	}
	
	private byte[] sign(byte[] query) {
		try {					
        	RSAPrivateCrtKeyParameters sk = (RSAPrivateCrtKeyParameters)keyPair.getPrivate();
        	BigInteger N = sk.getModulus();
        	BigInteger d = sk.getExponent();
        	BigInteger x = Utils.bytesToBigInteger(query, 0, query.length);
        	// System.out.println(x.toString(16));
        	BigInteger y = x.modPow(d, N);
        	        	
        	/* check result         	
        	BigInteger h = Utils.stringToBigInteger("123");
        	BigInteger z = h.modPow(d, N);
        	System.out.println("");
        	System.out.println(z.toString());*/
        	
        	return Utils.bigIntegerToBytes(y, false);        	
		 } catch (Exception e) {
			e.printStackTrace();
		 }
		return (new byte[1]);
	}
			
	private void generateKeys(int keySize) {
	     RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
	     gen.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(),
	                keySize, 80));

	     keyPair = gen.generateKeyPair();
	}	
	
	private void generateDB(int size) {
		RSAPrivateCrtKeyParameters sk = (RSAPrivateCrtKeyParameters)keyPair.getPrivate();
    	BigInteger N = sk.getModulus();
    	BigInteger d = sk.getExponent();  
    	System.out.println(d.toString());
		System.out.println("building DB...");		
		for (int i = 0; i < size; i ++) {
			BigInteger h = Utils.stringToBigInteger(i+"");
		   	BigInteger z = h.modPow(d, N);
	/*	   	bug: data change after writting to a file
	  		if (i == 123) {
		   		System.out.println(z.toString());
		   	}*/
		   	System.out.println(Utils.bigIntegerToBytes(z, false).length);
			Utils.writeLineToFile(DB, Utils.bigIntegerToBytes(z, false), i, size);								
		}	
	//	Utils.readFileAndTest((new byte[128]), DB, 128);
	}
}