package org.opennms.netmgt.poller.nsclient;

import org.opennms.netmgt.poller.nsclient.NsclientCheckParams;
import org.opennms.netmgt.poller.nsclient.NsclientException;
import org.opennms.netmgt.poller.nsclient.NsclientManager;
import org.opennms.netmgt.poller.nsclient.NsclientPacket;

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
            NsclientManager client = new NsclientManager(args[0], 1248);
            NsclientPacket response = null;

            client.setTimeout(5000);
            client.init();

            String param = "";
            try {
                param = args[4];
            } catch (ArrayIndexOutOfBoundsException e) {
                // don't do anything.
            }

            NsclientCheckParams params = new NsclientCheckParams(
                                                                 Integer.parseInt(args[2]),
                                                                 Integer.parseInt(args[3]),
                                                                 param);
            response = client.processCheckCommand(
                                                  NsclientManager.convertStringToType(args[1]),
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

}
