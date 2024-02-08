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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.IpFixMessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

public class NMS14130_Test {

    private interface FlowMessageFactory {
        FlowMessage create(final Integer in, final Integer out, final Integer ingress, final Integer egress);
    }

    public void testIfIndex(final FlowMessageFactory flowMessageFactory) {
        FlowMessage m;
        m = flowMessageFactory.create(1,2, null, null);
        assertEquals(m.getInputSnmpIfindex().getValue(), 1);
        assertEquals(m.getOutputSnmpIfindex().getValue(), 2);
        m = flowMessageFactory.create(1,2, 3, 4);
        assertEquals(m.getInputSnmpIfindex().getValue(), 3);
        assertEquals(m.getOutputSnmpIfindex().getValue(), 4);
        m = flowMessageFactory.create(null,2, 3, 4);
        assertEquals(m.getInputSnmpIfindex().getValue(), 3);
        assertEquals(m.getOutputSnmpIfindex().getValue(), 4);
        m = flowMessageFactory.create(1,null, 3, 4);
        assertEquals(m.getInputSnmpIfindex().getValue(), 3);
        assertEquals(m.getOutputSnmpIfindex().getValue(), 4);
        m = flowMessageFactory.create(null,null, 3, 4);
        assertEquals(m.getInputSnmpIfindex().getValue(), 3);
        assertEquals(m.getOutputSnmpIfindex().getValue(), 4);
    }

    @Test
    public void testNetflow9() {
        testIfIndex((in, out, ingress, egress) -> {
            final RecordEnrichment enrichment = (address -> Optional.empty());
            final List<Value<?>> record = new ArrayList<>();
            record.add(new UnsignedValue("@unixSecs", 1000));
            record.add(new UnsignedValue("@sysUpTime", 1000));

            record.add(new UnsignedValue("FIRST_SWITCHED", 2000));
            record.add(new UnsignedValue("LAST_SWITCHED", 3000));
            if (in != null) {
                record.add(new UnsignedValue("INPUT_SNMP", in));
            }
            if (out != null) {
                record.add(new UnsignedValue("OUTPUT_SNMP", out));
            }

            if (ingress != null) {
                record.add(new UnsignedValue("ingressPhysicalInterface", ingress));
            }

            if (egress != null) {
                record.add(new UnsignedValue("egressPhysicalInterface", egress));
            }

            return new Netflow9MessageBuilder().buildMessage(record, enrichment).build();
        });
    }

    @Test
    public void testIPFix() {
        testIfIndex((in, out, ingress, egress) -> {
            final RecordEnrichment enrichment = (address -> Optional.empty());
            final List<Value<?>> record = new ArrayList<>();
            record.add(new UnsignedValue("@unixSecs", 1000));
            record.add(new UnsignedValue("@sysUpTime", 1000));

            record.add(new UnsignedValue("flowStartSeconds", 2000));
            record.add(new UnsignedValue("flowEndSeconds", 3000));
            if (in != null) {
                record.add(new UnsignedValue("ingressInterface", in));
            }
            if (out != null) {
                record.add(new UnsignedValue("egressInterface", out));
            }

            if (ingress != null) {
                record.add(new UnsignedValue("ingressPhysicalInterface", ingress));
            }

            if (egress != null) {
                record.add(new UnsignedValue("egressPhysicalInterface", egress));
            }

            return new IpFixMessageBuilder().buildMessage(record, enrichment).build();
        });
    }
}
