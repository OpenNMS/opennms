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
package org.opennms.netmgt.dao.jaxb.callback;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.junit.Test;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;
import static org.opennms.netmgt.dao.jaxb.DefaultSnmpConfigDao.CONFIG_NAME;

public class SnmpConfigValidationCallbackTest {

    @Test(expected = ValidationException.class)
    public void testInvalidTopLevelProxyHost() {
        final SnmpConfig config = createConfig();
        config.setProxyHost("invalid");
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidDefinitionProxyHost() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        definition.setProxyHost("invalid");
        definition.addSpecific("192.168.0.1");
        config.addDefinition(definition);
        doTest(config);
    }

    @Test
    public void testValidTopLevelProxyHostIPv4() {
        final SnmpConfig config = createConfig();
        config.setProxyHost("192.168.0.1");
        doTest(config);
    }

    @Test
    public void testValidTopLevelProxyHostIPv6() {
        final SnmpConfig config = createConfig();
        config.setProxyHost("1234:abcd::1");
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidRangeBeginIpAddress() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        Range range = new Range();
        range.setBegin("invalid");
        range.setEnd("192.168.0.10");
        definition.addRange(range);
        config.addDefinition(definition);
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidRangeEndIpAddress() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        Range range = new Range();
        range.setBegin("192.168.0.1");
        range.setEnd("invalid");
        definition.addRange(range);
        config.addDefinition(definition);
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testMissingRangeBeginIpAddress() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        Range range = new Range();
        range.setEnd("192.168.0.10");
        definition.addRange(range);
        config.addDefinition(definition);
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testMissingRangeEndIpAddress() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        Range range = new Range();
        range.setBegin("192.168.0.1");
        definition.addRange(range);
        config.addDefinition(definition);
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidSpecificIpAddress() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        definition.addSpecific("invalid");
        config.addDefinition(definition);
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testMissingSpecificIpAddress() {
        final SnmpConfig config = createConfig();
        Definition definition = new Definition();
        definition.addSpecific(null);
        config.addDefinition(definition);
        doTest(config);
    }

    private static void doTest(final SnmpConfig snmpConfig) {
        SnmpConfigConfigurationValidationCallback callback = new SnmpConfigConfigurationValidationCallback();
        JSONObject json;
        if (snmpConfig == null) {
            json = null;
        } else {
            json = new JSONObject(ConfigConvertUtil.objectToJson(snmpConfig));
        }
        ConfigUpdateInfo info = new ConfigUpdateInfo(CONFIG_NAME, DEFAULT_CONFIG_ID, json);
        callback.accept(info);
    }

    private SnmpConfig createConfig() {
        final SnmpConfig snmpConfig = new SnmpConfig();
        snmpConfig.setProxyHost("192.168.0.1");
        final Definition def1 = new Definition();
        def1.setSpecifics(Lists.newArrayList("172.16.0.1", "1234:abcd::1"));
        snmpConfig.addDefinition(def1);
        final Definition def2 = new Definition();
        final Range range1 = new Range();
        range1.setBegin("172.16.0.1");
        range1.setEnd("172.16.0.10");
        def2.setProxyHost("192.168.0.1");
        def2.addRange(range1);
        final Range range2 = new Range();
        range2.setBegin("1234:abcd::1");
        range2.setEnd("1234:abcd::10");
        def2.setProxyHost("1234:abcd::11");
        def2.addRange(range2);
        snmpConfig.addDefinition(def2);
        return snmpConfig;
    }
}
