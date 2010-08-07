package edu.ncsu.pdgrenon;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Main {

	/**
	 * @param args
	 * @throws IOException  	
	 */
	public static void main(String[] args) throws IOException {
		main(args, System.out);
	}
	public static void main(String[] args, OutputStream out) throws IOException {
		Collector c = new Collector();
		for(int i=0 ; i < args.length ; i++){
			c.readLogMessagesFromFile(args[i]);
		}

		PrintWriter writer = new PrintWriter(out,true);
		
		c.printReport(writer);

	}

}
