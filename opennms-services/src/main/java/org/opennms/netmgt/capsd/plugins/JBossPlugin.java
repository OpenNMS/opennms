/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.capsd.plugins;

/*
 * This class determines whether a JBoss instance exists for an IP Address.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JBossConnectionFactory;

/**
 * @author mjamison
 *
 */
public class JBossPlugin extends JMXPlugin {
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.JMXPlugin#getMBeanServer()
     */
    public ConnectionWrapper getMBeanServerConnection(Map<String, Object> map, InetAddress address) {
        return  JBossConnectionFactory.getMBeanServerConnection(map, address);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    public String getProtocolName(Map<String, Object> map) {
        return "jboss";
    }
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress)
     */
    public boolean isProtocolSupported(InetAddress address) {
        
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("port", "1099");

        return isProtocolSupported(address, map);
    }
}
