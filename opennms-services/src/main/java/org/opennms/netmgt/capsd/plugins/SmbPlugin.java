/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import jcifs.netbios.NbtAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for SMB
 * support on remote interfaces. The class implements the Plugin interface that
 * allows it to be used along with other plugins by the daemon.
 * </P>
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class SmbPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(SmbPlugin.class);
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
        boolean isAServer = false;
        try {
            LOG.debug("host.getHostAddress(): {}", InetAddressUtils.str(host));
            NbtAddress nbtAddr = NbtAddress.getByName(InetAddressUtils.str(host));

            // If the retrieved SMB name is equal to the IP address
            // of the host, the it is safe to assume that the interface
            // does not support SMB
            //
            LOG.debug("nbtAddr.getHostName(): {}", nbtAddr.getHostName());
            if (nbtAddr.getHostName().equals(InetAddressUtils.str(host))) {

                LOG.debug("SmbPlugin: failed to retrieve SMB name for {}", InetAddressUtils.str(host));
            } else {
                isAServer = true;
            }
        } catch (UnknownHostException e) {

            LOG.debug("SmbPlugin: UnknownHostException: {}", e.getMessage());
        } catch (Throwable t) {
            LOG.error("SmbPlugin: An undeclared throwable exception was caught checking host {}", InetAddressUtils.str(host), t);
        }

        return isAServer;
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        return isSmb(address);
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        return isSmb(address);
    }

}
