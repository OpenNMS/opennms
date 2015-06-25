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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.Objects;

class DefaultConnectionWrapper implements JmxServerConnectionWrapper {

    private JMXConnector connector;
    private MBeanServerConnection connection;

    protected DefaultConnectionWrapper(JMXConnector connector, MBeanServerConnection connection) {
        this.connector = Objects.requireNonNull(connector, "connector must not be null");
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
    }

    @Override
    public MBeanServerConnection getMBeanServerConnection() {
        return connection;
    }

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
