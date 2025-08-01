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
package org.opennms.protocols.nsclient;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.snmp.InetAddrUtils;

public class NSClientAgentConfigTest extends XmlTestNoCastor<NSClientAgentConfig> { 

    public NSClientAgentConfigTest(NSClientAgentConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() {
        NSClientAgentConfig agentConfig = new NSClientAgentConfig();
        agentConfig.setAddress(InetAddrUtils.addr("127.0.0.1"));
        
        return Arrays.asList(new Object[][] {
            {
                agentConfig,
                "<nsclient-agent-config address=\"127.0.0.1\" timeout=\"3000\""
                + " retries=\"1\" port=\"1248\" password=\"None\"/>"
            }
        });
    }
}
