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

@XmlRootElement(name = "image-size")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImageSize implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "hz-size", required = true)
    private HzSize hzSize;

    @XmlElement(name = "vt-size", required = true)
    private VtSize vtSize;

    public ImageSize() {
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
        
        if (obj instanceof ImageSize) {
            ImageSize temp = (ImageSize)obj;
            boolean equals = Objects.equals(temp.hzSize, hzSize)
                && Objects.equals(temp.vtSize, vtSize);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'hzSize'.
     * 
     * @return the value of field 'HzSize'.
     */
    public HzSize getHzSize() {
        return this.hzSize;
    }

    /**
     * Returns the value of field 'vtSize'.
     * 
     * @return the value of field 'VtSize'.
     */
    public VtSize getVtSize() {
        return this.vtSize;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            hzSize, 
            vtSize);
        return hash;
    }

    /**
     * Sets the value of field 'hzSize'.
     * 
     * @param hzSize the value of field 'hzSize'.
     */
    public void setHzSize(final HzSize hzSize) {
        if (hzSize == null) {
            throw new IllegalArgumentException("'hz-size' is a required attribute!");
        }
        this.hzSize = hzSize;
    }

    /**
     * Sets the value of field 'vtSize'.
     * 
     * @param vtSize the value of field 'vtSize'.
     */
    public void setVtSize(final VtSize vtSize) {
        if (vtSize == null) {
            throw new IllegalArgumentException("'vt-size' is a required attribute!");
        }
        this.vtSize = vtSize;
    }

}
