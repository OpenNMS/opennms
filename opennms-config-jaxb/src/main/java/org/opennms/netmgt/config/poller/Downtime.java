/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Downtime model. This determines the rates at which addresses are to be
 * polled when they remain down for extended periods. Usually polling is done
 * at lower rates when a node is down until a certain amount of downtime at
 * which the node is marked 'deleted'.
 */

@XmlRootElement(name="downtime")
@XmlAccessorType(XmlAccessType.NONE)
public class Downtime implements Serializable {
    private static final long serialVersionUID = -2436661386464207644L;

    /**
     * Start of the interval.
     */
    @XmlAttribute(name="begin")
    private Long m_begin;

    /**
     * End of the interval.
     */
    @XmlAttribute(name="end")
    private Long m_end;

    /**
     * Attribute that determines if service is to be deleted when down
     * continuously until the start time.
     */
    @XmlAttribute(name="delete")
    private String m_delete;

    /**
     * Interval at which service is to be polled between the specified start
     * and end when service has been continously down.
     */
    @XmlAttribute(name="interval")
    private Long m_interval;


    public Downtime() {
        super();
    }

    public Downtime(final long interval, final long begin, final long end) {
        this();
        setInterval(interval);
        setBegin(begin);
        setEnd(end);
    }

    public Downtime(final long begin, final boolean delete) {
        this();
        setBegin(begin);
        setDelete(delete? "true":"false");
    }

    /**
     * Start of the interval.
     */
    public Long getBegin() {
        return m_begin == null? 0 : m_begin;
    }

    public void setBegin(final Long begin) {
        m_begin = begin;
    }

    @XmlTransient
    public void setBegin(final Integer begin) {
        m_begin = begin == null? null : begin.longValue();
    }

    /**
     * End of the interval.
     */
    public Long getEnd() {
        return m_end == null? 0 : m_end;
    }

    public void setEnd(final Long end) {
        m_end = end;
    }

    @XmlTransient
    public void setEnd(final Integer end) {
        m_end = end == null? null : end.longValue();
    }

    public boolean hasEnd() {
        return m_end != null;
    }

    /**
     * Attribute that determines if service is to be deleted when down
     * continuously until the start time.
     */
    public String getDelete() {
        return m_delete;
    }

    public void setDelete(final String delete) {
        m_delete = delete;
    }

    /**
     * Interval at which service is to be polled between the specified start
     * and end when service has been continuously down.
     */
    public Long getInterval() {
        return m_interval == null? 0 : m_interval;
    }

    public void setInterval(final Long interval) {
        m_interval = interval;
    }

    @XmlTransient
    public void setInterval(final Integer interval) {
        m_interval = interval == null? null : interval.longValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_begin == null) ? 0 : m_begin.hashCode());
        result = prime * result + ((m_delete == null) ? 0 : m_delete.hashCode());
        result = prime * result + ((m_end == null) ? 0 : m_end.hashCode());
        result = prime * result + ((m_interval == null) ? 0 : m_interval.hashCode());
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
        if (!(obj instanceof Downtime)) {
            return false;
        }
        final Downtime other = (Downtime) obj;
        if (m_begin == null) {
            if (other.m_begin != null) {
                return false;
            }
        } else if (!m_begin.equals(other.m_begin)) {
            return false;
        }
        if (m_delete == null) {
            if (other.m_delete != null) {
                return false;
            }
        } else if (!m_delete.equals(other.m_delete)) {
            return false;
        }
        if (m_end == null) {
            if (other.m_end != null) {
                return false;
            }
        } else if (!m_end.equals(other.m_end)) {
            return false;
        }
        if (m_interval == null) {
            if (other.m_interval != null) {
                return false;
            }
        } else if (!m_interval.equals(other.m_interval)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Downtime [begin=" + m_begin + ", end=" + m_end + ", delete=" + m_delete + ", interval=" + m_interval + "]";
    }
}
