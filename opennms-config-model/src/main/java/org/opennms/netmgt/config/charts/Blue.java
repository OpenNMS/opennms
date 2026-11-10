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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "blue")
@XmlAccessorType(XmlAccessType.FIELD)
public class Blue implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "rgb-color", required = true)
    private Integer rgbColor;

    public Blue() {
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
        
        if (obj instanceof Blue) {
            Blue temp = (Blue)obj;
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
