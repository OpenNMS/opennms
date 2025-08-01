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
package org.opennms.netmgt.config.snmp;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * IP Address Range
 */

@XmlRootElement(name="range")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_begin", "m_end"})
public class Range implements Serializable {
    private static final long serialVersionUID = 3817543154652004131L;

    /**
     * Starting IP address of the range.
     */
    @XmlAttribute(name="begin", required=true)
    private String m_begin;

    /**
     * Ending IP address of the range.
     */
    @XmlAttribute(name="end", required=true)
    private String m_end;

    public Range() {
        super();
    }

    public Range(final String begin, final String end) {
        m_begin = begin;
        m_end = end;
    }

    /**
     * Starting IP address of the range.
     */
    public String getBegin() {
        return m_begin;
    }

    public void setBegin(final String begin) {
        m_begin = begin;
    }

    /**
     * Ending IP address of the range.
     */
    public String getEnd() {
        return m_end;
    }

    public void setEnd(final String end) {
        m_end = end;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Range)) {
            return false;
        }
        Range other = (Range) obj;
        if (m_begin == null) {
            if (other.m_begin != null) {
                return false;
            }
        } else if (!m_begin.equals(other.m_begin)) {
            return false;
        }
        if (m_end == null) {
            if (other.m_end != null) {
                return false;
            }
        } else if (!m_end.equals(other.m_end)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Range [begin=" + m_begin + ", end=" + m_end + "]";
    }
}
