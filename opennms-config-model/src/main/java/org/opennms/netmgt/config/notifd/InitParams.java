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

package org.opennms.netmgt.config.notifd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class InitParams.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "init-params")
@XmlAccessorType(XmlAccessType.FIELD)
public class InitParams implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "param-name", required = true)
    private String paramName;

    @XmlElement(name = "param-value", required = true)
    private String paramValue;

    public InitParams() {
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
        
        if (obj instanceof InitParams) {
            InitParams temp = (InitParams)obj;
            boolean equals = Objects.equals(temp.paramName, paramName)
                && Objects.equals(temp.paramValue, paramValue);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'paramName'.
     * 
     * @return the value of field 'ParamName'.
     */
    public String getParamName() {
        return this.paramName;
    }

    /**
     * Returns the value of field 'paramValue'.
     * 
     * @return the value of field 'ParamValue'.
     */
    public String getParamValue() {
        return this.paramValue;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            paramName, 
            paramValue);
        return hash;
    }

    /**
     * Sets the value of field 'paramName'.
     * 
     * @param paramName the value of field 'paramName'.
     */
    public void setParamName(final String paramName) {
        this.paramName = paramName;
    }

    /**
     * Sets the value of field 'paramValue'.
     * 
     * @param paramValue the value of field 'paramValue'.
     */
    public void setParamValue(final String paramValue) {
        this.paramValue = paramValue;
    }

}
