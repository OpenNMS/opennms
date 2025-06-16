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
package org.opennms.netmgt.trapd;

import static org.opennms.netmgt.trapd.TrapListenerTest.createUser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapListenerConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.diff.JsonDiff;

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
                "OpenNMS", "DES", "OpenNMS", null);
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
