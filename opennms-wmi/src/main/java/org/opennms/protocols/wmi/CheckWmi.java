//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.protocols.wmi;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This is an example command-line tool to perform checks against WMI
 * services using <code>WmiClient</code>
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 */
public class CheckWmi {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("domain", true,
				"the NT/AD domain the credentials belong to");
		options.addOption("wmiClass", true, "the object class in WMI to query");
		options.addOption("wmiObject", true, "the object to query in WMI");
        options.addOption("wmiWql", true, "the query string to execute in WMI");
		options.addOption("op", true,
				"compare operation: NOOP, EQ, NEQ, GT, LT");
		options.addOption("value", true, "the value to compare to");
		options.addOption("matchType", true,
				"type of matching for multiple results: all, none, some, one");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		List<String> arguments = cmd.getArgList();
		if (arguments.size() < 2) {
			usage(options, cmd);
			System.exit(1);
		}

		String host = arguments.remove(0);
		String user = arguments.remove(0);
		String pass = arguments.remove(0);

		String wmiClass = "";
		if (cmd.hasOption("wmiClass")) {
			wmiClass = cmd.getOptionValue("wmiClass");
		}/* else {
			usage(options, cmd);
			System.exit(1);
		}*/

		String wmiObject = "";
		if (cmd.hasOption("wmiObject")) {
			wmiObject = cmd.getOptionValue("wmiObject");
		} else {
			usage(options, cmd);
			System.exit(1);
		}
        
        String wmiWql = "";
        if (cmd.hasOption("wmiWql")) {
            wmiWql = cmd.getOptionValue("wmiWql");
        } /*else {
            usage(options, cmd);
            System.exit(1);
        } */

		String compVal = "";
		if (cmd.hasOption("value")) {
			compVal = cmd.getOptionValue("value");
		} else {
			usage(options, cmd);
			System.exit(1);
		}

		String compOp = "";
		if (cmd.hasOption("op")) {
			compOp = cmd.getOptionValue("op");
		} else {
			usage(options, cmd);
			System.exit(1);
		}

		String domain = "";
		if (cmd.hasOption("domain")) {
			domain = cmd.getOptionValue("domain");
		}

		String matchType = "all";
		if (cmd.hasOption("matchType")) {
			matchType = cmd.getOptionValue("matchType");
		}

		try {
			// Hold the WMI objects from the results.
			ArrayList<Object> wmiObjects;
			// Create the check parameters holder.
			WmiParams clientParams;
            if(wmiWql == null || wmiWql.length() == 0)
                clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF, compVal, compOp, wmiClass,
					wmiObject);
            else
                clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL, compVal, compOp, wmiWql,
                                            wmiObject);
			// Create the WMI Manager
			WmiManager mgr = new WmiManager(host, user, pass, domain, matchType);

			// Connect to the WMI server.
			mgr.init();

			// Perform the operation specified in the parameters.
			WmiResult result = mgr.performOp(clientParams);
			// And retrieve the WMI objects from the results.
			wmiObjects = result.getResponse();
			
			// Now output a brief report of the check results.
			System.out.println("Checking: " + wmiWql + " for " + wmiObject
					+ " Op: " + compOp + " Val: " + compVal);
			System.out.println("Check results: "
					+ WmiResult.convertStateToString(result.getResultCode())
					+ " (" + wmiObjects.size() + ")");
            
			for (int i = 0; i < wmiObjects.size(); i++) {
				System.out.println("Result for (" + (i + 1) + ") " + wmiClass
						+ "\\" + wmiObject + ": " + wmiObjects.get(i));
			}

			// Disconnect when we're done.
			mgr.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void usage(Options options, CommandLine cmd, String error,
			Exception e) {
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.out);
		if (error != null) {
			pw.println("An error occurred: " + error + "\n");
		}
		formatter.printHelp(
				"usage: CheckWmi [options] <host> <username> <password>",
				options);

		if (e != null) {
			pw.println(e.getMessage());
			e.printStackTrace(pw);
		}

		pw.close();
	}

	private static void usage(Options options, CommandLine cmd) {
		usage(options, cmd, null, null);
	}

}
