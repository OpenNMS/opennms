/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 06: Created this class from the ProtocolInfo inner class
 *              in CapsdConfigManager. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.util.Map;

import org.opennms.netmgt.capsd.Plugin;

/**
 * This class is used to encapsulate the basic protocol information read
 * from the config file. The information includes the plugin, the protocol
 * name, the merged parameters to the plugin, and the action to be taken.
 *
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class CapsdProtocolInfo {
    public enum Action { 
        /**
         * This is used to represent the protocol scan
         * configuration. If this value is used the the plugin should be used to
         * scan the address.
         */
        SCAN,
        
        /**
         * This is used to represent that the protocol plugin should
         * not be used to scan the interface.
         */
        SKIP,
        
        /**
         * This is used to represent the state when the protocol
         * should be automatically set, with any status checks.
         */
        AUTO_SET
    };
    
    /**
     * The plugin used to poll the interface
     */
    private Plugin m_plugin;

    /**
     * The name of the protocol supported by the plugin.
     */
    private String m_protocol;

    /**
     * the map or parameters passed to the plugin
     */
    private Map<String, Object> m_parameters;

    /**
     * The integer value that represents the action that should be taken to
     * poll the interface.
     */
    private CapsdProtocolInfo.Action m_action;

    /**
     * Constructs a new protocol information element.
     *
     * @param proto
     *            The protocol supported.
     * @param plugin
     *            The plugin module
     * @param params
     *            The parameters for the plugin.
     * @param action
     *            The action to take.
     */
    public CapsdProtocolInfo(String proto, Plugin plugin, Map<String, Object> params, CapsdProtocolInfo.Action action) {
        m_plugin = plugin;
        m_protocol = proto;
        m_parameters = params;
        m_action = action;
    }

    /**
     * Returns the protocol name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProtocol() {
        return m_protocol;
    }

    /**
     * Returns the plugin module
     *
     * @return a {@link org.opennms.netmgt.capsd.Plugin} object.
     */
    public Plugin getPlugin() {
        return m_plugin;
    }

    /**
     * Returns the input parameters for the plugin
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getParameters() {
        return m_parameters;
    }

    /**
     * Returns true if the configuration has this particular module set as
     * automatically enabled.
     *
     * @return a boolean.
     */
    public boolean isAutoEnabled() {
        return m_action == Action.AUTO_SET;
    }
}
