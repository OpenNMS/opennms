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

package org.opennms.netmgt.config.reporting;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A string parameter passed to the report engine
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "int-parm")
@XmlAccessorType(XmlAccessType.FIELD)
public class IntParm implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * the name of this parameter as passed to the report
     *  engine
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * the name of this parameter as displayed in the
     *  webui
     */
    @XmlAttribute(name = "display-name", required = true)
    private String displayName;

    /**
     * the type of input field used. Currently freeText onlly
     */
    @XmlAttribute(name = "input-type", required = true)
    private String inputType;

    /**
     * value
     */
    @XmlElement(name = "default")
    private Integer _default;

    public IntParm() {
    }

    /**
     */
    public void deleteDefault() {
        this._default= null;
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
        
        if (obj instanceof IntParm) {
            IntParm temp = (IntParm)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.displayName, displayName)
                && Objects.equals(temp.inputType, inputType)
                && Objects.equals(temp._default, _default);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'default'. The field 'default' has the following
     * description: value
     * 
     * @return the value of field 'Default'.
     */
    public Integer getDefault() {
        return this._default;
    }

    /**
     * Returns the value of field 'displayName'. The field 'displayName' has the
     * following description: the name of this parameter as displayed in the
     *  webui
     * 
     * @return the value of field 'DisplayName'.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Returns the value of field 'inputType'. The field 'inputType' has the
     * following description: the type of input field used. Currently freeText
     * onlly
     * 
     * @return the value of field 'InputType'.
     */
    public String getInputType() {
        return this.inputType;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: the name of this parameter as passed to the report
     *  engine
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method hasDefault.
     * 
     * @return true if at least one Default has been added
     */
    public boolean hasDefault() {
        return this._default != null;
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
            displayName, 
            inputType, 
            _default);
        return hash;
    }

    /**
     * Sets the value of field 'default'. The field 'default' has the following
     * description: value
     * 
     * @param _default
     * @param default the value of field 'default'.
     */
    public void setDefault(final Integer _default) {
        this._default = _default;
    }

    /**
     * Sets the value of field 'displayName'. The field 'displayName' has the
     * following description: the name of this parameter as displayed in the
     *  webui
     * 
     * @param displayName the value of field 'displayName'.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the value of field 'inputType'. The field 'inputType' has the
     * following description: the type of input field used. Currently freeText
     * onlly
     * 
     * @param inputType the value of field 'inputType'.
     */
    public void setInputType(final String inputType) {
        this.inputType = inputType;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: the name of this parameter as passed to the report
     *  engine
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

}
