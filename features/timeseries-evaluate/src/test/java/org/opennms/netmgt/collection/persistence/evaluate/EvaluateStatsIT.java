/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
        Assert.assertEquals(2, registry.getGauges().get("evaluate.resources").getValue());
        Assert.assertEquals(4, registry.getGauges().get("evaluate.groups").getValue());
        Assert.assertEquals(8, registry.getGauges().get("evaluate.numeric-attributes").getValue());
        Assert.assertEquals(2, registry.getGauges().get("evaluate.string-attributes").getValue());
        Assert.assertEquals(8, registry.getMeters().get("evaluate.samples").getCount());
    }

}