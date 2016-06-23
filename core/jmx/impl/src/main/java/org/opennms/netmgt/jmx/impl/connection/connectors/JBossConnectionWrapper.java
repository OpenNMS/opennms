/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jmx.impl.connection.connectors;

import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

/**
 * The JBossConnectionWrapper manages the connection to a JBoss server.  The
 * {@link JBossMBeanServerConnector}
 * creates the connection to the server and closes the connection, so the {@link #close()} method doesn't
 * need to do anything.
 */
class JBossConnectionWrapper implements JmxServerConnectionWrapper {
    private MBeanServer mbeanServer;

    public JBossConnectionWrapper(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    @Override
    public MBeanServerConnection getMBeanServerConnection() {
        return mbeanServer;
    }

    @Override
    public void close() {
        mbeanServer = null;
    }
}
