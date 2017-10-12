package com.oblivm.backend.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import com.oblivm.backend.flexsc.CompEnv;
import com.oblivm.backend.util.GenPreRunnable;
import com.oblivm.backend.util.GenRunnable;
import com.oblivm.backend.util.InitOTEvaRunnable;
import com.oblivm.backend.util.InitOTGenRunnable;
public class AES {

	static public<T> T[] compute(CompEnv<T> gen, T[] inputA, T[] inputB){
		return new AESLib<T>(gen).readFile(inputA, inputB);
	}
	
	public static class PreGenerator<T> extends GenPreRunnable<T> {
		//FOR AES
		int input_length = 128;
		int key_length = 1408;
		//String input_file = new String("AES_input.txt");
		String key_file = new String("AES_exp_key.txt");

		T[] inputA;
		T[] inputB;
		T[] scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) throws FileNotFoundException {
			File file = new File(key_file);
			Scanner scanner = new Scanner(file);
			String a = scanner.nextLine();
			String[] content = a.split("");
			boolean[] in = new boolean[key_length];
			for(int i = 0; i < in.length; ++i){
				if( content[i].equals("0")){
					in[i] = false;
				}
				else if( content[i].equals("1") ){
					in[i] = true;
				}
				else{
					System.out.println("problem");
				}
			}
			inputA = gen.inputOfAlice(in); 
/*			for(int i = 0; i < inputA.length; ++i){
				if(inputA[i].equals(true)){
					System.out.print("1");
				}
				else if(inputA[i].equals(false)){
					System.out.print("0");
				}
			}*/
			scanner.close();
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {			
			inputB = gen.createPairOfBob(input_length, null);
			
			scResult = compute(gen, inputB, inputA);
			
			for( int i = 0; i < scResult.length; i++ ){
				gen.writeToFile(scResult[i]); 
			}
		}
	}
	
	public static class InitOTGenerator<T> extends InitOTGenRunnable<T> {
	}
	
	public static class InitOTEvaluator<T> extends InitOTEvaRunnable<T> {
	}

	public static class Generator<T> extends GenRunnable<T> {
		//FOR AES
		int input_length = 128;
		int output_length = 128;

		T[] scResult;
		
		public void prepareInputBob(CompEnv<T> gen) throws FileNotFoundException {
			gen.inputOfBob(new boolean[input_length]);
			gen.flush();
		}
	}
}
