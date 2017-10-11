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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_monitors == null) ? 0 : m_monitors.hashCode());
        result = prime * result + ((m_nextOutageId == null) ? 0 : m_nextOutageId.hashCode());
        result = prime * result + ((m_nodeOutage == null) ? 0 : m_nodeOutage.hashCode());
        result = prime * result + ((m_packages == null) ? 0 : m_packages.hashCode());
        result = prime * result + ((m_pathOutageEnabled == null) ? 0 : m_pathOutageEnabled.hashCode());
        result = prime * result + ((m_serviceUnresponsiveEnabled == null) ? 0 : m_serviceUnresponsiveEnabled.hashCode());
        result = prime * result + ((m_threads == null) ? 0 : m_threads.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PollerConfiguration)) {
            return false;
        }
        final PollerConfiguration other = (PollerConfiguration) obj;
        if (m_monitors == null) {
            if (other.m_monitors != null) {
                return false;
            }
        } else if (!m_monitors.equals(other.m_monitors)) {
            return false;
        }
        if (m_nextOutageId == null) {
            if (other.m_nextOutageId != null) {
                return false;
            }
        } else if (!m_nextOutageId.equals(other.m_nextOutageId)) {
            return false;
        }
        if (m_nodeOutage == null) {
            if (other.m_nodeOutage != null) {
                return false;
            }
        } else if (!m_nodeOutage.equals(other.m_nodeOutage)) {
            return false;
        }
        if (m_packages == null) {
            if (other.m_packages != null) {
                return false;
            }
        } else if (!m_packages.equals(other.m_packages)) {
            return false;
        }
        if (m_pathOutageEnabled == null) {
            if (other.m_pathOutageEnabled != null) {
                return false;
            }
        } else if (!m_pathOutageEnabled.equals(other.m_pathOutageEnabled)) {
            return false;
        }
        if (m_serviceUnresponsiveEnabled == null) {
            if (other.m_serviceUnresponsiveEnabled != null) {
                return false;
            }
        } else if (!m_serviceUnresponsiveEnabled.equals(other.m_serviceUnresponsiveEnabled)) {
            return false;
        }
        if (m_threads == null) {
            if (other.m_threads != null) {
                return false;
            }
        } else if (!m_threads.equals(other.m_threads)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PollerConfiguration[" +
                "threads=" + m_threads +
                ",nextOutageId=" + m_nextOutageId +
                ",serviceUnresponsiveEnabled=" + m_serviceUnresponsiveEnabled +
                ",pathOutageEnabled=" + m_pathOutageEnabled +
                ",nodeOutage=" + m_nodeOutage +
                ",packages=" + m_packages +
                ",monitors=" + m_monitors +
                "]";
    }

}
