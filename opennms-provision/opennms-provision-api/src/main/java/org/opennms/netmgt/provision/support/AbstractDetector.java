/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.support;

import org.opennms.netmgt.provision.ServiceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AbstractDetector class.</p>
 *
 * @author ranger
 */
public abstract class AbstractDetector implements ServiceDetector {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDetector.class);
    
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_RETRIES = 1;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_serviceName;
    
    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected AbstractDetector(final String serviceName, final int port, final int timeout, final int retries) {
        m_serviceName = serviceName;
        m_port = port;
        m_timeout = timeout;
        m_retries = retries;
    }

    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected AbstractDetector(final String serviceName, final int port) {
        this(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * <p>init</p>
     */
    @Override
    public final void init() {
        if (m_serviceName == null || m_timeout < -1) {
            throw new IllegalStateException(String.format("ServiceName is null or timeout of %d is invalid. Timeout must be > -1", m_timeout));
        }
        onInit();
    }
    
    /**
     * <p>onInit</p>
     */
    abstract protected void onInit();
    
    /**
     * <p>dispose</p>
     */
    @Override
    abstract public void dispose();
    
    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    @Override
    public final void setPort(final int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    @Override
    public final int getPort() {
        return m_port;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(final int retries) {
        m_retries = retries;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    @Override
    public final void setTimeout(final int timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    @Override
    public final int getTimeout() {
        return m_timeout;
    }

    /** {@inheritDoc} */
    @Override
    public final void setServiceName(final String serviceName) {
        m_serviceName = serviceName;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getServiceName() {
        return m_serviceName;
    }

}
