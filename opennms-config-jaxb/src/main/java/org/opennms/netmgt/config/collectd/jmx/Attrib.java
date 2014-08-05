/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="attrib")
@XmlAccessorType(XmlAccessType.FIELD)
// TODO mvr remove fishy methods
@SuppressWarnings("all")
public class Attrib implements java.io.Serializable {


    @XmlAttribute(name="name", required=true)
    private String _name;

    @XmlAttribute(name="alias")
    private String _alias;

    @XmlAttribute(name="type", required=true)
    private String _type;

    @XmlAttribute(name="maxval")
    private String _maxval;

    @XmlAttribute(name="minval")
    private String _minval;

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof Attrib) {
            Attrib temp = (Attrib)obj;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name)))
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._alias != null) {
                if (temp._alias == null) return false;
                else if (!(this._alias.equals(temp._alias)))
                    return false;
            }
            else if (temp._alias != null)
                return false;
            if (this._type != null) {
                if (temp._type == null) return false;
                else if (!(this._type.equals(temp._type)))
                    return false;
            }
            else if (temp._type != null)
                return false;
            if (this._maxval != null) {
                if (temp._maxval == null) return false;
                else if (!(this._maxval.equals(temp._maxval)))
                    return false;
            }
            else if (temp._maxval != null)
                return false;
            if (this._minval != null) {
                if (temp._minval == null) return false;
                else if (!(this._minval.equals(temp._minval)))
                    return false;
            }
            else if (temp._minval != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'alias'.
     *
     * @return the value of field 'Alias'.
     */
    public String getAlias(
    ) {
        return this._alias;
    }

    /**
     * Returns the value of field 'maxval'.
     *
     * @return the value of field 'Maxval'.
     */
    public String getMaxval(
    ) {
        return this._maxval;
    }

    /**
     * Returns the value of field 'minval'.
     *
     * @return the value of field 'Minval'.
     */
    public String getMinval(
    ) {
        return this._minval;
    }

    /**
     * Returns the value of field 'name'.
     *
     * @return the value of field 'Name'.
     */
    public String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'type'.
     *
     * @return the value of field 'Type'.
     */
    public String getType(
    ) {
        return this._type;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode(
    ) {
        int result = 17;

        long tmp;
        if (_name != null) {
           result = 37 * result + _name.hashCode();
        }
        if (_alias != null) {
           result = 37 * result + _alias.hashCode();
        }
        if (_type != null) {
           result = 37 * result + _type.hashCode();
        }
        if (_maxval != null) {
           result = 37 * result + _maxval.hashCode();
        }
        if (_minval != null) {
           result = 37 * result + _minval.hashCode();
        }

        return result;
    }

     /**
     * Sets the value of field 'alias'.
     *
     * @param alias the value of field 'alias'.
     */
    public void setAlias(
            final String alias) {
        this._alias = alias;
    }

    /**
     * Sets the value of field 'maxval'.
     *
     * @param maxval the value of field 'maxval'.
     */
    public void setMaxval(
            final String maxval) {
        this._maxval = maxval;
    }

    /**
     * Sets the value of field 'minval'.
     *
     * @param minval the value of field 'minval'.
     */
    public void setMinval(
            final String minval) {
        this._minval = minval;
    }

    /**
     * Sets the value of field 'name'.
     *
     * @param name the value of field 'name'.
     */
    public void setName(
            final String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'type'.
     *
     * @param type the value of field 'type'.
     */
    public void setType(
            final String type) {
        this._type = type;
    }
}
