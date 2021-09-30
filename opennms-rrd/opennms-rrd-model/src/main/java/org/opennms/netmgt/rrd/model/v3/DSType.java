/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model.v3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * The Enumeration DSType (Data Source Type).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlEnum
@XmlType(name = "type")
public enum DSType {

    /** The gauge data source. */
    GAUGE,

    /** The counter data source. */
    COUNTER,

    /** The derive data source. */
    DERIVE,

    /** The absolute data source. */
    ABSOLUTE,

    /** The compute data source. */
    COMPUTE;

    /**
     * Gets the DS Value.
     *
     * @return the string representation of the data source
     */
    public String value() {
        return name();
    }

    /**
     * From value.
     *
     * @param v the string name of the DS
     * @return the data source type
     */
    public static DSType fromValue(String v) {
        return v == null ? null : valueOf(v.trim());
    }
}
