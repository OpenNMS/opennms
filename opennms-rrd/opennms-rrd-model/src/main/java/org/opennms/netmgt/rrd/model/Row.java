/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The Class Row.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="row")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Row {

    /** The values. */
    private List<Double> values = new ArrayList<>();

    /**
     * Gets the values.
     *
     * @return the values
     */
    @XmlElement(name="v")
    public List<Double> getValues() {
        return values;
    }

    /**
     * Gets the value.
     *
     * @param index the index
     * @return the value
     */
    public Double getValue(int index) {
        return values.get(index);
    }

    /**
     * Sets the values.
     *
     * @param values the new values
     */
    public void setValues(List<Double> values) {
        this.values = values;
    }

    /**
     * Checks if is all the values are NaN.
     *
     * @return true, if all the values are NaN.
     */
    @XmlTransient
    public boolean isNan() {
        for (Double v : values) {
            if (!v.isNaN()) {
                return false;
            }
        }
        return true;
    }
}
