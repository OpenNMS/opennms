/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * For regex matches, assign the value of a matching group
 *  to a named event parameter
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "parameter-assignment")
@XmlAccessorType(XmlAccessType.FIELD)
public class ParameterAssignment implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The number of the matching group from the regex
     *  whose value will be assigned. Group 0 always refers
     *  to the entire string matched by the expression. If
     *  the referenced group does not exist, the empty string
     *  will be assigned.
     *  
     */
    @XmlAttribute(name = "matching-group", required = true)
    private Integer matchingGroup;

    /**
     * The name of the event parameter to which the named
     *  matching group's value will be assigned
     *  
     */
    @XmlAttribute(name = "parameter-name", required = true)
    private String parameterName;

    public ParameterAssignment() {
    }

    /**
     */
    public void deleteMatchingGroup() {
        this.matchingGroup= null;
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof ParameterAssignment) {
            ParameterAssignment temp = (ParameterAssignment)obj;
            boolean equals = Objects.equals(temp.matchingGroup, matchingGroup)
                && Objects.equals(temp.parameterName, parameterName);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'matchingGroup'. The field 'matchingGroup' has
     * the following description: The number of the matching group from the regex
     *  whose value will be assigned. Group 0 always refers
     *  to the entire string matched by the expression. If
     *  the referenced group does not exist, the empty string
     *  will be assigned.
     *  
     * 
     * @return the value of field 'MatchingGroup'.
     */
    public Integer getMatchingGroup() {
        return this.matchingGroup;
    }

    /**
     * Returns the value of field 'parameterName'. The field 'parameterName' has
     * the following description: The name of the event parameter to which the
     * named
     *  matching group's value will be assigned
     *  
     * 
     * @return the value of field 'ParameterName'.
     */
    public String getParameterName() {
        return this.parameterName;
    }

    /**
     * Method hasMatchingGroup.
     * 
     * @return true if at least one MatchingGroup has been added
     */
    public boolean hasMatchingGroup() {
        return this.matchingGroup != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            matchingGroup, 
            parameterName);
        return hash;
    }

    /**
     * Sets the value of field 'matchingGroup'. The field 'matchingGroup' has the
     * following description: The number of the matching group from the regex
     *  whose value will be assigned. Group 0 always refers
     *  to the entire string matched by the expression. If
     *  the referenced group does not exist, the empty string
     *  will be assigned.
     *  
     * 
     * @param matchingGroup the value of field 'matchingGroup'.
     */
    public void setMatchingGroup(final Integer matchingGroup) {
        this.matchingGroup = matchingGroup;
    }

    /**
     * Sets the value of field 'parameterName'. The field 'parameterName' has the
     * following description: The name of the event parameter to which the named
     *  matching group's value will be assigned
     *  
     * 
     * @param parameterName the value of field 'parameterName'.
     */
    public void setParameterName(final String parameterName) {
        this.parameterName = parameterName;
    }

}
