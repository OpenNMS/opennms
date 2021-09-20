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

package org.opennms.netmgt.config.reporting;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * 24 hour clock time
 */
@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class Time implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "hours", required = true)
    private Integer m_hours;

    @XmlElement(name = "minutes", required = true)
    private Integer m_minutes;

    public Time() {
    }

    public Integer getHours() {
        return m_hours;
    }

    public void setHours(final Integer hours) {
        m_hours = ConfigUtils.assertNotNull(hours, "hours");
    }

    public Integer getMinutes() {
        return m_minutes;
    }

    public void setMinutes(final Integer minutes) {
        m_minutes = ConfigUtils.assertNotNull(minutes, "minutes");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_hours, m_minutes);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Time) {
            final Time that = (Time)obj;
            return Objects.equals(this.m_hours, that.m_hours)
                    && Objects.equals(this.m_minutes, that.m_minutes);
        }
        return false;
    }

}
