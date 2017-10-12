package com.example.psiclient;
import android.annotation.SuppressLint;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {
		
	public static boolean readFileAndTest(byte[] result, File file, int buffer_size) {	
		try {
			FileInputStream f_in = new FileInputStream(file);			
			byte[] buffer = new byte[buffer_size];
			
			int i = 0;
			while (f_in.read(buffer) > 0) {
		//		System.out.println(bytesToBigInteger(buffer, 0, buffer_size).toString(16));
		
				i ++;
				if (Arrays.equals(result, buffer))
    			{
					f_in.close();
    			    return true;
    			}
			}
			f_in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void sendBytes(Socket socket, byte[] bytes) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static byte[] receiveBytes(Socket socket) {
		byte[] bytes = null;
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			bytes = (byte[])in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
			
		return bytes;
	}
	
	public static byte[][] receive2DBytes(Socket socket) {
		byte[][] bytes = null;
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			bytes = (byte[][])in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
			
		return bytes;
	}
	
	public static long receiveInteger(DataInputStream d_in) {
		try {
			long size = d_in.readLong();
			return size;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void sendString(Socket socket, String s) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter  
			        (new OutputStreamWriter(socket.getOutputStream())), true);
			out.println(s);
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}
	
	public static String receiveString(Socket socket) {			
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void receiveFile(DataInputStream d_in, File file, long size) {
		try {	
			FileOutputStream f_out = new FileOutputStream(file);
			byte[] buffer = new byte[4096];
			
			int read = 0;
			long totalRead = 0;
			long remaining = size;
			int count;
			if (remaining < buffer.length) {
				count = (int)remaining;
			} else {
				count = buffer.length;
			}
			while((read = d_in.read(buffer, 0, count)) > 0) {
				totalRead += read;
				remaining -= read;
				if (remaining < count) {
					count = (int)remaining;
				}
				System.out.println(size + "------------------Download:" + totalRead + " bytes-----------" + file.getPath());
				f_out.write(buffer, 0, read);
			}
			f_out.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	
	public static BigInteger stringToBigInteger(String s) {
		BigInteger ret = null;
		try {
			byte[] input = s.getBytes("UTF-8");
			ret = bytesToBigInteger(input, 0, input.length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static BigInteger bytesToBigInteger(byte[] in, int inOff, int inLen) {
		byte[]  block;

		if (inOff != 0 || inLen != in.length) {
			block = new byte[inLen];

			System.arraycopy(in, inOff, block, 0, inLen);
		} else {
			block = in;
		}

		BigInteger res = new BigInteger(1, block);

		return res;
	}
	
	public static byte[] bigIntegerToBytes(BigInteger result, boolean forEncryption) {
		byte[] output = result.toByteArray();

	/*	if (forEncryption) {
			if (output[0] == 0 && output.length > getOutputBlockSize(forEncryption)) {       // have ended up with an extra zero byte, copy down.	            
				byte[]  tmp = new byte[output.length - 1];
				System.arraycopy(output, 1, tmp, 0, tmp.length);
				return tmp;
			}

			if (output.length < getOutputBlockSize(forEncryption)) {    // have ended up with less bytes than normal, lengthen	            
				byte[]  tmp = new byte[getOutputBlockSize(forEncryption)];
				System.arraycopy(output, 0, tmp, tmp.length - output.length, output.length);
				return tmp;
			}
		} else {*/
			if (output[0] == 0) {        // have ended up with an extra zero byte, copy down.
				byte[]  tmp = new byte[output.length - 1];
				System.arraycopy(output, 1, tmp, 0, tmp.length);
				return tmp;
			}
//		}

		return output;	
	}
	
	@SuppressLint("NewApi")
	public static byte[] sha1(String input, int length) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
         
        return Arrays.copyOfRange(result, 0, length/8);
    }
	
	public static String bytesToBinaryString(byte[] input) {
		StringBuilder output = new StringBuilder("");
		for (int i = 0; i < input.length; i ++) {
			output = output.append(String.format("%8s", Integer.toBinaryString(input[i] & 0xFF)).replace(' ', '0'));
		}	
		return output.toString();
	}	
	
	public static byte[] booleansToBytes(boolean[] input) {
	    byte[] output = new byte[input.length / 8];
	    for (int entry = 0; entry < output.length; entry++) {
	        for (int bit = 0; bit < 8; bit++) {
	            if (input[entry * 8 + bit]) {
	            	output[entry] |= (128 >> bit);
	            }
	        }
	    }

	    return output;
	} 
	
	public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
/*	
	public static int getOutputBlockSize(boolean forEncryption) {
        int bitSize = ((RSAKeyParameters)keyPair.getPublic()).getModulus().bitLength();

        if (forEncryption) {
            return (bitSize + 7) / 8;
        }
        else {
            return (bitSize + 7) / 8 - 1;
        }
    }*/
}
