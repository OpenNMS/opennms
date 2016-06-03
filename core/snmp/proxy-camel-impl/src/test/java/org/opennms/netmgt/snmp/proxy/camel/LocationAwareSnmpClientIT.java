/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy.camel;

import static org.junit.Assert.assertNotEquals;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.proxy.common.DelegatingLocationAwareSnmpClientImpl;
import org.opennms.netmgt.snmp.proxy.common.SnmpRequestExecutor;
import org.opennms.netmgt.snmp.proxy.common.testutils.ExpectedResults;
import org.opennms.netmgt.snmp.proxy.common.testutils.IPAddressGatheringTracker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * Used to test the Camel context defined in applicationContext-snmp-client-proxy.xml.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice.xml",
        "classpath:/META-INF/opennms/applicationContext-snmp-client-proxy.xml"
})
@JUnitConfigurationEnvironment
@TestExecutionListeners(JUnitSnmpAgentExecutionListener.class)
@JUnitSnmpAgent(host="192.0.2.205", resource="classpath:loadSnmpDataTest.properties")
public class LocationAwareSnmpClientIT {

    private static final String REMOTE_LOCATION_NAME = "remote";

    private static BrokerService s_broker = null;

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private DelegatingLocationAwareSnmpClientImpl locationAwareSnmpClient;

    @Autowired
    @Qualifier("localSnmpRequestExecutor")
    private SnmpRequestExecutor localSnmpRequestExecutor;

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    private SnmpAgentConfig agentConfig;

    @BeforeClass
    public static void setUpClass() throws Exception {
        s_broker = new BrokerService();
        s_broker.start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (s_broker != null) {
            s_broker.stop();
        }
    }

    @Before
    public void setUp() {
        SnmpPeerFactory.setInstance(snmpPeerFactory);
        agentConfig = snmpPeerFactory.getAgentConfig(InetAddressUtils.addr("192.0.2.205"));
        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
    }

    /**
     * Verifies that the IP Address tables can be directly walked. Used a basis for comparison.
     */
    @Test
    public void canWalkIpAddressTableDirectly() throws InterruptedException {
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "IP address tables", tracker);
        walker.start();
        walker.waitFor();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
    }

    /**
     * Verifies that the IP Address tables can be walked when using the current location.
     */
    @Test
    public void canWalkIpAddressTableViaCurrentLocation() throws UnknownHostException, InterruptedException, ExecutionException {
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .atLocation(identity.getLocation())
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
    }

    /**
     * Verifies that the IP Address tables can be walked when using the current location.
     */
    @Test
    public void canWalkIpAddressTableViaAnotherLocation() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());

        final AsyncSnmpRequestProcesor snmpRequestExecutorCamelAsync = new AsyncSnmpRequestProcesor();
        snmpRequestExecutorCamelAsync.setSnmpRequestExecutor(localSnmpRequestExecutor);

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext mockDiscoverer = new DefaultCamelContext(registry);
        mockDiscoverer.addComponent("queuingservice", queuingservice);
        mockDiscoverer.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("queuingservice:snmp-proxy@" + REMOTE_LOCATION_NAME)
                .setExchangePattern(ExchangePattern.InOut)
                .process(snmpRequestExecutorCamelAsync);
            };
        });
        mockDiscoverer.start();

        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .atLocation(REMOTE_LOCATION_NAME)
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());

        mockDiscoverer.stop();
    }
}
