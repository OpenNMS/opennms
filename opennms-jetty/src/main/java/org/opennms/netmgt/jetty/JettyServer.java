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
package org.opennms.netmgt.jetty;

import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.ConnectorStatistics;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Web Application within OpenNMS as a Service Daemon.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class JettyServer extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

    private static final String LOG4J_CATEGORY = "jetty-server";

    int m_port = 8080;

    private Server m_server;

    /**
     * <p>Constructor for JettyServer.</p>
     */
    protected JettyServer() {
        super(LOG4J_CATEGORY);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        final File jettyXml = new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "jetty.xml");
        InputStream jettyXmlStream = null;

        try {
            m_server = new Server();

            // Add JMX MBeans for the Jetty server
            MBeanContainer mbeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
            m_server.addEventListener(mbeanContainer);
            m_server.addBean(mbeanContainer);

            // If we were using Jetty's loggers we would need to manually add it to the MBean registry
            //
            // @see http://wiki.eclipse.org/Jetty/Tutorial/JMX
            //
            //container.addBean(Log.getLog());

            if (jettyXml.exists()) {
                jettyXmlStream = jettyXml.toURI().toURL().openStream();
            } else {
                jettyXmlStream = getClass().getResourceAsStream("jetty.xml");
            }
            if (jettyXmlStream == null) {
                throw new RuntimeException("Unable to locate jetty.xml in the classpath!");
            }
            final XmlConfiguration xmlConfiguration = new XmlConfiguration(jettyXmlStream);
            xmlConfiguration.configure(m_server);
        } catch (final Exception ioe) {
            throw new RuntimeException(ioe);
        }

        m_server.setStopAtShutdown(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        try {
            m_server.start();
        } catch (final Throwable t) {
            LOG.error("Error starting Jetty Server", t);
            throw new IllegalStateException(t);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        try {
            m_server.stop();
        } catch (final Throwable t) {
            LOG.error("Error stopping Jetty Server", t);
            throw new IllegalStateException(t);
        }
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }

    private static boolean isSsl(final ServerConnector conn) {
        return conn.getConnectionFactory(SslConnectionFactory.class) != null;
    }

    private Stream<ServerConnector> getHttpsServerStream() {
        return Arrays.stream(m_server.getConnectors())
                .filter(conn -> conn instanceof ServerConnector)
                .map(conn -> (ServerConnector)conn)
                .filter(JettyServer::isSsl);
    }

    private Stream<ServerConnector> getHttpServerStream() {
        return Arrays.stream(m_server.getConnectors())
                .filter(conn -> conn instanceof ServerConnector)
                .map(conn -> (ServerConnector)conn)
                .filter(conn -> !isSsl(conn));
    }

    public long getHttpsConnectionsTotal() {
        return getHttpsServerStream()
                .map(conn -> conn.getBean(ConnectorStatistics.class))
                .mapToLong(ConnectorStatistics::getConnections)
                .sum();
    }

    public long getHttpsConnectionsOpen() {
        return getHttpsServerStream()
                .map(conn -> conn.getBean(ConnectorStatistics.class))
                .mapToLong(ConnectorStatistics::getConnectionsOpen)
                .sum();
    }

    public long getHttpsConnectionsOpenMax() {
        return getHttpsServerStream()
                .map(conn -> conn.getBean(ConnectorStatistics.class))
                .mapToLong(ConnectorStatistics::getConnectionsOpenMax)
                .sum();
    }

    public long getHttpConnectionsTotal() {
        return getHttpServerStream()
                .map(conn -> conn.getBean(ConnectorStatistics.class))
                .mapToLong(ConnectorStatistics::getConnections)
                .sum();
    }
    
    public long getHttpConnectionsOpen() {
        return getHttpServerStream()
                .map(conn -> conn.getBean(ConnectorStatistics.class))
                .mapToLong(ConnectorStatistics::getConnectionsOpen)
                .sum();
    }
    
    public long getHttpConnectionsOpenMax() {
        return getHttpServerStream()
                .map(conn -> conn.getBean(ConnectorStatistics.class))
                .mapToLong(ConnectorStatistics::getConnectionsOpenMax)
                .sum();
    }

}
