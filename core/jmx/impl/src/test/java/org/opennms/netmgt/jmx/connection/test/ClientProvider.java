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
package org.opennms.netmgt.jmx.connection.test;

import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;

/**
 * This JMXConnectorProvider is used for testing purposes, allowing for
 * mock connectors to be returned to JMXConnectorFactory.newJMXConnector(url, env).
 *
 * To use this provider:
 *   System.setProperty(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "org.opennms.netmgt.jmx.connection")
 *
 * and a JMX URL like: "service:jmx:test://oops"
 *
 * @author jwhite
 */
public class ClientProvider implements JMXConnectorProvider {

    private static JMXConnector connector;

    @Override
    public JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map<String, ?> environment) {
        return connector;
    }

    public static void setConnector(JMXConnector connector) {
        ClientProvider.connector = connector;
    }

    public static JMXConnector getConnector() {
        return connector;
    }
}
