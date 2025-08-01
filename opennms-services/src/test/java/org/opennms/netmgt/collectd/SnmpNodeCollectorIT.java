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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

@RunWith(Parameterized.class)
public class SnmpNodeCollectorIT extends SnmpCollectorITCase {

	public SnmpNodeCollectorIT(int config) {
		setVersion(config);
        m_allowWarnings = true; // Don't fail because of SocketException from SNMP4J
    }

	@Parameters
	public static Collection<Object[]> params() {
		Object[][] retval = new Object[][] {
			{ SnmpAgentConfig.VERSION1 },
			{ SnmpAgentConfig.VERSION2C },
			{ SnmpAgentConfig.VERSION3 }
		};
		return Arrays.asList(retval);
	}

    @Test
    public void testZeroVars() throws Exception {
        SnmpNodeCollector collector = createNodeCollector();
        assertMibObjectsPresent(collector.getCollectionSet().getNodeInfo(), getAttributeList());
    }

    @Test
    public void testInvalidVar() throws Exception {
        addAttribute("invalid", ".1.3.6.1.2.1.2", "0", "string");
        SnmpNodeCollector collector = createNodeCollector();
        assertTrue(collector.getEntry().isEmpty());
    }

    @Test
    public void testInvalidInst() throws Exception {
        addAttribute("invalid", ".1.3.6.1.2.1.1.3", "1", "timeTicks");
        SnmpNodeCollector collector = createNodeCollector();
        assertTrue(collector.getEntry().isEmpty());
    }

    @Test
    public void testOneVar() throws Exception {
        addSysName();
        SnmpNodeCollector collector = createNodeCollector();
        assertMibObjectsPresent(collector.getCollectionSet().getNodeInfo(), getAttributeList());
    }

    private SnmpNodeCollector createNodeCollector() throws Exception, InterruptedException {
        initializeAgent();

        SnmpNodeCollector collector = new SnmpNodeCollector(InetAddress.getLocalHost(), getCollectionSet().getAttributeList(), getCollectionSet());

        createWalker(collector);
        waitForSignal();
        assertNotNull("No entry data", collector.getEntry());
        assertFalse("Timeout collecting data", collector.timedOut());
        assertFalse("Collector failed to collect data", collector.failed());
        return collector;
    }

}
