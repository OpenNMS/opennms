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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfo;
import org.opennms.netmgt.flows.processing.impl.InterfaceMarkerImpl;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class MarkerCacheIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    @Autowired
    private InterfaceToNodeCache interfaceToNodeCache;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();
        this.interfaceToNodeCache.dataSourceSync();
    }

    private EnrichedFlow getMockFlow(final Direction direction) {
        final EnrichedFlow flow = new EnrichedFlow();
        flow.setNetflowVersion(NetflowVersion.V5);
        flow.setDirection(direction);
        flow.setIpProtocolVersion(4);
        flow.setSrcAddr("192.168.1.2");
        flow.setInputSnmp(2);
        flow.setDstAddr("192.168.2.2");
        flow.setOutputSnmp(3);
        flow.setVlan(null);
        flow.setSrcAddrHostname(null);
        flow.setDstAddrHostname(null);
        flow.setNextHopHostname(null);
        flow.setExporterNodeInfo(new NodeInfo() {{
            this.setNodeId(MarkerCacheIT.this.databasePopulator.getNode1().getId());
        }});
        return flow;
    }

    @Test
    public void testMarkerCache() throws Exception {
        final var markerCache = new InterfaceMarkerImpl(this.sessionUtils,
                                                        this.nodeDao,
                                                        this.snmpInterfaceDao);

        Assert.assertThat(nodeDao.findAllHavingFlows(), is(empty()));
        Assert.assertThat(snmpInterfaceDao.findAllHavingFlows(1), is(empty()));

        markerCache.mark(Lists.newArrayList(getMockFlow(Direction.INGRESS)));

        Assert.assertThat(nodeDao.findAllHavingFlows(), contains(hasProperty("id", is(1))));
        Assert.assertThat(snmpInterfaceDao.findAllHavingFlows(1), contains(
                hasProperty("ifIndex", is(2))));
    }

    @Test
    public void testNMS12740() throws Exception {
        Assert.assertFalse(OnmsSnmpInterface.INGRESS_AND_EGRESS_REQUIRED);

        final var markerCache = new InterfaceMarkerImpl(this.sessionUtils,
                                                        this.nodeDao,
                                                        this.snmpInterfaceDao);

        Assert.assertThat(nodeDao.findAllHavingFlows(), is(empty()));
        Assert.assertThat(snmpInterfaceDao.findAllHavingFlows(1), is(empty()));

        markerCache.mark(Lists.newArrayList(getMockFlow(Direction.EGRESS)));

        assertEquals(0, snmpInterfaceDao.findAllHavingIngressFlows(2).size());
        assertEquals(0, snmpInterfaceDao.findAllHavingEgressFlows(2).size());

        // the following call resulted to two wrong entries before, since the wrong query returned entries from other nodes with egress flows
        assertEquals(0, snmpInterfaceDao.findAllHavingFlows(2).size());
}

    @Test
    public void shouldDistinguishBetweenIngressAndEgressWhenDeterminingIfFlowsAreAvailable() throws Exception {
        Assert.assertFalse(OnmsSnmpInterface.INGRESS_AND_EGRESS_REQUIRED);

        final var markerCache = new InterfaceMarkerImpl(this.sessionUtils,
                                                        this.nodeDao,
                                                        this.snmpInterfaceDao);

        final int ingress = 2;
        final int egress = 3;

        // no flows persisted -> we shouldn't have any interfaces
        expectAllInterfaces();
        expectIngressInterfaces();
        expectEgressInterfaces();

        // persist ingress flow
        markerCache.mark(Lists.newArrayList(getMockFlow(Direction.INGRESS)));
        expectAllInterfaces(ingress);
        expectIngressInterfaces(ingress);
        expectEgressInterfaces();

        // persist egress flow
        markerCache.mark(Lists.newArrayList(getMockFlow(Direction.EGRESS)));
        expectAllInterfaces(ingress, egress);
        expectEgressInterfaces(egress);
        expectIngressInterfaces(ingress);

        // verify the total number of interfaces marked with flows
        assertThat(snmpInterfaceDao.getNumInterfacesWithFlows(), equalTo(2L));
    }

    private void expectAllInterfaces(final Integer... expectedInterfaces) {
        expectInterfaces((n) -> snmpInterfaceDao.findAllHavingFlows(n), expectedInterfaces);
    }

    private void expectEgressInterfaces(final Integer... expectedInterfaces) {
        expectInterfaces((n) -> snmpInterfaceDao.findAllHavingEgressFlows(n), expectedInterfaces);
    }

    private void expectIngressInterfaces(final Integer... expectedInterfaces) {
        expectInterfaces((n) -> snmpInterfaceDao.findAllHavingIngressFlows(n), expectedInterfaces);
    }

    private void expectInterfaces(final Function<Integer, List<OnmsSnmpInterface>> flowFinder, final Integer... expectedInterfaces) {
        final Integer nodeId = 1;
        sessionUtils.withTransaction(() -> {
            List<Integer> interfaces = flowFinder.apply(nodeId).stream()
                    .map(OnmsSnmpInterface::getIfIndex)
                    .sorted()
                    .collect(Collectors.toList());
            assertEquals(Arrays.stream(expectedInterfaces).sorted().collect(Collectors.toList()), interfaces);
            return null;
        });
    }
}
