package com.oblivm.backend.gc;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.flexsc.Party;
import com.oblivm.backend.network.Network;
import com.oblivm.backend.ot.OTExtReceiver;
import com.oblivm.backend.ot.OTReceiver;

public abstract class GCEvaComp extends GCCompEnv{

	OTReceiver rcv;

	protected long gid = 0;
	
	public GCEvaComp(FileInputStream in, FileInputStream inr, Mode mode) {
		super(in, inr, Party.Bob, mode);
	}
	
	public void setOTNetwork(Network channel, DataInputStream in){
		setNetwork(channel);
		
		rcv = new OTExtReceiver(channel, in);
	}
	
	public GCSignal[] inputOfAlice(boolean[] x) {
		Flag.sw.startOT();
		GCSignal[] result = new GCSignal[x.length];
		for (int i = 0; i < x.length; ++i){
			//result[i] = GCSignal.receive(channel);
			byte len;
			try {
				len = (byte) input.read();
				//System.out.println(len);
				byte[] buf = new byte[len];
				input.read(buf);
				result[i] = new GCSignal(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("File receive");
		Flag.sw.stopOT();
		return result;
	}
	
	public GCSignal[] inputOfBob(boolean[] x) {
		GCSignal[] ret = new GCSignal[x.length];
		for(int i = 0; i < x.length; i+=Flag.OTBlockSize) {
			GCSignal[] tmp = inputOfBobInter(Arrays.copyOfRange(x, i, Math.min(i+Flag.OTBlockSize, x.length)));
			System.arraycopy(tmp, 0, ret, i, tmp.length);
		}
		return ret;
	}
	
	public GCSignal[] inputOfBobInter(boolean[] x) {
		Flag.sw.startOT();
		GCSignal[] signal = null;
		try {
			signal = rcv.receive(x);
			System.out.println("Receive OT");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Flag.sw.stopOT();
		return signal;
	}
	
	@Override
	public GCSignal[] createPairOfBob(int x, FileOutputStream out) {
		return null;
	}
	
	public boolean outputToBob(GCSignal out) {
		if (out.isPublic())
			return out.v;

		//GCSignal lb = GCSignal.receive(channel);
		GCSignal lb = null;
		byte len;
		byte[] buf;
		try {
			len = (byte) inRes.read();
			buf = new byte[len];
			inRes.read(buf);
			lb = new GCSignal(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (lb.equals(out))
			return false;
		else
			return true;
	}

	public boolean[] outputToBob(GCSignal[] out) {
		boolean[] result = new boolean[out.length];
		for (int i = 0; i < result.length; ++i) {
			result[i] = outputToBob(out[i]);
			System.out.println(result[i]);
		}
		return result;
	}

	public GCSignal xor(GCSignal a, GCSignal b) {
		if (a.isPublic() && b.isPublic())
			return  ((a.v ^ b.v) ?new GCSignal(true):new GCSignal(false));
		else if (a.isPublic())
			return a.v ? not(b) : b;
		else if (b.isPublic())
			return b.v ? not(a) : a;
		else
			return a.xor(b);
	}

	public GCSignal not(GCSignal a) {
		if (a.isPublic())
			return ((!a.v) ?new GCSignal(true):new GCSignal(false));
		else {
			return a;
		}
	}
}
