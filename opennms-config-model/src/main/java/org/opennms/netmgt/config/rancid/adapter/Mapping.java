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

package org.opennms.netmgt.config.rancid.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A map from sysoids masks and rancid device type.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "mapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mapping implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * sysoid to match.
     */
    @XmlAttribute(name = "sysoid-mask", required = true)
    private String sysoidMask;

    /**
     * regular expression to match sysdescription.
     */
    @XmlAttribute(name = "sysdescr-match")
    private String sysdescrMatch;

    /**
     * The rancid device type 
     *  for the specified sysoid mask.
     */
    @XmlAttribute(name = "type", required = true)
    private String type;

    public Mapping() {
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
        
        if (obj instanceof Mapping) {
            Mapping temp = (Mapping)obj;
            boolean equals = Objects.equals(temp.sysoidMask, sysoidMask)
                && Objects.equals(temp.sysdescrMatch, sysdescrMatch)
                && Objects.equals(temp.type, type);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'sysdescrMatch'. The field 'sysdescrMatch' has
     * the following description: regular expression to match sysdescription.
     * 
     * @return the value of field 'SysdescrMatch'.
     */
    public String getSysdescrMatch() {
        return this.sysdescrMatch;
    }

    /**
     * Returns the value of field 'sysoidMask'. The field 'sysoidMask' has the
     * following description: sysoid to match.
     * 
     * @return the value of field 'SysoidMask'.
     */
    public String getSysoidMask() {
        return this.sysoidMask;
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the following
     * description: The rancid device type 
     *  for the specified sysoid mask.
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            sysoidMask, 
            sysdescrMatch, 
            type);
        return hash;
    }

    /**
     * Sets the value of field 'sysdescrMatch'. The field 'sysdescrMatch' has the
     * following description: regular expression to match sysdescription.
     * 
     * @param sysdescrMatch the value of field 'sysdescrMatch'.
     */
    public void setSysdescrMatch(final String sysdescrMatch) {
        this.sysdescrMatch = sysdescrMatch;
    }

    /**
     * Sets the value of field 'sysoidMask'. The field 'sysoidMask' has the
     * following description: sysoid to match.
     * 
     * @param sysoidMask the value of field 'sysoidMask'.
     */
    public void setSysoidMask(final String sysoidMask) {
        this.sysoidMask = sysoidMask;
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the following
     * description: The rancid device type 
     *  for the specified sysoid mask.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

}
