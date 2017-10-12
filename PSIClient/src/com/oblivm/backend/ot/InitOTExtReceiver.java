// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package com.oblivm.backend.ot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.gc.GCSignal;
import com.oblivm.backend.network.Network;
import com.oblivm.backend.ot.OTExtSender.SecurityParameter;
import com.oblivm.backend.rand.ISAACProvider;

public class InitOTExtReceiver extends OTReceiver {
	static SecureRandom rnd;
	static {
		Security.addProvider(new ISAACProvider());
		try {
			rnd = SecureRandom.getInstance("ISAACRandom");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private OTSender snder;
	private GCSignal[][] keyPairs;
	public FileOutputStream out;
	public FileOutputStream outMatrix;

	public InitOTExtReceiver(Network channel, FileOutputStream out, FileOutputStream outMatrix) {
		super(channel);
		
		this.out = out;
		this.outMatrix = outMatrix;

		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean[] s = new boolean[SecurityParameter.k1];

	public GCSignal[] receive(boolean[] choices) throws IOException {
		return null;
	}

	private void initialize() throws Exception {
		Flag.sw.startOTIO();
		msgBitLength = channel.readInt();
		Flag.sw.stopOTIO();

		snder = new NPOTSender(OTExtSender.SecurityParameter.k1, channel);

		keyPairs = new GCSignal[OTExtSender.SecurityParameter.k1][2];
		for (int i = 0; i < OTExtSender.SecurityParameter.k1; i++) {
			keyPairs[i][0] = GCSignal.freshLabel(rnd);
			keyPairs[i][1] = GCSignal.freshLabel(rnd);
		}
	
		snder.send(keyPairs);
		channel.flush();
		
		for(int j = 0; j < SecurityParameter.k1; ++j){
			for (int i = 0; i < keyPairs[j][0].bytes.length; i++){
				out.write(keyPairs[j][0].bytes[i]);
			}
			for (int i = 0; i < keyPairs[j][1].bytes.length; i++){
				out.write(keyPairs[j][1].bytes[i]);
			}
		}
		out.flush();
		
		BitMatrix T = new BitMatrix(208, SecurityParameter.k1); //208 = 128 + 80
		T.initialize(rnd);
		
		T.writeBitMatrix(outMatrix);
		outMatrix.flush();
	}

	GCSignal[] pool;
	int poolIndex = 0;

	@Override
	public GCSignal receive(boolean c) {
		try {
			throw new Exception(
					"It doesn't make sense to do single OT with OT extension!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}