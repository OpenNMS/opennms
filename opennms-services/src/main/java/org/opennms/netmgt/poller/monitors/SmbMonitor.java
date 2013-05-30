/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import java.net.UnknownHostException;
import java.util.Map;

import jcifs.netbios.NbtAddress;

import org.apache.log4j.Level;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the DNS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 */

// I this thise needs a jcifs.properties file so we can't distribute it now
@Distributable(DistributionContext.DAEMON)
final public class SmbMonitor extends AbstractServiceMonitor {
    /**
     * Default retries.
     */
    /*
     * TODO: Use it or loose it.
     * Commented out because it is not currently used in this monitor
     */
    //private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    /*
     * TODO: Use it or loose it.
     * Commented out because it is not currently used in this monitor
     */
    //private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * {@inheritDoc}
     *
     * <P>
     * Poll the specified address for response to NetBIOS name queries.
     * </P>
     *
     * <P>
     * During the poll ...
     * </P>
     */
    
    /**
     * Do a node-status request before checking name?
     * First appears in OpenNMS 1.10.10. Default is true.
     */
    private static final String DO_NODE_STATUS = "do-node-status";
    private static final boolean DO_NODE_STATUS_DEFAULT = true;
    
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        /*
         * TODO: Use it or loose it.
         * Commented out because it is not currently used in this monitor
         */
        // get parameters
        //int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        //int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

        // Extract the address
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        // Default is a failed status
        //
        PollStatus serviceStatus = PollStatus.unavailable();

        // Attempt to retrieve NetBIOS name of this interface in order
        // to determine if SMB is supported.
        //
        NbtAddress nbtAddr = null;
        
        /*
         * This try block was updated to reflect the behavior of the plugin.
         */
        final String hostAddress = InetAddressUtils.str(ipv4Addr);
        
        final boolean doNodeStatus = ParameterMap.getKeyedBoolean(parameters, DO_NODE_STATUS, DO_NODE_STATUS_DEFAULT);
        
        try {
            nbtAddr = NbtAddress.getByName(hostAddress);
            
            if (doNodeStatus) {
                nbtAddr.getNodeType();
            }
            
            if (!nbtAddr.getHostName().equals(hostAddress))
                serviceStatus = PollStatus.available();

        } catch (UnknownHostException uhE) {
        	serviceStatus = logDown(Level.DEBUG, "Unknown host exception generated for " + hostAddress + ", reason: " + uhE.getLocalizedMessage());
        } catch (RuntimeException rE) {
        	serviceStatus = logDown(Level.ERROR, "Unexpected runtime exception", rE);
        } catch (Throwable e) {
        	serviceStatus = logDown(Level.DEBUG, "Unexpected exception", e);
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

}
