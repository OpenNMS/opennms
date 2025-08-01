/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
