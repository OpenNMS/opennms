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
