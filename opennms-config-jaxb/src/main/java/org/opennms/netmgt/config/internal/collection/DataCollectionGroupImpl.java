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

package org.opennms.netmgt.config.internal.collection;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionGroup;
import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IResourceType;
import org.opennms.netmgt.config.api.collection.ISystemDef;
import org.opennms.netmgt.config.api.collection.ITable;

@XmlRootElement(name="datacollection-group")
@XmlAccessorType(XmlAccessType.NONE)
public class DataCollectionGroupImpl implements IDataCollectionGroup {

    @XmlAttribute(name="name")
    String m_name;

    @XmlElement(name="resourceType")
    List<ResourceTypeImpl> m_resourceTypes = new ArrayList<>();

    @XmlElement(name="table")
    List<TableImpl> m_tables = new ArrayList<>();

    @XmlElement(name="group")
    List<GroupImpl> m_groups = new ArrayList<>();

    @XmlElement(name="systemDef")
    List<SystemDefImpl> m_systemDefs = new ArrayList<>();

    public DataCollectionGroupImpl() {
    }

    public DataCollectionGroupImpl(final String name) {
        m_name = name;
    }

    @Override
    public IGroup[] getGroups() {
        return m_groups.toArray(new IGroup[0]);
    }

    public void addGroup(final GroupImpl group) {
        m_groups.add(group);
    }

    @Override
    public ITable[] getTables() {
        return m_tables.toArray(new ITable[0]);
    }

    public void addTable(final TableImpl table) {
        m_tables.add(table);
    }

    @Override
    public ISystemDef[] getSystemDefs() {
        return m_systemDefs.toArray(new ISystemDef[0]);
    }

    public void addSystemDef(final SystemDefImpl def) {
        m_systemDefs.add(def);
    }

    @Override
    public IResourceType[] getResourceTypes() {
        return m_resourceTypes.toArray(new IResourceType[0]);
    }

    public void addResourceType(final ResourceTypeImpl resourceType) {
        m_resourceTypes.add(resourceType);
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String toString() {
        return "DataCollectionGroupImpl [name=" + m_name + ", resourceTypes=" + m_resourceTypes + ", tables=" + m_tables + ", groups=" + m_groups + ", systemDefs=" + m_systemDefs + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_groups == null) ? 0 : m_groups.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_resourceTypes == null) ? 0 : m_resourceTypes.hashCode());
        result = prime * result + ((m_systemDefs == null) ? 0 : m_systemDefs.hashCode());
        result = prime * result + ((m_tables == null) ? 0 : m_tables.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DataCollectionGroupImpl)) {
            return false;
        }
        DataCollectionGroupImpl other = (DataCollectionGroupImpl) obj;
        if (m_groups == null) {
            if (other.m_groups != null) {
                return false;
            }
        } else if (!m_groups.equals(other.m_groups)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_resourceTypes == null) {
            if (other.m_resourceTypes != null) {
                return false;
            }
        } else if (!m_resourceTypes.equals(other.m_resourceTypes)) {
            return false;
        }
        if (m_systemDefs == null) {
            if (other.m_systemDefs != null) {
                return false;
            }
        } else if (!m_systemDefs.equals(other.m_systemDefs)) {
            return false;
        }
        if (m_tables == null) {
            if (other.m_tables != null) {
                return false;
            }
        } else if (!m_tables.equals(other.m_tables)) {
            return false;
        }
        return true;
    }



}
