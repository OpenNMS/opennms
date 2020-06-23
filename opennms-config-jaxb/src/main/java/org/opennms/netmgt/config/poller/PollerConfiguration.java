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

package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.pagesequence.PageSequence;

/**
 * Top-level element for the poller-configuration.xml
 *  configuration file.
 */

@XmlRootElement(name="poller-configuration")
@ValidateUsing("poller-configuration.xsd")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({PageSequence.class})
public class PollerConfiguration implements Serializable {
    private static final long serialVersionUID = 3402898044699865749L;

    /**
     * The maximum number of threads used for polling.
     */
    @XmlAttribute(name="threads")
    private Integer m_threads = 30;

    /**
     * SQL query for getting the next outage ID.
     */
    @XmlAttribute(name="nextOutageId")
    private String m_nextOutageId = "SELECT nextval('outageNxtId')";

    /**
     * Enable/disable serviceUnresponsive behavior
     */
    @XmlAttribute(name="serviceUnresponsiveEnabled")
    private String m_serviceUnresponsiveEnabled = "false";

    /**
     * Flag which indicates if the optional path outage feature is enabled
     */
    @XmlAttribute(name="pathOutageEnabled")
    private String m_pathOutageEnabled = "false";

    @XmlAttribute(name="defaultCriticalPathIp")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress m_defaultCriticalPathIp;

    @XmlAttribute(name="defaultCriticalPathService")
    private String m_defaultCriticalPathService;

    @XmlAttribute(name="defaultCriticalPathTimeout")
    private Integer m_defaultCriticalPathTimeout;

    @XmlAttribute(name="defaultCriticalPathRetries")
    private Integer m_defaultCriticalPathRetries;

    /**
     * Configuration of node-outage functionality
     */
    @XmlElement(name="node-outage")
    private NodeOutage m_nodeOutage;

    /**
     * Package encapsulating addresses, services to be polled for these
     * addresses, etc..
     */
    @XmlElement(name="package")
    private List<Package> m_packages = new ArrayList<>();

    /**
     * Service monitors
     */
    @XmlElement(name="monitor")
    private List<Monitor> m_monitors = new ArrayList<>();

    /**
     * The maximum number of threads used for polling.
     */
    public Integer getThreads() {
        return m_threads == null? 0 : m_threads;
    }

    public void setThreads(final Integer threads) {
        m_threads = threads;
    }

    /**
     * SQL query for getting the next outage ID.
     */
    public String getNextOutageId() {
        return m_nextOutageId == null? "SELECT nextval('outageNxtId')" : m_nextOutageId;
    }

    public void setNextOutageId(final String nextOutageId) {
        m_nextOutageId = nextOutageId;
    }

