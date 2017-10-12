package com.oblivm.backend.gc;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;
import com.oblivm.backend.network.Network;
import com.oblivm.backend.ot.OTExtSender;
import com.oblivm.backend.ot.OTSender;

public abstract class GCGenComp extends GCCompEnv {

	static public GCSignal R = null;
	static {
		R = GCSignal.freshLabel(CompEnv.rnd);
		R.setLSB();
	}
	
	OTSender snd;
	protected long gid = 0;
	protected boolean gatesRemain = false;
	
	public GCGenComp(FileOutputStream o, FileOutputStream op, FileInputStream in, FileInputStream inr, FileOutputStream outr, Mode mode) {
		super(o, op, in, inr, outr, Party.Alice, mode);
	}
	
	public void setOTNetwork(Network channel, DataInputStream in){
		setNetwork(channel);
		
		snd = new OTExtSender(80, channel, in);
	}

	public static GCSignal[] genPairForLabel(Mode mode) {
		GCSignal[] label = new GCSignal[2];
		label[0] = GCSignal.freshLabel(rnd);
		label[1] = R.xor(label[0]);
		return label;
	}
	
	public static GCSignal[] genPair() {
		GCSignal[] label = new GCSignal[2];
		label[0] = GCSignal.freshLabel(rnd);
		label[1] = R.xor(label[0]);
		return label;
	}
	
	public GCSignal[] inputOfAlice(boolean[] x)  {
		System.out.println("File send");
		Flag.sw.startOT();
		GCSignal[][] pairs = new GCSignal[x.length][2];
		GCSignal[] result = new GCSignal[x.length];
		for (int i = 0; i < x.length; ++i) {
			pairs[i] = genPairForLabel(mode);
			result[i] = pairs[i][0];
		}
		Flag.sw.startOTIO();
		for (int i = 0; i < x.length; ++i){
			//pairs[i][x[i] ? 1 : 0].send(channel);
			try {
				out.write(pairs[i][x[i] ? 1 : 0].bytes.length);
				//System.out.println(pairs[i][x[i] ? 1 : 0].bytes.length);
				for (int j = 0; j < pairs[i][x[i] ? 1 : 0].bytes.length; j++){
					out.write(pairs[i][x[i] ? 1 : 0].bytes[j]);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Flag.sw.stopOTIO();
		Flag.sw.stopOT();
		return result;
	}
	
	public GCSignal[] createPairOfBob(int len, FileOutputStream out) {
		Flag.sw.startOT();
		GCSignal[][] pair = new GCSignal[len][2];
		for (int i = 0; i < len; ++i)
			pair[i] = genPairForLabel(mode);
		GCSignal[] result = new GCSignal[len];
		for (int i = 0; i < len; ++i){
			result[i] = pair[i][0];
			try {
				outPair.write(pair[i][0].bytes.length);
				for (int j = 0; j < pair[i][0].bytes.length; j++){
					outPair.write(pair[i][0].bytes[j]);
				}
				outPair.write(pair[i][0].bytes.length);
				for (int j = 0; j < pair[i][1].bytes.length; j++){
					outPair.write(pair[i][1].bytes[j]);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Flag.sw.stopOT();
		return result;
	}
	
	public GCSignal[] inputOfBob(boolean[] x) {
		Flag.sw.startOT();
		int len = x.length;
		try {
			byte[] buf;
			GCSignal[][] pair = new GCSignal[len][2];
			byte lenPair1;
			byte lenPair2;
			
			for (int i = 0; i < len; ++i){
				try {
					lenPair1 = (byte) inPair.read();
					//System.out.println("lenp1 " + lenPair1);
					buf = new byte[lenPair1];
					inPair.read(buf);
					pair[i][0] = new GCSignal(buf);
					
					lenPair2 = (byte) inPair.read();
					//System.out.println("lenp2 " + lenPair2);
					buf = new byte[lenPair2];
					inPair.read(buf);
					pair[i][1] = new GCSignal(buf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			snd.send(pair);
			System.out.println("Send OT");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Flag.sw.stopOT();
		//return dummy output
		return null;
	}
	
	public boolean outputToBob(GCSignal out) {
		if (!out.isPublic())
			out.send(channel);
		return false;
	}

	public boolean[] outputToBob(GCSignal[] out) {
		boolean[] result = new boolean[out.length];

		for (int i = 0; i < out.length; ++i) {
			if (!out[i].isPublic()){
				//System.out.println("Send " + i);
				out[i].send(channel);
			}
		}
		flush();

		//dummy result
		for (int i = 0; i < result.length; ++i)
			result[i] = false;
		return result;
	}

	public GCSignal xor(GCSignal a, GCSignal b) {
		if (a.isPublic() && b.isPublic())
			return new GCSignal(a.v ^ b.v);
		else if (a.isPublic())
			return a.v ? not(b) : new GCSignal(b);
		else if (b.isPublic())
			return b.v ? not(a) : new GCSignal(a);
		else {
			return a.xor(b);
		}
	}

	public GCSignal not(GCSignal a) {
		if (a.isPublic())
			return new GCSignal(!a.v);
		else
			return R.xor(a);
	}
}
