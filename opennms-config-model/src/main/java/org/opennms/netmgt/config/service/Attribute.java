/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
package org.opennms.netmgt.config.service;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Attribute.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute implements Serializable {
    private static final long serialVersionUID = -5369595993818814229L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _name.
     */
    @XmlElement(name = "name")
    private String _name;

    /**
     * Field _value.
     */
    @XmlElement(name = "value")
    private Value _value;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Attribute() {
        super();
    }

    public Attribute(final String name, final String type,
            final String content) {
        super();
        setName(name);
        setValue(new Value(type, content));
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final java.lang.Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Attribute) {

            Attribute temp = (Attribute) obj;
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
     * Returns the value of field 'value'.
     * 
     * @return the value of field 'Value'.
     */
    public Value getValue() {
        return this._value;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

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
     * Sets the value of field 'value'.
     * 
     * @param value
     *            the value of field 'value'.
     */
    public void setValue(final Value value) {
        this._value = value;
    }
}
