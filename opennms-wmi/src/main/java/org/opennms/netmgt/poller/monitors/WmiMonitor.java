/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
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
public class WmiMonitor extends AbstractServiceMonitor {

	private final static String DEFAULT_WMI_CLASS = "Win32_ComputerSystem";
	private final static String DEFAULT_WMI_OBJECT = "Status";
	private final static String DEFAULT_WMI_COMP_VAL = "OK";
	private final static String DEFAULT_WMI_MATCH_TYPE = "all";
	private final static String DEFAULT_WMI_COMP_OP = "EQ";
    private final static String DEFAULT_WMI_WQL = "NOTSET";

	/**
	 * {@inheritDoc}
	 *
	 * Poll the specified address for service availability. During the poll an
	 * attempt is made to connect the WMI agent on the specified host. If the
	 * connection request is successful, the parameters are parsed and turned
	 * into <code>WmiParams</code> and a check is performed against the
	 * remote WMI service. If the <code>WmiManager</code> responds
	 * with a <code>WmiResult</code> containing a result code of
	 * <code>WmiResult.RES_STATE_OK</code> then we have determined that
	 * we are talking to a valid service and we set the service status to
	 * SERVICE_AVAILABLE and return.
	 */
    @Override
	public PollStatus poll(final MonitoredService svc, final Map<String,Object> parameters) {
		// Holds the response reason.
		String reason = null;
		// Used to exit the retry loop early, if possible.
		int serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
		// This will hold the data the server sends back.
		WmiResult response = null;
		// Used to track how long the request took.
		Double responseTime = null;
		final NetworkInterface<InetAddress> iface = svc.getNetInterface();
		// Get the address we're going to poll.
		final InetAddress ipv4Addr = iface.getAddress();
		
		// Validate the interface type.
		if (iface.getType() != NetworkInterface.TYPE_INET) {
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");
		}

		final WmiAgentConfig agentConfig = WmiPeerFactory.getInstance().getAgentConfig(ipv4Addr);
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
            
            matchType = ParameterMap.getKeyedString(parameters, "matchType", DEFAULT_WMI_MATCH_TYPE);
			compVal = ParameterMap.getKeyedString(parameters, "compareValue", DEFAULT_WMI_COMP_VAL);
			compOp = ParameterMap.getKeyedString(parameters, "compareOp", DEFAULT_WMI_COMP_OP);
            wmiWqlStr = ParameterMap.getKeyedString(parameters, "wql", DEFAULT_WMI_WQL);
            wmiClass = ParameterMap.getKeyedString(parameters, "wmiClass", DEFAULT_WMI_CLASS);
			wmiObject = ParameterMap.getKeyedString(parameters, "wmiObject", DEFAULT_WMI_OBJECT);
		}

        final TimeoutTracker tracker = new TimeoutTracker(parameters, agentConfig.getRetries(), agentConfig.getTimeout());

        final String hostAddress = InetAddressUtils.str(ipv4Addr);
		LogUtils.debugf(this, "poll: address = %s, user = %s, %s", hostAddress, agentConfig.getUsername(), tracker);
        
        WmiManager mgr = null;
        
        for (tracker.reset(); tracker.shouldRetry() && serviceStatus != PollStatus.SERVICE_AVAILABLE; tracker.nextAttempt()) {
			try {

				tracker.startAttempt();

				LogUtils.debugf(this, "poll: creating WmiManager object.");

                // Create a client, set up details and connect.
				mgr = new WmiManager(hostAddress, agentConfig.getUsername(), agentConfig.getPassword(), agentConfig.getDomain(), matchType);

				mgr.setTimeout(tracker.getSoTimeout());
				mgr.init();

				LogUtils.debugf(this, "Completed initializing WmiManager object.");
                
                // We are connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.SERVICE_UNRESPONSIVE;

                // Set up the parameters the client will use to validate the response.
                // Note: We will check and see if we were provided with a WQL string.
                WmiParams clientParams = null;
                if(DEFAULT_WMI_WQL.equals(wmiWqlStr)) {
				    clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF, compVal, compOp, wmiClass, wmiObject);
				    LogUtils.debugf(this, "Attempting to perform operation: \\\\%s\\%s", wmiClass, wmiObject);
                } else {
                    // Create parameters to run a WQL query.
                    clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL, compVal, compOp, wmiWqlStr, wmiObject);
                    LogUtils.debugf(this, "Attempting to perform operation: %s", wmiWqlStr);
                }                
                
                // Send the request to the server and receive the response..
				response = mgr.performOp(clientParams);

				LogUtils.debugf(this, "Received result: %s", response);
                
                // Now save the time it took to process the check command.
				responseTime = tracker.elapsedTimeInMillis();

				if (response == null) {
					continue;
				}

				final ArrayList<Object> wmiObjects = response.getResponse();

				final StringBuffer reasonBuffer = new StringBuffer();
				// If there's no WQL string then use the class\object name as the result message
				if (DEFAULT_WMI_WQL.equals(wmiWqlStr)) {
				    reasonBuffer.append("Result for ").append(wmiClass).append("\\").append(wmiObject);
				} else {
				    // Otherwise, print the WQL statement in the result message
				    reasonBuffer.append("Result for \"").append(wmiWqlStr).append("\"");
				}

				if (response.getResultCode() == WmiResult.RES_STATE_OK) {
					serviceStatus = PollStatus.SERVICE_AVAILABLE;
					reasonBuffer.append(": ").append(wmiObjects.get(0));
				} else if (response.getResultCode() == WmiResult.RES_STATE_CRIT) {
					serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
					// set this to null so we don't try to save data when the node is down
					responseTime = null;
				}
				reason = reasonBuffer.toString();
			} catch (final WmiException e) {
				LogUtils.debugf(this, e, "WMI Poller received exception from client.");
				reason = "WmiException: " + e.getMessage();
			} finally {
                if (mgr != null) {
                    try {
                        mgr.close();
                    } catch (WmiException e) {
                        LogUtils.warnf(this, e, "An error occurred closing the WMI Manager.");
                    }
                }
            }
		} // end for(;;)
		return PollStatus.get(serviceStatus, reason, responseTime);
	}

}
