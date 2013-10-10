/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.rrdtool;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class RraDS.
 */
@XmlRootElement(name="ds")
@XmlAccessorType(XmlAccessType.FIELD)
public class RraDS {
    
    /** The primary value. */
    @XmlElement(name="primary_value")
    private Double primaryValue;
    
    /** The secondary value. */
    @XmlElement(name="secondary_value")
    private Double secondaryValue;
    
    /** The value. */
    @XmlElement(name="value")
    private Double value;
    
    /** The unknown data points. */
    @XmlElement(name="unknown_datapoints")
    private Long unknownDataPoints = 0L;

    /**
     * Gets the primary value.
     *
     * @return the primary value
     */
    public Double getPrimaryValue() {
        return primaryValue;
    }
    
    /**
     * Sets the primary value.
     *
     * @param primaryValue the new primary value
     */
    public void setPrimaryValue(Double primaryValue) {
        this.primaryValue = primaryValue;
    }
    
    /**
     * Gets the secondary value.
     *
     * @return the secondary value
     */
    public Double getSecondaryValue() {
        return secondaryValue;
    }
    
    /**
     * Sets the secondary value.
     *
     * @param secondaryValue the new secondary value
     */
    public void setSecondaryValue(Double secondaryValue) {
        this.secondaryValue = secondaryValue;
    }
    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public Double getValue() {
        return value;
    }
    
    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(Double value) {
        this.value = value;
    }
    
    /**
     * Gets the unknown data points.
     *
     * @return the unknown data points
     */
    public Long getUnknownDataPoints() {
        return unknownDataPoints;
    }
    
    /**
     * Sets the unknown data points.
     *
     * @param unknownDataPoints the new unknown data points
     */
    public void setUnknownDataPoints(Long unknownDataPoints) {
        this.unknownDataPoints = unknownDataPoints;
    }
    
}
