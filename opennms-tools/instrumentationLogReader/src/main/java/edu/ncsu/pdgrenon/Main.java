package edu.ncsu.pdgrenon;

import java.io.IOException;
import java.io.PrintWriter;

public class Main {

	/**
	 * @param args
	 * @throws IOException  	
	 */
	public static void main(String[] args) throws IOException {
		Collector c = new Collector();
		for(int i=0 ; i < args.length ; i++){
			c.readLogMessagesFromFile(args[i]);
		}

		PrintWriter out = new PrintWriter(System.out,true);
		
		c.printReport(out);

	}

}
