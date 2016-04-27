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
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.NameNotFoundException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Abstract JMXDetector class.</p>
 *
 * @author ranger, fooker
 * @version $Id: $
 */
public abstract class JMXDetector extends SyncAbstractDetector {

    private static final Logger LOG = LoggerFactory.getLogger(JMXDetector.class);

    /**
     * The object name to check for, or {@code null} if no check should be performed.
     */
    private String m_object = null;

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

    abstract protected JmxServerConnectionWrapper connect(final InetAddress address, final int port, final int timeout) throws ConnectException, IOException, MalformedURLException;

    /** {@inheritDoc} */
    @Override
    public final boolean isServiceDetected(final InetAddress address) {
        final String ipAddr = InetAddressUtils.str(address);

        final int port = getPort();
        final int retries = getRetries();
        final int timeout = getTimeout();
        LOG.info("isServiceDetected: {}: Checking address: {} for capability on port {}", getServiceName(), ipAddr, port);

        for (int attempts = 0; attempts < retries; attempts++) {

            try (final JmxServerConnectionWrapper client = this.connect(address, port, timeout)) {
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
    protected void onInit() {
        // Do nothing by default
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
}
