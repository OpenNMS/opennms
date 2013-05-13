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
 * <p>JBossPlugin class.</p>
 *
 * @author mjamison
 * @version $Id: $
 */
public class JBossPlugin extends JMXPlugin {
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.JMXPlugin#getMBeanServer()
     */
    /** {@inheritDoc} */
    @Override
    public ConnectionWrapper getMBeanServerConnection(Map<String, Object> map, InetAddress address) {
        return  JBossConnectionFactory.getMBeanServerConnection(map, address);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /** {@inheritDoc} */
    @Override
    public String getProtocolName(Map<String, Object> map) {
        return "jboss";
    }
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress)
     */
    /** {@inheritDoc} */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("port", "1099");

        return isProtocolSupported(address, map);
    }
}
