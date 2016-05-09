/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.jmx;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.JmxServerConnector;
import org.opennms.netmgt.jmx.impl.connection.connectors.Jsr160ConnectionFactory;
import org.opennms.netmgt.jmx.impl.connection.connectors.PlatformMBeanServerConnector;

import com.google.common.collect.Maps;

/**
 * <p>Abstract AbstractJsr160Detector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractJsr160Detector extends JMXDetector {
   
    /** Constant <code>DEFAULT_PORT=9003</code> */
    protected static int DEFAULT_PORT = 9003;
    
    private String m_factory = "STANDARD";
    private String m_friendlyName = "jsr160";
    private String m_protocol = "rmi";
    private String m_type = "default";
    private String m_urlPath = "/jmxrmi";
    private String m_username = "opennms";
    private String m_password = "OPENNMS";

    protected JmxConfigDao m_jmxConfigDao = null;
    
    /**
     * <p>Constructor for AbstractJsr160Detector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected AbstractJsr160Detector(String serviceName, int port) {
        super(serviceName, port);
        // TODO Auto-generated constructor stub
    }

    /** 
     * @throws IOException 
     */
    @Override
    protected JmxServerConnectionWrapper connect(final InetAddress address, final int port, final int timeout) throws MalformedURLException, IOException {
        if (m_jmxConfigDao == null) {
            m_jmxConfigDao = BeanUtils.getBean("daoContext", "jmxConfigDao", JmxConfigDao.class);
        }

        Map<String, String> props = Maps.newHashMap();
        props.put("port", String.valueOf(port));
        props.put("timeout", String.valueOf(timeout));
        props.put("factory", getFactory());
        props.put("friendlyname", getFriendlyName());
        props.put("username", getUsername());
        props.put("password", getPassword());
        props.put("urlPath", getUrlPath());
        props.put("type", getType());
        props.put("protocol", getProtocol());

        final MBeanServer server = m_jmxConfigDao.getConfig().lookupMBeanServer(InetAddressUtils.str(address), port);
        if (server != null) {
            props.putAll(server.getParameterMap());
        }

        // TODO: Refactor this to use the same code as 
        // {@link org.opennms.netmgt.jmx.impl.connection.connectors.DefaultJmxConnector}

        // If remote JMX access is enabled, this will return a non-null value
        String jmxPort = System.getProperty(JmxServerConnector.JMX_PORT_SYSTEM_PROPERTY);

        if (
            address != null && 
            // If we're trying to create a connection to a localhost address...
            address.isLoopbackAddress() &&
            (
                // If the port matches the port of the current JVM...
                String.valueOf(port).equals(jmxPort) ||
                // Or if remote JMX RMI is disabled and we're attempting to connect
                // to the default OpenNMS JMX port...
                (jmxPort == null && JmxServerConnector.DEFAULT_OPENNMS_JMX_PORT.equals(String.valueOf(port)))
            )
        ) {
            // ...then use the {@link PlatformMBeanServerConnector} to connect to 
            // this JVM's MBeanServer directly.
            try {
                return new PlatformMBeanServerConnector().createConnection(address, props);
            } catch (JmxServerConnectionException e) {
                throw new ConnectException(e.getMessage());
            }
        }

        return Jsr160ConnectionFactory.getMBeanServerConnection(props, address);
    }

    /**
     * <p>setFactory</p>
     *
     * @param factory a {@link java.lang.String} object.
     */
    public void setFactory(String factory) {
        m_factory = factory;
    }

    /**
     * <p>getFactory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFactory() {
        return m_factory;
    }

    /**
     * <p>setFriendlyName</p>
     *
     * @param friendlyName a {@link java.lang.String} object.
     */
    public void setFriendlyName(String friendlyName) {
        m_friendlyName = friendlyName;
    }

    /**
     * <p>getFriendlyName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFriendlyName() {
        return m_friendlyName;
    }

    /**
     * <p>setProtocol</p>
     *
     * @param protocol a {@link java.lang.String} object.
     */
    public void setProtocol(String protocol) {
        m_protocol = protocol;
    }

    /**
     * <p>getProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProtocol() {
        return m_protocol;
    }

    /**
     * <p>setType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        m_type = type;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * <p>setUrlPath</p>
     *
     * @param urlPath a {@link java.lang.String} object.
     */
    public void setUrlPath(String urlPath) {
        m_urlPath = urlPath;
    }

    /**
     * <p>getUrlPath</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrlPath() {
        return m_urlPath;
    }

    /**
     * <p>setUsername</p>
     *
     * @param username a {@link java.lang.String} object.
     */
    public void setUsername(String username) {
        m_username = username;
    }

    /**
     * <p>getUsername</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUsername() {
        return m_username;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_password;
    }

    public JmxConfigDao getJmxConfigDao() {
        return m_jmxConfigDao;
    }

    public void setJmxConfigDao(final JmxConfigDao jmxConfigDao) {
        this.m_jmxConfigDao = jmxConfigDao;
    }
}
