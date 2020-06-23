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

package org.opennms.netmgt.config.charts;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rgb")
@XmlAccessorType(XmlAccessType.FIELD)
public class Rgb implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "red", required = true)
    private Red red;

    @XmlElement(name = "green", required = true)
    private Green green;

    @XmlElement(name = "blue", required = true)
    private Blue blue;

    public Rgb() {
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
        
        if (obj instanceof Rgb) {
            Rgb temp = (Rgb)obj;
            boolean equals = Objects.equals(temp.red, red)
                && Objects.equals(temp.green, green)
                && Objects.equals(temp.blue, blue);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'blue'.
     * 
     * @return the value of field 'Blue'.
     */
    public Blue getBlue() {
        return this.blue;
    }

    /**
     * Returns the value of field 'green'.
     * 
     * @return the value of field 'Green'.
     */
    public Green getGreen() {
        return this.green;
    }

    /**
     * Returns the value of field 'red'.
     * 
     * @return the value of field 'Red'.
     */
    public Red getRed() {
        return this.red;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            red, 
            green, 
            blue);
        return hash;
    }

    /**
     * Sets the value of field 'blue'.
     * 
     * @param blue the value of field 'blue'.
     */
    public void setBlue(final Blue blue) {
        if (blue == null) {
            throw new IllegalArgumentException("'blue' is a required element!");
        }
        this.blue = blue;
    }

    /**
     * Sets the value of field 'green'.
     * 
     * @param green the value of field 'green'.
     */
    public void setGreen(final Green green) {
        if (green == null) {
            throw new IllegalArgumentException("'green' is a required element!");
        }
        this.green = green;
    }

    /**
     * Sets the value of field 'red'.
     * 
     * @param red the value of field 'red'.
     */
    public void setRed(final Red red) {
        if (red == null) {
            throw new IllegalArgumentException("'red' is a required element!");
        }
        this.red = red;
    }

}
