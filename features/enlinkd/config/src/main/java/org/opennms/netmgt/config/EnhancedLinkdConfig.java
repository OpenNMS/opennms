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

import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;


/**
 * <p>EnhancedLinkdConfig interface.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 * @version $Id: $
 */
public interface EnhancedLinkdConfig {

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    int getThreads();

    /**
     * <p>getExecutorThreads</p>
     *
     * @return a int.
     */
    int getExecutorThreads();

    /**
     * <p>getExecutorQueueSize</p>
     *
     * @return a int.
     */
    int getExecutorQueueSize();

    /**
     * <p>getMaxBft</p>
     *
     * @return a int.
     */
    int getMaxBft();

    /**
     * <p>getDiscoveryBridgeThreads</p>
     *
     * @return a int.
     */
    int getDiscoveryBridgeThreads();

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    long getInitialSleepTime();

    /**
     * <p>getCdpRescanInterval</p>
     *
     * @return a long.
     */
    long getCdpRescanInterval();

    /**
     * <p>getCdpPriority</p>
     *
     * @return a int.
     */
    int getCdpPriority();

    /**
     * <p>getLldpRescanInterval</p>
     *
     * @return a long.
     */
    long getLldpRescanInterval();

    /**
     * <p>getLldpPriority</p>
     *
     * @return a int.
     */
    int getLldpPriority();

    /**
     * <p>getBridgeRescanInterval</p>
     *
     * @return a long.
     */
    long getBridgeRescanInterval();

    /**
     * <p>getBridgePriority</p>
     *
     * @return a int.
     */
    int getBridgePriority();

    /**
     * <p>getOspfRescanInterval</p>
     *
     * @return a long.
     */
    long getOspfRescanInterval();

    /**
     * <p>getOspfPriority</p>
     *
     * @return a int.
     */
    int getOspfPriority();

    /**
     * <p>getIsisRescanInterval</p>
     *
     * @return a long.
     */
    long getIsisRescanInterval();

    /**
     * <p>getIsisPriority</p>
     *
     * @return a int.
     */
    int getIsisPriority();

    /**
     * <p>getBridgeTopologyInterval</p>
     *
     * @return a long.
     */

    long getBridgeTopologyInterval();

    /**
     * <p>getTopologyUpdaterInterval</p>
     *
     * @return a long.
     */
    long getTopologyInterval();

    /**
     * <p>useCdpDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useCdpDiscovery();

    /**
     * <p>useBridgeDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useBridgeDiscovery();

    /**
     * <p>useLldpDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useLldpDiscovery();

    /**
     * <p>useOspfDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useOspfDiscovery();

    /**
     * <p>useIsisDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useIsisDiscovery();

    boolean disableBridgeVlanDiscovery();

    /**
     * <p>reload</p>
     * <p>Reload the configuration file<p>
     *
     * @throws java.io.IOException if any.
     */
    void reload() throws IOException;
    
    /**
     * <p>save</p>
     *
     * @throws java.io.IOException if any.
     */
    void save() throws IOException;

    /**
     * <p>getConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration} object.
     */
     EnlinkdConfiguration getConfiguration();    
        
    Lock getReadLock();

    Lock getWriteLock();

}
