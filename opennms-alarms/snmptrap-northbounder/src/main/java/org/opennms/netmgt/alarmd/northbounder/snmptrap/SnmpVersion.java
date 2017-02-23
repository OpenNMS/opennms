/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
