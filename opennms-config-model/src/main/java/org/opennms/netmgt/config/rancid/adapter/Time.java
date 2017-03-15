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

package org.opennms.netmgt.config.rancid.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * defines start/end time for the outage
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.FIELD)
public class Time implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * an identifier for this event used for reference in the web gui. If this
     *  identifer is not assigned it will be assigned an identifer by web gui.
     *  
     */
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "day")
    private String day;

    /**
     * when the outage starts
     */
    @XmlAttribute(name = "begins", required = true)
    private String begins;

    /**
     * when the outage ends
     */
    @XmlAttribute(name = "ends", required = true)
    private String ends;

    public Time() {
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
            boolean equals = Objects.equals(temp.id, id)
                && Objects.equals(temp.day, day)
                && Objects.equals(temp.begins, begins)
                && Objects.equals(temp.ends, ends);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'begins'. The field 'begins' has the following
     * description: when the outage starts
     * 
     * @return the value of field 'Begins'.
     */
    public String getBegins() {
        return this.begins;
    }

    /**
     * Returns the value of field 'day'.
     * 
     * @return the value of field 'Day'.
     */
    public String getDay() {
        return this.day;
    }

    /**
     * Returns the value of field 'ends'. The field 'ends' has the following
     * description: when the outage ends
     * 
     * @return the value of field 'Ends'.
     */
    public String getEnds() {
        return this.ends;
    }

    /**
     * Returns the value of field 'id'. The field 'id' has the following
     * description: an identifier for this event used for reference in the web
     * gui. If this
     *  identifer is not assigned it will be assigned an identifer by web gui.
     *  
     * 
     * @return the value of field 'Id'.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            id, 
            day, 
            begins, 
            ends);
        return hash;
    }

    /**
     * Sets the value of field 'begins'. The field 'begins' has the following
     * description: when the outage starts
     * 
     * @param begins the value of field 'begins'.
     */
    public void setBegins(final String begins) {
        this.begins = begins;
    }

    /**
     * Sets the value of field 'day'.
     * 
     * @param day the value of field 'day'.
     */
    public void setDay(final String day) {
        this.day = day;
    }

    /**
     * Sets the value of field 'ends'. The field 'ends' has the following
     * description: when the outage ends
     * 
     * @param ends the value of field 'ends'.
     */
    public void setEnds(final String ends) {
        this.ends = ends;
    }

    /**
     * Sets the value of field 'id'. The field 'id' has the following description:
     * an identifier for this event used for reference in the web gui. If this
     *  identifer is not assigned it will be assigned an identifer by web gui.
     *  
     * 
     * @param id the value of field 'id'.
     */
    public void setId(final String id) {
        this.id = id;
    }

}
