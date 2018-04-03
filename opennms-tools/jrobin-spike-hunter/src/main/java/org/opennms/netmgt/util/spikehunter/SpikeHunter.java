/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * 
 */
package org.opennms.netmgt.util.spikehunter;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.jrobin.core.Archive;
import org.jrobin.core.FetchData;
import org.jrobin.core.Robin;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;

/**
 * @author Jeff Gehlbach <jeffg@opennms.org>
 *
 */
public class SpikeHunter {
	static RrdDb m_rrdFile;
	
	static String m_rrdFileName;		// RRD file to open
	static String m_dsNames;			// Data source names, e.g. "ifInOctets,ifOutOctets"
	
	static int m_analysisStrategy;	// Maps into ANALYSIS_STRATEGIES enum
	static List<Double> m_operands;	// Passed into the chosen analysis strategy
	static int m_replacementStrategy;	// Maps into REPLACEMENT_STRATEGIES enum
	static boolean m_dryRun;
	static boolean m_dumpContents;
	static boolean m_quiet;
	static boolean m_verbose;
	
	protected static Options m_options = new Options();
	protected static CommandLine m_commandLine;
	private static PrintStream m_out;
	
	private static enum ANALYSIS_STRATEGIES {
		PERCENTILE_STRATEGY
	}
	
	private static enum REPLACEMENT_STRATEGIES {
		NAN_STRATEGY,
		PREVIOUS_STRATEGY,
		NEXT_STRATEGY
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		m_out = System.out;
		try {
			parseCmdLine(args);
		} catch (Throwable e) {
			System.out.println("Error parsing command line arguments: " + e.getMessage());
		}
		try {
			m_rrdFile = openRrd();
		} catch (IOException ioe) {
			System.out.println("IO Exception trying to open RRD file: " + ioe.getMessage());
			System.exit(-1);
		} catch (RrdException rrde) {
			System.out.println("RRD Exception trying to open RRD file: " + rrde.getMessage());
			System.exit(-1);
		}
		if (m_dumpContents) {
			dumpContents();
			System.exit(0);
		}
		doReplacement();
		closeRrd();
	}
	
	public static void parseCmdLine(String[] argv) throws Exception {
		m_options.addOption("h", "help", false, "This help text");
		m_options.addOption("f", "file", true, "JRobin disk file on which to operate");
		m_options.addOption("d", "ds-name", true, "Data source names on which to operate, comma-separated. If unspecified, operate on all DSes.");
		m_options.addOption("a", "analysis-strategy", true, "Data analysis strategy. Defaults to percentile.");
		m_options.addOption("o", "operands", true, "Operands (numeric, comma-separated) for the selected analysis strategy. Defaults to 95,5.");
		m_options.addOption("r", "replacement-strategy", true, "Strategy for replacing spike samples, one of nan|previous|next, defaults to nan");
		m_options.addOption("n", "dry-run", false, "Just report spikes, do not make any changes to the JRobin disk file.");
		m_options.addOption("p", "dump-contents", false, "Just dump the DSes and RRAs in the JRobin disk file.");
		m_options.addOption("q", "quiet", false, "Do not print any informational output");
		m_options.addOption("v", "verbose", false, "Print plenty of informational output");
		
		CommandLineParser parser = new PosixParser();
        m_commandLine = parser.parse(m_options, argv);
        if (m_commandLine.hasOption("h")) {
        	usage(m_options, m_commandLine);
        	System.exit(0);
        }
        
        Map<String,Integer> analysisStrategies = new HashMap<String,Integer>();
        analysisStrategies.put("percentile", ANALYSIS_STRATEGIES.PERCENTILE_STRATEGY.ordinal());
        
        Map<String,Integer> replacementStrategies = new HashMap<String,Integer>();
        replacementStrategies.put("nan", REPLACEMENT_STRATEGIES.NAN_STRATEGY.ordinal());
        replacementStrategies.put("previous", REPLACEMENT_STRATEGIES.PREVIOUS_STRATEGY.ordinal());
        replacementStrategies.put("next", REPLACEMENT_STRATEGIES.NEXT_STRATEGY.ordinal());
        
        m_rrdFileName = m_commandLine.getOptionValue("f");
        m_dsNames = m_commandLine.getOptionValue("d", null);
        
        m_operands = new ArrayList<>();
        for (String operandStr : m_commandLine.getOptionValue("o", "95,5").split(",")) {
        	m_operands.add(Double.parseDouble(operandStr));
        }
        
        m_analysisStrategy = analysisStrategies.get(m_commandLine.getOptionValue("l", "percentile").toLowerCase());
        m_replacementStrategy = replacementStrategies.get(m_commandLine.getOptionValue("r", "nan").toLowerCase());
        
        m_dryRun = m_commandLine.hasOption("n");
        m_dumpContents = m_commandLine.hasOption("p");
        m_quiet = m_commandLine.hasOption("q");
        m_verbose = m_commandLine.hasOption("v");
	}

