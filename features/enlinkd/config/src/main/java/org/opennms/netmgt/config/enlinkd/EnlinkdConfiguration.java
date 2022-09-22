/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    @XmlAttribute(name = "threads", required = true)
    private Integer m_threads;

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
    private Long m_topologyInterval;

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
     *  Ospf Data Collection scheduled Time interval in mill seconds.
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
     *  IS-IS Data Collection scheduled Time interval in mill seconds.
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

    public Integer getThreads() {
        return m_threads;
    }

    public void setThreads(final Integer threads) {
        m_threads = ConfigUtils.assertNotNull(threads, "threads");
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
        return m_topologyInterval == null? 30000L : m_topologyInterval;
    }

    public void setTopologyInterval(Long topologyInterval) {
        m_topologyInterval = topologyInterval;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnlinkdConfiguration that = (EnlinkdConfiguration) o;

        if (!m_threads.equals(that.m_threads)) return false;
        if (!Objects.equals(m_discoveryBridgeThreads, that.m_discoveryBridgeThreads))
            return false;
        if (!Objects.equals(m_initialSleepTime, that.m_initialSleepTime))
            return false;
        if (!Objects.equals(m_bridgeTopologyInterval, that.m_bridgeTopologyInterval))
            return false;
        if (!Objects.equals(m_topologyInterval, that.m_topologyInterval))
            return false;
        if (!Objects.equals(m_cdpRescanInterval, that.m_cdpRescanInterval))
            return false;
        if (!Objects.equals(m_useCdpDiscovery, that.m_useCdpDiscovery))
            return false;
        if (!Objects.equals(m_bridgeRescanInterval, that.m_bridgeRescanInterval))
            return false;
        if (!Objects.equals(m_useBridgeDiscovery, that.m_useBridgeDiscovery))
            return false;
        if (!Objects.equals(m_lldpRescanInterval, that.m_lldpRescanInterval))
            return false;
        if (!Objects.equals(m_useLldpDiscovery, that.m_useLldpDiscovery))
            return false;
        if (!Objects.equals(m_ospfRescanInterval, that.m_ospfRescanInterval))
            return false;
        if (!Objects.equals(m_useOspfDiscovery, that.m_useOspfDiscovery))
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
        int result = m_threads.hashCode();
        result = 31 * result + (m_discoveryBridgeThreads != null ? m_discoveryBridgeThreads.hashCode() : 0);
        result = 31 * result + (m_initialSleepTime != null ? m_initialSleepTime.hashCode() : 0);
        result = 31 * result + (m_bridgeTopologyInterval != null ? m_bridgeTopologyInterval.hashCode() : 0);
        result = 31 * result + (m_topologyInterval != null ? m_topologyInterval.hashCode() : 0);
        result = 31 * result + (m_cdpRescanInterval != null ? m_cdpRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useCdpDiscovery != null ? m_useCdpDiscovery.hashCode() : 0);
        result = 31 * result + (m_bridgeRescanInterval != null ? m_bridgeRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useBridgeDiscovery != null ? m_useBridgeDiscovery.hashCode() : 0);
        result = 31 * result + (m_lldpRescanInterval != null ? m_lldpRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useLldpDiscovery != null ? m_useLldpDiscovery.hashCode() : 0);
        result = 31 * result + (m_ospfRescanInterval != null ? m_ospfRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useOspfDiscovery != null ? m_useOspfDiscovery.hashCode() : 0);
        result = 31 * result + (m_isisRescanInterval != null ? m_isisRescanInterval.hashCode() : 0);
        result = 31 * result + (m_useIsisDiscovery != null ? m_useIsisDiscovery.hashCode() : 0);
        result = 31 * result + (m_disableBridgeVlanDiscovery != null ? m_disableBridgeVlanDiscovery.hashCode() : 0);
        result = 31 * result + (m_maxBft != null ? m_maxBft.hashCode() : 0);
        return result;
    }
}
