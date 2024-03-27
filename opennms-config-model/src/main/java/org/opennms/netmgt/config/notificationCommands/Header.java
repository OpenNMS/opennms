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
package org.opennms.netmgt.config.notificationCommands;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Class Header.
 */
@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class Header implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "ver", required = true)
    private String ver;

    /**
     * creation time in the 'dow mon dd hh:mm:ss zzz yyyy'
     *  format
     */
    @XmlElement(name = "created", required = true)
    private String created;

    @XmlElement(name = "mstation", required = true)
    private String mstation;

    public Header() {
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
        
        if (obj instanceof Header) {
            Header temp = (Header)obj;
            boolean equals = Objects.equals(temp.ver, ver)
                && Objects.equals(temp.created, created)
                && Objects.equals(temp.mstation, mstation);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'created'. The field 'created' has the following
     * description: creation time in the 'dow mon dd hh:mm:ss zzz yyyy'
     *  format
     * 
     * @return the value of field 'Created'.
     */
    public String getCreated() {
        return this.created;
    }

    /**
     * Returns the value of field 'mstation'.
     * 
     * @return the value of field 'Mstation'.
     */
    public String getMstation() {
        return this.mstation;
    }

    /**
     * Returns the value of field 'ver'.
     * 
     * @return the value of field 'Ver'.
     */
    public String getVer() {
        return this.ver;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            ver, 
            created, 
            mstation);
        return hash;
    }

    /**
     * Sets the value of field 'created'. The field 'created' has the following
     * description: creation time in the 'dow mon dd hh:mm:ss zzz yyyy'
     *  format
     * 
     * @param created the value of field 'created'.
     */
    public void setCreated(final String created) {
        this.created = created;
    }

    /**
     * Sets the value of field 'mstation'.
     * 
     * @param mstation the value of field 'mstation'.
     */
    public void setMstation(final String mstation) {
        this.mstation = mstation;
    }

    /**
     * Sets the value of field 'ver'.
     * 
     * @param ver the value of field 'ver'.
     */
    public void setVer(final String ver) {
        this.ver = ver;
    }

}
