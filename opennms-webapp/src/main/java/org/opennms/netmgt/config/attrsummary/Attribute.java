/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.attrsummary;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("attr-summary.xsd")
public class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "min")
    private Double m_min;

    @XmlElement(name = "average")
    private Double m_average;

    @XmlElement(name = "max")
    private Double m_max;

    public Attribute() {
    }

    public Attribute(final String name) {
        m_name = name;
    }

    public Attribute(final String name, final Double min, final Double average, final Double max) {
        m_name = name;
        m_min = min;
        m_average = average;
        m_max = max;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public Double getMin() {
        return m_min;
    }

    public void setMin(final Double min) {
        m_min = min;
    }

    public Double getAverage() {
        return m_average;
    }

    public void setAverage(final Double average) {
        m_average = average;
    }

    public Double getMax() {
        return m_max;
    }

    public void setMax(final Double max) {
        m_max = max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_min, m_average, m_max);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Attribute other = (Attribute) obj;

        return Objects.equals(m_name, other.m_name) &&
                Objects.equals(m_min, other.m_min) &&
                Objects.equals(m_average, other.m_average) &&
                Objects.equals(m_max, other.m_max);
    }

}
