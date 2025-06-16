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
