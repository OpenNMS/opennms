/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api.model;

import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.xml.event.Snmp;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableSnmp}'.
 */
public class ImmutableSnmpTest {

    @Test
    public void test() {
        Snmp snmp = new Snmp();
        snmp.setId("ID");
        snmp.setTrapOID("trapOID");
        snmp.setIdtext("ID-TEXT");
        snmp.setVersion("v2c");
        snmp.setSpecific(0);
        snmp.setGeneric(0);
        snmp.setCommunity("");
        snmp.setTimeStamp(0L);

        // Mutable to Immutable
        ISnmp immutableSnmp = ImmutableMapper.fromMutableSnmp(snmp);

        // Immutable to Mutable
        Snmp convertedSnmp = Snmp.copyFrom(immutableSnmp);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(snmp);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedSnmp);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
