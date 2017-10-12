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

import android.os.Environment;

public abstract class InitOTGenRunnable<T> extends com.oblivm.backend.network.Server implements Runnable {	
	
	Mode m = Mode.getMode("OPT");
	
	public int port;

	public void setParameter(int port) {
		this.port = port;
	}
		
	public void runCore() {
		try{
			File ot_file = new File(Environment.getExternalStorageDirectory(), "/otfile.txt");
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(ot_file, true)));
			
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
}