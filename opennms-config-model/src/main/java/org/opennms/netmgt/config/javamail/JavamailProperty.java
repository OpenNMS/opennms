/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.javamail;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class JavamailProperty.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="javamail-property", namespace="http://xmlns.opennms.org/xsd/config/javamail-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class JavamailProperty implements Serializable {

    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -9126185637981119403L;

    /** The name. */
    @XmlAttribute(name="name")
    private String _name;

    /** The value. */
    @XmlAttribute(name="value")
    private String _value;

    //----------------/
    //- Constructors -/
    //----------------/

    /**
     * Instantiates a new javamail property.
     */
    public JavamailProperty() {
        super();
    }

    //-----------/
    //- Methods -/
    //-----------/

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        if (obj instanceof JavamailProperty) {
            JavamailProperty temp = (JavamailProperty)obj;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name))) 
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._value != null) {
                if (temp._value == null) return false;
                else if (!(this._value.equals(temp._value))) 
                    return false;
            }
            else if (temp._value != null)
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
    public String getValue() {
        return this._value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override()
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
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'value'.
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(final String value) {
        this._value = value;
    }

}
