package com.oblivm.backend.gc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;

public abstract class GCCompEnv extends CompEnv<GCSignal> {
	
	public GCCompEnv(FileInputStream in, FileInputStream inr, Party p, Mode mode) {
		super(in, inr, p, mode);
	}
	
	public GCCompEnv(FileOutputStream o, FileOutputStream op, FileInputStream in, FileInputStream inr, FileOutputStream outr, Party p, Mode mode) {
		super(o, op, in, inr, outr, p, mode);
	}

	public GCSignal ONE() {
		return new GCSignal(true);
	}
	
	public GCSignal ZERO() {
		return new GCSignal(false);
	}
	
	public GCSignal[] newTArray(int len) {
		return new GCSignal[len];
	}
	
	public GCSignal newT(boolean v) {
		return new GCSignal(v);
	}
	
	public GCSignal newT(GCSignal v) {
		return v;
	}

	public void writeToFile(GCSignal s){
		try {
			outRes.write(s.bytes.length);
			for( int i = 0; i < s.bytes.length; ++i ){
				outRes.write(s.bytes[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
