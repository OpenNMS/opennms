/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller.outages;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Interface to which the outage applies.
 * 
 */

@XmlRootElement(name="interface", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class Interface implements Serializable {
    private static final long serialVersionUID = -90255076329128075L;

    /**
     * IP address
     */
    @XmlAttribute(name="address")
    private String _address;

    public Interface() {
        super();
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof Interface) {

            Interface temp = (Interface)obj;
            if (this._address != null) {
                if (temp._address == null) return false;
                else if (!(this._address.equals(temp._address))) 
                    return false;
            }
            else if (temp._address != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'address'. The field 'address'
     * has the following description: IP address
     * 
     * @return the value of field 'Address'.
     */
    public String getAddress() {
        return this._address;
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
    public int hashCode() {
        int result = 17;

        if (_address != null) {
            result = 37 * result + _address.hashCode();
        }

        return result;
    }

    /**
     * Sets the value of field 'address'. The field 'address' has
     * the following description: IP address
     * 
     * @param address the value of field 'address'.
     */
    public void setAddress(final String address) {
        this._address = address;
    }

}
