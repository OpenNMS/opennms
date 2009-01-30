/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.provision.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.Duration;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
@XmlRootElement(name="foreign-source")
public class OnmsForeignSource implements Serializable, Comparable<OnmsForeignSource> {
    private static final long serialVersionUID = 1L;
    
    @XmlAttribute(name="name")
    private String m_name;
    
    @XmlElement(name="scan-interval")
    @XmlJavaTypeAdapter(StringIntervalAdapter.class)
    private Duration m_scanInterval = Duration.standardDays(1);

    @XmlElementWrapper(name="detectors")
    @XmlElement(name="detector")
    private List<PluginConfig> m_detectors = new ArrayList<PluginConfig>();
    
    @XmlElementWrapper(name="policies")
    @XmlElement(name="policy")
    private List<PluginConfig> m_policies = new ArrayList<PluginConfig>();

    private boolean m_default;

    public OnmsForeignSource() {
    }
    
    public OnmsForeignSource(String name) {
        setName(name);
    }
    
    /**
     * @return the name
     */
    @XmlTransient
    public String getName() {
        return m_name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * @return the scanInterval
     */
    @XmlTransient
    public Duration getScanInterval() {
        return m_scanInterval;
    }
    /**
     * @param scanInterval the scanInterval to set
     */
    public void setScanInterval(Duration scanInterval) {
        m_scanInterval = scanInterval;
    }
    /**
     * @return the detectors
     */
    @XmlTransient
    public List<PluginConfig> getDetectors() {
        return m_detectors;
    }
    /**
     * @param detectors the detectors to set
     */
    public void setDetectors(List<PluginConfig> detectors) {
        m_detectors = detectors;
    }
    
    @XmlTransient
    public List<PluginConfig> getPolicies() {
        return m_policies;
    }
    
    public void setPolicies(List<PluginConfig> policies) {
        m_policies = policies;
    }
    
    public void addDetector(PluginConfig detector) {
        m_detectors.add(detector);
    }

    public void addPolicy(PluginConfig policy) {
        m_policies.add(policy);
    }

    public PluginConfig getDetector(String detector) {
        for (PluginConfig pc : m_detectors) {
            if (pc.getName().equals(detector)) {
                return pc;
            }
        }
        return null;
    }

    public PluginConfig getPolicy(String policy) {
        for (PluginConfig pc : m_policies) {
            if (pc.getName().equals(policy)) {
                return pc;
            }
        }
        return null;
    }

    public boolean isDefault() {
        return m_default;
    }

    @XmlTransient
    public void setDefault(boolean isDefault) {
        m_default = isDefault;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("scan-interval", getScanInterval())
            .append("detectors", getDetectors().toArray(new PluginConfig[0]))
            .append("policies", getPolicies().toArray(new PluginConfig[0]))
            .toString();
    }

    public int compareTo(OnmsForeignSource obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getScanInterval(), obj.getScanInterval())
            .append(getDetectors().toArray(new PluginConfig[0]), obj.getDetectors().toArray(new PluginConfig[0]))
            .append(getPolicies().toArray(new PluginConfig[0]), obj.getPolicies().toArray(new PluginConfig[0]))
            .append(isDefault(), obj.isDefault())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OnmsForeignSource) {
            OnmsForeignSource other = (OnmsForeignSource) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getScanInterval(), other.getScanInterval())
                .append(getDetectors().toArray(new PluginConfig[0]), other.getDetectors().toArray(new PluginConfig[0]))
                .append(getPolicies().toArray(new PluginConfig[0]), other.getPolicies().toArray(new PluginConfig[0]))
                .append(isDefault(), other.isDefault())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(getScanInterval())
            .append(getDetectors().toArray(new PluginConfig[0]))
            .append(getPolicies().toArray(new PluginConfig[0]))
            .append(isDefault())
            .toHashCode();
      }
}
