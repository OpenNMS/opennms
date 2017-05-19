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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("groups.xsd")
public class Time implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * an identifier for this event used for reference in the web gui. If this
     *  identifer is not assigned it will be assigned an identifer by web gui.
     *  
     */
    @XmlAttribute(name = "id")
    private String m_id;

    @XmlAttribute(name = "day")
    private String m_day;

    /**
     * when the outage starts
     */
    @XmlAttribute(name = "begins", required = true)
    private String m_begins;

    /**
     * when the outage ends
     */
    @XmlAttribute(name = "ends", required = true)
    private String m_ends;

    public Time() {
    }

    public Optional<String> getId() {
        return Optional.ofNullable(m_id);
    }

    public void setId(final String id) {
        m_id = id;
    }

    public Optional<String> getDay() {
        return Optional.ofNullable(m_day);
    }

    public void setDay(final String day) {
        m_day = day;
    }

    public String getBegins() {
        return m_begins;
    }

    public void setBegins(final String begins) {
        m_begins = ConfigUtils.assertNotEmpty(begins, "begins");
    }

    public String getEnds() {
        return m_ends;
    }

    public void setEnds(final String ends) {
        m_ends = ConfigUtils.assertNotEmpty(ends, "ends");
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_id, 
                            m_day, 
                            m_begins, 
                            m_ends);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Time) {
            final Time that = (Time)obj;
            return Objects.equals(this.m_id, that.m_id)
                    && Objects.equals(this.m_day, that.m_day)
                    && Objects.equals(this.m_begins, that.m_begins)
                    && Objects.equals(this.m_ends, that.m_ends);
        }
        return false;
    }

}
