//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Eliminate a warning. - dj@opennms.org
// 2006 Apr 27: Added support for pathOutageEnabled
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.rws.BaseUrl;
import org.opennms.netmgt.config.rws.RwsConfiguration;
import org.opennms.netmgt.config.rws.StandbyUrl;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.rancid.ConnectionProperties;

/**
 * <p>Abstract RWSConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class RWSConfigManager implements RWSConfig {
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
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    @Deprecated
    public RWSConfigManager(final Reader reader) throws MarshalException, ValidationException, IOException {
        reloadXML(reader);
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

    public Lock getReadLock() {
        return m_readLock;
    }
    
    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * <p>getBase</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    public ConnectionProperties getBase() {
        getReadLock().lock();
        try {
            LogUtils.debugf(this, "Connections used: %s%s", getBaseUrl().getServer_url(), getBaseUrl().getDirectory());
            LogUtils.debugf(this, "RWS timeout(sec): %d", getBaseUrl().getTimeout());
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
    public ConnectionProperties getNextStandBy() {
        if (! hasStandbyUrl()) return null; 

        getReadLock().lock();
        try {
            final StandbyUrl standByUrl = getNextStandbyUrl();
            LogUtils.debugf(this, "Connections used: %s%s", standByUrl.getServer_url(), standByUrl.getDirectory());
            LogUtils.debugf(this, "RWS timeout(sec): %d", standByUrl.getTimeout());
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
    public ConnectionProperties[] getStandBy() {
        return null;
    }

    
    /**
     * <p>getBaseUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.BaseUrl} object.
     */
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
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    @Deprecated
    protected void reloadXML(final Reader reader) throws MarshalException, ValidationException, IOException {
        getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshal(RwsConfiguration.class, reader, CastorUtils.PRESERVE_WHITESPACE);
        } finally {
            getWriteLock().unlock();
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
            m_config = CastorUtils.unmarshal(RwsConfiguration.class, stream, CastorUtils.PRESERVE_WHITESPACE);
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
