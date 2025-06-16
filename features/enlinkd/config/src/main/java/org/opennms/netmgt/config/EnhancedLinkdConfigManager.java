/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
     * @return a {@link org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration} object.
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

    public boolean disableBridgeVlanDiscovery() {
        return Boolean.TRUE.equals(m_config.getDisableBridgeVlanDiscovery());
    }

    public long getInitialSleepTime() {
        return m_config.getInitialSleepTime();
    }

    public long getCdpRescanInterval() {
        return m_config.getCdpRescanInterval();
    }
    public long getLldpRescanInterval() {
        return m_config.getLldpRescanInterval();
    }
    public long getBridgeRescanInterval() {
        return m_config.getBridgeRescanInterval();
    }
    public long getOspfRescanInterval() {
        return m_config.getOspfRescanInterval();
    }
    public long getIsisRescanInterval() {
        return m_config.getIsisRescanInterval();
    }
    public int getCdpPriority() {
        return m_config.getCdpPriority();
    }
    public int getLldpPriority() {
        return m_config.getLldpPriority();
    }
    public int getBridgePriority() {
        return m_config.getBridgePriority();
    }
    public int getOspfPriority() {
        return m_config.getOspfPriority();
    }
    public int getIsisPriority() {
        return m_config.getIsisPriority();
    }

    public long getBridgeTopologyInterval() {
        return m_config.getBridgeTopologyInterval();
    }

    public long getTopologyInterval() {
        return m_config.getTopologyInterval();
    }


    /**
     * <p>getExecutorThreads</p>
     *
     * @return a int.
     */
    public int getExecutorThreads() {
        if (m_config.getExecutorThreads() != null) return m_config.getExecutorThreads();
        return 5;
    }

    /**
     * <p>getExecutorQueueSize</p>
     *
     * @return a int.
     */
    public int getExecutorQueueSize() {
        if (m_config.getExecutorQueueSize() != null) return m_config.getExecutorQueueSize();
        return 100;
    }

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    public int getThreads() {
        if (m_config.getThreads() != null) return m_config.getThreads();
        return 3;
    }

    public int getMaxBft() {
        if (m_config.getMaxBft() != null) return m_config.getMaxBft();
        return 100;
    }

    public int getDiscoveryBridgeThreads() {
        if (m_config.getDiscoveryBridgeThreads() != null) return m_config.getDiscoveryBridgeThreads();
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
