/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.telemetry.adapters.nxos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis;

import com.google.common.io.Resources;
import com.google.protobuf.ExtensionRegistry;

public class NxOsGpbParserTest {
    
    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();

    static {
        TelemetryBis.registerAllExtensions(s_registry);
    }


    @Test
    public void verifyWithOffset() throws IOException {

        String load_avg_str = null;

        byte[] nxosMsgBytes = Resources.toByteArray(Resources.getResource("nxos-proto-buf.raw"));
        ByteBuffer buf = ByteBuffer.wrap(nxosMsgBytes, 6, nxosMsgBytes.length - 6);
        TelemetryBis.Telemetry msg = TelemetryBis.Telemetry.parseFrom(buf, s_registry);

        assertNotNull(msg);

        if (!msg.getDataGpbkvList().isEmpty()) {
            if (!msg.getDataGpbkvList().get(0).getFieldsList().isEmpty()
                    && msg.getDataGpbkvList().get(0).getFieldsList().size() >= 2) {
                if (!msg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().isEmpty()) {
                    if (!msg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().get(0).getFieldsList()
                            .isEmpty()) {
                        if (msg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().get(0).getFieldsList()
                                .get(0).getName().equals("load_avg_1min")) {
                            load_avg_str = msg.getDataGpbkvList().get(0).getFieldsList().get(1).getFieldsList().get(0)
                                    .getFieldsList().get(0).getStringValue();

                        }
                    }
                }
            }
        }
        assertEquals("1.25", load_avg_str);
    }
    
}
