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
package org.opennms.netmgt.config.enlinkd;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the enlinkd-configuration.xml
 *  configuration file.
 */
@XmlRootElement(name = "enlinkd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("enlinkd-configuration.xsd")
public class EnlinkdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Max number of threads used for executing data collection.
     */
    @XmlAttribute(name = "executor-threads", required = true)
    private Integer m_executorThreads;

    /**
     * The initial queue size of Priority Executor.
     */
    @XmlAttribute(name = "executor-queue-size", required = true)
    private Integer m_executorQueueSize;

    /**
     * Max number of threads used for executing data collection.
     */
    @XmlAttribute(name = "threads", required = true)
    private Integer m_schedulerThreads;

    /**
     * The number of threads used for calculate bridge topology
     */
    @XmlAttribute(name = "discovery-bridge-threads")
    private Integer m_discoveryBridgeThreads;

    /**
     * The initial sleep time in mill seconds before starting
     *  node Link Discovery.
     */
    @XmlAttribute(name = "initial_sleep_time")
    private Long m_initialSleepTime;

    /**
     *  Bridge Topology Discovery Time interval in mill seconds.
     */
    @XmlAttribute(name = "bridge_topology_interval")
    private Long m_bridgeTopologyInterval;

    /**
     *  Topology Updater scheduled Time interval in mill seconds.
     */
    @XmlAttribute(name = "topology_interval")
    private Long m_topologyUpdaterInterval;

    /**
     * The priority used for executing cdp data collection
     */
    @XmlAttribute(name = "cdp-priority")
    private Integer m_cdpPriority;

    /**
     *  Cdp Data Collection scheduled Time interval in mill seconds.
     */
    @XmlAttribute(name = "cdp_rescan_interval")
    private Long m_cdpRescanInterval;

    /**
     * Whether links discovery process should use
     *  cisco discovery protocol cache table.
     */
    @XmlAttribute(name = "use-cdp-discovery")
    private Boolean m_useCdpDiscovery;

    /**
     * The priority used for executing cdp data collection
     */
    @XmlAttribute(name = "bridge-priority")
    private Integer m_bridgePriority;

    /**
     *  Cdp Data Collection scheduled Time interval in mill seconds.
     */
    @XmlAttribute(name = "bridge_rescan_interval")
    private Long m_bridgeRescanInterval;


    /**
     * Whether links discovery process should use
     *  Bridge mib data.
     */
    @XmlAttribute(name = "use-bridge-discovery")
    private Boolean m_useBridgeDiscovery;

    /**
     * The priority used for executing lldp data collection
     */
    @XmlAttribute(name = "lldp-priority")
    private Integer m_lldpPriority;

    /**
     *  Lldp Data Collection scheduled Time interval in mill seconds.
     */
    @XmlAttribute(name = "lldp_rescan_interval")
    private Long m_lldpRescanInterval;

    /**
     * Whether links discovery process should use
     *  lldp mib data.
     */
    @XmlAttribute(name = "use-lldp-discovery")
    private Boolean m_useLldpDiscovery;

    /**
     * The priority used for executing ospf data collection
     */
    @XmlAttribute(name = "ospf-priority")
    private Integer m_ospfPriority;

    /**
     *  Cdp Data Collection scheduled Time interval in mill seconds.
     */
    @XmlAttribute(name = "ospf_rescan_interval")
    private Long m_ospfRescanInterval;

    /**
     * Whether links discovery process should use
     *  ospf mib data.
     */
    @XmlAttribute(name = "use-ospf-discovery")
    private Boolean m_useOspfDiscovery;

    /**
     * The priority used for executing cdp data collection
     */
    @XmlAttribute(name = "isis-priority")
    private Integer m_isisPriority;

    /**
     *  Cdp Data Collection scheduled Time interval in mill seconds.
     */
    @XmlAttribute(name = "isis_rescan_interval")
    private Long m_isisRescanInterval;

    /**
     * Whether links discovery process should use
     *  isis mib data.
     */
    @XmlAttribute(name = "use-isis-discovery")
    private Boolean m_useIsisDiscovery;

    /**
     * Set to true to skip VLAN enumeration and scanning during bridge discovery
     */
    @XmlAttribute(name = "disable-bridge-vlan-discovery")
    private Boolean m_disableBridgeVlanDiscovery;

    /**
     * Max bridge forwarding table to hold in memory.
     */
    @XmlAttribute(name = "max_bft")
    private Integer m_maxBft;

    public EnlinkdConfiguration() {
    }

    public Integer getExecutorThreads() {
        return m_executorThreads;
    }

    public void setExecutorThreads(final Integer executorThreads) {
        m_executorThreads = ConfigUtils.assertNotNull(executorThreads, "executorThreads");
    }

    public Integer getExecutorQueueSize() {
        return m_executorQueueSize;
    }

    public void setExecutorQueueSize(final Integer executorQueueSize) {
        m_executorQueueSize = ConfigUtils.assertNotNull(executorQueueSize, "executorQueueSize");
    }

    public Integer getThreads() {
        return m_schedulerThreads;
    }

    public void setThreads(final Integer schedulerThreads) {
        m_schedulerThreads = ConfigUtils.assertNotNull(schedulerThreads, "schedulerThreads");
    }

    public Long getInitialSleepTime() {
        return m_initialSleepTime == null? 60000L : m_initialSleepTime;
    }

    public void setInitialSleepTime(final Long initialSleepTime) {
        m_initialSleepTime = initialSleepTime;
    }

    public Long getCdpRescanInterval() {
        return m_cdpRescanInterval == null? 86400000L : m_cdpRescanInterval;
    }

    public void setCdpRescanInterval(final Long rescanInterval) {
        m_cdpRescanInterval = rescanInterval;
    }

    public Long getLldpRescanInterval() {
        return m_lldpRescanInterval == null? 86400000L : m_lldpRescanInterval;
    }

    public void setLldpRescanInterval(final Long rescanInterval) {
        m_lldpRescanInterval = rescanInterval;
    }

    public Long getBridgeRescanInterval() {
        return m_bridgeRescanInterval == null? 86400000L : m_bridgeRescanInterval;
    }

    public void setBridgeRescanInterval(final Long rescanInterval) {
        m_bridgeRescanInterval = rescanInterval;
    }

    public Long getOspfRescanInterval() {
        return m_ospfRescanInterval == null? 86400000L : m_ospfRescanInterval;
    }

    public void setOspfRescanInterval(final Long rescanInterval) {
        m_ospfRescanInterval = rescanInterval;
    }

    public Long getIsisRescanInterval() {
        return m_isisRescanInterval == null? 86400000L : m_isisRescanInterval;
    }

    public void setIsisRescanInterval(final Long rescanInterval) {
        m_isisRescanInterval = rescanInterval;
    }


    public Long getBridgeTopologyInterval() {
        return m_bridgeTopologyInterval == null? 300000L : m_bridgeTopologyInterval;
    }

    public void setBridgeTopologyInterval(Long bridgeTopologyInterval) {
        m_bridgeTopologyInterval = bridgeTopologyInterval;
    }

    public Long getTopologyInterval() {
        return m_topologyUpdaterInterval == null? 30000L : m_topologyUpdaterInterval;
    }

    public void setTopologyInterval(Long topologyInterval) {
        m_topologyUpdaterInterval = topologyInterval;
    }

    public Integer getMaxBft() {
        return m_maxBft != null ? m_maxBft : 100;
    }

    public void setMaxBft(final Integer maxBft) {
        m_maxBft = maxBft;
    }

    public Integer getDiscoveryBridgeThreads() {
        return m_discoveryBridgeThreads != null ? m_discoveryBridgeThreads : 1;
    }

    public void setDiscoveryBridgeThreads(Integer discoveryBridgeThreads) {
        m_discoveryBridgeThreads = discoveryBridgeThreads;
    }

    public Boolean getUseCdpDiscovery() {
        return m_useCdpDiscovery != null ? m_useCdpDiscovery : true;
    }

    public void setUseCdpDiscovery(final Boolean useCdpDiscovery) {
        m_useCdpDiscovery = useCdpDiscovery;
    }

    public Boolean getUseBridgeDiscovery() {
        return m_useBridgeDiscovery != null ? m_useBridgeDiscovery : true;
    }

    public void setUseBridgeDiscovery(final Boolean useBridgeDiscovery) {
        m_useBridgeDiscovery = useBridgeDiscovery;
    }

    public Boolean getUseLldpDiscovery() {
        return m_useLldpDiscovery != null ? m_useLldpDiscovery : true;
    }

    public void setUseLldpDiscovery(final Boolean useLldpDiscovery) {
        this.m_useLldpDiscovery = useLldpDiscovery;
    }

    public Boolean getUseOspfDiscovery() {
        return m_useOspfDiscovery != null ? m_useOspfDiscovery : true;
    }

    public void setUseOspfDiscovery(final Boolean useOspfDiscovery) {
        m_useOspfDiscovery = useOspfDiscovery;
    }

    public Boolean getUseIsisDiscovery() {
        return m_useIsisDiscovery != null ? m_useIsisDiscovery : true;
    }

    public void setUseIsisDiscovery(final Boolean useIsisDiscovery) {
        m_useIsisDiscovery = useIsisDiscovery;
    }

    public Boolean getDisableBridgeVlanDiscovery() {
        return m_disableBridgeVlanDiscovery;
    }

    public void setDisableBridgeVlanDiscovery(Boolean disableBridgeVlanDiscovery) {
        this.m_disableBridgeVlanDiscovery = disableBridgeVlanDiscovery;
    }

    public Integer getCdpPriority() {
        return m_cdpPriority != null ? m_cdpPriority : 1000;
    }

    public void setCdpPriority(Integer cdpPriority) {
        m_cdpPriority = cdpPriority;
    }

    public Integer getBridgePriority() {
        return m_bridgePriority != null ? m_bridgePriority : 10000;
    }

    public void setBridgePriority(Integer bridgePriority) {
        m_bridgePriority = bridgePriority;
    }

    public Integer getLldpPriority() {
        return m_lldpPriority != null ? m_lldpPriority : 2000;
    }

    public void setLldpPriority(Integer lldpPriority) {
        m_lldpPriority = lldpPriority;
    }

    public Integer getOspfPriority() {
        return m_ospfPriority != null ? m_ospfPriority : 3000;
    }

    public void setOspfPriority(Integer ospfPriority) {
        m_ospfPriority = ospfPriority;
    }

    public Integer getIsisPriority() {
        return m_isisPriority != null ? m_isisPriority : 4000;
    }

    public void setIsisPriority(Integer isisPriority) {
        m_isisPriority = isisPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnlinkdConfiguration that = (EnlinkdConfiguration) o;

        if (!m_executorThreads.equals(that.m_executorThreads)) return false;
        if (!m_executorQueueSize.equals(that.m_executorQueueSize)) return false;
        if (!m_schedulerThreads.equals(that.m_schedulerThreads)) return false;
        if (!Objects.equals(m_discoveryBridgeThreads, that.m_discoveryBridgeThreads))
            return false;
        if (!Objects.equals(m_initialSleepTime, that.m_initialSleepTime))
            return false;
        if (!Objects.equals(m_bridgeTopologyInterval, that.m_bridgeTopologyInterval))
            return false;
        if (!Objects.equals(m_topologyUpdaterInterval, that.m_topologyUpdaterInterval))
            return false;
        if (!Objects.equals(m_cdpPriority, that.m_cdpPriority))
            return false;
        if (!Objects.equals(m_cdpRescanInterval, that.m_cdpRescanInterval))
            return false;
        if (!Objects.equals(m_useCdpDiscovery, that.m_useCdpDiscovery))
            return false;
        if (!Objects.equals(m_bridgePriority, that.m_bridgePriority))
            return false;
        if (!Objects.equals(m_bridgeRescanInterval, that.m_bridgeRescanInterval))
            return false;
        if (!Objects.equals(m_useBridgeDiscovery, that.m_useBridgeDiscovery))
            return false;
        if (!Objects.equals(m_lldpPriority, that.m_lldpPriority))
            return false;
        if (!Objects.equals(m_lldpRescanInterval, that.m_lldpRescanInterval))
            return false;
        if (!Objects.equals(m_useLldpDiscovery, that.m_useLldpDiscovery))
            return false;
        if (!Objects.equals(m_ospfPriority, that.m_ospfPriority))
            return false;
        if (!Objects.equals(m_ospfRescanInterval, that.m_ospfRescanInterval))
            return false;
        if (!Objects.equals(m_useOspfDiscovery, that.m_useOspfDiscovery))
            return false;
        if (!Objects.equals(m_isisPriority, that.m_isisPriority))
            return false;
        if (!Objects.equals(m_isisRescanInterval, that.m_isisRescanInterval))
            return false;
        if (!Objects.equals(m_useIsisDiscovery, that.m_useIsisDiscovery))
            return false;
        if (!Objects.equals(m_disableBridgeVlanDiscovery, that.m_disableBridgeVlanDiscovery))
            return false;
        return Objects.equals(m_maxBft, that.m_maxBft);
    }

    @Override
    public int hashCode() {
        int result = m_executorThreads.hashCode();
        result = 31 * result + m_executorQueueSize.hashCode();
        result = 31 * result + m_schedulerThreads.hashCode();
        result = 31 * result + (m_discoveryBridgeThreads != null ? m_discoveryBridgeThreads.hashCode() : 0);
        result = 31 * result + (m_initialSleepTime != null ? m_initialSleepTime.hashCode() : 0);
        result = 31 * result + (m_bridgeTopologyInterval != null ? m_bridgeTopologyInterval.hashCode() : 0);
        result = 31 * result + (m_topologyUpdaterInterval != null ? m_topologyUpdaterInterval.hashCode() : 0);
        result = 31 * result + (m_cdpPriority != null ? m_cdpPriority.hashCode() : 0);
        result = 31 * result + (m_cdpRescanInterval != null ? m_cdpRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useCdpDiscovery != null ? m_useCdpDiscovery.hashCode() : 0);
        result = 31 * result + (m_bridgePriority != null ? m_bridgePriority.hashCode() : 0);
        result = 31 * result + (m_bridgeRescanInterval != null ? m_bridgeRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useBridgeDiscovery != null ? m_useBridgeDiscovery.hashCode() : 0);
        result = 31 * result + (m_lldpPriority != null ? m_lldpPriority.hashCode() : 0);
        result = 31 * result + (m_lldpRescanInterval != null ? m_lldpRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useLldpDiscovery != null ? m_useLldpDiscovery.hashCode() : 0);
        result = 31 * result + (m_ospfPriority != null ? m_ospfPriority.hashCode() : 0);
        result = 31 * result + (m_ospfRescanInterval != null ? m_ospfRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useOspfDiscovery != null ? m_useOspfDiscovery.hashCode() : 0);
        result = 31 * result + (m_isisPriority != null ? m_isisPriority.hashCode() : 0);
        result = 31 * result + (m_isisRescanInterval != null ? m_isisRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useIsisDiscovery != null ? m_useIsisDiscovery.hashCode() : 0);
        result = 31 * result + (m_disableBridgeVlanDiscovery != null ? m_disableBridgeVlanDiscovery.hashCode() : 0);
        result = 31 * result + (m_maxBft != null ? m_maxBft.hashCode() : 0);
        return result;
    }
}
