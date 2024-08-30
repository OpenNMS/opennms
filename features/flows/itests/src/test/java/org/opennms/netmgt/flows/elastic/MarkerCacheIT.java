/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
