import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

public class PSIDH implements PSI {
	public PSIDH(int size) {
		long startTime = System.currentTimeMillis();
		
		generateKey();
		DB = new File("./DB_DH");		
		generateDB(size);
	
		System.out.println("-----------------Time used:" + (System.currentTimeMillis() - startTime));
	}
	
	public void sendDB(Socket socket) {
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
		byte[] result = bytesToECPoint(query).multiply(key).getEncoded(false);
	//	System.out.println(Utils.bytesToBigInteger(result, 0, result.length).toString());

		Utils.sendBytes(socket, result);
	}
	
	private BigInteger key;
	private ECPoint G;
	private File DB;
	
	private void generateKey() {
		ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");	
		BigInteger n = ecSpec.getN();
		G = ecSpec.getG();
		key = org.bouncycastle.util.BigIntegers.createRandomInRange(BigInteger.ONE, 
    			n.subtract(BigInteger.ONE), new SecureRandom());
	}
	
	private void generateDB(int size) {
		System.out.println("building DB...");		
		for (int i = 0; i < size; i ++) {
			BigInteger I = Utils.stringToBigInteger(i+"");
			byte[] result = G.multiply(I).multiply(key).getEncoded(false);
			// System.out.println(result.length);
			Utils.writeLineToFile(DB, result, i, size);
			
			// System.out.println(bytesToECPoint(result).equals(G.multiply(I).multiply(key)));
		}		
	}
	
	private ECPoint bytesToECPoint(byte[] bytes) {
		ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
		ECPoint point = ecSpec.getCurve().decodePoint(bytes);	
		
		return point;
	}
}
