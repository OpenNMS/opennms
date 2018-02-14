/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.commands;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxHelper;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.MBeanServerQueryException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;

/**
 * Class for all commands which need a JMX server connection.
 */
public abstract class JmxCommand extends Command {

    @Option(name = "--host", usage = "Hostname or IP-Address of JMX-RMI host.", forbids = "--url", metaVar = "<host>")
    private String hostName;

    @Option(name = "--port", usage = "Port of JMX-RMI service", forbids = "--url", metaVar = "<port>")
    private String port;

    @Option(name = "--username", usage = "Username for JMX-RMI Authentication", metaVar = "<username>")
    private String username;

    @Option(name = "--password", usage = "Password for JMX-RMI Authentication", metaVar = "<password>")
    private String password;

    @Option(name = "--jmxmp", usage = "Use JMXMP and not JMX-RMI")
    private boolean jmxmp;

    @Option(name = "--url",
            usage = "JMX URL Usage: <hostname>:<port> OR service:jmx:<protocol>:<sap> OR service:jmx:remoting-jmx://<hostname>:<port>",
            forbids = {"--host", "--port"},
            metaVar = "<url>")
    private String url;

    @Override
    protected void validate(CmdLineParser parser) throws CmdLineException {
        if (url != null && (hostName != null ||port != null)) {
            LOG.warn("WARNING: You have defined an url and a hostname and/or port. Using url '{}' and ignoring hostname:port", url);
        }
        if (url == null && (hostName == null || port == null)) {
            throw new CmdLineException(parser, "You have to define either an URL or an hostname and port to connect to a JMX server.");
        }
    }

    protected void execute() throws CmdLineException, CmdRunException {
        try (JMXConnector connector = getJmxConnector()) {
            MBeanServerConnection mbeanServerConnection = connector.getMBeanServerConnection();
            execute(mbeanServerConnection);
        } catch (MBeanServerQueryException | JMException | IOException e) {
            throw new CmdRunException(e);
        }
    }

    /**
     * This method gets the JmxConnector to connect with the given
     * jmxServiceURL.

     * @return a jmxConnector
     * @throws IOException
     *             if the connection to the given jmxServiceURL fails (e.g.
     *             authentication failure or not reachable)
     */
    private JMXConnector getJmxConnector() throws IOException {
        return JmxHelper.createJmxConnector(username, password, JmxHelper.createJmxServiceUrl(url, hostName, port, jmxmp));
    }

    protected abstract void execute(MBeanServerConnection mBeanServerConnection) throws MBeanServerQueryException, IOException, JMException;
}
