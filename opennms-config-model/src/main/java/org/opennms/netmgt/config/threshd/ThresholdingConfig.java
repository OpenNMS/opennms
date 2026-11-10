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
package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the thresholds.xml configuration file.
 */
@XmlRootElement(name = "thresholding-config")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("thresholding.xsd")
public class ThresholdingConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Thresholding group element
     */
    private List<Group> m_groups = new ArrayList<>();

    private final Map<String, Group> m_groupMap = new ConcurrentHashMap<>();

    public ThresholdingConfig() {
    }

    @XmlElement(name = "group")
    public List<Group> getGroups() {
        return m_groups;
    }

    public void setGroups(final List<Group> groups) {
        if (groups == m_groups) {
            // Cover the case where jax-b already set the field reflectively and is now calling our setter in which case
            // we just need to make sure the group map is populated
            if (m_groupMap.isEmpty()) {
                groups.forEach(g -> m_groupMap.put(g.getName(), g));
            }
        } else {
            m_groups.clear();
            m_groupMap.clear();
            if (groups != null) {
                m_groups.addAll(groups);
                groups.forEach(g -> m_groupMap.put(g.getName(), g));
            }
        }
    }
    
    public Group getGroup(String groupName) {
        Objects.requireNonNull(groupName);
        Group group = m_groupMap.get(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Thresholding group " + groupName + " does not exist.");
        }
        return group;
    }

    public void addGroup(final Group group) {
        m_groups.add(group);
        m_groupMap.put(group.getName(), group);
    }

    public boolean removeGroup(final Group group) {
        m_groupMap.remove(group.getName());
        return m_groups.remove(group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_groups);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ThresholdingConfig) {
            final ThresholdingConfig that = (ThresholdingConfig)obj;
            return Objects.equals(this.m_groups, that.m_groups);
        }
        return false;
    }

}
