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
package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.processing.TestFlow;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.flows.processing.impl.DocumentMangler;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class NodeIdentificationIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    private int nodeAId;
    private int nodeBId;

    @Before
    public void before() {
        this.databasePopulator.populateDatabase();

        nodeAId = this.databasePopulator.getNodeDao().save(buildNodeA());
        nodeBId = this.databasePopulator.getNodeDao().save(buildNodeB());

        this.interfaceToNodeCache.dataSourceSync();
    }

    @Test
    public void testSomething() throws InterruptedException {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> Collections.emptyList(), FilterService.NOOP);
        final DocumentEnricherImpl documentEnricher = new DocumentEnricherImpl(
                new MetricRegistry(),
                databasePopulator.getNodeDao(), databasePopulator.getIpInterfaceDao(),
                interfaceToNodeCache, sessionUtils, classificationEngine,
                new CacheConfigBuilder()
                        .withName("flows.node")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .build(), 0,
                new DocumentMangler(new ScriptEngineManager()));

        final TestFlow testFlow = new TestFlow();
        testFlow.setSrcAddr("1.1.1.1");
        testFlow.setSrcPort(1);
        testFlow.setDstAddr("2.2.2.2");
        testFlow.setDstPort(2);
        testFlow.setProtocol(6);
        testFlow.setDirection(Flow.Direction.INGRESS);

        List<EnrichedFlow> flowDocuments;

        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.99.99", null));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeAId));

        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.11.11", null));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeBId));

        testFlow.setNodeIdentifier("99099");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeAId));

        testFlow.setNodeIdentifier("11011");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo().getNodeId(), is(nodeBId));

        testFlow.setNodeIdentifier("22022");
        flowDocuments = documentEnricher.enrich(Lists.newArrayList(testFlow), new FlowSource(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, "192.168.22.22", new ContextKey("testContext","testKey")));
        Assert.assertThat(flowDocuments.get(0).getExporterNodeInfo(), nullValue());
    }

    private OnmsNode buildNodeA() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeA").setForeignSource("nodeIdTest:").setForeignId("A").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.99.99").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "99099"));
        return node;
    }

    private OnmsNode buildNodeB() {
        final NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("nodeB").setForeignSource("nodeIdTest:").setForeignId("B").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.11.11").setIsManaged("M").setIsSnmpPrimary("P");
        OnmsNode node = builder.getCurrentNode();
        node.getMetaData().add(new OnmsMetaData("testContext", "testKey", "11011"));
        return node;
    }
}
