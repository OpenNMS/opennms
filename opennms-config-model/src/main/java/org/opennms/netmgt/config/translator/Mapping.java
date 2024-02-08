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
package org.opennms.netmgt.config.translator;

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

/**
 * A mapping for a given event. This translation is only
 *  applied if it is the first that matches
 */
@XmlRootElement(name = "mapping")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("translator-configuration.xsd")
public class Mapping implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "preserve-snmp-data")
    private Boolean m_preserveSnmpData;

    /**
     * An element representing an assignment to an attribute of the event
     *  
     */
    @XmlElement(name = "assignment")
    private List<Assignment> m_assignments = new ArrayList<>();

    public Boolean getPreserveSnmpData() {
        return m_preserveSnmpData != null ? m_preserveSnmpData : Boolean.FALSE;
    }

    public void setPreserveSnmpData(final Boolean preserveSnmpData) {
        m_preserveSnmpData = preserveSnmpData;
    }

    public List<Assignment> getAssignments() {
        return m_assignments;
    }

    public void setAssignments(final List<Assignment> assignments) {
        if (assignments == m_assignments) return;
        m_assignments.clear();
        if (assignments != null) m_assignments.addAll(assignments);
    }

    public void addAssignment(final Assignment assignment) {
        m_assignments.add(assignment);
    }

    public boolean removeAssignment(final Assignment assignment) {
        return m_assignments.remove(assignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_preserveSnmpData, m_assignments);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Mapping) {
            final Mapping that = (Mapping)obj;
            return Objects.equals(this.m_preserveSnmpData, that.m_preserveSnmpData)
                    && Objects.equals(this.m_assignments, that.m_assignments);
        }
        return false;
    }

}
