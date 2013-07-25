/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.config.ackd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Parameters to be used for collecting this service. Parameters are specfic
 * to the service monitor.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter implements Serializable {
    private static final long serialVersionUID = -6535297763331028940L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _key.
     */
    @XmlAttribute(name = "key")
    private String _key;

    /**
     * Field _value.
     */
    @XmlAttribute(name = "value")
    private String _value;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Parameter() {
        super();
    }

    public Parameter(final String key, final String value) {
        super();
        setKey(key);
        setValue(value);
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
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Parameter) {

            Parameter temp = (Parameter) obj;
            if (this._key != null) {
                if (temp._key == null)
                    return false;
                else if (!(this._key.equals(temp._key)))
                    return false;
            } else if (temp._key != null)
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
     * Returns the value of field 'key'.
     * 
     * @return the value of field 'Key'.
     */
    public String getKey() {
        return this._key;
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
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_key != null) {
            result = 37 * result + _key.hashCode();
        }
        if (_value != null) {
            result = 37 * result + _value.hashCode();
        }

        return result;
    }

    /**
     * Sets the value of field 'key'.
     * 
     * @param key
     *            the value of field 'key'.
     */
    public void setKey(final String key) {
        this._key = key;
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
