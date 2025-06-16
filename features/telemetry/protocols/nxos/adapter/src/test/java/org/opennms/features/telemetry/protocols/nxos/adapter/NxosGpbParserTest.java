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
package org.opennms.features.telemetry.protocols.nxos.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.NxosGpbParserUtil;
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis;

import com.google.common.io.Resources;
import com.google.protobuf.ExtensionRegistry;

public class NxosGpbParserTest {

    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();
    public TelemetryBis.Telemetry telemetryMsg;

    static {
        TelemetryBis.registerAllExtensions(s_registry);
    }

    @Before
    public void init() {
        TelemetryBis.TelemetryField loadavg1 = TelemetryBis.TelemetryField.newBuilder().setName("load_avg")
                .setUint64Value(23).build();
        TelemetryBis.TelemetryField loadavg2 = TelemetryBis.TelemetryField.newBuilder().setName("load_avg")
                .setUint64Value(18).build();
        TelemetryBis.TelemetryField field1 = TelemetryBis.TelemetryField.newBuilder().setName("field1")
                .addFields(loadavg1).build();
        TelemetryBis.TelemetryField field2 = TelemetryBis.TelemetryField.newBuilder().setName("field2")
                .addFields(loadavg2).build();
        TelemetryBis.TelemetryField field = TelemetryBis.TelemetryField.newBuilder().setName("field").addFields(field1)
                .addFields(field2).build();
        telemetryMsg = TelemetryBis.Telemetry.newBuilder().setNodeIdStr("nxos").addDataGpbkv(field)
                .setSubscriptionIdStr("18374686715878047745").setCollectionId(4).setCollectionStartTime(1510584351)
                .setCollectionEndTime(1510584402).setMsgTimestamp(new Date().getTime()).build();

    }

    @Test
    public void verifyWithOffset() throws IOException {

        byte[] nxosMsgBytes = Resources.toByteArray(Resources.getResource("nxos-proto-buf.raw"));
        ByteBuffer buf = ByteBuffer.wrap(nxosMsgBytes, 6, nxosMsgBytes.length - 6);
        TelemetryBis.Telemetry msg = TelemetryBis.Telemetry.parseFrom(buf, s_registry);
        assertNotNull(msg);
        assertNotNull(msg.getDataGpbkvList());

        // Test fields that are unique and not relative to any row/field
        assertEquals(NxosGpbParserUtil.getValueAsDouble(msg, "load_avg_1min"), 1.25d, 0.001d);
        assertEquals(NxosGpbParserUtil.getValueAsDouble(msg, "memory_usage_total"), 8061904, 0.001d);
        assertEquals(NxosGpbParserUtil.getValueAsString(msg, "current_memory_status"), "OK");

        // Test fields for a row ( array of structures)
        for (TelemetryBis.TelemetryField row : NxosGpbParserUtil.getRowsFromTable(msg, "cpu_usage")) {

            assertNotEquals(NxosGpbParserUtil.getValueFromRowAsDouble(row, "cpuid"), Double.NaN);
            assertNotEquals(NxosGpbParserUtil.getValueFromRowAsDouble(row, "kernel"), Double.NaN);
        }

        // Same metric but under different field structure.
        assertEquals(NxosGpbParserUtil.getValueAsDoubleRelativeToField(telemetryMsg, "field1", "load_avg"), 23d,
                0.001d);
        assertEquals(NxosGpbParserUtil.getValueAsDoubleRelativeToField(telemetryMsg, "field2", "load_avg"), 18d,
                0.001d);

        // Same metric but under different field structure, get as String
        assertEquals(NxosGpbParserUtil.getValueAsStringRelativeToField(telemetryMsg, "field1", "load_avg"), "23");
        assertEquals(NxosGpbParserUtil.getValueAsStringRelativeToField(telemetryMsg, "field2", "load_avg"), "18");
    }

}
