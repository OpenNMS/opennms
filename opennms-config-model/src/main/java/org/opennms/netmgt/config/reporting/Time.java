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
