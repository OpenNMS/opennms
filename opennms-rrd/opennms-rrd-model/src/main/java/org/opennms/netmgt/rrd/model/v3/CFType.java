/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

/**
 * The Enumeration CFType (Consolidation Function Type).
 */
public enum CFType {

    /** v3: RRA:AVERAGE:xff:steps:rows. */
    AVERAGE,

    /** v3: RRA:MIN:xff:steps:rows. */
    MIN,

    /** v3: RRA:MAX:xff:steps:rows. */
    MAX,

    /** v3: RRA:LAST:xff:steps:rows. */
    LAST,

    /** v3: RRA:HWPREDICT:rows:alpha:beta:seasonal-period[:rra-num]. */
    HWPREDICT,

    /** v3: RRA:SEASONAL:seasonal-period:gamma:rra-num[:smoothing-window=fraction]. */
    SEASONAL,

    /** v3: RRA:DEVSEASONAL:seasonal-period:gamma:rra-num[:smoothing-window=fraction]. */
    DEVSEASONAL,

    /** v3: RRA:DEVPREDICT:rows:rra-num. */
    DEVPREDICT,

    /** v3: RRA:FAILURES:rows:threshold:window-length:rra-num. */
    FAILURES,

    /** v4: RRA:MHWPREDICT:rows:alpha:beta:seasonal-period[:rra-num] */
    MHWPREDICT;

    /**
     * Gets the CF Value.
     *
     * @return the string representation of the consolidation function
     */
    public String value() {
        return name();
    }

    /**
     * From value.
     *
     * @param v the string name of the CF
     * @return the consolidation function type
     */
    public static CFType fromValue(String v) {
        return v == null ? null : valueOf(v.trim());
    }
}
