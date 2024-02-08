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
