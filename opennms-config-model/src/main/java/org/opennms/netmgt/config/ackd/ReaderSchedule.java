/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.ackd;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * A very basic configuration for defining simple input to a schedule
 */
@XmlRootElement(name = "reader-schedule")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ackd-configuration.xsd")
public class ReaderSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final long DEFAULT_INTERVAL = 1L;

    public static final String DEFAULT_UNIT = "m";

    @XmlAttribute(name = "interval")
    private Long m_interval;

    @XmlAttribute(name = "unit")
    private String m_unit;

    public ReaderSchedule() {
    }

    public ReaderSchedule(final Long interval, final String unit) {
        setInterval(interval);
        setUnit(unit);
    }

    public long getInterval() {
        return m_interval == null ? DEFAULT_INTERVAL : m_interval;
    }

    public void setInterval(final long interval) {
        m_interval = interval;
    }

    public java.lang.String getUnit() {
        return m_unit == null ? DEFAULT_UNIT : m_unit;
    }

    public void setUnit(final String unit) {
        m_unit = unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_interval, m_unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ReaderSchedule) {
            final ReaderSchedule that = (ReaderSchedule) obj;
            return Objects.equals(this.m_interval, that.m_interval) &&
                    Objects.equals(this.m_unit, that.m_unit);
        }
        return false;
    }
}
