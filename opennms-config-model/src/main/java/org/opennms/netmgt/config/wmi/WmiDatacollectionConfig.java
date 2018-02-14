/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wmi-datacollection}wmi-collection" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rrdRepository" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "m_wmiCollections"
})
@XmlRootElement(name = "wmi-datacollection-config")
@ValidateUsing("wmi-datacollection.xsd")
public class WmiDatacollectionConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "wmi-collection", required = true)
    protected List<WmiCollection> m_wmiCollections = new ArrayList<>();

    @XmlAttribute(name = "rrdRepository", required = true)
    protected String m_rrdRepository;

    public List<WmiCollection> getWmiCollections() {
        return m_wmiCollections;
    }

    public void setWmiCollections(final List<WmiCollection> wmiCollections) {
        if (wmiCollections == m_wmiCollections) return;
        m_wmiCollections.clear();
        if (wmiCollections != null) m_wmiCollections.addAll(wmiCollections);
    }

    public void addWmiCollection(final WmiCollection wmiCollection) {
        m_wmiCollections.add(wmiCollection);
    }

    public boolean removeWmiCollection(final WmiCollection wmiCollection) {
        return m_wmiCollections.remove(wmiCollection);
    }

    public String getRrdRepository() {
        return m_rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        m_rrdRepository = ConfigUtils.assertNotEmpty(rrdRepository, "rrdRepository");
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof WmiDatacollectionConfig)) {
            return false;
        }
        final WmiDatacollectionConfig that = (WmiDatacollectionConfig) obj;
        return Objects.equals(this.m_wmiCollections, that.m_wmiCollections)
                && Objects.equals(this.m_rrdRepository, that.m_rrdRepository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_wmiCollections, m_rrdRepository);
    }

}
