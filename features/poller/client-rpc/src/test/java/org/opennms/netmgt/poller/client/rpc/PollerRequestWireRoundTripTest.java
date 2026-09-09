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
package org.opennms.netmgt.poller.client.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.InetAddress;

import org.junit.Test;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;

public class PollerRequestWireRoundTripTest {

    @Test
    public void wireRoundTripWithSnmpAgentConfig() throws Exception {
        final PollerRequestDTO original = PollerRequestDTOTest.getPollerRequestWithAgentConfig();
        final XmlHandler<PollerRequestDTO> handler = XmlHandler.forWire(PollerRequestDTO.class);

        final String xml = handler.marshal(original);
        assertFalse(xml.contains("\n  "));

        final PollerRequestDTO restored = handler.unmarshal(xml);
        assertEquals(original, restored);
    }

    @Test
    public void wireRoundTripWithPageSequence() throws Exception {
        final PageSequence pageSequence = new PageSequence();
        final Page page = new Page();
        page.setPath("/health");
        pageSequence.addPage(page);

        final PollerRequestDTO original = new PollerRequestDTO();
        original.setLocation("MINION");
        original.setClassName("org.opennms.netmgt.poller.monitors.HttpMonitor");
        original.setAddress(InetAddress.getByName("127.0.0.1"));
        original.addAttribute("page-sequence", pageSequence);

        final XmlHandler<PollerRequestDTO> handler = XmlHandler.forWire(PollerRequestDTO.class);
        final String xml = handler.marshal(original);
        assertFalse(xml.contains("\n  "));

        final PollerRequestDTO restored = handler.unmarshal(xml);
        assertEquals(original, restored);
    }

    @Test
    public void wireRoundTripWithNestedAttribute() throws Exception {
        final PollerRequestDTO original = PollerRequestDTOTest.getPollerRequestWithObject();
        final XmlHandler<PollerRequestDTO> handler = XmlHandler.forWire(PollerRequestDTO.class);

        final PollerRequestDTO restored = handler.unmarshal(handler.marshal(original));
        assertEquals(original, restored);
    }
}
