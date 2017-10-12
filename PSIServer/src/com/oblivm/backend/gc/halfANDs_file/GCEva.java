package com.oblivm.backend.gc.halfANDs_file;

import java.io.FileInputStream;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.gc.GCEvaComp;
import com.oblivm.backend.gc.GCSignal;

import com.oblivm.backend.gc.halfANDs_file.Garbler;

public class GCEva extends GCEvaComp {
	Garbler gb;
	
	public GCEva(FileInputStream in, FileInputStream inr) {
		super(in, inr, Mode.OPT);
		gb = new Garbler();
	}

	/*Evaluating and gate*/
	public GCSignal and(GCSignal a, GCSignal b) {
		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = ((a.v && b.v)? new GCSignal(true): new GCSignal(false));
		else if (a.isPublic())
			res =  a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {
			++numOfAnds;
			int i0 = a.getLSB();
			int i1 = b.getLSB();

			GCSignal TG = GCSignal.ZERO, WG, TE = GCSignal.ZERO, WE;
			try {
				Flag.sw.startGCIO();
				//TG = GCSignal.receive(channel);
				//TE = GCSignal.receive(channel);
				//Thread.sleep(100);
				byte len = (byte) input.read();
				//System.out.print(len);
				byte[] buf = new byte[len];
				input.read(buf);
				TG = new GCSignal(buf);
				len = (byte) input.read();
				//System.out.print("len " + len);
				buf = new byte[len];
				input.read(buf);
				TE = new GCSignal(buf);
				//TG = GCSignal.receive(channel);
				//System.out.println("receive TG");
				//TE = GCSignal.receive(channel);
				//System.out.println("receive TE");
				Flag.sw.stopGCIO();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			WG = gb.hash(a, gid, false).xor((i0 == 1) ? TG : GCSignal.ZERO);
			WE = gb.hash(b, gid, true).xor((i1 == 1) ? (TE.xor(a)) : GCSignal.ZERO);
			
			res = WG.xor(WE);
			
			gid++;
		}
		Flag.sw.stopGC();
		return res;
	}
}