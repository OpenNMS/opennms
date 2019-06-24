/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.config.eventd.EventdConfiguration;

/**
 * <p>EventdConfigManager class.</p>
 *
 * @author david
 */
public class EventdConfigManager implements EventdConfig {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * The config class loaded from the config file
     */
    protected EventdConfiguration m_config;

    /**
     * <p>Constructor for EventdConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public EventdConfigManager() throws IOException {
        reload();
    }
    
    EventdConfigManager(final InputStream stream) throws IOException {
        try (final InputStreamReader isr = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(EventdConfiguration.class, isr);
        }
    }
    
    private void reload() throws IOException {
        try (final Reader r = new FileReader(ConfigFileConstants.getFile(ConfigFileConstants.EVENTD_CONFIG_FILE_NAME))) {
            m_config = JaxbUtils.unmarshal(EventdConfiguration.class, r);           
        }
    }

    public Lock getReadLock() {
        return m_readLock;
    }
    
    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * Return the IP address on which eventd listens for TCP connections.
     *
     * @return the IP address on which eventd listens for TCP connections
     */
    public String getTCPIpAddress() {
        getReadLock().lock();
        try {
            return m_config.getTCPAddress().orElse(null);
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * Return the port on which eventd listens for TCP connections.
     *
     * @return the port on which eventd listens for TCP connections
     */
    public int getTCPPort() {
        getReadLock().lock();
        try {
            return m_config.getTCPPort();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the IP address on which eventd listens for UDP packets.
     *
     * @return the IP address on which eventd listens for UDP packets
     */
    public String getUDPIpAddress() {
        getReadLock().lock();
        try {
            return m_config.getUDPAddress().orElse(null);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the port on which eventd listens for UDP data.
     *
     * @return the port on which eventd listens for UDP data
     */
    public int getUDPPort() {
        getReadLock().lock();
        try {
            return m_config.getUDPPort();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the number of event receivers to be started.
     *
     * @return the number of event receivers to be started
     */
    public int getReceivers() {
        getReadLock().lock();
        try {
            return m_config.getReceivers();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the length of the incoming event queue.
     *
     * @return the maximum number of events that can be stored in the incoming event queue
     */
    public int getQueueLength() {
        getReadLock().lock();
        try {
            return m_config.getQueueLength().orElse(Integer.MAX_VALUE);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return string indicating if timeout is to be set on the socket.
     *
     * @return string indicating if timeout is to be set on the socket
     */
    public String getSocketSoTimeoutRequired() {
        getReadLock().lock();
        try {
            return m_config.getSocketSoTimeoutRequired();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return timeout to be set on the socket.
     *
     * @return timeout is to be set on the socket
     */
    public int getSocketSoTimeoutPeriod() {
        getReadLock().lock();
        try {
            return m_config.getSocketSoTimeoutPeriod().orElse(0);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return flag indicating if timeout to be set on the socket is specified.
     *
     * @return flag indicating if timeout to be set on the socket is specified
     */
    public boolean hasSocketSoTimeoutPeriod() {
        getReadLock().lock();
        try {
            return m_config.getSocketSoTimeoutPeriod().isPresent();
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * Whether or not Eventd should log event summaries.
     */
    public boolean shouldLogEventSummaries() {
        getReadLock().lock();
        try {
            return m_config.getLogEventSummaries() == null? false : m_config.getLogEventSummaries();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the SQL statement to get the next event ID.
     *
     * @deprecated This is only used when using {@link JdbcEventWriter}
     * so when we remove the JDBC implementation, we can get rid of this
     * class.
     * 
     * @return the SQL statement to get the next event ID
     */
    public String getGetNextEventID() {
        getReadLock().lock();
        try {
            return m_config.getGetNextEventID().orElse(null);
        } finally {
            getReadLock().unlock();
        }
    }
    
    @Override
    public int getNumThreads() {
        getReadLock().lock();
        try {
            if (m_config.getNumThreads() <= 0) {
                return Runtime.getRuntime().availableProcessors() * 2;
            } else {
                return m_config.getNumThreads();
            }
        } finally {
            getReadLock().unlock();
        }
    }

    @Override
    public int getQueueSize() {
        getReadLock().lock();
        try {
            return m_config.getQueueSize();
        } finally {
            getReadLock().unlock();
        }
    }

    @Override
    public int getBatchSize() {
        getReadLock().lock();
        try {
            return m_config.getBatchSize();
        } finally {
            getReadLock().unlock();
        }
    }

    @Override
    public int getBatchIntervalMs() {
        getReadLock().lock();
        try {
            return m_config.getBatchInterval();
        } finally {
            getReadLock().unlock();
        }
    }
}
