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

package org.opennms.netmgt.config.reporting;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 24 hour clock time
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.FIELD)
public class Time implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * hours
     */
    @XmlElement(name = "hours", required = true)
    private Integer hours;

    /**
     * minutes
     */
    @XmlElement(name = "minutes", required = true)
    private Integer minutes;

    public Time() {
    }

    /**
     */
    public void deleteHours() {
        this.hours= null;
    }

    /**
     */
    public void deleteMinutes() {
        this.minutes= null;
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
        
        if (obj instanceof Time) {
            Time temp = (Time)obj;
            boolean equals = Objects.equals(temp.hours, hours)
                && Objects.equals(temp.minutes, minutes);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'hours'. The field 'hours' has the following
     * description: hours
     * 
     * @return the value of field 'Hours'.
     */
    public Integer getHours() {
        return this.hours;
    }

    /**
     * Returns the value of field 'minutes'. The field 'minutes' has the following
     * description: minutes
     * 
     * @return the value of field 'Minutes'.
     */
    public Integer getMinutes() {
        return this.minutes;
    }

    /**
     * Method hasHours.
     * 
     * @return true if at least one Hours has been added
     */
    public boolean hasHours() {
        return this.hours != null;
    }

    /**
     * Method hasMinutes.
     * 
     * @return true if at least one Minutes has been added
     */
    public boolean hasMinutes() {
        return this.minutes != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            hours, 
            minutes);
        return hash;
    }

    /**
     * Sets the value of field 'hours'. The field 'hours' has the following
     * description: hours
     * 
     * @param hours the value of field 'hours'.
     */
    public void setHours(final Integer hours) {
        this.hours = hours;
    }

    /**
     * Sets the value of field 'minutes'. The field 'minutes' has the following
     * description: minutes
     * 
     * @param minutes the value of field 'minutes'.
     */
    public void setMinutes(final Integer minutes) {
        this.minutes = minutes;
    }

}
