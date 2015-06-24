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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * (THIS IS BEING DEPRECATED) actions modify the database based on results of
 * a trigger
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "auto-event")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoEvent implements Serializable {
    private static final long serialVersionUID = 3309476961723377055L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _name.
     */
    @XmlAttribute(name = "name")
    private String _name;

    /**
     * Field _fields.
     */
    @XmlAttribute(name = "fields")
    private String _fields;

    /**
     * Must be a UEI defined in event-conf.xml
     */
    @XmlElement(name = "uei")
    private Uei _uei;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public AutoEvent() {
        super();
    }

    public AutoEvent(final String name, final String fields, final Uei uei) {
        super();
        setName(name);
        setFields(fields);
        setUei(uei);
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

        if (obj instanceof AutoEvent) {

            AutoEvent temp = (AutoEvent) obj;
            if (this._name != null) {
                if (temp._name == null)
                    return false;
                else if (!(this._name.equals(temp._name)))
                    return false;
            } else if (temp._name != null)
                return false;
            if (this._fields != null) {
                if (temp._fields == null)
                    return false;
                else if (!(this._fields.equals(temp._fields)))
                    return false;
            } else if (temp._fields != null)
                return false;
            if (this._uei != null) {
                if (temp._uei == null)
                    return false;
                else if (!(this._uei.equals(temp._uei)))
                    return false;
            } else if (temp._uei != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'fields'.
     *
     * @return the value of field 'Fields'.
     */
    public String getFields() {
        return this._fields;
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
     * Returns the value of field 'uei'. The field 'uei' has the following
     * description: Must be a UEI defined in event-conf.xml
     *
     * @return the value of field 'Uei'.
     */
    public Uei getUei() {
        return this._uei;
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

        if (_name != null) {
            result = 37 * result + _name.hashCode();
        }
        if (_fields != null) {
            result = 37 * result + _fields.hashCode();
        }
        if (_uei != null) {
            result = 37 * result + _uei.hashCode();
        }

        return result;
    }

    /**
     * Sets the value of field 'fields'.
     *
     * @param fields
     *            the value of field 'fields'.
     */
    public void setFields(final String fields) {
        this._fields = fields;
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
     * Sets the value of field 'uei'. The field 'uei' has the following
     * description: Must be a UEI defined in event-conf.xml
     *
     * @param uei
     *            the value of field 'uei'.
     */
    public void setUei(final Uei uei) {
        this._uei = uei;
    }
}
