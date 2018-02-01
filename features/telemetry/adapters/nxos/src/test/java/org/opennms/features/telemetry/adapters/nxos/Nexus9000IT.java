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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis;
import com.google.protobuf.ExtensionRegistry;

/**
 * The Class Nexus9000IT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Nexus9000IT {

    /** The Constant s_registry. */
    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();

    static {
        TelemetryBis.registerAllExtensions(s_registry);
    }

    /**
     * Test JSON.
     *
     * @throws Exception the exception
     */
    @Test
    public void testJSON() throws Exception {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(new File("src/test/resources/json.bin")));
        JSONObject json = new JSONObject(new String(Arrays.copyOfRange(bytes, 6, bytes.length)));
        System.out.println(json);

        Assert.assertEquals("nexus9k", json.getString("node_id_str"));
        Assert.assertEquals("4.44", json.getJSONObject("data").getString("load_avg_1min"));
    }

    /**
     * Test GPB.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGPB() throws Exception {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(new File("src/test/resources/gpb.bin")));
        final TelemetryBis.Telemetry msg = TelemetryBis.Telemetry.parseFrom(Arrays.copyOfRange(bytes, 6, bytes.length), s_registry);
        System.out.println(msg);

        Assert.assertEquals("nexus9k", msg.getNodeIdStr());
        Assert.assertEquals("load_avg_1min", msg.getDataGpbkvList().get(0).getFields(1).getFieldsList().get(0).getFields(0).getName());
        Assert.assertEquals("1.25", msg.getDataGpbkvList().get(0).getFields(1).getFieldsList().get(0).getFields(0).getStringValue());
    }

}
