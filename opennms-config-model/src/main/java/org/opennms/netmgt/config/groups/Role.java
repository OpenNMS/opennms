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
package org.opennms.netmgt.config.groups;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "role")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("groups.xsd")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "membership-group", required = true)
    private String m_membershipGroup;

    @XmlAttribute(name = "supervisor", required = true)
    private String m_supervisor;

    @XmlAttribute(name = "description")
    private String m_description;

    @XmlElement(name = "schedule")
    private List<Schedule> m_schedules = new ArrayList<>();

    public Role() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getMembershipGroup() {
        return m_membershipGroup;
    }

    public void setMembershipGroup(final String membershipGroup) {
        m_membershipGroup = ConfigUtils.assertNotEmpty(membershipGroup, "membership-group");
    }

    public String getSupervisor() {
        return m_supervisor;
    }

    public void setSupervisor(final String supervisor) {
        m_supervisor = ConfigUtils.assertNotEmpty(supervisor, "supervisor");
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(m_description);
    }

    public void setDescription(final String description) {
        m_description = ConfigUtils.normalizeString(description);
    }

    public List<Schedule> getSchedules() {
        return m_schedules;
    }

    public void setSchedules(final List<Schedule> schedules) {
        if (schedules == m_schedules) return;
        m_schedules.clear();
        if (schedules != null) m_schedules.addAll(schedules);
    }

    public void addSchedule(final Schedule schedule) {
        m_schedules.add(schedule);
    }

    public void clearSchedules() {
        m_schedules.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_membershipGroup, 
                            m_supervisor, 
                            m_description, 
                            m_schedules);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Role) {
            final Role that = (Role)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_membershipGroup, that.m_membershipGroup)
                    && Objects.equals(this.m_supervisor, that.m_supervisor)
                    && Objects.equals(this.m_description, that.m_description)
                    && Objects.equals(this.m_schedules, that.m_schedules);
        }
        return false;
    }

}
