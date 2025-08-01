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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "basicSchedule")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("groups.xsd")
public class BasicSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * outage name
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * outage type
     */
    @XmlAttribute(name = "type", required = true)
    private String m_type;

    /**
     * defines start/end time for the outage
     */
    @XmlElement(name = "time", required = true)
    private List<Time> m_times = new ArrayList<>();

    public BasicSchedule() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public List<Time> getTimes() {
        return m_times;
    }

    public void setTimes(final List<Time> times) {
        if (times == m_times) return;
        m_times.clear();
        if (times != null) m_times.addAll(times);
    }

    public void addTime(final Time time) {
        m_times.add(time);
    }

    /**
     */
    public void clearTimes() {
        m_times.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_type, 
                            m_times);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof BasicSchedule) {
            final BasicSchedule that = (BasicSchedule)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_times, that.m_times);
        }
        return false;
    }

}
