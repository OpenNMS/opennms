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

package org.opennms.netmgt.config.snmpAsset.adapter;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class AssetField.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "assetField")
@XmlAccessorType(XmlAccessType.FIELD)
public class AssetField implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "formatString", required = true)
    private String formatString;

    @XmlElement(name = "mibObjs", required = true)
    private MibObjs mibObjs;

    public AssetField() {
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
        
        if (obj instanceof AssetField) {
            AssetField temp = (AssetField)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.formatString, formatString)
                && Objects.equals(temp.mibObjs, mibObjs);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'formatString'.
     * 
     * @return the value of field 'FormatString'.
     */
    public String getFormatString() {
        return this.formatString;
    }

    /**
     * Returns the value of field 'mibObjs'.
     * 
     * @return the value of field 'MibObjs'.
     */
    public MibObjs getMibObjs() {
        return this.mibObjs;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            formatString, 
            mibObjs);
        return hash;
    }

    /**
     * Sets the value of field 'formatString'.
     * 
     * @param formatString the value of field 'formatString'.
     */
    public void setFormatString(final String formatString) {
        this.formatString = formatString;
    }

    /**
     * Sets the value of field 'mibObjs'.
     * 
     * @param mibObjs the value of field 'mibObjs'.
     */
    public void setMibObjs(final MibObjs mibObjs) {
        this.mibObjs = mibObjs;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

}
