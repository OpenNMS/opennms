//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 29: Added response times to certain monitors.
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
import java.net.UnknownHostException;
import java.util.Map;

import jcifs.netbios.NbtAddress;

import org.apache.log4j.Level;
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
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */

// I this thise needs a jcifs.properties file so we can't distribute it now
@Distributable(DistributionContext.DAEMON)
final public class SmbMonitor extends IPv4Monitor {
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
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface iface = svc.getNetInterface();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

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
        try {
            nbtAddr = NbtAddress.getByName(ipv4Addr.getHostAddress());
            
            if (!nbtAddr.getHostName().equals(ipv4Addr.getHostAddress()))
                serviceStatus = PollStatus.available();

        } catch (UnknownHostException uhE) {
        	serviceStatus = logDown(Level.DEBUG, "Unknown host exception generated for " + ipv4Addr.toString() + ", reason: " + uhE.getLocalizedMessage());
        } catch (RuntimeException rE) {
        	serviceStatus = logDown(Level.ERROR, "Unexpected runtime exception", rE);
        } catch (Exception e) {
        	serviceStatus = logDown(Level.DEBUG, "Unexpected exception", e);
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

}
