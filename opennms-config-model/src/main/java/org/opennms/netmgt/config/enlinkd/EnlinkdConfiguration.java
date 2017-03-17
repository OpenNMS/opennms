/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the enlinkd-configuration.xml
 *  configuration file.
 */
@XmlRootElement(name = "enlinkd-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnlinkdConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The max number of threads used for polling snmp
     *  devices and discovery links.
     *  
     */
    @XmlAttribute(name = "threads", required = true)
    private Integer threads;

    /**
     * The initial sleep time in mill seconds before starting
     *  node Link Discovery.
     *  
     */
    @XmlAttribute(name = "initial_sleep_time", required = true)
    private Long initial_sleep_time;

    /**
     * Node Link Discovery Rescan Time interval in mill seconds.
     *  
     */
    @XmlAttribute(name = "rescan_interval", required = true)
    private Long rescan_interval;

    /**
     * Max bridge forwarding table to hold in memory.
     *  
     */
    @XmlAttribute(name = "max_bft")
    private Integer max_bft;

    /**
     * Whether links discovery process should use
     *  cisco discovery protocol cache table.
     *  
     */
    @XmlAttribute(name = "use-cdp-discovery")
    private Boolean useCdpDiscovery;

    /**
     * Whether links discovery process should use
     *  Bridge mib data.
     *  
     */
    @XmlAttribute(name = "use-bridge-discovery")
    private Boolean useBridgeDiscovery;

    /**
     * Whether links discovery process should use
     *  lldp mib data.
     *  
     */
    @XmlAttribute(name = "use-lldp-discovery")
    private Boolean useLldpDiscovery;

    /**
     * Whether links discovery process should use
     *  ospf mib data.
     *  
     */
    @XmlAttribute(name = "use-ospf-discovery")
    private Boolean useOspfDiscovery;

    /**
     * Whether links discovery process should use
     *  isis mib data.
     *  
     */
    @XmlAttribute(name = "use-isis-discovery")
    private Boolean useIsisDiscovery;

    public EnlinkdConfiguration() {
    }

    /**
     */
    public void deleteInitial_sleep_time() {
        this.initial_sleep_time= null;
    }

    /**
     */
    public void deleteMax_bft() {
        this.max_bft= null;
    }

    /**
     */
    public void deleteRescan_interval() {
        this.rescan_interval= null;
    }

    /**
     */
    public void deleteThreads() {
        this.threads= null;
    }

    /**
     */
    public void deleteUseBridgeDiscovery() {
        this.useBridgeDiscovery= null;
    }

    /**
     */
    public void deleteUseCdpDiscovery() {
        this.useCdpDiscovery= null;
    }

    /**
     */
    public void deleteUseIsisDiscovery() {
        this.useIsisDiscovery= null;
    }

    /**
     */
    public void deleteUseLldpDiscovery() {
        this.useLldpDiscovery= null;
    }

    /**
     */
    public void deleteUseOspfDiscovery() {
        this.useOspfDiscovery= null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof EnlinkdConfiguration) {
            EnlinkdConfiguration temp = (EnlinkdConfiguration)obj;
            boolean equals = Objects.equals(temp.threads, threads)
                && Objects.equals(temp.initial_sleep_time, initial_sleep_time)
                && Objects.equals(temp.rescan_interval, rescan_interval)
                && Objects.equals(temp.max_bft, max_bft)
                && Objects.equals(temp.useCdpDiscovery, useCdpDiscovery)
                && Objects.equals(temp.useBridgeDiscovery, useBridgeDiscovery)
                && Objects.equals(temp.useLldpDiscovery, useLldpDiscovery)
                && Objects.equals(temp.useOspfDiscovery, useOspfDiscovery)
                && Objects.equals(temp.useIsisDiscovery, useIsisDiscovery);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'initial_sleep_time'. The field
     * 'initial_sleep_time' has the following description: The initial sleep time
     * in mill seconds before starting
     *  node Link Discovery.
     *  
     * 
     * @return the value of field 'Initial_sleep_time'.
     */
    public Optional<Long> getInitialSleepTime() {
        return Optional.ofNullable(this.initial_sleep_time);
    }

    /**
     * Returns the value of field 'max_bft'. The field 'max_bft' has the following
     * description: Max bridge forwarding table to hold in memory.
     *  
     * 
     * @return the value of field 'Max_bft'.
     */
    public Integer getMaxBft() {
        return this.max_bft != null ? this.max_bft : Integer.valueOf("1");
    }

    /**
     * Returns the value of field 'rescan_interval'. The field 'rescan_interval'
     * has the following description: Node Link Discovery Rescan Time interval in
     * mill seconds.
     *  
     * 
     * @return the value of field 'Rescan_interval'.
     */
    public Long getRescanInterval() {
        return this.rescan_interval;
    }

    /**
     * Returns the value of field 'threads'. The field 'threads' has the following
     * description: The max number of threads used for polling snmp
     *  devices and discovery links.
     *  
     * 
     * @return the value of field 'Threads'.
     */
    public Integer getThreads() {
        return this.threads;
    }

    /**
     * Returns the value of field 'useBridgeDiscovery'. The field
     * 'useBridgeDiscovery' has the following description: Whether links discovery
     * process should use
     *  Bridge mib data.
     *  
     * 
     * @return the value of field 'UseBridgeDiscovery'.
     */
    public Boolean getUseBridgeDiscovery() {
        return this.useBridgeDiscovery != null ? this.useBridgeDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useCdpDiscovery'. The field 'useCdpDiscovery'
     * has the following description: Whether links discovery process should use
     *  cisco discovery protocol cache table.
     *  
     * 
     * @return the value of field 'UseCdpDiscovery'.
     */
    public Boolean getUseCdpDiscovery() {
        return this.useCdpDiscovery != null ? this.useCdpDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useIsisDiscovery'. The field 'useIsisDiscovery'
     * has the following description: Whether links discovery process should use
     *  isis mib data.
     *  
     * 
     * @return the value of field 'UseIsisDiscovery'.
     */
    public Boolean getUseIsisDiscovery() {
        return this.useIsisDiscovery != null ? this.useIsisDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useLldpDiscovery'. The field 'useLldpDiscovery'
     * has the following description: Whether links discovery process should use
     *  lldp mib data.
     *  
     * 
     * @return the value of field 'UseLldpDiscovery'.
     */
    public Boolean getUseLldpDiscovery() {
        return this.useLldpDiscovery != null ? this.useLldpDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useOspfDiscovery'. The field 'useOspfDiscovery'
     * has the following description: Whether links discovery process should use
     *  ospf mib data.
     *  
     * 
     * @return the value of field 'UseOspfDiscovery'.
     */
    public Boolean getUseOspfDiscovery() {
        return this.useOspfDiscovery != null ? this.useOspfDiscovery : Boolean.valueOf("true");
    }

    /**
     * Method hasInitial_sleep_time.
     * 
     * @return true if at least one Initial_sleep_time has been added
     */
    public boolean hasInitialSleepTime() {
        return this.initial_sleep_time != null;
    }

    /**
     * Method hasMax_bft.
     * 
     * @return true if at least one Max_bft has been added
     */
    public boolean hasMaxBft() {
        return this.max_bft != null;
    }

    /**
     * Method hasRescan_interval.
     * 
     * @return true if at least one Rescan_interval has been added
     */
    public boolean hasRescanInterval() {
        return this.rescan_interval != null;
    }

    /**
     * Method hasThreads.
     * 
     * @return true if at least one Threads has been added
     */
    public boolean hasThreads() {
        return this.threads != null;
    }

    /**
     * Method hasUseBridgeDiscovery.
     * 
     * @return true if at least one UseBridgeDiscovery has been added
     */
    public boolean hasUseBridgeDiscovery() {
        return this.useBridgeDiscovery != null;
    }

    /**
     * Method hasUseCdpDiscovery.
     * 
     * @return true if at least one UseCdpDiscovery has been added
     */
    public boolean hasUseCdpDiscovery() {
        return this.useCdpDiscovery != null;
    }

    /**
     * Method hasUseIsisDiscovery.
     * 
     * @return true if at least one UseIsisDiscovery has been added
     */
    public boolean hasUseIsisDiscovery() {
        return this.useIsisDiscovery != null;
    }

    /**
     * Method hasUseLldpDiscovery.
     * 
     * @return true if at least one UseLldpDiscovery has been added
     */
    public boolean hasUseLldpDiscovery() {
        return this.useLldpDiscovery != null;
    }

    /**
     * Method hasUseOspfDiscovery.
     * 
     * @return true if at least one UseOspfDiscovery has been added
     */
    public boolean hasUseOspfDiscovery() {
        return this.useOspfDiscovery != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            threads, 
            initial_sleep_time, 
            rescan_interval, 
            max_bft, 
            useCdpDiscovery, 
            useBridgeDiscovery, 
            useLldpDiscovery, 
            useOspfDiscovery, 
            useIsisDiscovery);
        return hash;
    }

    /**
     * Returns the value of field 'useBridgeDiscovery'. The field
     * 'useBridgeDiscovery' has the following description: Whether links discovery
     * process should use
     *  Bridge mib data.
     *  
     * 
     * @return the value of field 'UseBridgeDiscovery'.
     */
    public Boolean isUseBridgeDiscovery() {
        return this.useBridgeDiscovery != null ? this.useBridgeDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useCdpDiscovery'. The field 'useCdpDiscovery'
     * has the following description: Whether links discovery process should use
     *  cisco discovery protocol cache table.
     *  
     * 
     * @return the value of field 'UseCdpDiscovery'.
     */
    public Boolean isUseCdpDiscovery() {
        return this.useCdpDiscovery != null ? this.useCdpDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useIsisDiscovery'. The field 'useIsisDiscovery'
     * has the following description: Whether links discovery process should use
     *  isis mib data.
     *  
     * 
     * @return the value of field 'UseIsisDiscovery'.
     */
    public Boolean isUseIsisDiscovery() {
        return this.useIsisDiscovery != null ? this.useIsisDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useLldpDiscovery'. The field 'useLldpDiscovery'
     * has the following description: Whether links discovery process should use
     *  lldp mib data.
     *  
     * 
     * @return the value of field 'UseLldpDiscovery'.
     */
    public Boolean isUseLldpDiscovery() {
        return this.useLldpDiscovery != null ? this.useLldpDiscovery : Boolean.valueOf("true");
    }

    /**
     * Returns the value of field 'useOspfDiscovery'. The field 'useOspfDiscovery'
     * has the following description: Whether links discovery process should use
     *  ospf mib data.
     *  
     * 
     * @return the value of field 'UseOspfDiscovery'.
     */
    public Boolean isUseOspfDiscovery() {
        return this.useOspfDiscovery != null ? this.useOspfDiscovery : Boolean.valueOf("true");
    }

    /**
     * Sets the value of field 'initial_sleep_time'. The field
     * 'initial_sleep_time' has the following description: The initial sleep time
     * in mill seconds before starting
     *  node Link Discovery.
     *  
     * 
     * @param initial_sleep_time the value of field 'initial_sleep_time'.
     */
    public void setInitialSleepTime(final Long initial_sleep_time) {
        if (initial_sleep_time == null) {
            throw new IllegalArgumentException("'initial_sleep_time' is a required attribute!");
        }
        this.initial_sleep_time = initial_sleep_time;
    }

    /**
     * Sets the value of field 'max_bft'. The field 'max_bft' has the following
     * description: Max bridge forwarding table to hold in memory.
     *  
     * 
     * @param max_bft the value of field 'max_bft'.
     */
    public void setMaxBft(final Integer max_bft) {
        this.max_bft = max_bft;
    }

    /**
     * Sets the value of field 'rescan_interval'. The field 'rescan_interval' has
     * the following description: Node Link Discovery Rescan Time interval in mill
     * seconds.
     *  
     * 
     * @param rescan_interval the value of field 'rescan_interval'.
     */
    public void setRescanInterval(final Long rescan_interval) {
        if (rescan_interval == null) {
            throw new IllegalArgumentException("'rescan_interval' is a required attribute!");
        }
        this.rescan_interval = rescan_interval;
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has the following
     * description: The max number of threads used for polling snmp
     *  devices and discovery links.
     *  
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(final Integer threads) {
        if (threads == null) {
            throw new IllegalArgumentException("'threads' is a required attribute!");
        }
        this.threads = threads;
    }

    /**
     * Sets the value of field 'useBridgeDiscovery'. The field
     * 'useBridgeDiscovery' has the following description: Whether links discovery
     * process should use
     *  Bridge mib data.
     *  
     * 
     * @param useBridgeDiscovery the value of field 'useBridgeDiscovery'.
     */
    public void setUseBridgeDiscovery(final Boolean useBridgeDiscovery) {
        this.useBridgeDiscovery = useBridgeDiscovery;
    }

    /**
     * Sets the value of field 'useCdpDiscovery'. The field 'useCdpDiscovery' has
     * the following description: Whether links discovery process should use
     *  cisco discovery protocol cache table.
     *  
     * 
     * @param useCdpDiscovery the value of field 'useCdpDiscovery'.
     */
    public void setUseCdpDiscovery(final Boolean useCdpDiscovery) {
        this.useCdpDiscovery = useCdpDiscovery;
    }

    /**
     * Sets the value of field 'useIsisDiscovery'. The field 'useIsisDiscovery'
     * has the following description: Whether links discovery process should use
     *  isis mib data.
     *  
     * 
     * @param useIsisDiscovery the value of field 'useIsisDiscovery'.
     */
    public void setUseIsisDiscovery(final Boolean useIsisDiscovery) {
        this.useIsisDiscovery = useIsisDiscovery;
    }

    /**
     * Sets the value of field 'useLldpDiscovery'. The field 'useLldpDiscovery'
     * has the following description: Whether links discovery process should use
     *  lldp mib data.
     *  
     * 
     * @param useLldpDiscovery the value of field 'useLldpDiscovery'.
     */
    public void setUseLldpDiscovery(final Boolean useLldpDiscovery) {
        this.useLldpDiscovery = useLldpDiscovery;
    }

    /**
     * Sets the value of field 'useOspfDiscovery'. The field 'useOspfDiscovery'
     * has the following description: Whether links discovery process should use
     *  ospf mib data.
     *  
     * 
     * @param useOspfDiscovery the value of field 'useOspfDiscovery'.
     */
    public void setUseOspfDiscovery(final Boolean useOspfDiscovery) {
        this.useOspfDiscovery = useOspfDiscovery;
    }

}
