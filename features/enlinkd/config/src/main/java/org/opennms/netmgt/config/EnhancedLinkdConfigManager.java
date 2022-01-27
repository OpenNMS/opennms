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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;

/**
 * <p>Abstract LinkdConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public abstract class EnhancedLinkdConfigManager extends AbstractCmJaxbConfigDao<EnlinkdConfiguration> implements EnhancedLinkdConfig {
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();
    private final Lock readLock = globalLock.readLock();
    private final Lock writeLock = globalLock.writeLock();

    /**
     * Object containing all EnhancedLinkd-configuration objects parsed from the XML
     * file
     */
    protected static EnlinkdConfiguration config;

    /**
     * <p>Constructor for LinkdConfigManager.</p>
     *
     */
    protected EnhancedLinkdConfigManager() {
        super(EnlinkdConfiguration.class ,"Enlinkd-Configuration");
    }

    public Lock getReadLock() {
        return readLock;
    }

    public Lock getWriteLock() {
        return writeLock;
    }


    /**
     * Return the linkd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration} object.
     */
    public EnlinkdConfiguration getConfiguration() {
        getReadLock().lock();
        try {
            return config;
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
        if (config.getUseCdpDiscovery() != null) return config.getUseCdpDiscovery();
        return true;
    }

    /**
     * <p>useBridgeDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useBridgeDiscovery() {
        if (config.getUseBridgeDiscovery() != null) return config.getUseBridgeDiscovery();
        return true;
    }

    /**
     * <p>useLldpDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useLldpDiscovery() {
        if (config.getUseLldpDiscovery() != null) return config.getUseLldpDiscovery();
        return true;
    }

    /**
     * <p>useOspfDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useOspfDiscovery() {
        if (config.getUseOspfDiscovery() != null) return config.getUseOspfDiscovery();
        return true;
    }

    /**
     * <p>useIsisDiscovery</p>
     *
     * @return a boolean.
     */
    public boolean useIsisDiscovery() {
        if (config.getUseIsisDiscovery() != null) return config.getUseIsisDiscovery();
        return true;
    }

    public boolean disableBridgeVlanDiscovery() {
        return Boolean.TRUE.equals(config.getDisableBridgeVlanDiscovery());
    }

    public long getInitialSleepTime() {
        return config.getInitialSleepTime();
    }

    public long getRescanInterval() {
        return config.getRescanInterval();
    }

    public long getBridgeTopologyInterval() {
        return config.getBridgeTopologyInterval();
    }

    public long getTopologyInterval() {
        return config.getTopologyInterval();
    }


    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads() {
        if (config.getThreads() != null) return config.getThreads();
        return 5;
    }

    public int getMaxBft() {
        if (config.getMaxBft() != null) return config.getMaxBft();
        return 100;
    }

    public int getDiscoveryBridgeThreads() {
        if (config.getDiscoveryBridgeThreads() != null) return config.getDiscoveryBridgeThreads();
        return 1;
    }
}
