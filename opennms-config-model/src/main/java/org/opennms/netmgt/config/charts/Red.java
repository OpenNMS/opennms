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

@XmlRootElement(name = "red")
@XmlAccessorType(XmlAccessType.FIELD)
public class Red implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "rgb-color", required = true)
    private Integer rgbColor;

    public Red() {
    }

    /**
     */
    public void deleteRgbColor() {
        this.rgbColor= null;
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
        
        if (obj instanceof Red) {
            Red temp = (Red)obj;
            boolean equals = Objects.equals(temp.rgbColor, rgbColor);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'rgbColor'.
     * 
     * @return the value of field 'RgbColor'.
     */
    public Integer getRgbColor() {
        return this.rgbColor;
    }

    /**
     * Method hasRgbColor.
     * 
     * @return true if at least one RgbColor has been added
     */
    public boolean hasRgbColor() {
        return this.rgbColor != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            rgbColor);
        return hash;
    }

    /**
     * Sets the value of field 'rgbColor'.
     * 
     * @param rgbColor the value of field 'rgbColor'.
     */
    public void setRgbColor(final Integer rgbColor) {
        if (rgbColor == null) {
            throw new IllegalArgumentException("'rgb-color' is a required element!");
        }
        this.rgbColor = rgbColor;
    }

}
