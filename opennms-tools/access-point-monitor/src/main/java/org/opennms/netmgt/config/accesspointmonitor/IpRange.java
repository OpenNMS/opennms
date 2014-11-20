/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * IpRange class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class IpRange implements Serializable, Comparable<IpRange> {
    private static final long serialVersionUID = -982213514208208854L;

    @XmlAttribute(name = "begin", required = true)
    private String m_begin;

    @XmlAttribute(name = "end", required = true)
    private String m_end;

    public IpRange() {

    }

    public IpRange(IpRange copy) {
        if (copy.m_begin != null) {
            m_begin = new String(copy.m_begin);
        }
        if (copy.m_end != null) {
            m_end = new String(copy.m_end);
        }
    }

    public IpRange(String begin, String end) {
        m_begin = begin;
        m_end = end;
    }

    @XmlTransient
    public String getBegin() {
        return m_begin;
    }

    public void setBegin(String begin) {
        m_begin = begin;
    }

    @XmlTransient
    public String getEnd() {
        return m_end;
    }

    public void setEnd(String end) {
        m_end = end;
    }

    @Override
    public int compareTo(IpRange obj) {
        return new CompareToBuilder()
            .append(getBegin(), obj.getBegin())
            .append(getEnd(), obj.getEnd())
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_begin == null) ? 0 : m_begin.hashCode());
        result = prime * result + ((m_end == null) ? 0 : m_end.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IpRange) {
            IpRange other = (IpRange) obj;
            return new EqualsBuilder()
                .append(getBegin(), other.getBegin())
                .append(getEnd(), other.getEnd())
                .isEquals();
        }
        return false;
    }
}
