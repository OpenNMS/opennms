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
package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * RRD parms
 */

@XmlRootElement(name="rrd", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class Rrd implements Serializable {
    private static final long serialVersionUID = -3485298538075829212L;

    /**
     * step size for the RRD
     */
    @XmlAttribute(name="step", required=true)
    private Integer m_step;

    /**
     * Round Robin Archive definitions
     */
    @XmlElement(name="rra")
    private List<String> m_rras = new ArrayList<>();


    public Rrd() {
        super();
    }

    /**
     * step size for the RRD
     */
    public Integer getStep() {
        return m_step == null? 0 : m_step;
    }

    public void setStep(final Integer step) {
        m_step = step;
    }

    public List<String> getRras() {
        if (m_rras == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_rras);
        }
    }

    public void setRras(final List<String> rras) {
        m_rras = new ArrayList<String>(rras);
    }

    public void addRra(final String rra) throws IndexOutOfBoundsException {
        m_rras.add(rra.intern());
    }

    public boolean removeRra(final String rra) {
        return m_rras.remove(rra);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_rras == null) ? 0 : m_rras.hashCode());
        result = prime * result + ((m_step == null) ? 0 : m_step.hashCode());
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
        if (!(obj instanceof Rrd)) {
            return false;
        }
        final Rrd other = (Rrd) obj;
        if (m_rras == null) {
            if (other.m_rras != null) {
                return false;
            }
        } else if (!m_rras.equals(other.m_rras)) {
            return false;
        }
        if (m_step == null) {
            if (other.m_step != null) {
                return false;
            }
        } else if (!m_step.equals(other.m_step)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Rrd [step=" + m_step + ", rras=" + m_rras + "]";
    }

}
