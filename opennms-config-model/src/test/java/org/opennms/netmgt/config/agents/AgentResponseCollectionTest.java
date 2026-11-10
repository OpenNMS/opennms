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
