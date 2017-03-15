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

package org.opennms.netmgt.config.notifications;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The varbind element
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "varbind")
@XmlAccessorType(XmlAccessType.FIELD)
public class Varbind implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The varbind element number
     */
    @XmlElement(name = "vbname", required = true)
    private String vbname;

    /**
     * The varbind element value
     */
    @XmlElement(name = "vbvalue", required = true)
    private String vbvalue;

    public Varbind() {
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
        
        if (obj instanceof Varbind) {
            Varbind temp = (Varbind)obj;
            boolean equals = Objects.equals(temp.vbname, vbname)
                && Objects.equals(temp.vbvalue, vbvalue);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'vbname'. The field 'vbname' has the following
     * description: The varbind element number
     * 
     * @return the value of field 'Vbname'.
     */
    public String getVbname() {
        return this.vbname;
    }

    /**
     * Returns the value of field 'vbvalue'. The field 'vbvalue' has the following
     * description: The varbind element value
     * 
     * @return the value of field 'Vbvalue'.
     */
    public String getVbvalue() {
        return this.vbvalue;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            vbname, 
            vbvalue);
        return hash;
    }

    /**
     * Sets the value of field 'vbname'. The field 'vbname' has the following
     * description: The varbind element number
     * 
     * @param vbname the value of field 'vbname'.
     */
    public void setVbname(final String vbname) {
        if (vbname == null) {
            throw new IllegalArgumentException("vbname is a required field!");
        }
        this.vbname = vbname;
    }

    /**
     * Sets the value of field 'vbvalue'. The field 'vbvalue' has the following
     * description: The varbind element value
     * 
     * @param vbvalue the value of field 'vbvalue'.
     */
    public void setVbvalue(final String vbvalue) {
        this.vbvalue = vbvalue;
    }

}
