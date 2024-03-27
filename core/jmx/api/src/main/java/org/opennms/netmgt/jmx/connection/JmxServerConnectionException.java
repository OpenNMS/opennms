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
package org.opennms.netmgt.jmx.connection;

import java.io.IOException;

/**
 * Is used to indicate that a connection to a JMX Server (MBeanServer) could not be established.
 * The reason may be that the server is not reachable, or credentials are invalid or
 * there is no {@link org.opennms.netmgt.jmx.connection.JmxServerConnector} registered
 * for the {@link org.opennms.netmgt.jmx.connection.JmxConnectors}.
 * <p/>
 * The exception's <code>errorMessage</code> should provide details about the concrete error.
 */
public class JmxServerConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    public JmxServerConnectionException(final String errorMessage) {
        super(errorMessage);
    }

    public JmxServerConnectionException(final IOException ioException) {
        super(ioException);
    }

    public JmxServerConnectionException(String errorMessage, Exception exception) {
        super(errorMessage, exception);
    }
}
