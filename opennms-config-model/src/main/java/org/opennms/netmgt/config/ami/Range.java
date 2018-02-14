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

package org.opennms.netmgt.config.ami;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * IP Address Range
 */
@XmlRootElement(name = "range")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ami-config.xsd")
public class Range implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Starting IP address of the range.
     */
    @XmlAttribute(name = "begin", required = true)
    private String m_begin;

    /**
     * Ending IP address of the range.
     */
    @XmlAttribute(name = "end", required = true)
    private String m_end;

    public Range() {
    }

    public Range(final String begin, final String end) {
        setBegin(begin);
        setEnd(end);
    }

    public String getBegin() {
        return m_begin;
    }

    public void setBegin(final String begin) {
        m_begin = ConfigUtils.assertNotEmpty(begin, "begin");
    }

    public String getEnd() {
        return m_end;
    }

    public void setEnd(final String end) {
        m_end = ConfigUtils.assertNotEmpty(end, "end");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_begin, m_end);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Range) {
            final Range that = (Range) obj;
            return Objects.equals(this.m_begin, that.m_begin) &&
                    Objects.equals(this.m_end, that.m_end);
        }
        return false;
    }
}
