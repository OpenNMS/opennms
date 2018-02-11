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
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.util.Collections;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NameNotFoundException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.impl.connection.connectors.Jsr160ConnectionFactory;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.support.DetectResultsImpl;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


public class JMXDetector extends SyncAbstractDetector {

    private static final Logger LOG = LoggerFactory.getLogger(JMXDetector.class);

    /**
     * The object name to check for, or {@code null} if no check should be performed.
     */
    private String m_object = null;

    private String m_factory = "STANDARD";
    private String m_friendlyName = "jsr160";
    private String m_protocol = "rmi";
    private String m_type = "default";
    private String m_urlPath = "/jmxrmi";
    private String m_username = "opennms";
    private String m_password = "OPENNMS";
    private String m_url;

    /**
     * <p>Constructor for JMXDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected JMXDetector(String serviceName, int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>Constructor for JMXDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected JMXDetector(String serviceName, int port, int timeout, int retries) {
        super(serviceName, port, timeout, retries);
    }

    @Override
    public DetectResults detect(DetectRequest request) {
        return new DetectResultsImpl(isServiceDetected(request.getAddress(), request.getRuntimeAttributes()));
    }

    @Override
    public final boolean isServiceDetected(final InetAddress address) {
        return isServiceDetected(address, Collections.emptyMap());
    }

    public final boolean isServiceDetected(final InetAddress address, Map<String, String> runtimeAttributes) {
        final String ipAddr = InetAddressUtils.str(address);

        final int port = getPort();
        final int retries = getRetries();
        final int timeout = getTimeout();
        LOG.info("isServiceDetected: {}: Checking address: {} for capability on port {}", getServiceName(), ipAddr, port);

        for (int attempts = 0; attempts < retries; attempts++) {

            try (final JmxServerConnectionWrapper client = this.connect(address, port, timeout, runtimeAttributes)) {
                LOG.info("isServiceDetected: {}: Attempting to connect to address: {}, port: {}, attempt: #{}", getServiceName(), ipAddr, port, attempts);

                if (client.getMBeanServerConnection().getMBeanCount() <= 0) {
                    return false;
                }

                if (m_object != null) {
                    client.getMBeanServerConnection().getObjectInstance(new ObjectName(m_object));
                }

                return true;

            } catch (ConnectException e) {
                // Connection refused!! Continue to retry.
                LOG.info("isServiceDetected: {}: Unable to connect to address: {} port {}, attempt #{}",getServiceName(), ipAddr, port, attempts, e);
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                LOG.info("isServiceDetected: {}: No route to address {} was available", getServiceName(), ipAddr, e);
            } catch (final PortUnreachableException e) {
                // Port unreachable
                LOG.info("isServiceDetected: {}: Port unreachable while connecting to address {} port {} within timeout: {} attempt: {}", getServiceName(), ipAddr, port, timeout, attempts, e);
            } catch (InterruptedIOException e) {
                // Expected exception
                LOG.info("isServiceDetected: {}: Did not connect to address {} port {} within timeout: {} attempt: {}", getServiceName(), ipAddr, port, timeout, attempts, e);
            } catch (MalformedObjectNameException e) {
                LOG.info("isServiceDetected: {}: Object instance {} is not valid on address {} port {} within timeout: {} attempt: {}", getServiceName(), m_object, ipAddr, port, timeout, attempts, e);
            } catch (InstanceNotFoundException e) {
                LOG.info("isServiceDetected: {}: Object instance {} does not exists on address {} port {} within timeout: {} attempt: {}", getServiceName(), m_object, ipAddr, port, timeout, attempts, e);
            } catch (IOException e) {
                // NMS-8096: Because the JMX connections wrap lower-level exceptions in an IOException,
                // we need to unwrap the exceptions to provide INFO log messages about failures

                boolean loggedIt = false;

                // Unwrap exception
                Throwable cause = e.getCause();
                while (cause != null && loggedIt == false) {
                    if (cause instanceof ConnectException) {
                        // Connection refused!! Continue to retry.
                        LOG.info("isServiceDetected: {}: Unable to connect to address: {} port {}, attempt #{}",getServiceName(), ipAddr, port, attempts, e);
                        loggedIt = true;
                    } else if (cause instanceof NoRouteToHostException) {
                        // No Route to host!!!
                        LOG.info("isServiceDetected: {}: No route to address {} was available", getServiceName(), ipAddr, e);
                        loggedIt = true;
                    } else if (cause instanceof PortUnreachableException) {
                        // Port unreachable
                        LOG.info("isServiceDetected: {}: Port unreachable while connecting to address {} port {} within timeout: {} attempt: {}", getServiceName(), ipAddr, port, timeout, attempts, e);
                        loggedIt = true;
                    } else if (cause instanceof InterruptedIOException) {
                        // Expected exception
                        LOG.info("isServiceDetected: {}: Did not connect to address {} port {} within timeout: {} attempt: {}", getServiceName(), ipAddr, port, timeout, attempts, e);
                        loggedIt = true;
                    } else if (cause instanceof NameNotFoundException) {
                        LOG.info("isServiceDetected: {}: Name {} not found on address {} port {} within timeout: {} attempt: {}", getServiceName(), m_object, ipAddr, port, timeout, attempts, e);
                        loggedIt = true;
                    } else if (cause instanceof MalformedObjectNameException) {
                        LOG.info("isServiceDetected: {}: Object instance {} is not valid on address {} port {} within timeout: {} attempt: {}", getServiceName(), m_object, ipAddr, port, timeout, attempts, e);
                        loggedIt = true;
                    } else if (cause instanceof InstanceNotFoundException) {
                        LOG.info("isServiceDetected: {}: Object instance {} does not exists on address {} port {} within timeout: {} attempt: {}", getServiceName(), m_object, ipAddr, port, timeout, attempts, e);
                        loggedIt = true;
                    }

                    cause = cause.getCause();
                }

                if (!loggedIt) {
                    // If none of the causes are an expected type, log an error
                    LOG.error("isServiceDetected: {}: An unexpected I/O exception occured contacting address {} port {}",getServiceName(), ipAddr, port, e);
                }
            } catch (Throwable t) {
                LOG.error("isServiceDetected: {}: Unexpected error trying to detect {} on address {} port {}", getServiceName(), getServiceName(), ipAddr, port, t);
            }
        }
        return false;
    }

    @Override
    public void dispose(){
        // Do nothing by default
    }

    public String getObject() {
        return m_object;
    }

    public void setObject(final String object) {
        this.m_object = object;
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

    public String getUrl() {
        return m_url;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    @Override
    protected void onInit() {
        // Do nothing by default
    }

    protected JmxServerConnectionWrapper connect(final InetAddress address, final int port, final int timeout, final Map<String, String> runtimeAttributes) throws IOException {

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
        props.put("url", getUrl());
        // The runtime attributes contain the agent details pull from the JmxConfigDao
        props.putAll(runtimeAttributes);

        return Jsr160ConnectionFactory.getMBeanServerConnection(props, address);
    }
}
