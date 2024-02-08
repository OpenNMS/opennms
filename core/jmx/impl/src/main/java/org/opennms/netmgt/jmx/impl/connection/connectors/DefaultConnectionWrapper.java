/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.jmx.impl.connection.connectors;


import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.Objects;

public class DefaultConnectionWrapper implements JmxServerConnectionWrapper {

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
