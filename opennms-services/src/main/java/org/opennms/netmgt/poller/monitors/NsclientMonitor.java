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
package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.poller.nsclient.NSClientAgentConfig;
import org.opennms.netmgt.poller.nsclient.NsclientCheckParams;
import org.opennms.netmgt.poller.nsclient.NsclientException;
import org.opennms.netmgt.poller.nsclient.NsclientManager;
import org.opennms.netmgt.poller.nsclient.NsclientPacket;

/**
 * This class is designed to be used by the service poller framework to test
 * the availability of a generic TCP service on remote interfaces. The class
 * implements the ServiceMonitor interface that allows it to be used along
 * with other plug-ins by the service poller framework.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
public class NsclientMonitor extends IPv4Monitor {
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
     * SERVICE_AVAILABLE and return.
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used
     *            for this poll.
     * @param iface
     *            The network interface to test the service on.
     * 
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * @throws java.lang.RuntimeException
     *             Thrown if the interface experiences errors during the poll.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // Holds the response reason.
        String reason = null;
        // Used to exit the retry loop early, if possible.
        int serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;
        // This will hold the data the server sends back.
        NsclientPacket response = null;
        // Used to track how long the request took.
        Double responseTime = null;

        NetworkInterface iface = svc.getNetInterface();
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
        
        String password = ParameterMap.getKeyedString(parameters, "password", NSClientAgentConfig.DEFAULT_PASSWORD);
        String params = ParameterMap.getKeyedString(parameters, "parameter",
                                                    null);
        int critPerc = ParameterMap.getKeyedInteger(parameters,
                                                    "criticalPercent", 0);
        int warnPerc = ParameterMap.getKeyedInteger(parameters,
                                                    "warningPercent", 0);

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);


        // Get the address we're going to poll.
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        for (tracker.reset(); tracker.shouldRetry() && serviceStatus != PollStatus.SERVICE_AVAILABLE; tracker.nextAttempt()) {
            try {
                
                tracker.startAttempt();

                // Create a client, set up details and connect.
                NsclientManager client = new NsclientManager(
                                                             ipv4Addr.getHostAddress(),
                                                             port, password);
                client.setTimeout(tracker.getSoTimeout());
                client.setPassword(password);
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
                responseTime = tracker.elapsedTimeInMillis();

                if (response == null) {
                    continue;
                }

                if (response.getResultCode() == NsclientPacket.RES_STATE_OK) {
                    serviceStatus = PollStatus.SERVICE_AVAILABLE;
                    reason = response.getResponse();
                } else if (response.getResultCode() == NsclientPacket.RES_STATE_CRIT) {
                    serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
                    reason = response.getResponse();
                    // set this to null so we don't try to save data when the node is down
                    responseTime = null;
                }

            } catch (NsclientException e) {
                log.debug("Nsclient Poller received exception from client: "
                        + e.getMessage());
                reason = "NsclientException: " + e.getMessage();
            }
        } // end for(;;)
        return PollStatus.get(serviceStatus, reason, responseTime);

    }
}
