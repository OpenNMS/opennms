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
