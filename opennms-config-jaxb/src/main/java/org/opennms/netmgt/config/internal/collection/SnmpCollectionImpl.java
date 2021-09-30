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
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IDataCollectionGroup;
import org.opennms.netmgt.config.api.collection.IGroupReference;
import org.opennms.netmgt.config.api.collection.ISnmpCollection;

@XmlRootElement(name="snmp-collection")
@XmlAccessorType(XmlAccessType.NONE)
public class SnmpCollectionImpl implements ISnmpCollection {

    @XmlAttribute(name="name")
    private String m_name="default";

    @XmlElement(name="include-collection")
    private GroupReferenceImpl[] m_includedGroups;

    @XmlElement(name="datacollection-group")
    private DataCollectionGroupImpl[] m_dataCollectionGroups;

    public SnmpCollectionImpl() {
    }

    public SnmpCollectionImpl(final String name) {
        m_name = name;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public IGroupReference[] getIncludedGroups() {
        return m_includedGroups;
    }

    public void addIncludedGroup(final String groupName) {
        final List<GroupReferenceImpl> groups = m_includedGroups == null? new ArrayList<GroupReferenceImpl>() : new ArrayList<GroupReferenceImpl>(Arrays.asList(m_includedGroups));
        groups.add(new GroupReferenceImpl(groupName));
        m_includedGroups = groups.toArray(new GroupReferenceImpl[groups.size()]);
    }

    @Override
    public IDataCollectionGroup[] getDataCollectionGroups() {
        return m_dataCollectionGroups;
    }

    public void addDataCollectionGroup(final DataCollectionGroupImpl group) {
        final List<DataCollectionGroupImpl> groups = m_dataCollectionGroups == null? new ArrayList<DataCollectionGroupImpl>() : new ArrayList<DataCollectionGroupImpl>(Arrays.asList(m_dataCollectionGroups));
        groups.add(group);
        m_dataCollectionGroups = groups.toArray(new DataCollectionGroupImpl[groups.size()]);
    }

    @Override
    public String toString() {
        return "SnmpCollectionImpl [name=" + m_name + ", includedGroups=" + Arrays.toString(m_includedGroups)
                + ", dataCollectionGroups=" + Arrays.toString(m_dataCollectionGroups) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_dataCollectionGroups);
        result = prime * result + Arrays.hashCode(m_includedGroups);
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
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
        if (!(obj instanceof SnmpCollectionImpl)) {
            return false;
        }
        final SnmpCollectionImpl other = (SnmpCollectionImpl) obj;
        if (!Arrays.equals(m_dataCollectionGroups, other.m_dataCollectionGroups)) {
            return false;
        }
        if (!Arrays.equals(m_includedGroups, other.m_includedGroups)) {
            return false;
        }
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        return true;
    }

}
