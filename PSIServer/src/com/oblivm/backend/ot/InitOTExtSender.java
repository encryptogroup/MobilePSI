package com.oblivm.backend.ot;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.gc.GCSignal;
import com.oblivm.backend.network.Network;
import com.oblivm.backend.rand.ISAACProvider;

public class InitOTExtSender extends OTSender {
	static class SecurityParameter {
		public static final int k1 = 128; // number of columns in T
	}

	private static SecureRandom rnd;
	static {
		Security.addProvider(new ISAACProvider());
		try {
			rnd = SecureRandom.getInstance("ISAACRandom");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	private OTReceiver rcver;
	private boolean[] s;
	private GCSignal[] keys;
	public DataOutputStream out;

	public InitOTExtSender(int msgBitLength, Network channel, DataOutputStream out) {
		super(msgBitLength, channel);
		
		this.out = out;

		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int poolIndex = 0;

	@Override
	public void send(GCSignal[] m) {
		try {
			throw new Exception(
					"It doesn't make sense to do single OT with OT extension!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Everything in msgPairs are effective Sender's messages.
	 */
	GCSignal[][] keyPairs = new GCSignal[SecurityParameter.k1][2];

	public void send(GCSignal[][] msgPairs) throws IOException {
	}

	private void initialize() throws Exception {
		Flag.sw.startOTIO();
		channel.writeInt(msgBitLength);
		channel.flush();
		Flag.sw.stopOTIO();

		rcver = new NPOTReceiver(channel);
		channel.flush();
		
		s = new boolean[SecurityParameter.k1];
		for (int i = 0; i < s.length; i++)
			s[i] = rnd.nextBoolean();

		keys = rcver.receive(s);
		
		for(int j = 0; j < SecurityParameter.k1; ++j){
			out.writeBoolean(s[j]);
			//out.write(keys[j].bytes.length);
			for (int i = 0; i < keys[j].bytes.length; i++){
				out.write(keys[j].bytes[i]);
			}
		}
		out.flush();
		channel.flush();
	}
}
