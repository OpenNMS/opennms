//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 02: Use CastorUtils.unmarshal and add IP address methods. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * <p>EventdConfigManager class.</p>
 *
 * @author david
 */
public class EventdConfigManager {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    /**
     * The config class loaded from the config file
     */
    protected EventdConfiguration m_config;

    /**
     * Constructor
     *
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @param reader a {@link java.io.Reader} object.
     */
    @Deprecated
    protected EventdConfigManager(final Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(EventdConfiguration.class, reader, CastorUtils.PRESERVE_WHITESPACE);
        reader.close();

    }
    
    /**
     * <p>Constructor for EventdConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected EventdConfigManager(final InputStream stream) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(EventdConfiguration.class, stream, CastorUtils.PRESERVE_WHITESPACE);

    }
    
    /**
     * <p>Constructor for EventdConfigManager.</p>
     *
     * @param configFile a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public EventdConfigManager(final String configFile) throws FileNotFoundException, MarshalException, ValidationException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            m_config = CastorUtils.unmarshal(EventdConfiguration.class, stream, CastorUtils.PRESERVE_WHITESPACE);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
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
            return m_config.getTCPAddress();
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
            return m_config.getUDPAddress();
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
            return m_config.getSocketSoTimeoutPeriod();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return flag indicating if timeout to be set on the socket is specified.
     *
     * @return flag indicating if timeout to be set on the socket is specified <
     */
    public boolean hasSocketSoTimeoutPeriod() {
        getReadLock().lock();
        try {
            return m_config.hasSocketSoTimeoutPeriod();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Return the SQL statement to get the next event ID.
     *
     * @return the SQL statement to get the next event ID
     */
    public String getGetNextEventID() {
        getReadLock().lock();
        try {
            return m_config.getGetNextEventID();
        } finally {
            getReadLock().unlock();
        }
    }
}
