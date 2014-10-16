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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.vacuumd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Assignment.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "assignment")
@XmlAccessorType(XmlAccessType.FIELD)
public class Assignment implements java.io.Serializable {
    private static final long serialVersionUID = 3776658176226615168L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _type.
     */
    @XmlAttribute(name = "type")
    private String _type;

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private String _name;

    /**
     * Field _value.
     */
    @XmlAttribute(name = "value")
    private String _value;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Assignment() {
        super();
    }

    public Assignment(final String type, final String name, final String value) {
        super();
        setType(type);
        setName(name);
        setValue(value);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Assignment) {

            Assignment temp = (Assignment) obj;
            if (this._type != null) {
                if (temp._type == null)
                    return false;
                else if (!(this._type.equals(temp._type)))
                    return false;
            } else if (temp._type != null)
                return false;
            if (this._name != null) {
                if (temp._name == null)
                    return false;
                else if (!(this._name.equals(temp._name)))
                    return false;
            } else if (temp._name != null)
                return false;
            if (this._value != null) {
                if (temp._value == null)
                    return false;
                else if (!(this._value.equals(temp._value)))
                    return false;
            } else if (temp._value != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this._name;
    }

    /**
     * Returns the value of field 'type'.
     *
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this._type;
    }

    /**
     * Returns the value of field 'value'.
     *
     * @return the value of field 'Value'.
     */
    public String getValue() {
        return this._value;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_type != null) {
            result = 37 * result + _type.hashCode();
        }
        if (_name != null) {
            result = 37 * result + _name.hashCode();
        }
        if (_value != null) {
            result = 37 * result + _value.hashCode();
        }

        return result;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name
     *            the value of field 'name'.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'type'.
     *
     * @param type
     *            the value of field 'type'.
     */
    public void setType(final String type) {
        this._type = type;
    }

    /**
     * Sets the value of field 'value'.
     *
     * @param value
     *            the value of field 'value'.
     */
    public void setValue(final String value) {
        this._value = value;
    }
}
