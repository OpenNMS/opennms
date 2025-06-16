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
