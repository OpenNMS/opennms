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

package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Day.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "day")
@XmlAccessorType(XmlAccessType.FIELD)
public class Day implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "date")
    private Integer date;

    @XmlAttribute(name = "pctValue")
    private Double pctValue;

    @XmlAttribute(name = "visible")
    private Boolean visible;

    public Day() {
    }

    /**
     */
    public void deleteDate() {
        this.date= null;
    }

    /**
     */
    public void deletePctValue() {
        this.pctValue= null;
    }

    /**
     */
    public void deleteVisible() {
        this.visible= null;
    }

    /**
     * Returns the value of field 'date'.
     * 
     * @return the value of field 'Date'.
     */
    public Integer getDate() {
        return this.date;
    }

    /**
     * Returns the value of field 'pctValue'.
     * 
     * @return the value of field 'PctValue'.
     */
    public Double getPctValue() {
        return this.pctValue;
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public Boolean getVisible() {
        return this.visible;
    }

    /**
     * Method hasDate.
     * 
     * @return true if at least one Date has been added
     */
    public boolean hasDate() {
        return this.date != null;
    }

    /**
     * Method hasPctValue.
     * 
     * @return true if at least one PctValue has been added
     */
    public boolean hasPctValue() {
        return this.pctValue != null;
    }

    /**
     * Method hasVisible.
     * 
     * @return true if at least one Visible has been added
     */
    public boolean hasVisible() {
        return this.visible != null;
    }

    /**
     * Returns the value of field 'visible'.
     * 
     * @return the value of field 'Visible'.
     */
    public Boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets the value of field 'date'.
     * 
     * @param date the value of field 'date'.
     */
    public void setDate(final Integer date) {
        this.date = date;
    }

    /**
     * Sets the value of field 'pctValue'.
     * 
     * @param pctValue the value of field 'pctValue'.
     */
    public void setPctValue(final Double pctValue) {
        this.pctValue = pctValue;
    }

    /**
     * Sets the value of field 'visible'.
     * 
     * @param visible the value of field 'visible'.
     */
    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }

}
