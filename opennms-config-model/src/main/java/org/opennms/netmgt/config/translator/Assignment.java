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

package org.opennms.netmgt.config.translator;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An element representing an assignement to an attribute of the event
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "assignment")
@XmlAccessorType(XmlAccessType.FIELD)
public class Assignment implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * An element representing a value to be used in a
     *  translation. 
     *  
     */
    @XmlElement(name = "value", required = true)
    private org.opennms.netmgt.config.translator.Value value;

    public Assignment() {
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
        
        if (obj instanceof Assignment) {
            Assignment temp = (Assignment)obj;
            boolean equals = Objects.equals(temp.type, type)
                && Objects.equals(temp.name, name)
                && Objects.equals(temp.value, value);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has the following
     * description: An element representing a value to be used in a
     *  translation. 
     *  
     * 
     * @return the value of field 'Value'.
     */
    public org.opennms.netmgt.config.translator.Value getValue() {
        return this.value;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            type, 
            name, 
            value);
        return hash;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the following
     * description: An element representing a value to be used in a
     *  translation. 
     *  
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(final org.opennms.netmgt.config.translator.Value value) {
        this.value = value;
    }

}
