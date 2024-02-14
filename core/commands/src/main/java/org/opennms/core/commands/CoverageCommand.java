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
package org.opennms.core.commands;

import java.io.FileOutputStream;
import java.util.Collections;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.daemon.CurrentVirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "jacoco", name = "dump", description = "Trigger a code-coverage dump using the JMX agent interface.")
@Service
public class CoverageCommand implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(CoverageCommand.class);

    private static final String DEFAULT_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/jmxrmi";

    @Argument(index = 0, name = "file", description = "the file to dump (defaults to jacoco.exec)", required = false)
    private String m_file = "jacoco.exec";

    @SuppressWarnings({ "java:S106", "java:S3516" })
    @Override
    public Object execute() {
        JMXServiceURL url = null;

        final var jmxUrlString = getJmxUrl();
        try {
            url = new JMXServiceURL(jmxUrlString);
        } catch (final Exception j) {
            LOG.debug("failed to initialize JMX service URL {}", jmxUrlString, j);
            return null;
        }

        try (
                final JMXConnector jmxc = JMXConnectorFactory.connect(url, Collections.emptyMap());
                final FileOutputStream output = new FileOutputStream(m_file);
        ) {
            final MBeanServerConnection connection = jmxc.getMBeanServerConnection();
            final IJaCoCoProxy proxy = MBeanServerInvocationHandler.newProxyInstance(
                    connection,
                    new ObjectName("org.jacoco:type=Runtime"),
                    IJaCoCoProxy.class,
                    false
            );

            // Retrieve JaCoCo version and session id:
            System.out.println("JaCoCo Version: " + proxy.getVersion());
            System.out.println("Session ID: " + proxy.getSessionId());
            System.out.println("Dumping execution data to " + m_file + "...");

            // Retrieve dump and write to file:
            final byte[] data = proxy.getExecutionData(false);
            output.write(data);

            System.out.println("Wrote " + data.length + " bytes.");

            return null;
        } catch (final Exception e) {
            LOG.debug("failed to connect to JMX", e);
        }

        return null;
    }

    private String getJmxUrl() {
        try {
            return CurrentVirtualMachine.getJmxUri();
        } catch (final IllegalStateException e) {
            LOG.info("falling back to JMX over RMI (this probably won't work)", e);
        }
        return DEFAULT_SERVICE_URL;
    }

}
