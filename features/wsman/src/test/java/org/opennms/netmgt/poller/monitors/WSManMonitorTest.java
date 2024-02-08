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
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.springframework.expression.spel.SpelParseException;
import org.w3c.dom.Node;

import com.google.common.collect.Maps;
import com.mycila.xmltool.XMLDoc;

public class WSManMonitorTest {
    private final static Node EXAMPLE_NODE = XMLDoc.newDocument(true).addRoot("body")
            .addTag("DCIM_ComputerSystem")
                .addTag("IdentifyingDescriptions")
                .setText("CIM:GUID")
                .addTag("IdentifyingDescriptions")
                // Place the ServiceTag in the middle, so we can be sure that it's not just
                // picking up the first, or last
                .setText("DCIM:ServiceTag")
                .addTag("IdentifyingDescriptions")
                .setText("CIM:Tag")
                .addTag("OtherIdentifyingInfo")
                .setText("44454C4C-3700-104A-8052-C3C04BB25031")
                .addTag("OtherIdentifyingInfo")
                .setText("C7BBBP1")
                .addTag("OtherIdentifyingInfo")
                .setText("mainsystemchassis")
            .gotoRoot().getChildElement().get(0);

    @Test
    public void canPoll() throws UnknownHostException {
        // Positive match
        assertEquals(PollStatus.up(), poll(
                "#IdentifyingDescriptions matches '.*ServiceTag' and #OtherIdentifyingInfo matches 'C7BBBP1'",
                EXAMPLE_NODE));
        // Positive match using substitution
        assertEquals(PollStatus.up(), poll(
                "#IdentifyingDescriptions matches '.*ServiceTag' and #OtherIdentifyingInfo matches '{nodeLabel}'",
                EXAMPLE_NODE));
        // Negative match
        assertEquals(PollStatus.down(), poll(
                "#IdentifyingDescriptions matches '.*ServiceTag' and #OtherIdentifyingInfo matches '!C7BBBP1'",
                EXAMPLE_NODE));
        // No results
        assertEquals(PollStatus.down(), poll("", null));
    }

    @Test(expected=SpelParseException.class)
    public void failsOnInvalidExpression() {
        poll("!#!# invalid expression !#!#", EXAMPLE_NODE);
    }

    private static PollStatus poll(String rule, Node response) {
        String resourceUri = "mock-resource-uri";
        Map<String, String> selectors = Maps.newHashMap();
        selectors.put("mock-selector-a", "a1");
        selectors.put("mock-selector-b", "b1");

        Definition agentConfig = new Definition();
        WSManConfigDao configDao = mock(WSManConfigDao.class);
        when(configDao.getAgentConfig(anyObject())).thenReturn(agentConfig);

        WSManClient client = mock(WSManClient.class);
        when(client.get(resourceUri, selectors)).thenReturn(response);
        WSManClientFactory clientFactory = mock(WSManClientFactory.class);
        when(clientFactory.getClient(anyObject())).thenReturn(client);

        WsManMonitor monitor = new WsManMonitor();
        monitor.setWSManConfigDao(configDao);
        monitor.setWSManClientFactory(clientFactory);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(WsManMonitor.RESOURCE_URI_PARAM, resourceUri);
        selectors.entrySet().stream().forEach(e -> parameters.put(WsManMonitor.SELECTOR_PARAM_PREFIX + e.getKey(), e.getValue()));
        parameters.put(WsManMonitor.RULE_PARAM, rule);

        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        MonitoredService svc = mock(MonitoredService.class);
        when(svc.getAddress()).thenReturn(localhost);
        when(svc.getIpAddr()).thenReturn("127.0.0.1");
        when(svc.getNodeLabel()).thenReturn("C7BBBP1");

        Map<String, Object> subbedParams = Interpolator.interpolateAttributes(monitor.getRuntimeAttributes(svc, parameters), EmptyScope.EMPTY);
        // this would normally happen in the poller request builder implementation
        subbedParams.forEach((k, v) -> {
            parameters.put(k, v);
        });

        return monitor.poll(svc, parameters);
    }
}
