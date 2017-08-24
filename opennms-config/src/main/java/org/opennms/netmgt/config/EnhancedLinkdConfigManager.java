/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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


import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;

/**
 * <p>Abstract LinkdConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
abstract public class EnhancedLinkdConfigManager implements EnhancedLinkdConfig {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    /**
	 * Object containing all EnhancedLinkd-configuration objects parsed from the XML
	 * file
	 */
	protected static EnlinkdConfiguration m_config;
	 
    /**
     * <p>Constructor for LinkdConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public EnhancedLinkdConfigManager() {
    }

    public Lock getReadLock() {
        return m_readLock;
    }
    
    public Lock getWriteLock() {
        return m_writeLock;
    }


    /**
     * Return the linkd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.linkd.LinkdConfiguration} object.
     */
    public EnlinkdConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

    
    /**
     * <p>useCdpDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useCdpDiscovery() {
        if (m_config.getUseCdpDiscovery() != null) return m_config.getUseCdpDiscovery();
        return true;
    }
    
    /**
     * <p>useBridgeDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useBridgeDiscovery() {
        if (m_config.getUseBridgeDiscovery() != null) return m_config.getUseBridgeDiscovery();
        return true;
    }

    /**
     * <p>useLldpDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useLldpDiscovery() {
        if (m_config.getUseLldpDiscovery() != null) return m_config.getUseLldpDiscovery();
        return true;
    }

    /**
     * <p>useOspfDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useOspfDiscovery() {
        if (m_config.getUseOspfDiscovery() != null) return m_config.getUseOspfDiscovery();
        return true;
    }

    /**
     * <p>useIsisDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useIsisDiscovery() {
        if (m_config.getUseIsisDiscovery() != null) return m_config.getUseIsisDiscovery();
        return true;
    }
    

    public long getInitialSleepTime() {
        return m_config.getInitialSleepTime();
    }

    public long getRescanInterval() {
        return m_config.getRescanInterval();
    }

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads() {
        if (m_config.getThreads() != null) return m_config.getThreads();
        return 5;
    }
    
    public int getMaxBft() {
        if (m_config.getMaxBft() != null) return m_config.getMaxBft();
        return 1;
    }

    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(final String xml) throws IOException;
    
}
