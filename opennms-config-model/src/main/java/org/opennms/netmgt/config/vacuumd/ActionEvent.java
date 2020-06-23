/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.vacuumd;

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

@XmlRootElement(name = "action-event")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class ActionEvent implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final Boolean DEFAULT_FOR_EACH_RESULT_FLAG = Boolean.FALSE;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "for-each-result")
    private Boolean m_forEachResult;

    @XmlElement(name = "assignment")
    private List<Assignment> m_assignments = new ArrayList<>();

    public ActionEvent() {
    }

    public ActionEvent(final String name, final Boolean forEachResult, final List<Assignment> assignments) {
        setName(name);
        setForEachResult(forEachResult);
        setAssignments(assignments);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Boolean getForEachResult() {
        return m_forEachResult == null ? DEFAULT_FOR_EACH_RESULT_FLAG : m_forEachResult;
    }

    public void setForEachResult(final Boolean forEachResult) {
        m_forEachResult = forEachResult;
    }

    public List<Assignment> getAssignments() {
        return m_assignments;
    }

    public void setAssignments(final List<Assignment> assignments) {
        ConfigUtils.assertMinimumSize(assignments, 1, "assignment");
        if (assignments == m_assignments) return;
        m_assignments.clear();
        if (assignments != null) m_assignments.addAll(assignments);
    }

    public void addAssignment(final Assignment assignment) {
        m_assignments.add(assignment);
    }

    public Boolean removeAssignment(final Assignment assignment) {
        return m_assignments.remove(assignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_forEachResult, m_assignments);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ActionEvent) {
            final ActionEvent that = (ActionEvent) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_forEachResult, that.m_forEachResult) &&
                    Objects.equals(this.m_assignments, that.m_assignments);
        }
        return false;
    }
}
