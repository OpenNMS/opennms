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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import jcifs.netbios.NbtAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for SMB
 * support on remote interfaces. The class implements the Plugin interface that
 * allows it to be used along with other plugins by the daemon.
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * 
 */
public final class SmbPlugin extends AbstractPlugin {
    /**
     * The protocol that this plugin checks for.
     */
    private final static String PROTOCOL_NAME = "SMB";

    /**
     * Test to see if the passed host talks SMB &amp; has a NetBIOS name.
     * 
     * @param host
     *            The remote host to check.
     * 
     * @return True if the remote interface responds talks SMB and has a NETBIOS
     *         name. False otherwise.
     */
    private boolean isSmb(InetAddress host) {
        Category log = ThreadCategory.getInstance(getClass());
        boolean isAServer = false;
        try {
            log.debug("host.getHostAddress(): " + host.getHostAddress());
            NbtAddress nbtAddr = NbtAddress.getByName(host.getHostAddress());

            // If the retrieved SMB name is equal to the IP address
            // of the host, the it is safe to assume that the interface
            // does not support SMB
            //
            log.debug("nbtAddr.getHostNamer(): " + nbtAddr.getHostName());
            if (nbtAddr.getHostName().equals(host.getHostAddress())) {
                if (log.isDebugEnabled())
                    log.debug("SmbPlugin: failed to retrieve SMB name for " + host.getHostAddress());
            } else {
                isAServer = true;
            }
        } catch (UnknownHostException e) {
            if (log.isDebugEnabled())
                log.debug("SmbPlugin: UnknownHostException: " + e.getMessage());
        } catch (Throwable t) {
            log.error("SmbPlugin: An undeclared throwable exception was caught checking host " + host.getHostAddress(), t);
        }

        return isAServer;
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     * 
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * 
     * @param address
     *            The address to check for support.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address) {
        return isSmb(address);
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        return isSmb(address);
    }

}
