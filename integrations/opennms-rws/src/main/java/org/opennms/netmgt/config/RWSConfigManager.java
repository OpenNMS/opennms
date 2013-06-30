/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.rws.BaseUrl;
import org.opennms.netmgt.config.rws.RwsConfiguration;
import org.opennms.netmgt.config.rws.StandbyUrl;
import org.opennms.rancid.ConnectionProperties;

/**
 * <p>Abstract RWSConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class RWSConfigManager implements RWSConfig {
    private static final Logger LOG = LoggerFactory.getLogger(RWSConfigManager.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    private int m_cursor = 0;

    private RwsConfiguration m_config;

    /**
     * <p>Constructor for RWSConfigManager.</p>
     */
    public RWSConfigManager() {
    }
    
    /**
     * <p>Constructor for RWSConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public RWSConfigManager(final InputStream stream) throws MarshalException, ValidationException, IOException {
        reloadXML(stream);
    }

    @Override
    public Lock getReadLock() {
        return m_readLock;
    }
    
    @Override
    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * <p>getBase</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    @Override
    public ConnectionProperties getBase() {
        getReadLock().lock();
        try {
            LOG.debug("Connections used: {}{}", getBaseUrl().getServer_url(), getBaseUrl().getDirectory());
            LOG.debug("RWS timeout(sec): {}", getBaseUrl().getTimeout());
            if (getBaseUrl().getUsername() == null) {
                return new ConnectionProperties(getBaseUrl().getServer_url(),getBaseUrl().getDirectory(),getBaseUrl().getTimeout());
            }
            String password = "";
            if (getBaseUrl().getPassword() != null)
                password = getBaseUrl().getPassword();
            return new ConnectionProperties(getBaseUrl().getUsername(),password,getBaseUrl().getServer_url(),getBaseUrl().getDirectory(),getBaseUrl().getTimeout());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getNextStandBy</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    @Override
    public ConnectionProperties getNextStandBy() {
        if (! hasStandbyUrl()) return null; 

        getReadLock().lock();
        try {
            final StandbyUrl standByUrl = getNextStandbyUrl();
            LOG.debug("Connections used: {}{}", standByUrl.getServer_url(), standByUrl.getDirectory());
            LOG.debug("RWS timeout(sec): {}", standByUrl.getTimeout());
            if (standByUrl.getUsername() == null) {
                return new ConnectionProperties(standByUrl.getServer_url(),standByUrl.getDirectory(),standByUrl.getTimeout());
            }
            String password = "";
            if (standByUrl.getPassword() != null) {
                password = standByUrl.getPassword();
            }
            return new ConnectionProperties(standByUrl.getUsername(),password,standByUrl.getServer_url(),standByUrl.getDirectory(),standByUrl.getTimeout());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getStandBy</p>
     *
     * @return an array of {@link org.opennms.rancid.ConnectionProperties} objects.
     */
    @Override
    public ConnectionProperties[] getStandBy() {
        return null;
    }

    
    /**
     * <p>getBaseUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.BaseUrl} object.
     */
    @Override
    public BaseUrl getBaseUrl() {
        getReadLock().lock();
        try {
            return m_config.getBaseUrl();
        } finally {
            getReadLock().unlock();
        }
    }
 
    /**
     * <p>getStanbyUrls</p>
     *
     * @return an array of {@link org.opennms.netmgt.config.rws.StandbyUrl} objects.
     */
    @Override
    public StandbyUrl[] getStanbyUrls() {
        getReadLock().lock();
        try {
            return m_config.getStandbyUrl();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getNextStandbyUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.StandbyUrl} object.
     */
    @Override
    public StandbyUrl getNextStandbyUrl() {
        getReadLock().lock();
        try {
            StandbyUrl standbyUrl = null;
            if (hasStandbyUrl()) {
                if (m_cursor == m_config.getStandbyUrlCount()) {
                    m_cursor = 0;
                }
                standbyUrl = m_config.getStandbyUrl(m_cursor++);
            }
            return standbyUrl;
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>hasStandbyUrl</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean hasStandbyUrl() {
        getReadLock().lock();
        try {
            return (m_config.getStandbyUrlCount() > 0);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>reloadXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected void reloadXML(final InputStream stream) throws MarshalException, ValidationException, IOException {
        getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshal(RwsConfiguration.class, stream);
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Return the poller configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.rws.RwsConfiguration} object.
     */
    public RwsConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }
}