    /**
     * Enable/disable serviceUnresponsive behavior
     */
    public String getServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }

    public void setServiceUnresponsiveEnabled(final String serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    /**
     * Flag which indicates if the optional path outage feature is enabled
     */
    public String getPathOutageEnabled() {
        return m_pathOutageEnabled == null? "false" : m_pathOutageEnabled;
    }

    public void setPathOutageEnabled(final String pathOutageEnabled) {
        m_pathOutageEnabled = pathOutageEnabled;
    }

    /**
     * Configuration of node-outage functionality
     */
    public NodeOutage getNodeOutage() {
        return m_nodeOutage;
    }

    public void setNodeOutage(final NodeOutage nodeOutage) {
        m_nodeOutage = nodeOutage;
    }

    public List<Package> getPackages() {
        if (m_packages == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_packages);
        }
    }

    public void setPackages(final List<Package> packages) {
        m_packages = new ArrayList<Package>(packages);
    }

    public void addPackage(final Package pack) throws IndexOutOfBoundsException {
        m_packages.add(pack);
    }

    public boolean removePackage(final Package pack) {
        return m_packages.remove(pack);
    }

    public Package getPackage(final String packageName) {
        for (final Package pkg : m_packages) {
            if (pkg.getName().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }

    public List<Monitor> getMonitors() {
        if (m_monitors == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_monitors);
        }
    }

    public void setMonitors(final List<Monitor> monitors) {
        m_monitors = new ArrayList<Monitor>(monitors);
    }

    public void addMonitor(final Monitor monitor) throws IndexOutOfBoundsException {
        m_monitors.add(monitor);
    }

    public void addMonitor(final String service, final String className) {
        addMonitor(new Monitor(service, className));
    }

    public boolean removeMonitor(final Monitor monitor) {
        return m_monitors.remove(monitor);
    }

    public PollerConfiguration getPollerConfigurationForPackages(final List<String> pollingPackageNames) {
        if (pollingPackageNames == null || pollingPackageNames.size() < 1) return null;
        
        final Set<String> seenMonitors = new HashSet<>();
        final PollerConfiguration newConfig = new PollerConfiguration();
        newConfig.setThreads(getThreads());
        newConfig.setNextOutageId(getNextOutageId());
        newConfig.setServiceUnresponsiveEnabled(getServiceUnresponsiveEnabled());
        newConfig.setPathOutageEnabled(getPathOutageEnabled());
        newConfig.setNodeOutage(getNodeOutage());
        
        // Add all requested polling packages to the config
        boolean foundPackage = false;
        for (String pollingPackageName : pollingPackageNames) {
            final Package pkg = getPackage(pollingPackageName);
            if (pkg != null) {
                newConfig.addPackage(pkg);
                foundPackage = true;
                for (final Service service : pkg.getServices()) {
                    seenMonitors.add(service.getName());
                }
            }
        }
        // If the list of polling packages doesn't match anything, then return null
        if (!foundPackage) return null;
        
        for (final Monitor monitor : getMonitors()) {
            if (seenMonitors.contains(monitor.getService())) {
                newConfig.addMonitor(monitor);
            }
        }
        return newConfig;
    }

    public InetAddress getDefaultCriticalPathIp() {
        return m_defaultCriticalPathIp;
    }

    public void setDefaultCriticalPathIp(final InetAddress ip) {
        m_defaultCriticalPathIp = ip;
    }

    public Integer getDefaultCriticalPathTimeout() {
        return m_defaultCriticalPathTimeout == null? 1500 : m_defaultCriticalPathTimeout;
    }

    public void setDefaultCriticalPathTimeout(final Integer timeout) {
        m_defaultCriticalPathTimeout = timeout;
    }

    public int getDefaultCriticalPathRetries() {
        return m_defaultCriticalPathRetries == null? 0 : m_defaultCriticalPathRetries;
    }

    public void setDefaultCriticalPathRetries(final Integer retries) {
        m_defaultCriticalPathRetries = retries;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_threads, m_nextOutageId, m_serviceUnresponsiveEnabled, m_pathOutageEnabled, m_defaultCriticalPathIp, m_defaultCriticalPathService, m_defaultCriticalPathTimeout, m_defaultCriticalPathRetries, m_nodeOutage, m_packages, m_monitors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PollerConfiguration that = (PollerConfiguration) o;
        return Objects.equals(m_threads, that.m_threads)
                && Objects.equals(m_nextOutageId, that.m_nextOutageId)
                && Objects.equals(m_serviceUnresponsiveEnabled, that.m_serviceUnresponsiveEnabled)
                && Objects.equals(m_pathOutageEnabled, that.m_pathOutageEnabled)
                && Objects.equals(m_defaultCriticalPathIp, that.m_defaultCriticalPathIp)
                && Objects.equals(m_defaultCriticalPathService, that.m_defaultCriticalPathService)
                && Objects.equals(m_defaultCriticalPathTimeout, that.m_defaultCriticalPathTimeout)
                && Objects.equals(m_defaultCriticalPathRetries, that.m_defaultCriticalPathRetries)
                && Objects.equals(m_nodeOutage, that.m_nodeOutage)
                && Objects.equals(m_packages, that.m_packages)
                && Objects.equals(m_monitors, that.m_monitors);
    }
    @Override
    public String toString() {
        return "PollerConfiguration[" +
                "threads=" + m_threads +
                ",nextOutageId=" + m_nextOutageId +
                ",serviceUnresponsiveEnabled=" + m_serviceUnresponsiveEnabled +
                ",pathOutageEnabled=" + m_pathOutageEnabled +
                ",pathOutageDefaultCriticalPathIp=" + m_defaultCriticalPathIp +
                ",pathOutageDefaultCriticalPathService=" + m_defaultCriticalPathService +
                ",pathOutageDefaultCriticalPathTimeout=" + m_defaultCriticalPathTimeout +
                ",pathOutageDefaultCriticalPathRetries=" + m_defaultCriticalPathRetries +
                ",nodeOutage=" + m_nodeOutage +
                ",packages=" + m_packages +
                ",monitors=" + m_monitors +
                "]";
    }

}
