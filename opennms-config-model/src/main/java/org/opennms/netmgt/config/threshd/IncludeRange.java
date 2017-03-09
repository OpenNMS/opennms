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

package org.opennms.netmgt.config.threshd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Range of adresses to be included in this
 *  package
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "include-range")
@XmlAccessorType(XmlAccessType.FIELD)
public class IncludeRange implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Starting address of the range
     */
    @XmlAttribute(name = "begin", required = true)
    private String begin;

    /**
     * Ending address of the range
     */
    @XmlAttribute(name = "end", required = true)
    private String end;

    public IncludeRange() {
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
        
        if (obj instanceof IncludeRange) {
            IncludeRange temp = (IncludeRange)obj;
            boolean equals = Objects.equals(temp.begin, begin)
                && Objects.equals(temp.end, end);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'begin'. The field 'begin' has the following
     * description: Starting address of the range
     * 
     * @return the value of field 'Begin'.
     */
    public String getBegin() {
        return this.begin;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the following
     * description: Ending address of the range
     * 
     * @return the value of field 'End'.
     */
    public String getEnd() {
        return this.end;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            begin, 
            end);
        return hash;
    }

    /**
     * Sets the value of field 'begin'. The field 'begin' has the following
     * description: Starting address of the range
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(final String begin) {
        this.begin = begin;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the following
     * description: Ending address of the range
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(final String end) {
        this.end = end;
    }

}
