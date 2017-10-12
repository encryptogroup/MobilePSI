package com.oblivm.backend.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.*;

import com.oblivm.backend.flexsc.CompEnv;

import android.os.Environment;

public class AESLib<T> {
	public CompEnv<T> env;
	public final T SIGNAL_ZERO;
	public final T SIGNAL_ONE;
	
	String circuit_file = new String("AES-SHDL.txt");
	int output_length = 128;
	int input_length = 128;
	int key_length = 1408;
	
	public AESLib(CompEnv<T> e) {
		env = e;
		SIGNAL_ZERO = e.ZERO();
		SIGNAL_ONE = e.ONE();
	}	
	
	public T and(T x, T y) {
		assert (x != null && y != null) : "AESLib.and: bad inputs";

		return env.and(x, y);
	}

	public T xor(T x, T y) {
		assert (x != null && y != null) : "AESLib.xor: bad inputs";

		return env.xor(x, y);
	}

	public T not(T x) {
		assert (x != null) : "AESLib.not: bad input";

		return env.xor(x, SIGNAL_ONE);
	}
	
	public T[] readFile(T[] x, T[] y){
		File file = new File(Environment.getExternalStorageDirectory(), "/AES-SHDL.txt");
		Scanner scanner;
		Map<Integer, T> wires = new HashMap<Integer, T>();
		T elem1;
		T elem2;
		int index;
		int index1;
		int index2;
		int outind;
		int i = 0;
		T[] result = env.newTArray(output_length);
		int index_output;
		int index_for_output = 0;
		boolean is_output;
		try {
			scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				index_output = 0; //starts as 0 all the time
				is_output = false;
				String a = scanner.nextLine();
		//		System.out.println(a);
				String[] content = a.split(" ");
				
				if( content[1].equals("input") ){
					index = Integer.parseInt(content[0]);
					if(i < input_length){
						wires.put(index, x[i]);
						//System.out.println(index + " " + wires.get(index));
					}
					else{
						wires.put(index, y[i - input_length]);
						//System.out.println(index + " " + wires.get(index));
					}
					i++;
				}
				else if( content[1].equals("output") ){
					//System.out.println(content[0]);
					index_output++;
					is_output = true;
				}
				
				if( content[index_output + 1].equals("gate") && content[index_output + 3].equals("2") ){
					index = Integer.parseInt(content[0]);
					index1 = Integer.parseInt(content[index_output + 13]);
					index2 = Integer.parseInt(content[index_output + 14]);
					elem1 = wires.get(index1);
					elem2 = wires.get(index2);
					//System.out.print(elem1 + "   " + elem2);
					if( content[index_output + 6].equals("0") && content[index_output + 7].equals("1") && 
						content[index_output + 8].equals("1") && content[index_output + 9].equals("0") ){
						wires.put( index, xor( elem1, elem2 ));
						//System.out.println(index + " " + wires.get(index1) + " XOR " + wires.get(index2) + " = " + wires.get(index));
					}
					else if( content[index_output + 6].equals("1") && content[index_output + 7].equals("0") && 
						content[index_output + 8].equals("0") && content[index_output + 9].equals("1") ) { 
						wires.put( index, not(xor(elem1, elem2)) );
						//System.out.println(index1 + " NXOR" + index2 + " " + index);
					}
					else if( content[index_output + 6].equals("0") && content[index_output + 7].equals("0") && 
						content[index_output + 8].equals("0") && content[index_output + 9].equals("1") ) { 
						wires.put( index, and(elem1, elem2) );
						//System.out.println(index + " " + wires.get(index1) + " AND " + wires.get(index2) + " = " + wires.get(index));
					}
					else if( content[index_output + 6].equals("0") && content[index_output + 7].equals("0") && 
						content[index_output + 8].equals("1") && content[index_output + 9].equals("0") ) { 
						wires.put( index, and(elem1, not(elem2)) );
						//System.out.println(index1 + " AND" + index2 + " NOT " + index);
					}
					else if( content[index_output + 6].equals("0") && content[index_output + 7].equals("1") && 
						content[index_output + 8].equals("0") && content[index_output + 9].equals("0") ) { 
						wires.put( index, and(not(elem1), elem2) );
						//System.out.println("NOT " + index1 + " AND" + index2 + " " + index);
					}
					else if( content[index_output + 6].equals("1") && content[index_output + 7].equals("0") && 
						content[index_output + 8].equals("0") && content[index_output + 9].equals("0") ) { 
						wires.put( index, and(not(elem1), not(elem2)) );
						//System.out.println("NOT " + index1 + " AND NOT" + index2 + " " + index);
					}
					else{
						System.out.println("PROBLEM NO GOOD GATE");
					}
					
					if( is_output ){
						result[index_for_output] = wires.get(index);
						index_for_output++;
					}
				}
				else if( content[index_output + 1].equals("gate") && content[index_output + 3].equals("1") ){
					index = Integer.parseInt(content[0]);
					index1 = Integer.parseInt(content[index_output + 11]);
					elem1 = wires.get(index1);
					if( content[index_output + 6].equals("1") && content[index_output + 7].equals("0") ) { 
						wires.put( index, not(elem1) );
						//System.out.println(index + " " + wires.get(index1) + " INV = " + wires.get(index));
					}
					else if( content[index_output + 6].equals("0") && content[index_output + 7].equals("1") ) { 
						wires.put( index, elem1 );
						//System.out.println(index + " " + wires.get(index1) + " ID = " + wires.get(index));
					}
					if( is_output ){
						result[index_for_output] = wires.get(index);
						index_for_output++;
					}
				}
				else if( content[0].equals("outputs") ){
					for( int j = 0; j < output_length; ++j ){
						outind = Integer.parseInt(content[j + 1]);
						//System.out.println(outind);
						result[j] = wires.get(outind);
					}
				}
			}
			scanner.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
