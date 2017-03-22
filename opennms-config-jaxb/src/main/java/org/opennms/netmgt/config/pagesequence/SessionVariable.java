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

package org.opennms.netmgt.config.pagesequence;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Assign the value of a regex match group to a session variable with a
 * user-defined name. The match group is identified by number and must be zero
 * or greater.
 */

@XmlRootElement(name="session-variable")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_matchGroup", "m_name"})
public class SessionVariable implements Serializable {
    private static final long serialVersionUID = 440115812786476754L;

    @XmlAttribute(name="match-group")
    private Integer m_matchGroup;

    @XmlAttribute(name="name")
    private String m_name;

    public SessionVariable() {
        super();
    }

    public Integer getMatchGroup() {
        return m_matchGroup == null? 0 : m_matchGroup;
    }

    public String getName() {
        return m_name;
    }

    public void setMatchGroup(final Integer matchGroup) {
        m_matchGroup = matchGroup;
    }

    public void setName(final String name) {
        m_name = name == null? null : name.intern();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_matchGroup == null) ? 0 : m_matchGroup.hashCode());
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
        if (!(obj instanceof SessionVariable)) {
            return false;
        }
        final SessionVariable other = (SessionVariable) obj;
        if (m_matchGroup == null) {
            if (other.m_matchGroup != null) {
                return false;
            }
        } else if (!m_matchGroup.equals(other.m_matchGroup)) {
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

    @Override
    public String toString() {
        return "SessionVariable [matchGroup=" + m_matchGroup + ", name=" + m_name + "]";
    }

}
