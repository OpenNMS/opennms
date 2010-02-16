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
// Modifications:
// 12 Feb 2010: Start with service status unavailable, going to unresponsive only
//              once we have established a WMI session. - jeffg@opennms.org
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
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.protocols.wmi.WmiAgentConfig;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;

/**
 * This class is designed to be used by the service poller framework to test
 * the availability of WMI services on remote interfaces. The class
 * implements the IPv4Monitor interface that allows it to be used along
 * with other plug-ins by the service poller framework.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
public class WmiMonitor extends IPv4Monitor {

	/**
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY = 0;

	/**
	 * Default timeout. Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT = 3000;

	private final static String DEFAULT_WMI_CLASS = "Win32_ComputerSystem";
	private final static String DEFAULT_WMI_OBJECT = "Status";
	private final static String DEFAULT_WMI_COMP_VAL = "OK";
	private final static String DEFAULT_WMI_MATCH_TYPE = "all";
	private final static String DEFAULT_WMI_COMP_OP = "EQ";
    private final static String DEFAULT_WMI_WQL = "NOTSET";

    /**
	 * Poll the specified address for service availability. During the poll an
	 * attempt is made to connect the WMI agent on the specified host. If the 
	 * connection request is successful, the parameters are parsed and turned 
	 * into <code>WmiParams</code> and a check is performed against the
	 * remote WMI service. If the <code>WmiManager</code> responds
	 * with a <code>WmiResult</code> containing a result code of
	 * <code>WmiResult.RES_STATE_OK</code> then we have determined that
	 * we are talking to a valid service and we set the service status to
	 * SERVICE_AVAILABLE and return.
	 * @param parameters
	 *            The package parameters (timeout, retry, etc...) to be used
	 *            for this poll.
	 * @param svc
	 *            The service containing the network interface to test the service on.
	 * 
	 * @return The availibility of the interface and if a transition event
	 *         should be supressed.
	 * @throws java.lang.RuntimeException
	 *             Thrown if the interface experiences errors during the poll.
	 */
	@Override
	public PollStatus poll(MonitoredService svc, Map parameters) {
		// Holds the response reason.
		String reason = null;
		// Used to exit the retry loop early, if possible.
		int serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
		// This will hold the data the server sends back.
		WmiResult response = null;
		// Used to track how long the request took.
		Double responseTime = null;
		NetworkInterface iface = svc.getNetInterface();
		// Get the address we're going to poll.
		InetAddress ipv4Addr = (InetAddress) iface.getAddress();
		
		Category log = ThreadCategory.getInstance(getClass());

		// Validate the interface type.
		if (iface.getType() != NetworkInterface.TYPE_IPV4) {
			throw new NetworkInterfaceNotSupportedException(
					"Unsupported interface type, only TYPE_IPV4 currently supported");
		}

		WmiAgentConfig agentConfig = WmiPeerFactory.getInstance().getAgentConfig(ipv4Addr);
		String matchType = DEFAULT_WMI_MATCH_TYPE;
		String compVal = DEFAULT_WMI_COMP_VAL;
		String compOp = DEFAULT_WMI_COMP_OP;
		String wmiClass = DEFAULT_WMI_CLASS;
		String wmiObject = DEFAULT_WMI_OBJECT;
        String wmiWqlStr = DEFAULT_WMI_WQL;

        if (parameters != null) {
            if (parameters.get("timeout") != null) {
            	int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout());
                agentConfig.setTimeout(timeout);
            }
            
            if (parameters.get("retry") != null) {
            	int retries = ParameterMap.getKeyedInteger(parameters, "retry", agentConfig.getRetries());
                agentConfig.setRetries(retries);
            }

            if (parameters.get("username") != null) {
                String user = ParameterMap.getKeyedString(parameters, "username", agentConfig.getUsername());
                agentConfig.setUsername(user);
            }
            
            if (parameters.get("password") != null) {
                String pass = ParameterMap.getKeyedString(parameters, "password", agentConfig.getPassword());
                agentConfig.setUsername(pass);
            }
            
            if (parameters.get("domain") != null) {
                String domain = ParameterMap.getKeyedString(parameters, "domain", agentConfig.getDomain());
                agentConfig.setUsername(domain);
            }
            
            matchType = ParameterMap.getKeyedString(parameters, "matchType",
					DEFAULT_WMI_MATCH_TYPE);
			compVal = ParameterMap.getKeyedString(parameters, "compareValue",
					DEFAULT_WMI_COMP_VAL);
			compOp = ParameterMap.getKeyedString(parameters, "compareOp", DEFAULT_WMI_COMP_OP);
            wmiWqlStr = ParameterMap.getKeyedString(parameters, "wql", DEFAULT_WMI_WQL);
            wmiClass = ParameterMap.getKeyedString(parameters, "wmiClass",
					DEFAULT_WMI_CLASS);
			wmiObject = ParameterMap.getKeyedString(parameters, "wmiObject",
					DEFAULT_WMI_OBJECT);
		}

		TimeoutTracker tracker = new TimeoutTracker(parameters, agentConfig.getRetries(),
				agentConfig.getTimeout());


        if (log().isDebugEnabled())
            log().debug("poll: address = " + ipv4Addr.getHostAddress() + ", user = " + agentConfig.getUsername() + ", " + tracker);
        
        WmiManager mgr = null;
        
        for (tracker.reset(); tracker.shouldRetry()
				&& serviceStatus != PollStatus.SERVICE_AVAILABLE; tracker
				.nextAttempt()) {
			try {

				tracker.startAttempt();

                if (log().isDebugEnabled())
                        log().debug("poll: creating WmiManager object.");

                // Create a client, set up details and connect.
				mgr = new WmiManager(ipv4Addr.getHostAddress(),
						agentConfig.getUsername(), agentConfig.getPassword(), agentConfig.getDomain(), matchType);

				mgr.setTimeout(tracker.getSoTimeout());
				mgr.init();

                if (log().isDebugEnabled())
                        log().debug("Completed initializing WmiManager object.");
                
                // We are connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;

                // Set up the parameters the client will use to validate the response.
                // Note: We will check and see if we were provided with a WQL string.
                WmiParams clientParams = null;
                if(wmiWqlStr.equals(DEFAULT_WMI_WQL)) {

				    clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF,
                                                 compVal, compOp, wmiClass, wmiObject);
                    if (log().isDebugEnabled())
                        log().debug("Attempting to perform operation: \\\\" + wmiClass + "\\" + wmiObject);
                } else {
                    // Create parameters to run a WQL query.
                    clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL,
                                                 compVal, compOp, wmiWqlStr, wmiObject);
                    if (log().isDebugEnabled())
                        log().debug("Attempting to perform operation: " + wmiWqlStr);
                }                
                
                // Send the request to the server and receive the response..
				response = mgr.performOp(clientParams);

                if (log().isDebugEnabled())
                        log().debug("Received result: " + response);
                
                // Now save the time it took to process the check command.
				responseTime = tracker.elapsedTimeInMillis();

				if (response == null) {
					continue;
				}

				ArrayList<Object> wmiObjects = response.getResponse();

				if (response.getResultCode() == WmiResult.RES_STATE_OK) {
					serviceStatus = PollStatus.SERVICE_AVAILABLE;
					reason = "Result for  " + wmiClass + "\\" + wmiObject
							+ ": " + wmiObjects.get(0).toString();
				} else if (response.getResultCode() == WmiResult.RES_STATE_CRIT) {
					serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
					reason = "Result for  " + wmiClass + "\\" + wmiObject
							+ ": " + wmiObjects.get(0).toString();
					// set this to null so we don't try to save data when the node is down
					responseTime = null;
				}

			} catch (WmiException e) {
				log.debug("WMI Poller received exception from client: "
						+ e.getMessage());
				reason = "WmiException: " + e.getMessage();
			} finally {
                if (mgr != null) {
                    try {
                        mgr.close();
                    } catch (WmiException e) {
                        log().warn("an error occurred closing the WMI Manager", e);
                    }
                }
            }
		} // end for(;;)
		return PollStatus.get(serviceStatus, reason, responseTime);
	}

}
