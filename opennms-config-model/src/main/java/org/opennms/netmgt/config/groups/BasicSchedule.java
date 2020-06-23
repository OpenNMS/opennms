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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "basicSchedule")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("groups.xsd")
public class BasicSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * outage name
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * outage type
     */
    @XmlAttribute(name = "type", required = true)
    private String m_type;

    /**
     * defines start/end time for the outage
     */
    @XmlElement(name = "time", required = true)
    private List<Time> m_times = new ArrayList<>();

    public BasicSchedule() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public List<Time> getTimes() {
        return m_times;
    }

    public void setTimes(final List<Time> times) {
        if (times == m_times) return;
        m_times.clear();
        if (times != null) m_times.addAll(times);
    }

    public void addTime(final Time time) {
        m_times.add(time);
    }

    /**
     */
    public void clearTimes() {
        m_times.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_type, 
                            m_times);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof BasicSchedule) {
            final BasicSchedule that = (BasicSchedule)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_times, that.m_times);
        }
        return false;
    }

}
