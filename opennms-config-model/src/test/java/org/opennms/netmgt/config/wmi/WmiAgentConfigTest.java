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
package org.opennms.netmgt.config.wmi;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.InetAddrUtils;

public class WmiAgentConfigTest extends XmlTestNoCastor<WmiAgentConfig> {

    public WmiAgentConfigTest(WmiAgentConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() {
        WmiAgentConfig agentConfig = new WmiAgentConfig();
        agentConfig.setAddress(InetAddrUtils.addr("127.0.0.1"));

        return Arrays.asList(new Object[][] {
            {
                agentConfig,
                "<wmi-agent-config address=\"127.0.0.1\" timeout=\"3000\" retries=\"1\"/>"
            }
        });
    }

    @Test
    public void canConvertToAndFromMap() {
        WmiAgentConfig expectedAgentConfig = new WmiAgentConfig();
        expectedAgentConfig.setAddress(InetAddressUtils.ONE_TWENTY_SEVEN);
        expectedAgentConfig.setUsername("who");
        expectedAgentConfig.setPassword("dat");
        expectedAgentConfig.setDomain("FOO");
        expectedAgentConfig.setRetries(99);
        expectedAgentConfig.setTimeout(100);
        assertEquals(expectedAgentConfig, WmiAgentConfig.fromMap(expectedAgentConfig.toMap()));
    }
}
