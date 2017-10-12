package com.oblivm.backend.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.ot.InitOTExtReceiver;

public abstract  class InitOTEvaRunnable<T> extends com.oblivm.backend.network.Client implements Runnable {
	Mode m;
	public int port;
	String host;
	public boolean verbose = true;

	public void setParameter(Mode m, String host, int port){
		this.m = m;
		this.port = port;
		this.host = host;
	}
	
	public void runCore() throws Exception {
		FileOutputStream out = new FileOutputStream("otpair.txt", true);
		FileOutputStream outMatrix = new FileOutputStream("matrix.txt", true);
		
		double s = System.nanoTime();
		Flag.sw.startTotal();	
		
		System.out.println("connecting");
		connect(host, port);
		System.out.println("connected");
			
		new InitOTExtReceiver(this, out, outMatrix);
		os.flush();
		
		Flag.sw.stopTotal();
		double e = System.nanoTime();
		disconnect();
		out.flush();
		out.close();
		System.out.println("InitOTEva running time:"+(e-s)/1e9);
	}
	
	public void run() {
		try {
			runCore();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void loadConfig(String fileName) {
		File file = new File(fileName);
		Scanner scanner;
		String host = null;
		int port = 0;
		Mode mode = null;

		try {
			scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String a = scanner.nextLine();
				String[] content = a.split(":");
				if(content.length == 2) {
					if(content[0].equals("Host"))
						host = content[1].replace(" ", "");
					else if(content[0].equals("Port"))
						port = new Integer(content[1].replace(" ", ""));
					else if(content[0].equals("Mode"))
						mode = Mode.getMode(content[1].replace(" ", ""));
					else{}
				}
			}
			scanner.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		this.setParameter(mode, host, port);
	}
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException, ClassNotFoundException {
		FileOutputStream out;
		try {
			out = new FileOutputStream("otpair.txt");
			out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		for( int i = 0; i < Flag.repeat; ++i ){
			Class<?> clazz = Class.forName(args[0]+"$InitOTEvaluator");
			InitOTEvaRunnable run = (InitOTEvaRunnable) clazz.newInstance();
			run.loadConfig("Config.conf");
			run.port += i;
			run.run();
			if(Flag.CountTime)
				Flag.sw.print();
			if(Flag.countIO)
				run.printStatistic();
		}
	}
}
