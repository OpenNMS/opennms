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

package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;
import org.joda.time.Duration;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.StringIntervalAdapter;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
@XmlRootElement(name="foreign-source")
public class ForeignSource implements Serializable, Comparable<ForeignSource> {
    private static final PluginConfig[] OF_PLUGIN_CONFIGS = new PluginConfig[0];

    private static final long serialVersionUID = 1L;
    
    @XmlAttribute(name="name")
    private String m_name;

    @XmlAttribute(name="date-stamp")
    protected XMLGregorianCalendar m_dateStamp;

    @XmlElement(name="scan-interval")
    @XmlJavaTypeAdapter(StringIntervalAdapter.class)
    private Duration m_scanInterval = Duration.standardDays(1);

    @XmlElementWrapper(name="detectors")
    @XmlElement(name="detector")
    private Set<PluginConfig> m_detectors = new LinkedHashSet<PluginConfig>();
    
    @XmlElementWrapper(name="policies")
    @XmlElement(name="policy")
    private Set<PluginConfig> m_policies = new LinkedHashSet<PluginConfig>();

    private boolean m_default;

    public ForeignSource() {
        updateDateStamp();
    }
    
    public ForeignSource(String name) {
        updateDateStamp();
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
     * @return the date stamp
     */
    @XmlTransient
    public XMLGregorianCalendar getDateStamp() {
        return m_dateStamp;
    }
    /**
     * @return the date stamp as a {@link java.util.Date}
     */
    @XmlTransient
    public Date getDateStampAsDate() {
        return m_dateStamp.toGregorianCalendar().getTime();
    }
    /**
     * @param value the date stamp
     */
    public void setDateStamp(XMLGregorianCalendar value) {
        m_dateStamp = value;
    }
    /**
     * Update the date stamp to the current date and time
     */
    public void updateDateStamp() {
        try {
            m_dateStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            log().warn("unable to update datestamp", e);
        }
    }
    /**
     * @return the detectors
     */
    @XmlTransient
    public Set<PluginConfig> getDetectors() {
        return m_detectors;
    }
    /**
     * @param detectors the detectors to set
     */
    public void setDetectors(Set<PluginConfig> detectors) {
        m_detectors = detectors;
    }
    
    @XmlTransient
    public Set<PluginConfig> getPolicies() {
        return m_policies;
    }
    
    public void setPolicies(Set<PluginConfig> policies) {
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

    /* an unfortunate naming convention because of the way PropertyPath works */
    public void removeDetectors(PluginConfig detector) {
        m_detectors.remove(detector);
    }

    public PluginConfig getPolicy(String policy) {
        for (PluginConfig pc : m_policies) {
            if (pc.getName().equals(policy)) {
                return pc;
            }
        }
        return null;
    }

    /* an unfortunate naming convention because of the way PropertyPath works */
    public void removePolicies(PluginConfig policy) {
        m_policies.remove(policy);
    }
    
    public boolean isDefault() {
        return m_default;
    }

    @XmlTransient
    public void setDefault(boolean isDefault) {
        m_default = isDefault;
    }

    private Category log() {
        return ThreadCategory.getInstance(ForeignSource.class);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("scan-interval", getScanInterval())
            .append("detectors", getDetectors().toArray(OF_PLUGIN_CONFIGS))
            .append("policies", getPolicies().toArray(OF_PLUGIN_CONFIGS))
            .toString();
    }

    public int compareTo(ForeignSource obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getScanInterval(), obj.getScanInterval())
            .append(getDetectors().toArray(OF_PLUGIN_CONFIGS), obj.getDetectors().toArray(OF_PLUGIN_CONFIGS))
            .append(getPolicies().toArray(OF_PLUGIN_CONFIGS), obj.getPolicies().toArray(OF_PLUGIN_CONFIGS))
            .append(isDefault(), obj.isDefault())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForeignSource) {
            ForeignSource other = (ForeignSource) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getScanInterval(), other.getScanInterval())
                .append(getDetectors().toArray(OF_PLUGIN_CONFIGS), other.getDetectors().toArray(OF_PLUGIN_CONFIGS))
                .append(getPolicies().toArray(OF_PLUGIN_CONFIGS), other.getPolicies().toArray(OF_PLUGIN_CONFIGS))
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
            .append(getDetectors().toArray(OF_PLUGIN_CONFIGS))
            .append(getPolicies().toArray(OF_PLUGIN_CONFIGS))
            .append(isDefault())
            .toHashCode();
      }
}
