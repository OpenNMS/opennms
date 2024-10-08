/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.wmi;

import java.io.PrintWriter;
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
 */
public abstract class CheckWmi {

	/**
	 * <p>main</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws org.apache.commons.cli.ParseException if any.
	 */
    public static void main(final String[] args) throws ParseException {
	    final Options options = new Options();
		options.addOption("domain", true, "the NT/AD domain the credentials belong to");
		options.addOption("wmiClass", true, "the object class in WMI to query");
		options.addOption("wmiNamespace", true, "the namespace in WMI to use (default: " + WmiParams.WMI_DEFAULT_NAMESPACE + ")");
		options.addOption("wmiObject", true, "the object to query in WMI");
		options.addOption("wmiWql", true, "the query string to execute in WMI");
		options.addOption("op", true, "compare operation: NOOP, EQ, NEQ, GT, LT");
		options.addOption("value", true, "the value to compare to");
		options.addOption("matchType", true, "type of matching for multiple results: all, none, some, one");

		final CommandLineParser parser = new PosixParser();
		final CommandLine cmd = parser.parse(options, args);

	    @SuppressWarnings("unchecked")
		List<String> arguments = (List<String>)cmd.getArgList();
		if (arguments.size() < 3) {
			usage(options, cmd);
			System.exit(1);
		}

		final String host = arguments.remove(0);
		final String user = arguments.remove(0);
		final String pass = arguments.remove(0);

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
		
		String wmiNamespace = WmiParams.WMI_DEFAULT_NAMESPACE;
		if (cmd.hasOption("wmiNamespace")) {
		    wmiNamespace = cmd.getOptionValue("wmiNamespace");
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
			List<Object> wmiObjects;
			// Create the check parameters holder.
			WmiParams clientParams;
            if(wmiWql == null || wmiWql.length() == 0)
                clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF, compVal, compOp, wmiClass, wmiObject);
            else
                clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL, compVal, compOp, wmiWql, wmiObject);
			// Create the WMI Manager
            final WmiManager mgr = new WmiManager(host, user, pass, domain, matchType);
            mgr.setNamespace(wmiNamespace);
            
			// Connect to the WMI server.
			mgr.init();

			// Perform the operation specified in the parameters.
			final WmiResult result = mgr.performOp(clientParams);
			// And retrieve the WMI objects from the results.
			wmiObjects = result.getResponse();

			// Now output a brief report of the check results.
			System.out.println("Checking: " + wmiWql + " for " + wmiObject + " Op: " + compOp + " Val: " + compVal);
			System.out.println("Check results: " + WmiResult.convertStateToString(result.getResultCode()) + " (" + wmiObjects.size() + ")");
            
			for (int i = 0; i < wmiObjects.size(); i++) {
				System.out.println("Result for (" + (i + 1) + ") " + wmiClass + "\\" + wmiObject + ": " + wmiObjects.get(i));
			}

			// Disconnect when we're done.
			mgr.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
	    final HelpFormatter formatter = new HelpFormatter();
	    final PrintWriter pw = new PrintWriter(System.out);
		if (error != null) {
			pw.println("An error occurred: " + error + "\n");
		}
		formatter.printHelp("usage: CheckWmi [options] <host> <username> <password>", options);

		if (e != null) {
			pw.println(e.getMessage());
			e.printStackTrace(pw);
		}

		pw.close();
	}

	private static void usage(final Options options, final CommandLine cmd) {
		usage(options, cmd, null, null);
	}

}
