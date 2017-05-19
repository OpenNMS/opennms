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

package org.opennms.netmgt.config.rancid.adapter;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the rancid-configuration.xml configuration file.
 */
@XmlRootElement(name = "rancid-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("rancid-adapter-configuration.xsd")
public class RancidConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_DEFAULT_TYPE = "cisco";

    /**
     * The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     */
    @XmlAttribute(name = "delay", required = true)
    private Long m_delay;

    /**
     * The maximum number of retry before
     *  sending a failure.
     */
    @XmlAttribute(name = "retries", required = true)
    private Integer m_retries;

    /**
     * If you want to use opennms categories
     *  to match rancid device type.
     */
    @XmlAttribute(name = "useCategories")
    private Boolean m_useCategories;

    /**
     * The Default Rancid type, it is used when no device type
     *  for provisioned node is found.
     *  
     */
    @XmlAttribute(name = "default-type")
    private String m_defaultType;

    /**
     * Configuration of Policy
     *  functionality
     */
    @XmlElement(name = "policies")
    private Policies m_policies;

    /**
     * A map from sysoids masks and rancid device type.
     */
    @XmlElement(name = "mapping")
    private List<Mapping> m_mappings = new ArrayList<>();

    public RancidConfiguration() {
    }

    public Long getDelay() {
        return m_delay;
    }

    public void setDelay(final Long delay) {
        m_delay = ConfigUtils.assertNotNull(delay, "delay");
    }

    public Integer getRetries() {
        return m_retries;
    }

    public void setRetries(final Integer retries) {
        m_retries = ConfigUtils.assertNotNull(retries, "retries");
    }

    public Boolean getUseCategories() {
        return m_useCategories != null ? m_useCategories : Boolean.FALSE;
    }

    public void setUseCategories(final Boolean useCategories) {
        m_useCategories = useCategories;
    }

    public String getDefaultType() {
        return m_defaultType != null ? m_defaultType : DEFAULT_DEFAULT_TYPE;
    }

    public void setDefaultType(final String defaultType) {
        m_defaultType = ConfigUtils.normalizeString(defaultType);
    }

    public Optional<Policies> getPolicies() {
        return Optional.ofNullable(m_policies);
    }

    public void setPolicies(final Policies policies) {
        m_policies = policies;
    }

    public List<Mapping> getMappings() {
        return m_mappings;
    }

    public void setMappings(final List<Mapping> mappings) {
        if (mappings == m_mappings) return;
        m_mappings.clear();
        if (mappings != null) m_mappings.addAll(mappings);
    }

    public void addMapping(final Mapping mapping) {
        m_mappings.add(mapping);
    }

    public boolean removeMapping(final Mapping mapping) {
        return m_mappings.remove(mapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_delay, 
                            m_retries, 
                            m_useCategories, 
                            m_defaultType, 
                            m_policies, 
                            m_mappings);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof RancidConfiguration) {
            final RancidConfiguration that = (RancidConfiguration)obj;
            return Objects.equals(this.m_delay, that.m_delay)
                    && Objects.equals(this.m_retries, that.m_retries)
                    && Objects.equals(this.m_useCategories, that.m_useCategories)
                    && Objects.equals(this.m_defaultType, that.m_defaultType)
                    && Objects.equals(this.m_policies, that.m_policies)
                    && Objects.equals(this.m_mappings, that.m_mappings);
        }
        return false;
    }

}
