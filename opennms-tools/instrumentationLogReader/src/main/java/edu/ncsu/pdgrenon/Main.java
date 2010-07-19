package edu.ncsu.pdgrenon;

import java.io.PrintWriter;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	Collector c = new Collector();
	c.printMessageTypeCounts(new PrintWriter(System.out,true));

	}

}
