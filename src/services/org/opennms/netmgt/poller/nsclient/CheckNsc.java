package org.opennms.netmgt.poller.nsclient;

import org.opennms.netmgt.poller.nsclient.NsclientCheckParams;
import org.opennms.netmgt.poller.nsclient.NsclientException;
import org.opennms.netmgt.poller.nsclient.NsclientManager;
import org.opennms.netmgt.poller.nsclient.NsclientPacket;
import java.util.*;

/**
 * This is an example commandline tool to perform checks against NSClient
 * services using <code>NsclientManager</code>
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 */
public class CheckNsc {

    /**
     * @param args
     *            args[0] must contain the remote host name args[1] must
     *            contain the check name (e.g. CLIENTVERSION) args[2] (crit)
     *            and args[2] (warn) must contain a numeric value args[4] must
     *            contain an empty string or a parameter related to the check
     */
    public static void main(String[] args) {
        try {
            ArrayList arguments = new ArrayList();
            for (int i = 0; i < args.length; i++) {
                arguments.add(args[i]);
            }

            if (arguments.size() < 2) {
            	usage();
            	System.exit(1);
            }
            
            String  host         = (String)arguments.remove(0);
            String  command      = (String)arguments.remove(0);
            int warningLevel     = 0;
            int criticalLevel    = 0;
            String  clientParams = "";
            
            if (!arguments.isEmpty()) {
            	warningLevel  = Integer.parseInt((String)arguments.remove(0));
            }
            
            if (!arguments.isEmpty()) {
            	criticalLevel = Integer.parseInt((String)arguments.remove(0));
            }

            /* whatever's left gets merged into "arg1&arg2&arg3" */
            if (!arguments.isEmpty()) {
            	for (int i=0; i < arguments.size(); i++) {
            		clientParams += arguments.get(i);
            		if (i < (arguments.size() - 1)) {
            			clientParams += "&";
            		}
            	}
            }
            
            int port = 1248;
            
            if (host.indexOf(":") >= 0) {
            	port = Integer.parseInt(host.split(":")[1]);
            	host = host.split(":")[0];
            }
        	
            NsclientManager client = new NsclientManager(host, port);
            NsclientPacket response = null;

            client.setTimeout(5000);
            client.init();

            NsclientCheckParams params = new NsclientCheckParams(
                                                                 warningLevel,
                                                                 criticalLevel,
                                                                 clientParams);
            response = client.processCheckCommand(
                                                  NsclientManager.convertStringToType(command),
                                                  params);
            System.out.println("NsclientPlugin: "
                    + args[1]
                    + ": "
                    + NsclientPacket.convertStateToString(response.getResultCode()) /* response.getResultCode() */
                    + " (" + response.getResponse() + ")");
        } catch (NsclientException e) {
            System.out.println("Exception: " + e.getMessage()
                    + ", root message: " + e.getCause().getMessage());
        }
    }

    private static void usage() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("usage: CheckNsc <host>[:port] <command> [[warning level] [critical level] [arg1..argn]]\n");
    	sb.append("\n");
    	sb.append("  host:           the hostname to connect to (and optionally, the port)\n");
    	sb.append("  command:        the command to run against NSClient\n");
    	sb.append("  warning level:  warn if the level is above X\n");
    	sb.append("  critical level: error if the level is above X\n");
    	sb.append("\n");
    	sb.append("  All subsequent arguments are considered arguments to the command.\n\n");
    	
    	System.out.print(sb);
    }
}