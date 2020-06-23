/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the datacollection group
 *  configuration file.
 */

@XmlRootElement(name="datacollection-group", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_name", "m_resourceTypes", "m_groups", "m_systemDefs"})
@ValidateUsing("datacollection-groups.xsd")
public class DatacollectionGroup implements Serializable {
    private static final long serialVersionUID = 4158343236805226912L;

    /**
     * data collector group name
     */
    @XmlAttribute(name="name", required=true)
    private String m_name;

    /**
     * Custom resource types
     */
    @XmlElement(name="resourceType")
    private List<ResourceType> m_resourceTypes = new ArrayList<>();

    /**
     * a MIB object group
     */
    @XmlElement(name="group")
    private List<Group> m_groups = new ArrayList<>();

    /**
     * list of system definitions
     */
    @XmlElement(name="systemDef")
    private List<SystemDef> m_systemDefs = new ArrayList<>();


    public DatacollectionGroup() {
        super();
    }

    /**
     * data collector group name
     */
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name == null? null : name.intern();
    }

    public List<ResourceType> getResourceTypes() {
        if (m_resourceTypes == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_resourceTypes);
        }
    }

    public void setResourceTypes(final List<ResourceType> resourceTypes) {
        m_resourceTypes = new ArrayList<ResourceType>(resourceTypes);
    }

    public void addResourceType(final ResourceType resourceType) throws IndexOutOfBoundsException {
        m_resourceTypes.add(resourceType);
    }

    public boolean removeResourceType(final ResourceType resourceType) {
        return m_resourceTypes.remove(resourceType);
    }

    public List<Group> getGroups() {
        if (m_groups == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_groups);
        }
    }

    public void setGroups(final List<Group> groups) {
        m_groups = new ArrayList<Group>(groups);
    }

    public void addGroup(final Group group) throws IndexOutOfBoundsException {
        m_groups.add(group);
    }

    public boolean removeGroup(final Group group) {
        return m_groups.remove(group);
    }

    public List<SystemDef> getSystemDefs() {
        if (m_systemDefs == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_systemDefs);
        }
    }

    public void setSystemDefs(final List<SystemDef> systemDefs) {
        m_systemDefs = new ArrayList<SystemDef>(systemDefs);
    }

    public void addSystemDef(final SystemDef systemDef) throws IndexOutOfBoundsException {
        m_systemDefs.add(systemDef);
    }

    public boolean removeSystemDef(final SystemDef systemDef) {
        return m_systemDefs.remove(systemDef);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_groups == null) ? 0 : m_groups.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_resourceTypes == null) ? 0 : m_resourceTypes.hashCode());
        result = prime * result + ((m_systemDefs == null) ? 0 : m_systemDefs.hashCode());
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
        if (!(obj instanceof DatacollectionGroup)) {
            return false;
        }
        final DatacollectionGroup other = (DatacollectionGroup) obj;
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
        return true;
    }

    @Override
    public String toString() {
        return "DatacollectionGroup [name=" + m_name + ", resourceTypes=" + m_resourceTypes + ", groups=" + m_groups + ", systemDefs=" + m_systemDefs + "]";
    }

}
