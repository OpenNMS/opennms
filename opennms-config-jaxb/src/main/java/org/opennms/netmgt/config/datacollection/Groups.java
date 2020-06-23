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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * MIB object groups
 */

@XmlRootElement(name="groups", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class Groups implements Serializable {
    private static final long serialVersionUID = 5015134343330744365L;

    /**
     * a MIB object group
     */
    @XmlElement(name="group")
    private List<Group> m_groups = new ArrayList<>();

    public Groups() {
        super();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_groups == null) ? 0 : m_groups.hashCode());
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
        if (!(obj instanceof Groups)) {
            return false;
        }
        final Groups other = (Groups) obj;
        if (m_groups == null) {
            if (other.m_groups != null) {
                return false;
            }
        } else if (!m_groups.equals(other.m_groups)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Groups [groups=" + m_groups + "]";
    }

}
