/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.netbios.NbtAddress;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SMB service on remote interfaces. Poll the specified address
 * for response to NetBIOS name queries.
 * 
 * The class implements the ServiceMonitor interface that allows it to be used along
 * with other plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 */
// I this thise needs a jcifs.properties file so we can't distribute it now
@Distributable(DistributionContext.DAEMON)
final public class SmbMonitor extends AbstractServiceMonitor {
    
    public static final Logger LOG = LoggerFactory.getLogger(SmbMonitor.class);
    
    /**
     * Do a node-status request before checking name?
     * First appears in OpenNMS 1.10.10. Default is true.
     */
    private static final String DO_NODE_STATUS = "do-node-status";
    private static final boolean DO_NODE_STATUS_DEFAULT = true;
    
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // Extract the address
        //
        InetAddress ipAddr = svc.getAddress();

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
        final String hostAddress = InetAddressUtils.str(ipAddr);

        final boolean doNodeStatus = ParameterMap.getKeyedBoolean(parameters, DO_NODE_STATUS, DO_NODE_STATUS_DEFAULT);

        try {
            nbtAddr = NbtAddress.getByName(hostAddress);
            
            if (doNodeStatus) {
                nbtAddr.getNodeType();
            }
            
            if (!nbtAddr.getHostName().equals(hostAddress))
                serviceStatus = PollStatus.available();

        } catch (UnknownHostException uhE) {
        	String reason = "Unknown host exception generated for " + hostAddress + ", reason: " + uhE.getLocalizedMessage();
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        } catch (RuntimeException rE) {
        	LOG.debug("Unexpected runtime exception", rE);
            serviceStatus = PollStatus.unavailable("Unexpected runtime exception");
        } catch (Throwable e) {
        	LOG.debug("Unexpected exception", e);
            serviceStatus = PollStatus.unavailable("Unexpected exception");
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

}
