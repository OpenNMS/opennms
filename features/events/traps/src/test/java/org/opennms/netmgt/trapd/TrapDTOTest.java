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

package org.opennms.netmgt.trapd;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.InetAddressUtils;

public class TrapDTOTest extends XmlTestNoCastor<TrapDTO> {

    private static final String UUID = "c5c306cd-0b4b-4d79-817a-f967eae7b403";

    /**
     * SNMP TimeTicks value.
     */
    private static final long TIMESTAMP = 5000;

    private static final long CREATION_TIME;

    static {
        long creationTime = -1;
        try {
            creationTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.US).parse("Jan 03, 2011 11:43:00 AM EST").getTime();
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        CREATION_TIME = creationTime;
    }

    public TrapDTOTest(TrapDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
            {
                getDetectorRequest(),
                "<trap-dto>" +
                  "<headers>" +
                     "<entry>" +
                       "<key>agentAddress</key>" +
                       "<value>127.0.0.1</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>community</key>" +
                       "<value>public</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>creationTime</key>" +
                       "<value>1294072980000</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>location</key>" +
                       "<value>Minion</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>pduLength</key>" +
                       "<value>5</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>sourceAddress</key>" +
                       "<value>192.0.2.123</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>systemId</key>" +
                       "<value>c5c306cd-0b4b-4d79-817a-f967eae7b403</value>" +
                     "</entry>" +
                     "<entry>" +
                       "<key>timestamp</key>" +
                       "<value>5000</value>" +
                     "</entry>" +
                  "</headers>" +
                  "<body>YmxhaGJsYWhibGFo</body>" + // blahblahblah
                  "<results/>" +
                "</trap-dto>"
            }
        });
    }

    public static TrapDTO getDetectorRequest() throws UnsupportedEncodingException {
        TrapDTO dto = new TrapDTO();
        dto.setAgentAddress(InetAddressUtils.ONE_TWENTY_SEVEN);
        dto.setCommunity("public");
        dto.setCreationTime(CREATION_TIME);
        dto.setBody("blahblahblah".getBytes("UTF-8"));
        //dto.setHeaders(newHeaders);
        dto.setLocation("Minion");
        dto.setPduLength(5);
        //dto.setResults(results);
        dto.setSourceAddress(InetAddressUtils.UNPINGABLE_ADDRESS);
        dto.setSystemId(UUID);
        dto.setTimestamp(TIMESTAMP);
        return dto;
    }
}