	private static void usage(Options options, CommandLine cmd) {
		usage(options, cmd, null, null);
	}

    private static void usage(Options options, CommandLine cmd, String error, Exception e) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(m_out);

        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }

        formatter.printHelp("usage: spike-hunter [options]", options);

        if (e != null) {
            pw.println(e.getMessage());
            e.printStackTrace(pw);
        }
        
        pw.close();
        
        System.exit(0);
    }
    
    public static void printToUser(String msg) {
    	if (m_quiet) {
    		return;
    	}
    	m_out.println(msg);
    }
    
	private static RrdDb openRrd() throws IOException, RrdException {
		return new RrdDb(m_rrdFileName, m_dryRun);
	}
	
	private static void closeRrd() {
		try {
			m_rrdFile.close();
		} catch (IOException ioe) {
			System.out.println("IO Exception trying to close RRD file: " + ioe.getMessage());
		}
	}
    
    private static void dumpContents() {
    	System.out.println("Number of archives: " + m_rrdFile.getArcCount());
    	for (int i = 0; i < m_rrdFile.getArcCount(); i++) {
    		org.jrobin.core.Archive arc = m_rrdFile.getArchive(i);
    		String consolFun = "";
    		double xff = Double.NaN;
    		long arcStep = 0;
    		int steps = 0, rows = 0;
			try {
				consolFun = arc.getConsolFun();
				xff = arc.getXff();
				arcStep = arc.getArcStep();
				steps = arc.getSteps();
				rows = arc.getRows();
			} catch (IOException e) {
				System.out.println("IO Exception trying to dump RRD file contents: " + e.getMessage());
			}
    		System.out.println("\t" + consolFun + ":" + xff + ":" + steps + ":" + rows + " (Step size: " + arcStep + ")");
    	}
    	System.out.println();
    	System.out.println("Number of data sources: " + m_rrdFile.getDsCount());
    	try {
			for (String dsName : m_rrdFile.getDsNames()) {
				System.out.println("\t" + dsName);
			}
		} catch (IOException e) {
			System.out.println("IO Exception trying to enumerate data source names: " + e.getMessage());
		}
    }
    
    private static DataAnalyzer getDataAnalyzer() {
    	DataAnalyzer analyzer = new PercentileDataAnalyzer(m_operands);
    	return analyzer;
    }
    
    private static DataReplacer getDataReplacer() {
    	DataReplacer replacer;
    	if (m_replacementStrategy == REPLACEMENT_STRATEGIES.PREVIOUS_STRATEGY.ordinal()) {
    		replacer = new PreviousDataReplacer();
    	} else if (m_replacementStrategy == REPLACEMENT_STRATEGIES.NEXT_STRATEGY.ordinal()) {
    		replacer = new NextDataReplacer();
    	} else {
    		replacer = new NanDataReplacer();
    	}
    	return replacer;
    }
    
    private static void doReplacement() {
		if (m_dryRun) {
			printToUser("Running in dry-run mode, no modifications will be made to the specified file");
		}
    	int arcCount = m_rrdFile.getArcCount();
    	for (int arcIndex = 0; arcIndex < arcCount; arcIndex++) {
    		org.jrobin.core.Archive thisArc = m_rrdFile.getArchive(arcIndex);
    		replaceInArchive(thisArc);
    	}
    }
    	
	private static void replaceInArchive(org.jrobin.core.Archive arc) {
		String consolFun = "";
		int arcSteps = 0;
		long startTime = 0;
		long endTime = 0;
		FetchData data = null;
		Robin robin = null;

		try {
			consolFun = arc.getConsolFun();
			arcSteps = arc.getSteps();
			startTime = arc.getStartTime();
			endTime = arc.getEndTime();
		} catch (IOException e) {
			System.out.println("IO Exception trying to get archive information from RRD file: " + e.getMessage());
			System.exit(-1);
		}
		printToUser("Operating on archive with CF " + consolFun + ", " + arcSteps + " steps");

		try {
			data = m_rrdFile.createFetchRequest(consolFun, startTime, endTime).fetchData();
		} catch (RrdException rrde) {
			System.out.println("RRD Exception trying to create fetch request: " + rrde.getMessage());
			System.exit(-1);
		} catch (IOException ioe) {
			System.out.println("IO Exception trying to create fetch request: " + ioe.getMessage());
			System.exit(-1);
		}
		
		String[] dsNames;
		if (m_dsNames == null) {
			dsNames = data.getDsNames();
		} else {
			dsNames = m_dsNames.split(","); 
		}
		for (String dsName : dsNames) {
			replaceInDs(arc, data, dsName);
		}
	}
	
	private static void replaceInDs(Archive arc, FetchData data, String dsName) {
		printToUser(" Operating on DS " + dsName);
		double[] origValues = null;
		try {
			origValues = data.getValues(dsName);
		} catch (RrdException e) {
			System.out.println("RRD Exception trying to get values from RRD file: " + e.getMessage());
			System.exit(-1);
		}
		DataAnalyzer analyzer = getDataAnalyzer();
		analyzer.setVerbose(m_verbose);
		List<Integer> violatorIndices = analyzer.findSamplesInViolation(origValues);
		if (m_verbose) {
			printToUser(" Number of values: " + origValues.length);
			printToUser(" Data analyzer: " + analyzer);
			printToUser(" Samples found in violation: " + violatorIndices.size());
		}
		DataReplacer replacer = getDataReplacer();
		double[] newValues = replacer.replaceValues(origValues, violatorIndices);
		printReplacementsToUser(data, dsName, newValues, violatorIndices);
		if (!m_dryRun) {
			replaceInFile(arc, data, dsName, newValues, violatorIndices);
		}
	}
	
	private static void printReplacementsToUser(FetchData data, String dsName, double[] newValues, List<Integer> violatorIndices) {
		long timestamps[] = data.getTimestamps();
		double origValues[] = null;
		try {
			origValues = data.getValues(dsName);
		} catch (RrdException e) {
			System.out.println("RRD Exception trying to get values from RRD file: " + e.getMessage());
		}
		for (int i : violatorIndices) {
			Date sampleDate = new Date(timestamps[i] * 1000);
			printToUser("   Sample with timestamp " + sampleDate + " and value " + origValues[i] + " replaced by value " + newValues[i]);
		}
	}
	
	private static void replaceInFile(Archive arc, FetchData data, String dsName, double[] newValues, List<Integer> violatorIndices) {
		Robin robin = null;
		try {
			robin = arc.getRobin(m_rrdFile.getDsIndex(dsName));
		} catch (RrdException rrde) {
			System.out.println("RRD Exception trying to retrieve Robin from RRD file: " + rrde.getMessage());
			System.exit(-1);
		} catch (IOException ioe) {
			System.out.println("RRD Exception trying to retrieve Robin from RRD file: " + ioe.getMessage());
			System.exit(-1);
		}
		//m_rrdFile.getArchive(int arcIndex)
		//m_rrdFile.getArchive(String consolFun, int steps)
		for (int i : violatorIndices) {
			try {
				robin.setValue(i, newValues[i]);
			} catch (IOException e) {
				System.out.println("IO Exception trying to set value for index " + i + " to " + newValues[i]);
			}
		}
	}
}
