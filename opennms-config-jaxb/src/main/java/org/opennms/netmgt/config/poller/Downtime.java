/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.ImmutableList;

/**
 * Downtime model. This determines the rates at which addresses are to be
 * polled when they remain down for extended periods. Usually polling is done
 * at lower rates when a node is down until a certain amount of downtime at
 * which the node is marked 'deleted'.
 */

@XmlRootElement(name="downtime")
@XmlAccessorType(XmlAccessType.NONE)
public class Downtime implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DELETE_ALWAYS = "always";
    public static final String DELETE_MANAGED = "managed";
    public static final String DELETE_NEVER = "never";

    private static final List<String> s_deleteValues = ImmutableList.of(DELETE_ALWAYS, DELETE_MANAGED, DELETE_NEVER);

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
     * continuously since the start time.
     */
    private String m_delete;

    /**
     * Interval at which service is to be polled between the specified start
     * and end when service has been continuously down.
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

    public Downtime(final long begin, final String delete) {
        this();
        setBegin(begin);
        setDelete(delete);
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
     * continuously since the start time.
     */
    @XmlAttribute(name="delete")
    public String getDelete() {
        return m_delete;
    }

    public void setDelete(final String delete) {
        if ("yes".equals(delete) || "true".equals(delete)) {
            m_delete = DELETE_MANAGED;
            return;
        } else if ("no".equals(delete) || "false".equals(delete)) {
            m_delete = DELETE_NEVER;
            return;
        } else if (delete != null && !s_deleteValues.contains(delete)) {
            throw new IllegalArgumentException("Downtime delete attribute must be one of 'always', 'managed', or 'never', but was '" + delete + "'.");
        }

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
