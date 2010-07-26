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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.joda.time.Duration;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.StringIntervalAdapter;

/**
 * <p>ForeignSource class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @version $Id: $
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
    private List<PluginConfig> m_detectors = new ArrayList<PluginConfig>();
    
    @XmlElementWrapper(name="policies")
    @XmlElement(name="policy")
    private List<PluginConfig> m_policies = new ArrayList<PluginConfig>();

    private boolean m_default;

    /**
     * <p>Constructor for ForeignSource.</p>
     */
    public ForeignSource() {
        updateDateStamp();
    }
    
    /**
     * <p>Constructor for ForeignSource.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public ForeignSource(String name) {
        updateDateStamp();
        setName(name);
    }
    
    /**
     * <p>getName</p>
     *
     * @return the name
     */
    @XmlTransient
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name the name to set
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * <p>getScanInterval</p>
     *
     * @return the scanInterval
     */
    @XmlTransient
    public Duration getScanInterval() {
        return m_scanInterval;
    }
    /**
     * <p>setScanInterval</p>
     *
     * @param scanInterval the scanInterval to set
     */
    public void setScanInterval(Duration scanInterval) {
        m_scanInterval = scanInterval;
    }
    /**
     * <p>getDateStamp</p>
     *
     * @return the date stamp
     */
    @XmlTransient
    public XMLGregorianCalendar getDateStamp() {
        return m_dateStamp;
    }
    /**
     * <p>getDateStampAsDate</p>
     *
     * @return the date stamp as a {@link java.util.Date}
     */
    @XmlTransient
    public Date getDateStampAsDate() {
        return m_dateStamp.toGregorianCalendar().getTime();
    }
    /**
     * <p>setDateStamp</p>
     *
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
     * <p>getDetectors</p>
     *
     * @return the detectors
     */
    @XmlTransient
    public List<PluginConfig> getDetectors() {
        return m_detectors;
    }
    /**
     * <p>setDetectors</p>
     *
     * @param detectors the detectors to set
     */
    public void setDetectors(List<PluginConfig> detectors) {
        m_detectors = detectors;
    }
    
    /**
     * <p>getPolicies</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlTransient
    public List<PluginConfig> getPolicies() {
        return m_policies;
    }
    
    /**
     * <p>setPolicies</p>
     *
     * @param policies a {@link java.util.List} object.
     */
    public void setPolicies(List<PluginConfig> policies) {
        m_policies = policies;
    }
    
    /**
     * <p>addDetector</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void addDetector(PluginConfig detector) {
        m_detectors.add(detector);
    }

    /**
     * <p>addPolicy</p>
     *
     * @param policy a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void addPolicy(PluginConfig policy) {
        m_policies.add(policy);
    }

    /**
     * <p>getDetector</p>
     *
     * @param detector a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public PluginConfig getDetector(String detector) {
        for (PluginConfig pc : m_detectors) {
            if (pc.getName().equals(detector)) {
                return pc;
            }
        }
        return null;
    }

    /* an unfortunate naming convention because of the way PropertyPath works */
    /**
     * <p>removeDetectors</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void removeDetectors(PluginConfig detector) {
        m_detectors.remove(detector);
    }

    /**
     * <p>getPolicy</p>
     *
     * @param policy a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public PluginConfig getPolicy(String policy) {
        for (PluginConfig pc : m_policies) {
            if (pc.getName().equals(policy)) {
                return pc;
            }
        }
        return null;
    }

    /* an unfortunate naming convention because of the way PropertyPath works */
    /**
     * <p>removePolicies</p>
     *
     * @param policy a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void removePolicies(PluginConfig policy) {
        m_policies.remove(policy);
    }
    
    /**
     * <p>isDefault</p>
     *
     * @return a boolean.
     */
    public boolean isDefault() {
        return m_default;
    }

    /**
     * <p>setDefault</p>
     *
     * @param isDefault a boolean.
     */
    @XmlTransient
    public void setDefault(boolean isDefault) {
        m_default = isDefault;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(ForeignSource.class);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("scan-interval", getScanInterval())
            .append("detectors", getDetectors().toArray(OF_PLUGIN_CONFIGS))
            .append("policies", getPolicies().toArray(OF_PLUGIN_CONFIGS))
            .toString();
    }

    /**
     * <p>compareTo</p>
     *
     * @param obj a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @return a int.
     */
    public int compareTo(ForeignSource obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getScanInterval(), obj.getScanInterval())
            .append(getDetectors().toArray(OF_PLUGIN_CONFIGS), obj.getDetectors().toArray(OF_PLUGIN_CONFIGS))
            .append(getPolicies().toArray(OF_PLUGIN_CONFIGS), obj.getPolicies().toArray(OF_PLUGIN_CONFIGS))
            .toComparison();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForeignSource) {
            ForeignSource other = (ForeignSource) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getScanInterval(), other.getScanInterval())
                .append(getDetectors().toArray(OF_PLUGIN_CONFIGS), other.getDetectors().toArray(OF_PLUGIN_CONFIGS))
                .append(getPolicies().toArray(OF_PLUGIN_CONFIGS), other.getPolicies().toArray(OF_PLUGIN_CONFIGS))
                .isEquals();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(443, 1061)
            .append(getName())
            .append(getScanInterval())
            .append(getDetectors().toArray(OF_PLUGIN_CONFIGS))
            .append(getPolicies().toArray(OF_PLUGIN_CONFIGS))
            .toHashCode();
      }
}
