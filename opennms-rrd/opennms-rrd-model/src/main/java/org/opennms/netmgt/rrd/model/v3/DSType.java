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
