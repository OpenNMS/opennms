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

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

/**
 * This class manages the connection to the remote jmx server.  The Jsr160ConnectionFactory
 * class creates the connection and the close method closes it.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 */
public class Jsr160ConnectionWrapper implements ConnectionWrapper{
    
    private JMXConnector connector;
    private MBeanServerConnection connection;

    /**
     * <p>Constructor for Jsr160ConnectionWrapper.</p>
     *
     * @param connector a {@link javax.management.remote.JMXConnector} object.
     * @param connection a {@link javax.management.MBeanServerConnection} object.
     */
    public Jsr160ConnectionWrapper(JMXConnector connector, MBeanServerConnection connection) {
        this.connector  = connector;
        this.connection = connection;
    }
    
    /**
     * <p>getMBeanServer</p>
     *
     * @return Returns the connection.
     */
    @Override
    public MBeanServerConnection getMBeanServer() {
        return connection;
    }
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        if (connector != null) {
            try {
                connector.close();
            } catch (IOException e) {
            }
        }
        connection = null;
    }
}
