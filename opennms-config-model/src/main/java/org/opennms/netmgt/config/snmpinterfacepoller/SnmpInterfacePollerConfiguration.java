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

package org.opennms.netmgt.config.snmpinterfacepoller;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the snmp-interface-poller-configuration.xml
 *  configuration file.
 */
@XmlRootElement(name = "snmp-interface-poller-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("snmp-interface-poller-configuration.xsd")
public class SnmpInterfacePollerConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Default Interval at which the interfaces are to be
     *  polled
     */
    @XmlAttribute(name = "interval")
    private Long m_interval;

    /**
     * The maximum number of threads used for
     *  snmp polling.
     */
    @XmlAttribute(name = "threads", required = true)
    private Integer m_threads;

    /**
     * The SNMP service string usually 'SNMP'.
     */
    @XmlAttribute(name = "service", required = true)
    private String m_service;

    /**
     * Flag which indicates to suppress Admin Status events at all.
     *  This is deprecated and will be ignored in the code!
     *  
     */
    @XmlAttribute(name = "suppressAdminDownEvent")
    private Boolean m_suppressAdminDownEvent;

    /**
     * Flag which indicates if the filters defined on packages and interface
     *  criterias must be used to select the SNMP interfaces to be tracked by the
     * poller
     *  instead of do this selection through requisition policies.
     *  
     */
    @XmlAttribute(name = "useCriteriaFilters")
    private Boolean m_useCriteriaFilters;

    /**
     * Configuration of node-outage functionality
     */
    @XmlElement(name = "node-outage", required = true)
    private NodeOutage m_nodeOutage;

    /**
     * Package encapsulating addresses, services to be
     *  polled for these addresses, etc..
     */
    @XmlElement(name = "package", required = true)
    private List<Package> m_packages = new ArrayList<>();

    public Long getInterval() {
        return m_interval != null ? m_interval : Long.valueOf("300000");
    }

    public void setInterval(final Long interval) {
        m_interval = ConfigUtils.assertNotNull(interval, "interval");
    }

    public Integer getThreads() {
        return m_threads;
    }

    public void setThreads(final Integer threads) {
        m_threads = ConfigUtils.assertNotNull(threads, "threads");
    }

    public String getService() {
        return m_service;
    }

    public void setService(final String service) {
        m_service = ConfigUtils.assertNotEmpty(service, "service");
    }

    public Boolean getSuppressAdminDownEvent() {
        return m_suppressAdminDownEvent != null ? m_suppressAdminDownEvent : Boolean.TRUE;
    }

    public void setSuppressAdminDownEvent(final Boolean suppressAdminDownEvent) {
        m_suppressAdminDownEvent = suppressAdminDownEvent;
    }

    public Boolean getUseCriteriaFilters() {
        return m_useCriteriaFilters != null ? m_useCriteriaFilters : Boolean.FALSE;
    }

    public void setUseCriteriaFilters(final Boolean useCriteriaFilters) {
        m_useCriteriaFilters = useCriteriaFilters;
    }

    public NodeOutage getNodeOutage() {
        return m_nodeOutage;
    }

    public void setNodeOutage(final NodeOutage nodeOutage) {
        m_nodeOutage = ConfigUtils.assertNotNull(nodeOutage, "nodeOutage");
    }

    public List<Package> getPackages() {
        return m_packages;
    }

    public void setPackages(final List<Package> packages) {
        ConfigUtils.assertMinimumSize(packages, 1, "package");
        if (packages == m_packages) return;
        m_packages.clear();
        if (packages != null) m_packages.addAll(packages);
    }

    public void addPackage(final Package p) {
        m_packages.add(p);
    }

    public boolean removePackage(final Package p) {
        return m_packages.remove(p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_interval, 
                            m_threads, 
                            m_service, 
                            m_suppressAdminDownEvent, 
                            m_useCriteriaFilters, 
                            m_nodeOutage, 
                            m_packages);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof SnmpInterfacePollerConfiguration) {
            final SnmpInterfacePollerConfiguration that = (SnmpInterfacePollerConfiguration)obj;
            return Objects.equals(this.m_interval, that.m_interval)
                    && Objects.equals(this.m_threads, that.m_threads)
                    && Objects.equals(this.m_service, that.m_service)
                    && Objects.equals(this.m_suppressAdminDownEvent, that.m_suppressAdminDownEvent)
                    && Objects.equals(this.m_useCriteriaFilters, that.m_useCriteriaFilters)
                    && Objects.equals(this.m_nodeOutage, that.m_nodeOutage)
                    && Objects.equals(this.m_packages, that.m_packages);
        }
        return false;
    }

}
