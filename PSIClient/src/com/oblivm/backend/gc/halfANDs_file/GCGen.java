package com.oblivm.backend.gc.halfANDs_file;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.oblivm.backend.flexsc.Flag;
import com.oblivm.backend.flexsc.Mode;
import com.oblivm.backend.gc.GCGenComp;
import com.oblivm.backend.gc.GCSignal;

public class GCGen extends GCGenComp {
	Garbler gb;
	
	public GCGen(FileOutputStream out, FileOutputStream outPair, FileInputStream inPair, FileInputStream inRes, FileOutputStream outRes){
		super(out, outPair, inPair, inRes, outRes, Mode.OPT);
		gb = new Garbler();
	}

	private GCSignal labelL[] = new GCSignal[2];
	private GCSignal labelR[] = new GCSignal[2];

	private GCSignal TG, WG, TE, WE;
	
	private GCSignal garble(GCSignal a, GCSignal b) {
		labelL[0] = a;
		labelL[1] = R.xor(labelL[0]);
		labelR[0] = b;
		labelR[1] = R.xor(labelR[0]);

		int cL = a.getLSB();
		int cR = b.getLSB();

		// first half gate
		GCSignal G1 = gb.hash(labelL[0], gid, false);
		TG = G1.xor(gb.hash(labelL[1], gid, false)).xor((cR == 1) ? R : GCSignal.ZERO);
		WG = G1.xor((cL == 1) ? TG : GCSignal.ZERO);
		
		// second half gate
		G1 = gb.hash(labelR[0], gid, true);
		TE = G1.xor(gb.hash(labelR[1], gid, true)).xor(labelL[0]);
		WE = G1.xor((cR == 1) ? (TE.xor(labelL[0])) : GCSignal.ZERO);
		
		// send the encrypted gate
		try {
			Flag.sw.startGCIO();
			//TG.send(channel);
			//TE.send(channel);
			out.write(TG.bytes.length);
			//System.out.print(TG.bytes.length);
			for (int i = 0; i < TG.bytes.length; i++){
				out.write(TG.bytes[i]);
				//System.out.println(TG.bytes[i]);
			}
			out.write(TE.bytes.length);
			for (int i = 0; i < TE.bytes.length; i++)
				out.write(TE.bytes[i]);
			/*synchronized(input){
				input.notifyAll();
			}*/
			//TG.send(channel);
			//System.out.println("send TG");
			//TE.send(channel);
			//System.out.println("send TE");
			Flag.sw.stopGCIO();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// combine halves
		return WG.xor(WE);
	}
	
	/*garbling and gate*/
	public GCSignal and(GCSignal a, GCSignal b) {

		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = ((a.v && b.v)? new GCSignal(true): new GCSignal(false));
		else if (a.isPublic())
			res = a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {
			++numOfAnds;
			GCSignal ret = garble(a, b);
			gid++;
			res = ret;
			gatesRemain = true;
		}
		Flag.sw.stopGC();
		return res;
	}
}