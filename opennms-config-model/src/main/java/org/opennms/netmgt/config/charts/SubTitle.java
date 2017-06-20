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

@XmlRootElement(name = "sub-title")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubTitle implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "position")
    private String position;

    @XmlAttribute(name = "horizontal-alignment")
    private String horizontalAlignment;

    @XmlElement(name = "title", required = true)
    private Title title;

    public SubTitle() {
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
        
        if (obj instanceof SubTitle) {
            SubTitle temp = (SubTitle)obj;
            boolean equals = Objects.equals(temp.position, position)
                && Objects.equals(temp.horizontalAlignment, horizontalAlignment)
                && Objects.equals(temp.title, title);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'horizontalAlignment'.
     * 
     * @return the value of field 'HorizontalAlignment'.
     */
    public Optional<String> getHorizontalAlignment() {
        return Optional.ofNullable(this.horizontalAlignment);
    }

    /**
     * Returns the value of field 'position'.
     * 
     * @return the value of field 'Position'.
     */
    public Optional<String> getPosition() {
        return Optional.ofNullable(this.position);
    }

    /**
     * Returns the value of field 'title'.
     * 
     * @return the value of field 'Title'.
     */
    public Title getTitle() {
        return this.title;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            position, 
            horizontalAlignment, 
            title);
        return hash;
    }

    /**
     * Sets the value of field 'horizontalAlignment'.
     * 
     * @param horizontalAlignment the value of field 'horizontalAlignment'.
     */
    public void setHorizontalAlignment(final String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Sets the value of field 'position'.
     * 
     * @param position the value of field 'position'.
     */
    public void setPosition(final String position) {
        this.position = position;
    }

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(final Title title) {
        if (title == null) {
            throw new IllegalArgumentException("'title' is a required element!");
        }
        this.title = title;
    }

}
