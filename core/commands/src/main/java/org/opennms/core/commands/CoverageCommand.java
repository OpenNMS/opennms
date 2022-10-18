/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
