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
