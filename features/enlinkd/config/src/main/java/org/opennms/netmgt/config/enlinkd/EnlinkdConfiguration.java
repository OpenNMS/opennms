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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the enlinkd-configuration.xml
 *  configuration file.
 */
public class EnlinkdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The max number of this.threads used for polling snmp
     *  devices and discovery links.
     */
    private Integer threads;

    /**
     * The initial sleep time in mill seconds before starting
     *  node Link Discovery.
     */
    @JsonProperty("initial_sleep_time")
    private Long initialSleepTime;

    /**
     * Node Link Discovery Rescan Time interval in millseconds.
     */
    @JsonProperty("rescan_interval")
    private Long rescanInterval;

    /**
     *  Bridge Topology Discovery Time interval in mill seconds.
     */
    @JsonProperty("bridge_topology_interval")
    private Long bridgeTopologyInterval;

    /**
     *  Topology Discovery Time interval in mill seconds.
     */
    @JsonProperty("topology_interval")
    private Long topologyInterval;

    /**
     * Max bridge forwarding table to hold in memory.
     */
    @JsonProperty("max_bft")
    private Integer maxBft;

    /**
     * The number of threads used for calculate bridge topology
     */
    private Integer discoveryBridgeThreads;

    /**
     * Whether links discovery process should use
     *  cisco discovery protocol cache table.
     */
    private Boolean useCdpDiscovery;

    /**
     * Whether links discovery process should use
     *  Bridge mib data.
     */
    private Boolean useBridgeDiscovery;

    /**
     * Whether links discovery process should use
     *  lldp mib data.
     */
    private Boolean useLldpDiscovery;

    /**
     * Whether links discovery process should use
     *  ospf mib data.
     */
    private Boolean useOspfDiscovery;

    /**
     * Whether links discovery process should use
     *  isis mib data.
     */
    private Boolean useIsisDiscovery;

    private Boolean disableBridgeVlanDiscovery;

    public EnlinkdConfiguration() {
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(final Integer threads) {
        this.threads = ConfigUtils.assertNotNull(threads, "threads");
    }

    public Long getInitialSleepTime() {
        return this.initialSleepTime == null? 60000l : this.initialSleepTime;
    }

    public void setInitialSleepTime(final Long initialSleepTime) {
        this.initialSleepTime = initialSleepTime;
    }

    public Long getRescanInterval() {
        return this.rescanInterval == null? 86400000l : this.rescanInterval;
    }

    public void setRescanInterval(final Long rescanInterval) {
        this.rescanInterval = rescanInterval;
    }

    public Long getBridgeTopologyInterval() {
        return this.bridgeTopologyInterval == null? 300000l : this.bridgeTopologyInterval;
    }

    public void setBridgeTopologyInterval(Long bridgeTopologyInterval) {
        this.bridgeTopologyInterval = bridgeTopologyInterval;
    }

    public Long getTopologyInterval() {
        return this.topologyInterval == null? 30000l : this.topologyInterval;
    }

    public void setTopologyInterval(Long topologyInterval) {
        this.topologyInterval = topologyInterval;
    }

    public Integer getMaxBft() {
        return this.maxBft != null ? this.maxBft : 100;
    }

    public void setMaxBft(final Integer maxBft) {
        this.maxBft = maxBft;
    }

    public Integer getDiscoveryBridgeThreads() {
        return this.discoveryBridgeThreads != null ? this.discoveryBridgeThreads : 1;
    }

    public void setDiscoveryBridgeThreads(Integer discoveryBridgeThreads) {
        this.discoveryBridgeThreads = discoveryBridgeThreads;
    }

    public Boolean getUseCdpDiscovery() {
        return this.useCdpDiscovery != null ? this.useCdpDiscovery : true;
    }

    public void setUseCdpDiscovery(final Boolean useCdpDiscovery) {
        this.useCdpDiscovery = useCdpDiscovery;
    }

    public Boolean getUseBridgeDiscovery() {
        return this.useBridgeDiscovery != null ? this.useBridgeDiscovery : true;
    }

    public void setUseBridgeDiscovery(final Boolean useBridgeDiscovery) {
        this.useBridgeDiscovery = useBridgeDiscovery;
    }

    public Boolean getUseLldpDiscovery() {
        return this.useLldpDiscovery != null ? this.useLldpDiscovery : true;
    }

    public void setUseLldpDiscovery(final Boolean useLldpDiscovery) {
        this.useLldpDiscovery = useLldpDiscovery;
    }

    public Boolean getUseOspfDiscovery() {
        return this.useOspfDiscovery != null ? this.useOspfDiscovery : true;
    }

    public void setUseOspfDiscovery(final Boolean useOspfDiscovery) {
        this.useOspfDiscovery = useOspfDiscovery;
    }

    public Boolean getUseIsisDiscovery() {
        return this.useIsisDiscovery != null ? this.useIsisDiscovery : true;
    }

    public void setUseIsisDiscovery(final Boolean useIsisDiscovery) {
        this.useIsisDiscovery = useIsisDiscovery;
    }

    public Boolean getDisableBridgeVlanDiscovery() {
        return this.disableBridgeVlanDiscovery;
    }

    public void setDisableBridgeVlanDiscovery(Boolean disableBridgeVlanDiscovery) {
        this.disableBridgeVlanDiscovery = disableBridgeVlanDiscovery;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(
            this.threads,
            this.initialSleepTime,
            this.rescanInterval,
            this.bridgeTopologyInterval,
            this.topologyInterval,
            this.maxBft,
            this.discoveryBridgeThreads,
            this.useCdpDiscovery,
            this.useBridgeDiscovery,
            this.useLldpDiscovery,
            this.useOspfDiscovery,
            this.useIsisDiscovery,
            this.disableBridgeVlanDiscovery);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof EnlinkdConfiguration) {
            final EnlinkdConfiguration that = (EnlinkdConfiguration)obj;
            return Objects.equals(this.threads, that.threads)
                && Objects.equals(this.initialSleepTime, that.initialSleepTime)
                && Objects.equals(this.rescanInterval, that.rescanInterval)
                && Objects.equals(this.bridgeTopologyInterval, that.bridgeTopologyInterval)
                && Objects.equals(this.topologyInterval, that.topologyInterval)
                && Objects.equals(this.maxBft, that.maxBft)
                && Objects.equals(this.discoveryBridgeThreads, that.discoveryBridgeThreads)
                && Objects.equals(this.useCdpDiscovery, that.useCdpDiscovery)
                && Objects.equals(this.useBridgeDiscovery, that.useBridgeDiscovery)
                && Objects.equals(this.useLldpDiscovery, that.useLldpDiscovery)
                && Objects.equals(this.useOspfDiscovery, that.useOspfDiscovery)
                && Objects.equals(this.useIsisDiscovery, that.useIsisDiscovery)
                && Objects.equals(this.disableBridgeVlanDiscovery, that.disableBridgeVlanDiscovery);
        }
        return false;
    }

}
