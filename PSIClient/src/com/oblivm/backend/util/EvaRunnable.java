package com.oblivm.backend.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;

import android.os.Environment;

public abstract  class EvaRunnable<T> extends com.oblivm.backend.network.Client implements Runnable {
	public abstract void prepareInput(CompEnv<T> gen) throws Exception;
	public abstract void prepareInputBob(CompEnv<T> gen) throws Exception;
	public abstract void secureCompute(CompEnv<T> gen) throws Exception;
	public abstract void prepareOutput(CompEnv<T> gen) throws Exception;
	public abstract byte[][] getInputsBob() throws Exception;
	
	Mode m;
	public int port;
	String host;
	protected String[] args;
	public boolean verbose = true;
	protected String to_be_encrypted;
	protected File GCTable;
	protected File res;
	protected byte[] result;
	protected int input_length = 128;
	
	public byte[] getResult() {
		return result;
	}
 
	public void setParameter(Mode m, String host, int port, String to_be_encrypted, File GCTable, File res){
		this.m = m;
		this.port = port;
		this.host = host;
		this.to_be_encrypted = to_be_encrypted;
		this.GCTable = GCTable;
		this.res = res;
	}
	
	public void runOTOnly(int intput_size) throws Exception {
		this.input_length = intput_size;
		
		@SuppressWarnings("unchecked")
		CompEnv<T> env = CompEnv.getEnv(Party.Bob, null, null, null, null, null, null);
		
		double s = System.nanoTime();
		Flag.sw.startTotal();
		
		if(verbose)
			System.out.println("connecting");
		
		connect(host, port);
		
		if(verbose)
			System.out.println("connected");
		
		DataInputStream inOT = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(Environment.getExternalStorageDirectory(), "otpair.txt"))));
		
		env.setOTNetwork(this, inOT);
		
		prepareInputBob(env);
		os.flush();
		
		Flag.sw.stopTotal();
		double e = System.nanoTime();
		disconnect();
		
		inOT.close();
		
		if(verbose){
			System.out.println("Eva running time:"+(e-s)/1e9);
			System.out.println("Number Of AND Gates:"+env.numOfAnds);
		}
	}
	
	public void runCore() throws Exception {

		FileInputStream input = new FileInputStream(GCTable);
		FileInputStream inr = new FileInputStream(res); 
		
		@SuppressWarnings("unchecked")
		CompEnv<T> env = CompEnv.getEnv(Party.Bob, input, null, null, null, inr, null);
		
		double s = System.nanoTime();
		Flag.sw.startTotal();
				
		prepareInput(env);
		
		if(verbose)
			System.out.println("connecting");
		connect(host, port);
		if(verbose)
			System.out.println("connected");
		
		DataInputStream inOT = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(Environment.getExternalStorageDirectory(), "otpair.txt"))));
		
		env.setOTNetwork(this, inOT);
		
		prepareInputBob(env);
		os.flush();
		
		secureCompute(env);
		
		prepareOutput(env);
		
		Flag.sw.stopTotal();
		double e = System.nanoTime();
		disconnect();
		
		input.close();
		inr.close();
		inOT.close();
		
		if(verbose){
			System.out.println("Eva running time:"+(e-s)/1e9);
			System.out.println("Number Of AND Gates:"+env.numOfAnds);
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
