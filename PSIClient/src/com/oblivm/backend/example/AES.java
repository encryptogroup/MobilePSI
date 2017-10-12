package com.oblivm.backend.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.gc.GCSignal;
import com.oblivm.backend.util.EvaRunnable;
import com.oblivm.backend.util.InitOTEvaRunnable;
import com.oblivm.backend.util.InitOTGenRunnable;

public class AES {

	static public<T> T[] compute(CompEnv<T> gen, T[] inputA, T[] inputB){
		return new AESLib<T>(gen).readFile(inputA, inputB);
	}
	
	public static class InitOTGenerator<T> extends InitOTGenRunnable<T> {
	}
	
	public static class InitOTEvaluator<T> extends InitOTEvaRunnable<T> {
	}
	public static class Evaluator<T> extends EvaRunnable<T> {
		//FOR AES
		int key_length = 1408;
		
		T[] inputA;
		T[] inputB;
		T[] scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) throws FileNotFoundException {
			inputA = gen.inputOfAlice(new boolean[key_length]);
		}
		
		@Override
		public void prepareInputBob(CompEnv<T> gen) throws FileNotFoundException {
		//	String[] content = to_be_encrypted.split("");
			boolean[] in = new boolean[input_length];
			for(int i = 0; i < in.length; ++i){
				if( to_be_encrypted.charAt(i) == '0' ){
					in[i] = false;
				}
				else if( to_be_encrypted.charAt(i) == '1') {
					in[i] = true;
				}
				else{
					System.out.println("problem " + to_be_encrypted.charAt(i));
				}
			}
			gen.flush();
			inputB = gen.inputOfBob(in);
			System.out.println();
			System.out.println();
			for(int i = 0; i < inputB.length; ++i){
				if(inputB[i].equals(true)){
					System.out.print("1");
				}
				else if(inputB[i].equals(false)){
					System.out.print("0");
				}
			}
		}
		
		@Override
		public byte[][] getInputsBob() {
			byte[][] ret = new byte[input_length][];
			for (int i = 0; i < input_length; i ++) {
				GCSignal signal = ((GCSignal) inputB[i]);
				ret[i] = signal.bytes;
			}
			
			return ret;
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			scResult = compute(gen, inputB, inputA);
		}
		
		@Override
		public void prepareOutput(CompEnv<T> gen) {
	//		System.out.println("----- OUTPUT -----");
			boolean[] tmp = gen.outputToBob(scResult);
			result = com.example.psiclient.Utils.booleansToBytes(tmp);
	//		System.out.println("Evaluator result");
	/*		for(int i = 0; i < tmp.length; ++i){
				if(tmp[i]){
					System.out.print("1");
				}
				else{
					System.out.print("0");
				}
			}
			System.out.println();*/
		}
	}
}
