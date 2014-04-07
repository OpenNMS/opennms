/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

/**
 * <p>OnmsMapList class.</p>
 */
@XmlRootElement(name = "links")
public class DataLinkInterfaceList implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name="link")
    private List<DataLinkInterface> m_dataLinkInterfaces = new ArrayList<DataLinkInterface>();
    private Integer m_totalCount;

    public DataLinkInterfaceList() {}
    public DataLinkInterfaceList(final Collection<? extends DataLinkInterface> dataLinkInterfaces) {
        m_dataLinkInterfaces.addAll(dataLinkInterfaces);
    }

    public List<DataLinkInterface> getDataLinkInterfaces() {
        return m_dataLinkInterfaces;
    }
    public void setDataLinkInterfaces(final List<DataLinkInterface> dataLinkInterfaces) {
        if (dataLinkInterfaces == m_dataLinkInterfaces) return;
        m_dataLinkInterfaces.clear();
        m_dataLinkInterfaces.addAll(dataLinkInterfaces);
    }

    public void add(final DataLinkInterface dataLinkInterface) {
        m_dataLinkInterfaces.add(dataLinkInterface);
    }
    public void addAll(final Collection<DataLinkInterface> dataLinkInterfaces) {
        m_dataLinkInterfaces.addAll(dataLinkInterfaces);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        if (m_dataLinkInterfaces.size() == 0) {
            return null;
        } else {
            return m_dataLinkInterfaces.size();
        }
    }
    public void setCount(final Integer count) {
        // dummy to make JAXB happy
    }
    public int size() {
        return m_dataLinkInterfaces.size();
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
        result = prime * result + ((m_dataLinkInterfaces == null) ? 0 : m_dataLinkInterfaces.hashCode());
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
        if (!(obj instanceof DataLinkInterfaceList)) {
            return false;
        }
        final DataLinkInterfaceList other = (DataLinkInterfaceList) obj;
        if (m_dataLinkInterfaces == null) {
            if (other.m_dataLinkInterfaces != null) {
                return false;
            }
        } else if (!m_dataLinkInterfaces.equals(other.m_dataLinkInterfaces)) {
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
    }
}
