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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * The Enumeration SnmpVersion.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlType
@XmlEnum(String.class)
public enum SnmpVersion {

    /** The V1. */
    @XmlEnumValue("v1")
    V1("v1"),

    /** The V2c. */
    @XmlEnumValue("v2c")
    V2c("v2c"),

    /** The V3. */
    @XmlEnumValue("v3")
    V3("v3"),

    /** The V2_ inform. */
    @XmlEnumValue("v2-inform")
    V2_INFORM("v2-inform"),

    /** The V3_ inform. */
    @XmlEnumValue("v3-inform")
    V3_INFORM("v3-inform");

    /** The id. */
    private String m_version;

    /**
     * Instantiates a new SNMP version.
     *
     * @param version the SNMP version as string
     */
    SnmpVersion(String version) {
        m_version = version;
    }

    /**
     * Gets the string representation of the SNMP version.
     *
     * @return the SNMP version as string
     */
    public String stringValue() {
        return m_version;
    }

    /**
     * Gets the integer representation of the SNMP version.
     *
     * @return the SNMP version as integer
     */
    public int intValue() {
        if (isV1())
            return 1;
        if (isV2())
            return 2;
        if (isV3())
            return 3;
        return 0;
    }

    /**
     * Checks if is v1.
     *
     * @return true, if is v1
     */
    public boolean isV1() {
        return m_version.startsWith("v1");
    }

    /**
     * Checks if is v2.
     *
     * @return true, if is v2
     */
    public boolean isV2() {
        return m_version.startsWith("v2");
    }

    /**
     * Checks if is v3.
     *
     * @return true, if is v3
     */
    public boolean isV3() {
        return m_version.startsWith("v3");
    }

}
