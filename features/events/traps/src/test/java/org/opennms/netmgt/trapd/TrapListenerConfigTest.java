/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapListenerConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.opennms.netmgt.trapd.TrapListenerTest.createUser;

public class TrapListenerConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testTrapListenerConfigPatching() throws IOException, JsonPatchException {
        TrapdConfiguration config = new TrapdConfiguration();
        config.setSnmpTrapPort(1162);
        config.setSnmpTrapAddress("localhost");
        Snmpv3User snmpv3User = createUser("MD5", "0p3nNMSv3", "some-engine-id",
                "DES", "0p3nNMSv3", "some-security-name");
        List<Snmpv3User> snmpv3UserList = new ArrayList<>();
        snmpv3UserList.add(snmpv3User);
        config.setSnmpv3User(snmpv3UserList);

        TrapListenerConfig trapListenerConfig = new TrapListenerConfig();
        TrapdConfigBean configBean = new TrapdConfigBean(config);
        trapListenerConfig.setSnmpV3Users(configBean.getSnmpV3Users());
        byte[] marshalledBytes = objectMapper.writeValueAsBytes(trapListenerConfig);
        TrapListenerConfig result = objectMapper.readValue(marshalledBytes, TrapListenerConfig.class);
        Assert.assertEquals(trapListenerConfig, result);
        JsonNode original = objectMapper.readTree(marshalledBytes);
        Snmpv3User user2 = createUser("MD5", "0p3nNMSv3", "engine-id-2",
                "DES", "0p3nNMSv3", "security-name-2");
        config.addSnmpv3User(user2);
        configBean = new TrapdConfigBean(config);
        trapListenerConfig.setSnmpV3Users(configBean.getSnmpV3Users());
        byte[] updatedBytes = objectMapper.writeValueAsBytes(trapListenerConfig);
        JsonNode updated = objectMapper.readTree(updatedBytes);
        JsonNode diff = JsonDiff.asJson(original, updated);
        byte[] diffBytes = diff.toString().getBytes(StandardCharsets.UTF_8);
        JsonNode resultingDiff = objectMapper.readTree(diffBytes);
        JsonPatch patch = JsonPatch.fromJson(resultingDiff);
        JsonNode resultNode = patch.apply(original);

        result = objectMapper.readValue(resultNode.toString().getBytes(StandardCharsets.UTF_8), TrapListenerConfig.class);
        Assert.assertThat(result.getSnmpV3Users(), Matchers.hasSize(2));
        Assert.assertEquals(result.getSnmpV3Users(), trapListenerConfig.getSnmpV3Users());

        // Check removal
        SnmpV3User user3 = new SnmpV3User("engineId", "horizon", "MD5",
                "OpenNMS", "DES", "OpenNMS");
        trapListenerConfig.setSnmpV3Users(new ArrayList<>());
        trapListenerConfig.getSnmpV3Users().add(user3);
        updatedBytes = objectMapper.writeValueAsBytes(trapListenerConfig);
        updated = objectMapper.readTree(updatedBytes);
        diff = JsonDiff.asJson(original, updated);
        diffBytes = diff.toString().getBytes(StandardCharsets.UTF_8);
        resultingDiff = objectMapper.readTree(diffBytes);
        patch = JsonPatch.fromJson(resultingDiff);
        resultNode = patch.apply(original);
        result = objectMapper.readValue(resultNode.toString().getBytes(StandardCharsets.UTF_8), TrapListenerConfig.class);
        Assert.assertThat(result.getSnmpV3Users(), Matchers.hasSize(1));
        Assert.assertEquals(result.getSnmpV3Users(), trapListenerConfig.getSnmpV3Users());
    }
}
