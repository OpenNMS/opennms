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

package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * For regex matches, assign the value of a matching group
 *  to a named event parameter
 */
@XmlRootElement(name = "parameter-assignment")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class ParameterAssignment implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The number of the matching group from the regex
     *  whose value will be assigned. Group 0 always refers
     *  to the entire string matched by the expression. If
     *  the referenced group does not exist, the empty string
     *  will be assigned.
     */
    @XmlAttribute(name = "matching-group", required = true)
    private Integer m_matchingGroup;

    /**
     * The name of the event parameter to which the named
     *  matching group's value will be assigned
     */
    @XmlAttribute(name = "parameter-name", required = true)
    private String m_parameterName;

    public ParameterAssignment() {
    }

    public Integer getMatchingGroup() {
        return m_matchingGroup;
    }

    public void setMatchingGroup(final Integer matchingGroup) {
        m_matchingGroup = ConfigUtils.assertNotNull(matchingGroup, "matching-group");
    }

    public String getParameterName() {
        return m_parameterName;
    }

    public void setParameterName(final String parameterName) {
        m_parameterName = ConfigUtils.assertNotEmpty(parameterName, "parameter-name");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_matchingGroup, m_parameterName);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ParameterAssignment) {
            final ParameterAssignment that = (ParameterAssignment)obj;
            return Objects.equals(this.m_matchingGroup, that.m_matchingGroup)
                    && Objects.equals(this.m_parameterName, that.m_parameterName);
        }
        return false;
    }

}
