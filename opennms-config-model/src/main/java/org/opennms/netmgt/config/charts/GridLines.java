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
