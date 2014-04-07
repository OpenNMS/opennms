/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnmsLocationAvailDefinitionList implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name="data")
    private List<OnmsLocationAvailDataPoint> m_locationAvailDefinitions = new ArrayList<OnmsLocationAvailDataPoint>();
    private Integer m_totalCount;

    public OnmsLocationAvailDefinitionList() {}
    public OnmsLocationAvailDefinitionList(final Collection<? extends OnmsLocationAvailDataPoint> locationAvailDefinitions) {
        m_locationAvailDefinitions.addAll(locationAvailDefinitions);
    }

    public List<OnmsLocationAvailDataPoint> getDefinitions() {
        return m_locationAvailDefinitions;
    }
    public void setDefinitions(final List<OnmsLocationAvailDataPoint> locationAvailDefinitions) {
        if (locationAvailDefinitions == m_locationAvailDefinitions) return;
        m_locationAvailDefinitions.clear();
        m_locationAvailDefinitions.addAll(locationAvailDefinitions);
    }

    public void add(final OnmsLocationAvailDataPoint locationAvailDefinition) {
        m_locationAvailDefinitions.add(locationAvailDefinition);
    }
    public void addAll(final Collection<OnmsLocationAvailDataPoint> locationAvailDefinitions) {
        m_locationAvailDefinitions.addAll(locationAvailDefinitions);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        if (m_locationAvailDefinitions.size() == 0) {
            return null;
        } else {
            return m_locationAvailDefinitions.size();
        }
    }
    public void setCount(final Integer count) {
        // dummy to make JAXB happy
    }
    public int size() {
        return m_locationAvailDefinitions.size();
    }
    
    @XmlAttribute(name="totalCount")
    public Integer getTotalCount() {
        return m_totalCount == null? getCount() : m_totalCount;
    }
    public void setTotalCount(final Integer totalCount) {
        m_totalCount = totalCount;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_locationAvailDefinitions == null) ? 0 : m_locationAvailDefinitions.hashCode());
        result = prime * result + ((m_totalCount == null) ? 0 : m_totalCount.hashCode());
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
        if (!(obj instanceof OnmsLocationAvailDefinitionList)) {
            return false;
        }
        final OnmsLocationAvailDefinitionList other = (OnmsLocationAvailDefinitionList) obj;
        if (m_locationAvailDefinitions == null) {
            if (other.m_locationAvailDefinitions != null) {
                return false;
            }
        } else if (!m_locationAvailDefinitions.equals(other.m_locationAvailDefinitions)) {
            return false;
        }
        if (getTotalCount() == null) {
            if (other.getTotalCount() != null) {
                return false;
            }
        } else if (!getTotalCount().equals(other.getTotalCount())) {
            return false;
        }
        return true;
    }}
