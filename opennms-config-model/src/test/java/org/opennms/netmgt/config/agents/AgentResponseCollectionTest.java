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

package org.opennms.netmgt.config.agents;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.network.IPAddress;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AgentResponseCollectionTest extends XmlTestNoCastor<AgentResponseCollection> {

    public AgentResponseCollectionTest(AgentResponseCollection sampleObject, String sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException, UnknownHostException {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("options", "value");

        final String ipAddr = "127.0.0.1";
        // Instantiate the InetAddress like the InetAddressXmlAdapter does
        final InetAddress inetAddr = new IPAddress(ipAddr).toInetAddress();

        final AgentResponse response = new AgentResponse(
                inetAddr,
                161,
                "SNMP",
                parameters
                );

        final AgentResponseCollection responses = new AgentResponseCollection(Arrays.asList(response));

        return Arrays.asList(new Object[][] { {
                responses,
                "<agents count=\"1\" totalCount=\"1\" offset=\"0\">" + 
                "<agent>" +
                "    <address>" + ipAddr + "</address>" +
                "    <port>161</port>" +
                "    <serviceName>SNMP</serviceName>" +
                "    <parameters>" +
                "       <entry>" +
                "          <key>options</key>" +
                "          <value>value</value>" +
                "       </entry>" +
                "    </parameters>" +
                "</agent>" +
                "</agents>",
                null, }, });
    }
}
