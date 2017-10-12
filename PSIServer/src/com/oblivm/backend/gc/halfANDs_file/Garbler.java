package com.oblivm.backend.gc.halfANDs_file;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.oblivm.backend.gc.GCSignal;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

final class Garbler {
	/*private MessageDigest sha1 = null;
	Garbler() {
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
            
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
	
	ByteBuffer buffer = ByteBuffer.allocate(GCSignal.len+9); 
	public GCSignal hash(GCSignal lb, long k, boolean b) {
		buffer.clear();
		sha1.update(buffer.put(lb.bytes).putLong(k).put(b?(byte)1:(byte)0));
		return GCSignal.newInstance(sha1.digest());
	}*/
	
	private static byte[] longToByteArray(long in){
		return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(in).array();
	}
	
	private static BigInteger XOR(BigInteger in1, BigInteger in2){
		return in1.xor(in2);
	}
	
	private static BigInteger XOR2(BigInteger in1, BigInteger in2){
		return in1.xor(in2.shiftLeft(1));
	}
	
	private static byte[] keyValue = new byte[] {'0','2','3','4','5','6','7','8','9','1','2','3','4','5','6','7'};// your key
	private SecretKey secKey = null;
	Cipher AesCipher = null;
	
	Garbler() {
        try {
        	secKey = new SecretKeySpec(keyValue, "AES");
            AesCipher = Cipher.getInstance("AES");
            AesCipher.init(Cipher.ENCRYPT_MODE, secKey);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
	public GCSignal hash(GCSignal lb, long k, boolean b) {
		byte[] tmp = null;
		byte[] res_Key = null;
		BigInteger temp_key = new BigInteger(lb.bytes);
		tmp = longToByteArray(k);
		BigInteger p = new BigInteger(tmp);
		BigInteger tmp2 = null;
		BigInteger res = null;
		BigInteger final_res = null;
		try {
			tmp2= XOR2(temp_key, p);
			res_Key = AesCipher.doFinal(tmp2.toByteArray());
			res = new BigInteger(res_Key);
			final_res = XOR(res, tmp2);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return new GCSignal(final_res.toByteArray());
	}
}