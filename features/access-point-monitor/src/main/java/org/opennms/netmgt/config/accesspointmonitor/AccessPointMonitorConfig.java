/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * AccessPointMonitorConfig class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@XmlRootElement(name = "access-point-monitor-configuration")
public class AccessPointMonitorConfig implements Serializable, Comparable<AccessPointMonitorConfig> {
    private static final long serialVersionUID = -7884808420236892997L;

    private static final ServiceTemplate[] OF_TEMPLATES = new ServiceTemplate[0];
    private static final Package[] OF_PACKAGES = new Package[0];
    private static final Monitor[] OF_MONITORS = new Monitor[0];

    @XmlAttribute(name = "threads")
    private int m_threads;

    @XmlAttribute(name = "package-scan-interval")
    private long m_packageScanInterval;

    @XmlElement(name = "service-template")
    private List<ServiceTemplate> m_serviceTemplates = new ArrayList<ServiceTemplate>();

    @XmlElement(name = "package")
    private List<Package> m_packages = new ArrayList<Package>();

    @XmlElement(name = "monitor")
    private List<Monitor> m_monitors = new ArrayList<Monitor>();

    public AccessPointMonitorConfig() {

    }

    @XmlTransient
    public int getThreads() {
        return m_threads;
    }

    public void setThreads(int threads) {
        m_threads = threads;
    }

    @XmlTransient
    public long getPackageScanInterval() {
        return m_packageScanInterval;
    }

    public void setPackageScanInterval(long packageScanInterval) {
        m_packageScanInterval = packageScanInterval;
    }

    @XmlTransient
    public List<ServiceTemplate> getServiceTemplates() {
        return m_serviceTemplates;
    }

    public void setServiceTemplates(List<ServiceTemplate> serviceTemplates) {
        m_serviceTemplates = serviceTemplates;
    }

    public void addServiceTemplate(ServiceTemplate svcTemplate) {
        m_serviceTemplates.add(svcTemplate);
    }

    @XmlTransient
    public List<Package> getPackages() {
        updateServiceTemplates();

        return m_packages;
    }

    public void setPackages(List<Package> packages) {
        m_packages = packages;
    }

    public void addPackage(Package pkg) {
        m_packages.add(pkg);
    }

    @XmlTransient
    public List<Monitor> getMonitors() {
        return m_monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        m_monitors = monitors;
    }

    public void addMonitor(Monitor monitor) {
        m_monitors.add(monitor);
    }

    @Override
    public int compareTo(AccessPointMonitorConfig obj) {
        return new CompareToBuilder()
            .append(getThreads(), obj.getThreads())
            .append(getServiceTemplates().toArray(OF_TEMPLATES), obj.getServiceTemplates().toArray(OF_TEMPLATES))
            .append(getPackages().toArray(OF_PACKAGES), obj.getPackages().toArray(OF_PACKAGES))
            .append(getMonitors().toArray(OF_MONITORS), obj.getMonitors().toArray(OF_MONITORS))
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_monitors == null) ? 0 : m_monitors.hashCode());
        result = prime * result + (int) (m_packageScanInterval ^ (m_packageScanInterval >>> 32));
        result = prime * result + ((m_packages == null) ? 0 : m_packages.hashCode());
        result = prime * result + ((m_serviceTemplates == null) ? 0 : m_serviceTemplates.hashCode());
        result = prime * result + m_threads;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AccessPointMonitorConfig) {
            AccessPointMonitorConfig other = (AccessPointMonitorConfig) obj;
            return new EqualsBuilder()
                .append(getThreads(), other.getThreads())
                .append(getServiceTemplates().toArray(OF_TEMPLATES), other.getServiceTemplates().toArray(OF_TEMPLATES))
                .append(getPackages().toArray(OF_PACKAGES), other.getPackages().toArray(OF_PACKAGES))
                .append(getMonitors().toArray(OF_MONITORS), other.getMonitors().toArray(OF_MONITORS))
                .isEquals();
        }
        return false;
    }

    // Automatically invoked by JAXB after unmarshalling
    //
    public void afterUnmarshal(Unmarshaller u, Object parent) {
        updateServiceTemplates();
    }

    public void updateServiceTemplates() {
        // Build a hash map of all service-templates
        Map<String, ServiceTemplate> serviceTemplateMap = new HashMap<String, ServiceTemplate>();
        if (getServiceTemplates() != null) {
            for (ServiceTemplate t : getServiceTemplates()) {
                serviceTemplateMap.put(t.getName(), t);
            }
        }

        // Iterate over all the services
        for (Package p : m_packages) {
            Service s = p.getService();

            // Default to null in case an existing template was removed
            s.setTemplate(null);

            if (StringUtils.isNotBlank(s.getTemplateName())) {
                // The template name is set, try and associate it to the
                // service
                if (serviceTemplateMap.containsKey(s.getTemplateName())) {
                    s.setTemplate(serviceTemplateMap.get(s.getTemplateName()));
                }
            } else {
                // The template name is not set, try and find one with the
                // service's name
                if (serviceTemplateMap.containsKey(s.getName())) {
                    s.setTemplate(serviceTemplateMap.get(s.getName()));
                }
            }
        }
    }
}
