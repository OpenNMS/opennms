/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
