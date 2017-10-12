package com.oblivm.backend.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.Socket;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;

public abstract class GenRunnable<T> extends com.oblivm.backend.network.Server implements Runnable {

	Mode m = Mode.getMode("OPT");
	public int port;
//	Socket socket;
	static FileInputStream inp;
	static DataInputStream inOT; 

	public void setParameter(int port) {
		this.port = port;
//		this.socket = socket;
	}

	public abstract void prepareInputBob(CompEnv<T> gen) throws Exception;
	
	public void prepareInputBobFrom(byte[][] keys0, byte[][] keys1, int input_size, CompEnv<T> gen) throws FileNotFoundException {
		((com.oblivm.backend.gc.GCGenComp)gen).inputOfBobFrom(keys0, keys1, input_size);
		gen.flush();
	}
	
	public void runOTOnly(byte[][] keys0, byte[][] keys1, int input_size) {
		try{
			double s = System.nanoTime();
			Flag.sw.startTotal();
			
			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(Party.Alice, null, null, null, null, null, null);
			
			System.out.println("listening");
			listen(port);
			System.out.println("connected");
			
			DataInputStream inOT = new DataInputStream(new BufferedInputStream(new FileInputStream("otfile.txt")));
			env.setOTNetwork(this, inOT);
			
			prepareInputBobFrom(keys0, keys1, input_size, env);
			os.flush();
			
			Flag.sw.stopTotal();
			double e = System.nanoTime();
			disconnect();

			System.out.println("Gen running time:"+(e-s)/1e9);
			
			inOT.close();
		} catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void runCore() {
		try{
			FileInputStream inp = new FileInputStream("pair.txt");

			double s = System.nanoTime();
			Flag.sw.startTotal();
			
			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(Party.Alice, null, null, null, inp, null, null);
			
			System.out.println("listening");
			listen(port);
			System.out.println("connected");
			
			DataInputStream inOT = new DataInputStream(new BufferedInputStream(new FileInputStream("otfile.txt")));
			env.setOTNetwork(this, inOT);
			
			prepareInputBob(env);
			os.flush();
			
			Flag.sw.stopTotal();
			double e = System.nanoTime();
			disconnect();

			System.out.println("Gen running time:"+(e-s)/1e9);
			
			inp.close();
			inOT.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	public void run() {
		try {
			runCore();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

