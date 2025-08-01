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
package org.opennms.protocols.nsclient;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This is an example commandline tool to perform checks against NSClient
 * services using <code>NsclientManager</code>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public abstract class CheckNsc {

    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws org.apache.commons.cli.ParseException if any.
     */
    public static void main(String[] args) throws ParseException {
    	
    	Options options = new Options();
    	options.addOption("port", true, "the port to connect to");
    	options.addOption("password", true, "the password to use when connecting");
    	options.addOption("warning", true, "treat the response as a warning if the level is above this value");
    	options.addOption("critical", true, "treat the response as a critical error if the level is above this value");

    	CommandLineParser parser = new PosixParser();
    	CommandLine cmd = parser.parse(options, args);

        @SuppressWarnings("unchecked")
    	List<String> arguments = cmd.getArgList();
    	if (arguments.size() < 2) {
    		usage(options, cmd);
    		System.exit(1);
    	}
    	
        NsclientManager client = null;
        NsclientPacket response = null;
        NsclientCheckParams params = null;
        
    	String host       = arguments.remove(0);
    	String command    = arguments.remove(0);
        int warningLevel  = 0;
        int criticalLevel = 0;
        int port          = 1248;
        
        if (cmd.hasOption("warning")) {
        	warningLevel = Integer.parseInt(cmd.getOptionValue("warning"));
        }
        if (cmd.hasOption("critical")) {
        	criticalLevel = Integer.parseInt(cmd.getOptionValue("critical"));
        }
        if (cmd.hasOption("port")) {
        	port = Integer.parseInt(cmd.getOptionValue("port"));
        }

        /* whatever's left gets merged into "arg1&arg2&arg3" */
        final StringBuilder clientParams = new StringBuilder();
        if (!arguments.isEmpty()) {
        	for (Iterator<String> i = arguments.iterator(); i.hasNext(); ) {
        		clientParams.append(i.next());
        		if (i.hasNext()) {
        			clientParams.append("&");
        		}
        	}
        }
        
        
        try {
        	client = new NsclientManager(host, port);
        }
        catch (Throwable e) {
        	usage(options, cmd, "An error occurred creating a new NsclientManager.", e);
        	System.exit(1);
        }
        
        if (cmd.hasOption("password")) {
        	client.setPassword(cmd.getOptionValue("password"));
        }

        try {
        	client.setTimeout(5000);
        	client.init();
        }
        catch (Throwable e) {
        	usage(options, cmd, "An error occurred initializing the NsclientManager.", e);
        }

        try {
        	params = new NsclientCheckParams( warningLevel, criticalLevel, clientParams.toString() );
        }
        catch (Throwable e) {
        	usage(options, cmd, "An error occurred creating the parameter object.", e);
        }

        try {
        	response = client.processCheckCommand(
                                              NsclientManager.convertStringToType(command),
                                              params);
        }
        catch(Throwable e) {
        	usage(options, cmd, "An error occurred processing the command.", e);
        }
        
        if (response == null) {
        	usage(options, cmd, "No response was returned.", null);
        } else {
            System.out.println("NsclientPlugin: "
                    + command
                    + ": "
                    + NsclientPacket.convertStateToString(response.getResultCode()) /* response.getResultCode() */
                    + " (" + response.getResponse() + ")");
        }
    }

	private static void usage(Options options, CommandLine cmd, String error, Throwable e) {
		HelpFormatter formatter = new HelpFormatter();
    	PrintWriter pw = new PrintWriter(System.out);
    	if (error != null) {
    		pw.println("An error occurred: " + error + "\n");
    	}
    	formatter.printHelp("usage: CheckNsc [options] host command [arguments]", options);
    	
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
