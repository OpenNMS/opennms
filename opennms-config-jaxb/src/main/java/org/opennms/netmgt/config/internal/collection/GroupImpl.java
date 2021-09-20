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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IGroup;
import org.opennms.netmgt.config.api.collection.IMibObject;

/**
 * &lt;group name="mib2-coffee-rfc2325"&gt;
 *      &lt;mibObj oid=".1.3.6.1.2.1.10.132.2" instance="0" alias="coffeePotCapacity" type="integer" /&gt;
 *      &lt;mibObj oid=".1.3.6.1.2.1.10.132.4.1.2" instance="0" alias="coffeePotLevel" type="integer" /&gt;
 *      &lt;mibObj oid=".1.3.6.1.2.1.10.132.4.1.6" instance="0" alias="coffeePotTemp" type="integer" /&gt;
 * &lt;/group&gt;
 * 
 * @author brozow
 *
 */
@XmlRootElement(name="group")
@XmlAccessorType(XmlAccessType.NONE)
public class GroupImpl implements IGroup {

    @XmlAttribute(name="name")
    private String m_name;

    @XmlElement(name="mibObj")
    private MibObjectImpl[] m_mibObjects = new MibObjectImpl[0];

    public GroupImpl() {
    }

    public GroupImpl(final String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public IMibObject[] getMibObjects() {
        return (IMibObject[]) m_mibObjects;
    }

    public void setMibObjects(final IMibObject[] mibObjects) {
        m_mibObjects = MibObjectImpl.asMibObjects(mibObjects);
    }

    public void addMibObject(final MibObjectImpl mibObject) {
        m_mibObjects = ArrayUtils.append(m_mibObjects, mibObject);
    }

    public static GroupImpl asGroup(final IGroup group) {
        if (group == null) return null;

        if (group instanceof GroupImpl) {
            return (GroupImpl)group;
        }

        final GroupImpl newGroup = new GroupImpl();
        newGroup.setName(group.getName());
        newGroup.setMibObjects(group.getMibObjects());
        return newGroup;
    }

    public static GroupImpl[] asGroups(final IGroup[] groups) {
        if (groups == null) return null;
        
        final GroupImpl[] newGroups = new GroupImpl[groups.length];
        for (int i=0; i < groups.length; i++) {
            newGroups[i] = GroupImpl.asGroup(groups[i]);
        }
        return newGroups;
    }

    @Override
    public String toString() {
        return "GroupImpl [name=" + m_name + ", mibObjects=" + Arrays.toString(m_mibObjects) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_mibObjects);
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
        if (!(obj instanceof GroupImpl)) {
            return false;
        }
        final GroupImpl other = (GroupImpl) obj;
        if (!Arrays.equals(m_mibObjects, other.m_mibObjects)) {
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
