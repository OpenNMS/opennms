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

package org.opennms.netmgt.snmp.proxy.common;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValueFactory;

public class SnmpMultiResponseDTOTest extends XmlTestNoCastor<SnmpMultiResponseDTO> {

    public SnmpMultiResponseDTOTest(SnmpMultiResponseDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSnmpMultiResponse(),
                    "<?xml version=\"1.0\"?>\n" +
                    "<snmp-response>\n" +
                        "<response correlation-id=\"42\">\n" +
                            "<result>\n" +
                              "<base>.1.3.6.1.2</base>\n" +
                              "<instance>1.3.6.1.2.1.4.34.1.3.1.2.3.4</instance>\n" +
                              "<value type=\"70\">Cg==</value>\n" +
                            "</result>\n" +
                        "</response>\n" +
                    "</snmp-response>"
                }
        });
    }

    private static SnmpMultiResponseDTO getSnmpMultiResponse() {
        final SnmpValueFactory snmpValueFactory = new Snmp4JValueFactory();
        final SnmpResult result = new SnmpResult(
                SnmpObjId.get(".1.3.6.1.2"),
                new SnmpInstId(".1.3.6.1.2.1.4.34.1.3.1.2.3.4"),
                snmpValueFactory.getCounter64(BigInteger.TEN));
        final SnmpResponseDTO responseDTO = new SnmpResponseDTO();
        responseDTO.setCorrelationId("42");
        responseDTO.getResults().add(result);

        final SnmpMultiResponseDTO multiResponseDTO = new SnmpMultiResponseDTO();
        multiResponseDTO.getResponses().add(responseDTO);
        return multiResponseDTO;
    }
}
