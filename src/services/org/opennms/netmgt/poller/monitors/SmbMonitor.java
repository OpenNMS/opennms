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

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;

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
 * 
 */
final public class SmbMonitor extends IPv4Monitor {
    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * <P>
     * Poll the specified address for response to NetBIOS name queries.
     * </P>
     * 
     * <P>
     * During the poll ...
     * </P>
     * 
     * @param iface
     *            The network interface to test the service on.
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * 
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * 
     */
    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

        // get the logger
        //
        Category log = ThreadCategory.getInstance(getClass());

        // get parameters
        //
        int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

        // Extract the address
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        // Default is a failed status
        //
        int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;

        // Attempt to retrieve NetBIOS name of this interface in order
        // to determine if SMB is supported.
        //
        NbtAddress nbtAddr = null;
        try {
            // Debugging only
            /*
             * if (m_logger.isDebugEnabled()) m_logger.debug("SmbMonitor.poll:
             * configuring netbios logging "); jcifs.netbios.Log.setPrintWriter(
             * new FileWriter( "/var/log/opennms/jcifs_netbios.log" )); if
             * (m_logger.isDebugEnabled()) m_logger.debug("SmbMonitor.poll:
             * configuring netbios logging mask");
             * jcifs.netbios.Log.setMask(jcifs.util.Log.ALL);
             * 
             * if (m_logger.isDebugEnabled()) m_logger.debug("SmbMonitor.poll:
             * configuring smb logging "); jcifs.smb.Log.setPrintWriter( new
             * FileWriter( "/var/log/opennms/jcifs_smb.log" ));
             * jcifs.smb.Log.setMask(jcifs.util.Log.ALL);
             */
            // end debugging
            //
            // Don't fully understand why yet but it isn't enough to
            // just call getByName(). getByName() will return a valid
            // NbtAddress object for an IP address for which it does not
            // successfully resolve a netbios name for...the netbios
            // name in this case retrieved by calling getHostName()
            // is set to the node's IP address. A subsequent call
            // to getNodeType() will however throw UnknownHostException
            // if the Netbios name is not resolved.
            //
            nbtAddr = NbtAddress.getByName(ipv4Addr.getHostAddress());
            int nodeType = nbtAddr.getNodeType();

            /*
             * if(log.isDebugEnabled()) { log.debug("Successfully created
             * NbtAddress for " + ipv4Addr.toString() + ". Netbios name= " +
             * nbtAddr.toString()); log.debug(" Host address: " +
             * nbtAddr.getHostAddress()); log.debug(" Netbios name: " +
             * nbtAddr.getHostName()); log.debug(" Node type: " + nodeType); }
             */

            serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
        } catch (UnknownHostException uhE) {
            if (log.isDebugEnabled())
                log.debug("poll: Unknown host exception generated for " + ipv4Addr.toString() + ", reason: " + uhE.getLocalizedMessage());
        } catch (RuntimeException rE) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("poll: Unexpected runtime exception", rE);
        } catch (Exception e) {
            if (log.isEnabledFor(Priority.ERROR))
                log.error("poll: Unexpected exception", e);
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }
}
