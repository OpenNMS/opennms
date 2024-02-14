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
package org.opennms.netmgt.config.collectd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.network.IPAddress;

/**
 * Range of addresses to be included in this
 *  package
 */

@XmlRootElement(name="include-range")
@XmlAccessorType(XmlAccessType.NONE)
public class IncludeRange implements Serializable {
    private static final long serialVersionUID = -4906787679582203815L;

    /**
     * Starting address of the range
     */
    @XmlAttribute(name="begin")
    private String m_begin;

    /**
     * Ending address of the range
     */
    @XmlAttribute(name="end")
    private String m_end;

    public IncludeRange() {
        super();
    }

    public IncludeRange(final String begin, final String end) {
        this();
        m_begin = begin;
        m_end = end;
    }

    /**
     * Starting address of the range
     */
    public String getBegin() {
        return m_begin;
    }

    public IPAddress getBeginAsAddress() {
        return m_begin == null? null : new IPAddress(m_begin);
    }

    public void setBegin(final String begin) {
        m_begin = begin;
    }

    /**
     * Ending address of the range
     */
    public String getEnd() {
        return m_end;
    }

    public IPAddress getEndAsAddress() {
        return m_end == null? null : new IPAddress(m_end);
    }

    public void setEnd(final String end) {
        m_end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 109;
        int result = 1;
        result = prime * result + ((m_begin == null) ? 0 : m_begin.hashCode());
        result = prime * result + ((m_end == null) ? 0 : m_end.hashCode());
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
        if (!(obj instanceof IncludeRange)) {
            return false;
        }
        final IncludeRange other = (IncludeRange) obj;
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
        return "IncludeRange [begin=" + m_begin + ", end=" + m_end + "]";
    }

}
