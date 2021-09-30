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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IGroupReference;

@XmlRootElement(name="includedGroup")
@XmlAccessorType(XmlAccessType.NONE)
public class GroupReferenceImpl implements IGroupReference {

    @XmlAttribute(name="dataCollectionGroup")
    public String m_dataCollectionGroup;

    public GroupReferenceImpl() {
    }

    public GroupReferenceImpl(final String groupName) {
        m_dataCollectionGroup = groupName;
    }

    @Override
    public String getDataCollectionGroup() {
        return m_dataCollectionGroup;
    }

    public void setDataCollectionGroup(final String group) {
        m_dataCollectionGroup = group;
    }

    @Override
    public String toString() {
        return "GroupReferenceImpl [dataCollectionGroup=" + m_dataCollectionGroup + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_dataCollectionGroup == null) ? 0 : m_dataCollectionGroup.hashCode());
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
        if (!(obj instanceof GroupReferenceImpl)) {
            return false;
        }
        final GroupReferenceImpl other = (GroupReferenceImpl) obj;
        if (m_dataCollectionGroup == null) {
            if (other.m_dataCollectionGroup != null) {
                return false;
            }
        } else if (!m_dataCollectionGroup.equals(other.m_dataCollectionGroup)) {
            return false;
        }
        return true;
    }
}
