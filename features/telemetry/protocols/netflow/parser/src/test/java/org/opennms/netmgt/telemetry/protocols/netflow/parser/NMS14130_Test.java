/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

public class NMS14130_Test {

    public FlowMessage createMessage(final Integer in, final Integer out, final Integer ingress, final Integer egress) {
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

        final Netflow9MessageBuilder builder = new Netflow9MessageBuilder();
        return builder.buildMessage(record, enrichment).build();
    }

    @Test
    public void testIfIndex() {
        FlowMessage m;
        m = createMessage(1,2, null, null);
        Assert.assertEquals(m.getInputSnmpIfindex().getValue(), 1);
        Assert.assertEquals(m.getOutputSnmpIfindex().getValue(), 2);
        m = createMessage(1,2, 3, 4);
        Assert.assertEquals(m.getInputSnmpIfindex().getValue(), 1);
        Assert.assertEquals(m.getOutputSnmpIfindex().getValue(), 2);
        m = createMessage(null,2, 3, 4);
        Assert.assertEquals(m.getInputSnmpIfindex().getValue(), 3);
        Assert.assertEquals(m.getOutputSnmpIfindex().getValue(), 2);
        m = createMessage(1,null, 3, 4);
        Assert.assertEquals(m.getInputSnmpIfindex().getValue(), 1);
        Assert.assertEquals(m.getOutputSnmpIfindex().getValue(), 4);
        m = createMessage(null,null, 3, 4);
        Assert.assertEquals(m.getInputSnmpIfindex().getValue(), 3);
        Assert.assertEquals(m.getOutputSnmpIfindex().getValue(), 4);
    }
}
