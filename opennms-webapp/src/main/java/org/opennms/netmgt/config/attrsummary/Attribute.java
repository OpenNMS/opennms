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
package org.opennms.netmgt.config.attrsummary;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("attr-summary.xsd")
public class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "min")
    private Double m_min;

    @XmlElement(name = "average")
    private Double m_average;

    @XmlElement(name = "max")
    private Double m_max;

    public Attribute() {
    }

    public Attribute(final String name) {
        m_name = name;
    }

    public Attribute(final String name, final Double min, final Double average, final Double max) {
        m_name = name;
        m_min = min;
        m_average = average;
        m_max = max;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public Double getMin() {
        return m_min;
    }

    public void setMin(final Double min) {
        m_min = min;
    }

    public Double getAverage() {
        return m_average;
    }

    public void setAverage(final Double average) {
        m_average = average;
    }

    public Double getMax() {
        return m_max;
    }

    public void setMax(final Double max) {
        m_max = max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_min, m_average, m_max);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Attribute other = (Attribute) obj;

        return Objects.equals(m_name, other.m_name) &&
                Objects.equals(m_min, other.m_min) &&
                Objects.equals(m_average, other.m_average) &&
                Objects.equals(m_max, other.m_max);
    }

}
