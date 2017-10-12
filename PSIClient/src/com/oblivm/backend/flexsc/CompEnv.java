// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package com.oblivm.backend.flexsc;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import com.oblivm.backend.network.Network;
import com.oblivm.backend.rand.ISAACProvider;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;
import com.oblivm.backend.gc.GCSignal;

public abstract class CompEnv<T> {
	public long numOfAnds = 0;
	public static SecureRandom rnd;
	static{
		Security.addProvider(new ISAACProvider());
		try {
			rnd = SecureRandom.getInstance("ISAACRandom");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static CompEnv getEnv(Party p, FileInputStream in, FileOutputStream o, FileOutputStream op, FileInputStream inp, FileInputStream inr, FileOutputStream outr) {
		if (p == Party.Bob)
			return new com.oblivm.backend.gc.halfANDs_file.GCEva(in, inr);
		else
			return new com.oblivm.backend.gc.halfANDs_file.GCGen(o, op, inp, inr, outr);
	}
	
	public void setNetwork(Network w){
		this.channel = w;
	}

	public abstract void setOTNetwork(Network channel, DataInputStream in);

	public Network channel;
	public FileInputStream input;
	public FileOutputStream out;
	public FileInputStream inPair;
	public FileOutputStream outPair;
	public FileInputStream inRes;
	public FileOutputStream outRes;
	public Party party;
	public Mode mode;
	
	public CompEnv(FileOutputStream o, FileOutputStream op, FileInputStream in, FileInputStream inr, FileOutputStream or, Party p, Mode m) {
		this.out = o;
		this.mode = m;
		this.party = p;
		this.inPair = in;
		this.outPair = op;
		this.inRes = inr;
		this.outRes = or;
	}
	
	public CompEnv(FileInputStream in, FileInputStream inr, Party p, Mode m) {
		this.input = in;
		this.mode = m;
		this.party = p;
		this.inRes = inr;
	}
	
	public abstract T[] inputOfAlice(boolean[] in);

	public abstract T[] inputOfBob(boolean[] in);
	
	public abstract T[] createPairOfBob(int len, FileOutputStream out);
	
	public abstract boolean outputToBob(T out);
	public abstract boolean[] outputToBob(T[] out);

	public abstract T and(T a, T b);

	public abstract T xor(T a, T b);

	public abstract T not(T a);

	public abstract T ONE();

	public abstract T ZERO();

	public abstract T[] newTArray(int len);

	public abstract T newT(boolean v);
	
	public abstract T newT(GCSignal v);
	
	public abstract void writeToFile(T s);
	
	public Party getParty() {
		return party;
	}

	public void flush() {
		channel.flush();
	}

	public Mode getMode() {
		return mode;
	}

}
