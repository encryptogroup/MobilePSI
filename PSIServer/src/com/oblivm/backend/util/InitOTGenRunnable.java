package com.oblivm.backend.util;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.ot.InitOTExtSender;

public abstract class InitOTGenRunnable<T> extends com.oblivm.backend.network.Server implements Runnable {	
	
	Mode m = Mode.getMode("OPT");
	
	public int port;

	public void setParameter(int port) {
		this.port = port;
	}
		
	public void runCore() {
		try{
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("otfile.txt", true)));
			
			double s = System.nanoTime();
			Flag.sw.startTotal();
			
			System.out.println("listening");
			listen(port);
			System.out.println("connected");
			
			new InitOTExtSender(80, this, out);
			os.flush();
			
			Flag.sw.stopTotal();
			double e = System.nanoTime();
			disconnect();
			out.flush();
			out.close();
			System.out.println("InitOTGen running time:"+(e-s)/1e9);
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
	

	public void loadConfig(String configFile) {
		File file = new File(configFile);

		Scanner scanner;
		int port=0;

		try {
			scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String a = scanner.nextLine();
				String[] content = a.split(":");
				if(content.length == 2) {
					if(content[0].equals("Port"))
						port = new Integer(content[1].replace(" ", ""));
					else{}
				}	 
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.setParameter(port);
	}
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException { 
		FileOutputStream out;
		try {
			out = new FileOutputStream("otfile.txt");
			out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		for( int i = 0; i < Flag.repeat; ++i ){
			Class<?> clazz = Class.forName(args[0]+"$InitOTGenerator");
			InitOTGenRunnable run = (InitOTGenRunnable) clazz.newInstance();
			run.loadConfig("Config.conf");
			run.port += i;
			run.run();
			if(Flag.CountTime)
				Flag.sw.print();
			
		}
	}
}