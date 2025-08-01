/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
