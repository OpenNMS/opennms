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
package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.validation.constraints.NotNull;
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
import org.joda.time.Duration;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.provision.persist.StringIntervalAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ForeignSource class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@XmlRootElement(name="foreign-source")
@ValidateUsing("foreign-sources.xsd")
public class ForeignSource implements Serializable, Comparable<ForeignSource> {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignSource.class);
    private static final long serialVersionUID = -1903289015976502808L;

    @XmlAttribute(name="name", required=true)
    @NotNull
    private String m_name;

    @XmlAttribute(name="date-stamp")
    protected XMLGregorianCalendar m_dateStamp;

    @XmlElement(name="scan-interval")
    @XmlJavaTypeAdapter(StringIntervalAdapter.class)
    private Duration m_scanInterval = Duration.standardDays(1);

    @XmlElementWrapper(name="detectors")
    @XmlElement(name="detector")
    private List<PluginConfig> m_detectors = new ArrayList<>();
    
    @XmlElementWrapper(name="policies")
    @XmlElement(name="policy")
    private List<PluginConfig> m_policies = new ArrayList<>();

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
    public ForeignSource(final String name) {
        this();
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
    public void setScanInterval(final Duration scanInterval) {
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
        return m_dateStamp == null ? null : m_dateStamp.toGregorianCalendar().getTime();
    }
    /**
     * <p>setDateStamp</p>
     *
     * @param value the date stamp
     */
    public void setDateStamp(final XMLGregorianCalendar value) {
        m_dateStamp = value;
    }
    /**
     * Update the date stamp to the current date and time
     */
    public void updateDateStamp() {
        try {
            m_dateStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (final DatatypeConfigurationException e) {
            LOG.warn("unable to update datestamp", e);
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
    public void setDetectors(final List<PluginConfig> detectors) {
        m_detectors = detectors;
    }
    
    @XmlTransient
    public List<String> getDetectorNames() {
        List<String> names = new ArrayList<String>(m_detectors.size());
        for(PluginConfig detector : m_detectors) {
            names.add(detector.getName());
        }
        return names;
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
    public void setPolicies(final List<PluginConfig> policies) {
        m_policies = policies;
    }
    
    /**
     * <p>addDetector</p>
     *
     * @param detector a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void addDetector(final PluginConfig detector) {
        m_detectors.add(detector);
    }

    /**
     * <p>addPolicy</p>
     *
     * @param policy a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void addPolicy(final PluginConfig policy) {
        m_policies.add(policy);
    }

    /**
     * <p>getDetector</p>
     *
     * @param detector a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public PluginConfig getDetector(final String detector) {
        for (final PluginConfig pc : m_detectors) {
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
    public void deleteDetectors(final PluginConfig detector) {
        m_detectors.remove(detector);
    }

    /**
     * <p>getPolicy</p>
     *
     * @param policy a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public PluginConfig getPolicy(final String policy) {
        for (final PluginConfig pc : m_policies) {
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
    public void deletePolicies(final PluginConfig policy) {
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
    public void setDefault(final boolean isDefault) {
        m_default = isDefault;
    }

    @Override
    public int hashCode() {
        final int prime = 109;
        int result = 1;
        result = prime * result + ((m_dateStamp == null) ? 0 : m_dateStamp.hashCode());
        result = prime * result + (m_default ? 1231 : 1237);
        result = prime * result + ((m_detectors == null) ? 0 : m_detectors.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_policies == null) ? 0 : m_policies.hashCode());
        result = prime * result + ((m_scanInterval == null) ? 0 : m_scanInterval.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ForeignSource)) return false;
        final ForeignSource other = (ForeignSource) obj;
        if (m_dateStamp == null) {
            if (other.m_dateStamp != null) return false;
        } else if (!m_dateStamp.equals(other.m_dateStamp)) {
            return false;
        }
        if (m_default != other.m_default) return false;
        if (m_detectors == null) {
            if (other.m_detectors != null) return false;
        } else if (!m_detectors.equals(other.m_detectors)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) return false;
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_policies == null) {
            if (other.m_policies != null) return false;
        } else if (!m_policies.equals(other.m_policies)) {
            return false;
        }
        if (m_scanInterval == null) {
            if (other.m_scanInterval != null) return false;
        } else if (!m_scanInterval.equals(other.m_scanInterval)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ForeignSource [name=" + m_name + ", dateStamp="
                + m_dateStamp + ", scanInterval=" + m_scanInterval
                + ", detectors=" + m_detectors + ", policies="
                + m_policies + ", default=" + m_default + "]";
    }

    @Override
    public int compareTo(final ForeignSource other) {
        return new CompareToBuilder()
            .append(m_name, other.m_name)
            .toComparison();
    }
}
