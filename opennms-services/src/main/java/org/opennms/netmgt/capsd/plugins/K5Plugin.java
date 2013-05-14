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
import org.opennms.netmgt.capsd.AbstractPlugin;


/**
 * This plugin does nothing more than return true for all
 * discovered interfaces so that we have an entry point
 * in the OpenNMS GUI for accessing K5 information.
 *
 * @author <A HREF="mailto:rchung@k5systems.com">rchung</A>
 * @version 1.1.1.1
 */
public class K5Plugin extends AbstractPlugin {

    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "K5Systems";

    /**
     * Default value for whether or not to use K5
     */
    private final static String DEFAULT_ACTIVE = "false";

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
     * Default to returning false unless K5 is set as active.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
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
        if (qualifiers != null) {
            String active = ParameterMap.getKeyedString(qualifiers, "active", DEFAULT_ACTIVE);
            if (active.equalsIgnoreCase("true")) {
                return true;
            }
        }

        return false;
    }
}

