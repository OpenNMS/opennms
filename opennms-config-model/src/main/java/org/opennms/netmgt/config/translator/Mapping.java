/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
