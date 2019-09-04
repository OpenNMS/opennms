/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.client.rpc;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.poller.PollerParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

public class PollerRequestDTOTest extends XmlTestNoCastor<PollerRequestDTO> {

    public PollerRequestDTOTest(PollerRequestDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
            {
                getPollerRequestWithString(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-request location=\"MINION\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\" address=\"127.0.0.1\">\n" +
                "   <attribute key=\"port\" value=\"18980\"/>\n" +
                "</poller-request>"
            },
            {
                getPollerRequestWithAgentConfig(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-request location=\"MINION\" class-name=\"org.opennms.netmgt.poller.monitors.IcmpMonitor\" address=\"127.0.0.1\">\n" +
                "   <attribute key=\"agent\">\n" +
                "      <snmpAgentConfig>\n" +
                "         <authPassPhrase>0p3nNMSv3</authPassPhrase>\n" +
                "         <authProtocol>MD5</authProtocol>\n" +
                "         <maxRepetitions>2</maxRepetitions>\n" +
                "         <maxRequestSize>65535</maxRequestSize>\n" +
                "         <maxVarsPerPdu>10</maxVarsPerPdu>\n" +
                "         <port>161</port>\n" +
                "         <privPassPhrase>0p3nNMSv3</privPassPhrase>\n" +
                "         <privProtocol>DES</privProtocol>\n" +
                "         <readCommunity>public</readCommunity>\n" +
                "         <retries>0</retries>\n" +
                "         <securityLevel>1</securityLevel>\n" +
                "         <securityName>opennmsUser</securityName>\n" +
                "         <timeout>3000</timeout>\n" +
                "         <version>1</version>\n" +
                "         <versionAsString>v1</versionAsString>\n" +
                "         <writeCommunity>private</writeCommunity>\n" +
                "      </snmpAgentConfig>\n" +
                "   </attribute>\n" +
                "</poller-request>"
            }
        });
    }
    public static PollerRequestDTO getPollerRequestWithString() throws Exception {
        PollerRequestDTO dto = new PollerRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.poller.monitors.IcmpMonitor");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addAttribute("port", PollerParameter.simple("18980"));
        return dto;
    }

    public static PollerRequestDTO getPollerRequestWithAgentConfig() throws Exception {
        PollerRequestDTO dto = new PollerRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.poller.monitors.IcmpMonitor");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addAttribute("agent", PollerParameter.complex(createSnmpAgentConfig()));
        return dto;
    }

    private static Element createSnmpAgentConfig() throws Exception {
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final Element rootElement = document.createElementNS(null, "snmpAgentConfig");
        document.appendChild(rootElement);

        final Element authPassPhraseElement = document.createElementNS(null, "authPassPhrase");
        authPassPhraseElement.appendChild(document.createTextNode("0p3nNMSv3"));
        rootElement.appendChild(authPassPhraseElement);

        final Element authProtocolElement = document.createElementNS(null, "authProtocol");
        authProtocolElement.appendChild(document.createTextNode("MD5"));
        rootElement.appendChild(authProtocolElement);

        final Element maxRepetitionsElement = document.createElementNS(null, "maxRepetitions");
        maxRepetitionsElement.appendChild(document.createTextNode("2"));
        rootElement.appendChild(maxRepetitionsElement);

        final Element maxRequestSizeElement = document.createElementNS(null, "maxRequestSize");
        maxRequestSizeElement.appendChild(document.createTextNode("65535"));
        rootElement.appendChild(maxRequestSizeElement);

        final Element maxVarsPerPduElement = document.createElementNS(null, "maxVarsPerPdu");
        maxVarsPerPduElement.appendChild(document.createTextNode("10"));
        rootElement.appendChild(maxVarsPerPduElement);

        final Element portElement = document.createElementNS(null, "port");
        portElement.appendChild(document.createTextNode("161"));
        rootElement.appendChild(portElement);

        final Element privPassPhraseElement = document.createElementNS(null, "privPassPhrase");
        privPassPhraseElement.appendChild(document.createTextNode("0p3nNMSv3"));
        rootElement.appendChild(privPassPhraseElement);

        final Element privProtocolElement = document.createElementNS(null, "privProtocol");
        privProtocolElement.appendChild(document.createTextNode("DES"));
        rootElement.appendChild(privProtocolElement);

        final Element readCommunityElement = document.createElementNS(null, "readCommunity");
        readCommunityElement.appendChild(document.createTextNode("public"));
        rootElement.appendChild(readCommunityElement);

        final Element retriesElement = document.createElementNS(null, "retries");
        retriesElement.appendChild(document.createTextNode("0"));
        rootElement.appendChild(retriesElement);

        final Element securityLevelElement = document.createElementNS(null, "securityLevel");
        securityLevelElement.appendChild(document.createTextNode("1"));
        rootElement.appendChild(securityLevelElement);

        final Element securityNameElement = document.createElementNS(null, "securityName");
        securityNameElement.appendChild(document.createTextNode("opennmsUser"));
        rootElement.appendChild(securityNameElement);

        final Element timeoutElement = document.createElementNS(null, "timeout");
        timeoutElement.appendChild(document.createTextNode("3000"));
        rootElement.appendChild(timeoutElement);

        final Element versionElement = document.createElementNS(null, "version");
        versionElement.appendChild(document.createTextNode("1"));
        rootElement.appendChild(versionElement);

        final Element versionAsStringElement = document.createElementNS(null, "versionAsString");
        versionAsStringElement.appendChild(document.createTextNode("v1"));
        rootElement.appendChild(versionAsStringElement);

        final Element writeCommunityElement = document.createElementNS(null, "writeCommunity");
        writeCommunityElement.appendChild(document.createTextNode("private"));
        rootElement.appendChild(writeCommunityElement);

        rootElement.appendChild(document.createTextNode("\n" + Strings.repeat("   ", 2)));

        return document.getDocumentElement();
    }
}
