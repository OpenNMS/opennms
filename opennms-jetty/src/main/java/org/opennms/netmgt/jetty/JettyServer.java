/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        try {
            m_server.stop();
        } catch (final Throwable t) {
            LOG.error("Error stopping Jetty Server", t);
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
