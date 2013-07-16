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
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;

/**
 * This class provides Capsd with the ability to check for ICMP support on new
 * interfaces as them are passed into the system. In order to minimize the
 * number of sockets and threads, this class creates a daemon thread to handle
 * all responses and a single socket for sending echo request to various hosts.
 *
 * @author <A HREF="mailto:weave@oculan.com">Weave </a>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class IcmpPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpPlugin.class);
    /**
     * The name of the protocol that is supported by this plugin
     */
    private static final String PROTOCOL_NAME = "ICMP";

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
		try {
	    	Number retval = PingerFactory.getInstance().ping(address);
	    	if (retval != null) {
	    		return true;
	    	}
		} catch (Throwable e) {
			LOG.warn("Pinger failed to ping {}", address, e);
		}
		return false;
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
    	int retries;
    	long timeout;

    	try {
    		if (qualifiers != null) {
    			retries = ParameterMap.getKeyedInteger(qualifiers, "retry", PingConstants.DEFAULT_RETRIES);
    			timeout = ParameterMap.getKeyedLong(qualifiers, "timeout", PingConstants.DEFAULT_TIMEOUT);
    		} else {
    			retries = PingConstants.DEFAULT_RETRIES;
    			timeout = PingConstants.DEFAULT_TIMEOUT;
    		}
    		Number retval = PingerFactory.getInstance().ping(address, timeout, retries);
    		if (retval != null) {
    			return true;
    		}
    	} catch (Throwable e) {
			LOG.warn("Pinger failed to ping {}", address, e);
        }
    	
    	return false;
    }
}
