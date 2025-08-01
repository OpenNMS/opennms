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
package org.opennms.netmgt.snmp;

import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;

public class SnmpAgentConfigTest {

    @Test
    public void testEqualsAndHashCode() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        SnmpAgentConfig config2 = new SnmpAgentConfig();

        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        fillAll(config);
        Assert.assertFalse(config.equals(config2));
        Assert.assertFalse(config.hashCode() == config2.hashCode());

        fillAll(config2);
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());
    }

    /**
     * In #NMS-6860 we had the issue that the {@link SnmpAgentConfig} NRTG
     * uses was not build correctly. The problem was that {@link SnmpAgentConfig#toProtocolConfigString()} did create
     * a wrong formatted string. Therefore {@link SnmpAgentConfig#parseProtocolConfigurationString(String)} created
     * an agent config with wrong values from the wrong formatted string.<br/><br/>
     *
     * This test ensures if a serialized config string is parsed both objects (the original one and the parsed one)
     * are at least equal.
     */
    @Test
    public void testProtocolConfiguration() throws Exception {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getByName("127.0.0.1"));
        config.setProxyFor(InetAddress.getByName("127.0.0.1"));
        SnmpAgentConfig  config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        config.setVersion(3);
        config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        config.setAuthPassPhrase(null);
        config.setPrivPassPhrase(null);
        config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        // config2 will have the default PrivPassphrase and such, so these will *not* actually equal each other
        Assert.assertFalse(config.equals(config2));
        Assert.assertFalse(config.hashCode() == config2.hashCode());
        config2.setAuthPassPhrase(null);
        config2.setPrivPassPhrase(null);
        // now they should match
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        fillAll(config);
        // toProtocolConfigurationString does not print all set value,
        // it is version dependent, therefore we have to manually
        // reset read and write community strings
        config.setReadCommunity(null);
        config.setWriteCommunity(null);
        config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        config2.setReadCommunity(null);
        config2.setWriteCommunity(null);
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());
    }

    private void fillAll(SnmpAgentConfig config) {
        config.setTimeout(12);
        config.setAuthPassPhrase("some random pass phrase");
        config.setAuthProtocol("some random protocol");
        config.setContextEngineId("some context engine id");
        config.setContextName("some context name");
        config.setEngineId("some engine id");
        config.setEnterpriseId("some enterprise id");
        config.setMaxRepetitions(34);
        config.setMaxRequestSize(56);
        config.setMaxVarsPerPdu(78);
        config.setPort(99);
        config.setPrivPassPhrase("some random private pass phrase");
        config.setPrivProtocol("some random private protocol");
        config.setReadCommunity("read community string");
        config.setWriteCommunity("write community string");
        config.setRetries(17);
        config.setSecurityLevel(3);
        config.setSecurityName("dummy");
        config.setVersion(3);
    }

    @Test
    public void canConvertToAndFromProtocolConfigString() throws Exception {
        SnmpAgentConfig config = new SnmpAgentConfig();
        String protocolConfigString = "{\"snmp\":{\"address\":null,\"proxyFor\":null,\"port\":\"161\",\"timeout\":\"3000\",\"retries\":\"0\",\"max-vars-per-pdu\":\"10\",\"max-repetitions\":\"2\",\"max-request-size\":\"65535\",\"version\":\"1\",\"security-level\":\"1\",\"security-name\":\"opennmsUser\",\"auth-passphrase\":\"0p3nNMSv3\",\"auth-protocol\":\"MD5\",\"priv-passphrase\":\"0p3nNMSv3\",\"priv-protocol\":\"DES\",\"context-name\":null,\"engine-id\":null,\"context-engine-id\":null,\"enterprise-id\":null,\"read-community\":\"public\",\"write-community\":\"private\"}}";
        Assert.assertEquals(protocolConfigString, config.toProtocolConfigString());
        Assert.assertEquals(config, SnmpAgentConfig.parseProtocolConfigurationString(protocolConfigString));
    }

    @Test
    public void canConvertToAndFromMap() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        Assert.assertEquals(config, SnmpAgentConfig.fromMap(config.toMap()));
    }
}
