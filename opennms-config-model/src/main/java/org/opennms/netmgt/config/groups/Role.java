/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
