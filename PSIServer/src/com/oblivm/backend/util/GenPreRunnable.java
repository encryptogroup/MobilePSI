package com.oblivm.backend.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;

public abstract class GenPreRunnable<T> implements Runnable {
	
	Mode m = Mode.getMode("OPT");
	
	public abstract void prepareInput(CompEnv<T> gen) throws Exception;
	public abstract void secureCompute(CompEnv<T> gen) throws Exception;
	
	public void runCore() {
		try{
			FileOutputStream out = new FileOutputStream("test.txt", true);
		//	FileInputStream input = new FileInputStream("test.txt");
			FileOutputStream op = new FileOutputStream("pair.txt", true);
		//	FileInputStream inp = new FileInputStream("pair.txt");
			FileOutputStream outr = new FileOutputStream("res.txt", true);
		//	FileInputStream inr = new FileInputStream("res.txt");		
			
			double s = System.nanoTime();
			Flag.sw.startTotal();

			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(Party.Alice, null, out, op, null, null, outr);
			
			prepareInput(env);

			secureCompute(env);
			
			Flag.sw.stopTotal();
			double e = System.nanoTime();
			out.flush();
			out.close();
		//	input.close();
			System.out.println("Offline running time:"+(e-s)/1e9);
			System.out.println("Number Of AND Gates:"+env.numOfAnds);
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
