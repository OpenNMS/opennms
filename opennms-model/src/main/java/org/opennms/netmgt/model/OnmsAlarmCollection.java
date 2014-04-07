/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Alarm, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Alarm, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Alarm, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsAlarmCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="alarms")
public class OnmsAlarmCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name="alarm")
    private List<OnmsAlarm> m_alarms = new ArrayList<OnmsAlarm>();
    private Integer m_totalCount;

    public OnmsAlarmCollection() {}
    public OnmsAlarmCollection(final Collection<? extends OnmsAlarm> alarms) {
        m_alarms.addAll(alarms);
    }

    public List<OnmsAlarm> getAlarms() {
        return m_alarms;
    }
    public void setAlarms(final List<OnmsAlarm> alarms) {
        if (alarms == m_alarms) return;
        m_alarms.clear();
        m_alarms.addAll(alarms);
    }

    public void add(final OnmsAlarm alarm) {
        m_alarms.add(alarm);
    }
    public void addAll(final Collection<OnmsAlarm> alarms) {
        m_alarms.addAll(alarms);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        if (m_alarms.size() == 0) {
            return null;
        } else {
            return m_alarms.size();
        }
    }
    public void setCount(final Integer count) {
        // dummy to make JAXB happy
    }
    public int size() {
        return m_alarms.size();
    }
    
    @XmlAttribute(name="totalCount")
    public Integer getTotalCount() {
        return m_totalCount == null? getCount() : m_totalCount;
    }
    public void setTotalCount(final Integer totalCount) {
        m_totalCount = totalCount;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alarms == null) ? 0 : m_alarms.hashCode());
        result = prime * result + ((m_totalCount == null) ? 0 : m_totalCount.hashCode());
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
        if (!(obj instanceof OnmsAlarmCollection)) {
            return false;
        }
        final OnmsAlarmCollection other = (OnmsAlarmCollection) obj;
        if (m_alarms == null) {
            if (other.m_alarms != null) {
                return false;
            }
        } else if (!m_alarms.equals(other.m_alarms)) {
            return false;
        }
        if (getTotalCount() == null) {
            if (other.getTotalCount() != null) {
                return false;
            }
        } else if (!getTotalCount().equals(other.getTotalCount())) {
            return false;
        }
        return true;
    }
}
