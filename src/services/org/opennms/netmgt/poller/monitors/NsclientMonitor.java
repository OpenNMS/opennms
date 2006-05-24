package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;
import java.lang.Object;
import java.util.regex.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;


/* BACKPORTING TO STABLE, HEAD IS NOT STABLE
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.utils.ParameterMap;
*/


import org.opennms.netmgt.poller.nsclient.NsclientCheckParams;
import org.opennms.netmgt.poller.nsclient.NsclientException;
import org.opennms.netmgt.poller.nsclient.NsclientPacket;
import org.opennms.netmgt.poller.nsclient.NsclientManager;

/**
 * This class is designed to be used by the service poller framework to test
 * the availability of a generic TCP service on remote interfaces. The class
 * implements the ServiceMonitor interface that allows it to be used along
 * with other plug-ins by the service poller framework.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class NsclientMonitor extends IPv4LatencyMonitor {
    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000;

    /**
     * Poll the specified address for service availability. During the poll an
     * attempt is made to connect on the specified port. If the connection
     * request is successful, the parameters are parsed and turned into
     * <code>NsclientCheckParams</code> and a check is performed against the
     * remote NSClient service. If the <code>NsclientManager</code> responds
     * with a <code>NsclientPacket</code> containing a result code of
     * <code>NsclientPacket.RES_STATE_OK</code> then we have determined that
     * we are talking to a valid service and we set the service status to
     * ServiceMonitor.SERVICE_AVAILABLE and return.
     * 
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used
     *            for this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * @throws java.lang.RuntimeException
     *             Thrown if the interface experiences errors during the poll.
    public PollStatus poll(MonitoredService svc, Map parameters,
            org.opennms.netmgt.config.poller.Package pkg) {
     */
    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {

        // Holds the response reason.
        String reason = null;
	int val1 = 0;
        // Used to exit the retry loop early, if possible.
        int serviceStatus = ServiceMonitor.SERVICE_UNRESPONSIVE;
        // This will hold the data the server sends back.
        NsclientPacket response = null;
        // Used to track how long the request took.
        long responseTime = -1;

	// NetworkInterface iface = svc.getNetInterface();
        Category log = ThreadCategory.getInstance(getClass());

        // Validate the interface type.
        if (iface.getType() != NetworkInterface.TYPE_IPV4) {
            throw new NetworkInterfaceNotSupportedException(
                                                            "Unsupported interface type, only TYPE_IPV4 currently supported");
        }

        // NSClient related parameters.
        String command = ParameterMap.getKeyedString(
                                                     parameters,
                                                     "command",
                                                     NsclientManager.convertTypeToString(NsclientManager.CHECK_CLIENTVERSION));
        int port = ParameterMap.getKeyedInteger(parameters, "port",
                                                NsclientManager.DEFAULT_PORT);
        String params = ParameterMap.getKeyedString(parameters, "parameter",
                                                    null);
        int critPerc = ParameterMap.getKeyedInteger(parameters,
                                                    "criticalPercent", 0);
        int warnPerc = ParameterMap.getKeyedInteger(parameters,
                                                    "warningPercent", 0);

        // Connection related parameters.
        int retry = ParameterMap.getKeyedInteger(parameters, "retry",
                                                 DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout",
                                                   DEFAULT_TIMEOUT);

        // Response Graph related parameters.
        String rrdPath = ParameterMap.getKeyedString(parameters,
                                                     "rrd-repository", null);
        String dsName = ParameterMap.getKeyedString(parameters, "ds-name",
                                                    null);

        // Validate the graph-related values.
        if (rrdPath == null) {
            log.info("poll: RRD repository not specified in parameters, latency data will not be stored.");
        }
        if (dsName == null) {
            dsName = DS_NAME;
        }

        // Get the address we're going to poll.
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        for (int attempts = 0; attempts <= retry
                && serviceStatus != ServiceMonitor.SERVICE_AVAILABLE; attempts++) {
            try {
                // Get the time, so we can keep track of how long the request
                // took.
                long sentTime = System.currentTimeMillis();

                // Create a client, set up details and connect.
                NsclientManager client = new NsclientManager(
                                                             ipv4Addr.getHostAddress(),
                                                             port);
                client.setTimeout(timeout);
                client.init();

                // Set up the parameters the client will use to validate the
                // response.
                NsclientCheckParams clientParams = new NsclientCheckParams(
                                                                           critPerc,
                                                                           warnPerc,
                                                                           params);

                // Send the request to the server and receive the response.
                response = client.processCheckCommand(
                                                      NsclientManager.convertStringToType(command),
                                                      clientParams);
                // Now save the time it took to process the check command.
                responseTime = System.currentTimeMillis() - sentTime;

                if (response == null) {
                    continue;
                }

                if (response.getResultCode() == NsclientPacket.RES_STATE_OK) {
                    serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
		    log.debug("reason: " + reason );
                    reason = response.getResponse();
		    // We will receive something like String s = "0.00000"

		    Pattern decimal = Pattern.compile("^[+-]?(?:(\\d+)(?:\\.(\\d*))+|\\.(\\d+))$");
		    Matcher m = decimal.matcher(reason);
		
		    if ((m = decimal.matcher(reason)).matches()) {
			 log.debug(reason + " ... See decimal! " );
  		         log.debug(reason + " Split " + m.group(1) + " Second " + m.group(2) + "  ... has a decimal point.");
			 int val = Integer.parseInt( m.group(2) );
			 val1 = Integer.parseInt( m.group(1) ) ;
			 if ( val > 5 ) {
				val1 = val1  + 1;
			 }		         	 
                    }
						

                    // Store response time in RRD
                    if (responseTime >= 0 && rrdPath != null) {

                        try {
			    this.updateRRD(rrdPath, ipv4Addr, dsName, val1, pkg);
    			    //this.updateRRD(rrdPath, ipv4Addr, dsName, spanker, pkg);
			    log.debug("Wrote to JE-RRD: " + reason );

                        } catch (RuntimeException rex) {
                            log.debug("There was a problem writing the RRD:"
                                    + rex);
                        }
                    }
                } else if (response.getResultCode() == NsclientPacket.RES_STATE_WARNING) {
                    serviceStatus = ServiceMonitor.SERVICE_UNRESPONSIVE;
                    reason = response.getResponse();
                } else if (response.getResultCode() == NsclientPacket.RES_STATE_CRIT) {
                    serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
                    reason = response.getResponse();
                }

            } catch (NsclientException e) {
                log.debug("Nsclient Poller received exception from client: "
                        + e.getMessage());
                reason = "NsclientException: " + e.getMessage();
            }
        } // end for(;;)
        log.debug("JE poller: " + reason);
        return serviceStatus;

    }
}
