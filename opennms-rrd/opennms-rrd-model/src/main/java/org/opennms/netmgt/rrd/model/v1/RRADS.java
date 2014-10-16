/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd.model.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.rrd.model.DoubleAdapter;
import org.opennms.netmgt.rrd.model.LongAdapter;

/**
 * The Class RraDS (RRA CDP Data Source).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="ds")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RRADS {

    /** The unknown data points. */
    private Long unknownDataPoints = 0L;

    /** The value. */
    private Double value;

    /**
     * Gets the unknown data points.
     *
     * @return the unknown data points
     */
    @XmlElement(name="unknown_datapoints")
    @XmlJavaTypeAdapter(LongAdapter.class)
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

    /**
     * Gets the value.
     *
     * @return the value
     */
    @XmlElement(name="value")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
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

}
