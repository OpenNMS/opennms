/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

/**
 * This test is maintained outside of the API project since it needs
 * access to an implementation of the {@link SnmpValueFactory}.
 *
 * @author jwhite
 */
public class SnmpResultXmlTest extends XmlTestNoCastor<SnmpResult> {

    public SnmpResultXmlTest(SnmpResult sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSnmpResult(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<snmp-result>\n" +
                       "<base>.1.3.6.1.2</base>\n" +
                       "<instance>1.3.6.1.2.1.4.34.1.3.1.2.3.4</instance>\n" +
                       "<value type=\"70\">Cg==</value>\n" +
                    "</snmp-result>"
                }
        });
    }

    private static SnmpResult getSnmpResult() {
        final SnmpValueFactory snmpValueFactory = new Snmp4JValueFactory();
        final SnmpResult result = new SnmpResult(
                SnmpObjId.get(".1.3.6.1.2"),
                new SnmpInstId(".1.3.6.1.2.1.4.34.1.3.1.2.3.4"),
                snmpValueFactory.getCounter64(BigInteger.TEN));
        return result;
    }
}
