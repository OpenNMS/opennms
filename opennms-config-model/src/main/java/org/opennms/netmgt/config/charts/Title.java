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

@XmlRootElement(name = "title")
@XmlAccessorType(XmlAccessType.FIELD)
public class Title implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "value", required = true)
    private String value;

    @XmlAttribute(name = "font")
    private String font;

    @XmlAttribute(name = "pitch")
    private Integer pitch;

    @XmlAttribute(name = "style")
    private String style;

    @XmlElement(name = "rgb")
    private Rgb rgb;

    public Title() {
    }

    /**
     */
    public void deletePitch() {
        this.pitch= null;
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
        
        if (obj instanceof Title) {
            Title temp = (Title)obj;
            boolean equals = Objects.equals(temp.value, value)
                && Objects.equals(temp.font, font)
                && Objects.equals(temp.pitch, pitch)
                && Objects.equals(temp.style, style)
                && Objects.equals(temp.rgb, rgb);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'font'.
     * 
     * @return the value of field 'Font'.
     */
    public Optional<String> getFont() {
        return Optional.ofNullable(this.font);
    }

    /**
     * Returns the value of field 'pitch'.
     * 
     * @return the value of field 'Pitch'.
     */
    public Optional<Integer> getPitch() {
        return Optional.ofNullable(this.pitch);
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
     * Returns the value of field 'style'.
     * 
     * @return the value of field 'Style'.
     */
    public Optional<String> getStyle() {
        return Optional.ofNullable(this.style);
    }

    /**
     * Returns the value of field 'value'.
     * 
     * @return the value of field 'Value'.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Method hasPitch.
     * 
     * @return true if at least one Pitch has been added
     */
    public boolean hasPitch() {
        return this.pitch != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            value, 
            font, 
            pitch, 
            style, 
            rgb);
        return hash;
    }

    /**
     * Sets the value of field 'font'.
     * 
     * @param font the value of field 'font'.
     */
    public void setFont(final String font) {
        this.font = font;
    }

    /**
     * Sets the value of field 'pitch'.
     * 
     * @param pitch the value of field 'pitch'.
     */
    public void setPitch(final Integer pitch) {
        this.pitch = pitch;
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
     * Sets the value of field 'style'.
     * 
     * @param style the value of field 'style'.
     */
    public void setStyle(final String style) {
        this.style = style;
    }

    /**
     * Sets the value of field 'value'.
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("'value' is a required attribute!");
        }
        this.value = value;
    }

}
