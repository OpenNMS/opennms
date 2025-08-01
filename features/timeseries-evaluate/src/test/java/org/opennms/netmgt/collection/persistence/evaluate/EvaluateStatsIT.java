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
package org.opennms.netmgt.collection.persistence.evaluate;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.LatencyTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.rrd.RrdRepository;

import com.codahale.metrics.MetricRegistry;

/**
 * The Class EvaluateStatsIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateStatsIT {

    /** The metric registry. */
    private MetricRegistry registry;

    /** The evaluation statistics. */
    private EvaluateStats stats;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
        registry = new MetricRegistry();
        stats = new EvaluateStats(registry, 5, 10);
    }

    /**
     * Test statistics.
     * 
     * @throws Exception the exception
     */
    @Test
    public void testStats() throws Exception {
        for (int i = 0; i < 10; i++) {
            stats.checkResource("resource" + i);
            for (int j = 0; j < 10; j++) {
                stats.checkGroup("resource" + i + "group" + j);
                for (int k = 0; k < 10; k++) {
                    stats.checkAttribute("resource" + i + "group" + j + "attribute" + k, true);
                    stats.markNumericSamplesMeter();
                }
            }
        }
        Assert.assertEquals(10, registry.getGauges().get("evaluate.resources").getValue());
        Assert.assertEquals(100, registry.getGauges().get("evaluate.groups").getValue());
        Assert.assertEquals(1000, registry.getGauges().get("evaluate.numeric-attributes").getValue());
        Assert.assertEquals(1000, registry.getMeters().get("evaluate.samples").getCount());
    }

    /**
     * Test persister.
     *
     * @throws Exception the exception
     */
    @Test
    public void testPersister() throws Exception {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File("/tmp"));
        EvaluateGroupPersister persister = new EvaluateGroupPersister(stats, new ServiceParameters(new HashMap<String,Object>()), repo);
        MockCollectionAgent agent = new MockCollectionAgent(1, "node.local", "Test", "001", InetAddressUtils.addr("127.0.0.1"));
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        NodeLevelResource node = new NodeLevelResource(agent.getNodeId());
        builder.withNumericAttribute(node, "mib2-host-resources-system", "hrSystemProcesses", 5.0, AttributeType.GAUGE);
        LatencyTypeResource icmp = new LatencyTypeResource("icmp", "10.0.0.10", "Default");
        builder.withNumericAttribute(icmp, "latency", "icmp", 50, AttributeType.GAUGE);
        InterfaceLevelResource eth0 = new InterfaceLevelResource(node, "eth0");
        builder.withNumericAttribute(eth0, "mib2-interfaces", "ifInErrors", 0.0, AttributeType.COUNTER);
        builder.withNumericAttribute(eth0, "mib2-interfaces", "ifOutErrors", 0.0, AttributeType.COUNTER);
        builder.withNumericAttribute(eth0, "mib2-X-interfaces", "ifHCInOctets", 100.0, AttributeType.COUNTER);
        builder.withNumericAttribute(eth0, "mib2-X-interfaces", "ifHCOutOctets", 100.0, AttributeType.COUNTER);
        builder.withStringAttribute(eth0, "mib2-X-interfaces", "ifHighSpeed", "1000");
        InterfaceLevelResource eth1 = new InterfaceLevelResource(node, "eth1");
        builder.withNumericAttribute(eth1, "mib2-interfaces", "ifInErrors", 0.0, AttributeType.COUNTER);
        builder.withNumericAttribute(eth1, "mib2-interfaces", "ifOutErrors", 0.0, AttributeType.COUNTER);
        builder.withNumericAttribute(eth1, "mib2-X-interfaces", "ifHCInOctets", 100.0, AttributeType.COUNTER);
        builder.withNumericAttribute(eth1, "mib2-X-interfaces", "ifHCOutOctets", 100.0, AttributeType.COUNTER);
        builder.withStringAttribute(eth1, "mib2-X-interfaces", "ifHighSpeed", "1000");
        builder.build().visit(persister);
        stats.dumpCache();

        Assert.assertEquals(1, registry.getGauges().get("evaluate.nodes").getValue());
        Assert.assertEquals(1, registry.getGauges().get("evaluate.interfaces").getValue());
        Assert.assertEquals(4, registry.getGauges().get("evaluate.resources").getValue());
        Assert.assertEquals(6, registry.getGauges().get("evaluate.groups").getValue());
        Assert.assertEquals(10, registry.getGauges().get("evaluate.numeric-attributes").getValue());
        Assert.assertEquals(2, registry.getGauges().get("evaluate.string-attributes").getValue());
        Assert.assertEquals(10, registry.getMeters().get("evaluate.samples").getCount());
    }

}
