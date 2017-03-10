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
 * An offset period used as a base to determine a real
 *  date when running the report
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "date-parm")
@XmlAccessorType(XmlAccessType.FIELD)
public class DateParm implements java.io.Serializable {
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
     * flag to use absolute date if possible
     */
    @XmlAttribute(name = "use-absolute-date")
    private Boolean useAbsoluteDate;

    @XmlElement(name = "default-interval", required = true)
    private String defaultInterval;

    @XmlElement(name = "default-count", required = true)
    private Integer defaultCount;

    @XmlElement(name = "default-time")
    private DefaultTime defaultTime;

    public DateParm() {
    }

    /**
     */
    public void deleteDefaultCount() {
        this.defaultCount= null;
    }

    /**
     */
    public void deleteUseAbsoluteDate() {
        this.useAbsoluteDate= null;
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
        
        if (obj instanceof DateParm) {
            DateParm temp = (DateParm)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.displayName, displayName)
                && Objects.equals(temp.useAbsoluteDate, useAbsoluteDate)
                && Objects.equals(temp.defaultInterval, defaultInterval)
                && Objects.equals(temp.defaultCount, defaultCount)
                && Objects.equals(temp.defaultTime, defaultTime);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'defaultCount'.
     * 
     * @return the value of field 'DefaultCount'.
     */
    public Integer getDefaultCount() {
        return this.defaultCount;
    }

    /**
     * Returns the value of field 'defaultInterval'.
     * 
     * @return the value of field 'DefaultInterval'.
     */
    public String getDefaultInterval() {
        return this.defaultInterval;
    }

    /**
     * Returns the value of field 'defaultTime'.
     * 
     * @return the value of field 'DefaultTime'.
     */
    public DefaultTime getDefaultTime() {
        return this.defaultTime;
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
     * Returns the value of field 'useAbsoluteDate'. The field 'useAbsoluteDate'
     * has the following description: flag to use absolute date if possible
     * 
     * @return the value of field 'UseAbsoluteDate'.
     */
    public Boolean getUseAbsoluteDate() {
        return this.useAbsoluteDate;
    }

    /**
     * Method hasDefaultCount.
     * 
     * @return true if at least one DefaultCount has been added
     */
    public boolean hasDefaultCount() {
        return this.defaultCount != null;
    }

    /**
     * Method hasUseAbsoluteDate.
     * 
     * @return true if at least one UseAbsoluteDate has been added
     */
    public boolean hasUseAbsoluteDate() {
        return this.useAbsoluteDate != null;
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
            useAbsoluteDate, 
            defaultInterval, 
            defaultCount, 
            defaultTime);
        return hash;
    }

    /**
     * Returns the value of field 'useAbsoluteDate'. The field 'useAbsoluteDate'
     * has the following description: flag to use absolute date if possible
     * 
     * @return the value of field 'UseAbsoluteDate'.
     */
    public Boolean isUseAbsoluteDate() {
        return this.useAbsoluteDate;
    }

    /**
     * Sets the value of field 'defaultCount'.
     * 
     * @param defaultCount the value of field 'defaultCount'.
     */
    public void setDefaultCount(final Integer defaultCount) {
        this.defaultCount = defaultCount;
    }

    /**
     * Sets the value of field 'defaultInterval'.
     * 
     * @param defaultInterval the value of field 'defaultInterval'.
     */
    public void setDefaultInterval(final String defaultInterval) {
        this.defaultInterval = defaultInterval;
    }

    /**
     * Sets the value of field 'defaultTime'.
     * 
     * @param defaultTime the value of field 'defaultTime'.
     */
    public void setDefaultTime(final DefaultTime defaultTime) {
        this.defaultTime = defaultTime;
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
     * Sets the value of field 'name'. The field 'name' has the following
     * description: the name of this parameter as passed to the report
     *  engine
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'useAbsoluteDate'. The field 'useAbsoluteDate' has
     * the following description: flag to use absolute date if possible
     * 
     * @param useAbsoluteDate the value of field 'useAbsoluteDate'.
     */
    public void setUseAbsoluteDate(final Boolean useAbsoluteDate) {
        this.useAbsoluteDate = useAbsoluteDate;
    }

}
