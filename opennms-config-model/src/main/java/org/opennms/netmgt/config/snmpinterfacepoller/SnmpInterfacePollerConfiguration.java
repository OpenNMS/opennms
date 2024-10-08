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
     * Values of ifAdminStatus and ifOperStatus to treat as up values.
     * Expects a comma separated list of values i.e. '1,3'.
     */
    @XmlAttribute(name = "up-values")
    private String m_upValues = "1";

    /**
     * Values of ifAdminStatus and ifOperStatus to treat as down values.
     * Expects a comma separated list of values i.e. '2,3,5,7'.
     */
    @XmlAttribute(name = "down-values")
    private String m_downValues = "2";

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

    public void setUpValues(final String upValues) {
        m_upValues = upValues;
    }

    public String getUpValues() {
        return m_upValues;
    }

    public void setDownValues(final String downValues) {
        m_downValues = downValues;
    }

    public String getDownValues() {
        return m_downValues;
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
                            m_upValues,
                            m_downValues,
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
                    && Objects.equals(this.m_upValues, that.m_upValues)
                    && Objects.equals(this.m_downValues, that.m_downValues)
                    && Objects.equals(this.m_nodeOutage, that.m_nodeOutage)
                    && Objects.equals(this.m_packages, that.m_packages);
        }
        return false;
    }
}
