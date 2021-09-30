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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "grid-lines")
@XmlAccessorType(XmlAccessType.FIELD)
public class GridLines implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "visible", required = true)
    private Boolean visible;

    @XmlElement(name = "rgb")
    private Rgb rgb;

    public GridLines() {
    }

    /**
     */
    public void deleteVisible() {
        this.visible= null;
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
        
        if (obj instanceof GridLines) {
            GridLines temp = (GridLines)obj;
            boolean equals = Objects.equals(temp.visible, visible)
                && Objects.equals(temp.rgb, rgb);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'rgb'.
     * 
     * @return the value of field 'Rgb'.
     */
    public Optional<Rgb> getRgb() {
        return Optional.ofNullable(this.rgb);
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public Boolean getVisible() {
        return this.visible;
    }

    /**
     * Method hasVisible.
     * 
     * @return true if at least one Visible has been added
     */
    public boolean hasVisible() {
        return this.visible != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            visible, 
            rgb);
        return hash;
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public Boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets the value of field 'rgb'.
     * 
     * @param rgb the value of field 'rgb'.
     */
    public void setRgb(final Rgb rgb) {
        this.rgb = rgb;
    }

    /**
     * Sets the value of field 'visible'.
     * 
     * @param visible the value of field 'visible'.
     */
    public void setVisible(final Boolean visible) {
        if (visible == null) {
            throw new IllegalArgumentException("'visible' is a required attribute!");
        }
        this.visible = visible;
    }

}
