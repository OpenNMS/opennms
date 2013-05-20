/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support.jmx.connectors;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

/**
 * The JBossConnectionWrapper class manages the connection to the JBoss server.  The 
 * JBossConnectionFactory creates the connection to the server and closes the 
 * connection to the naming server, so the close() method doesn't need to do anything.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 */
public class JBossConnectionWrapper implements ConnectionWrapper {
    private MBeanServer mbeanServer;
    
    /**
     * <p>Constructor for JBossConnectionWrapper.</p>
     *
     * @param mbeanServer a {@link javax.management.MBeanServer} object.
     */
    public JBossConnectionWrapper(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.jmx.connectors.ConnectionWrapper#closeConnection()
     */
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        mbeanServer = null;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.protocols.jmx.connectors.ConnectionWrapper#getMBeanServer()
     */
    /**
     * <p>getMBeanServer</p>
     *
     * @return a {@link javax.management.MBeanServerConnection} object.
     */
    @Override
    public MBeanServerConnection getMBeanServer() {
        return mbeanServer;
    }
}
