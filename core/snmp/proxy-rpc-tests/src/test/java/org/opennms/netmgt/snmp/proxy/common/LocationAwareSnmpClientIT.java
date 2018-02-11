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

package org.opennms.netmgt.snmp.proxy.common;

import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.provision.service.snmp.IpAddrTable;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.testutils.ExpectedResults;
import org.opennms.netmgt.snmp.proxy.common.testutils.IPAddressGatheringTracker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-snmp.xml"
})
@JUnitConfigurationEnvironment
@TestExecutionListeners(JUnitSnmpAgentExecutionListener.class)
@JUnitSnmpAgent(host="192.0.2.205", resource="classpath:/loadSnmpDataTest.properties")
public class LocationAwareSnmpClientIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    private SnmpAgentConfig agentConfig;

    @Autowired
    private LocationAwareSnmpClient locationAwareSnmpClient;

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MinionIdentity() {
                    @Override
                    public String getId() {
                        return "0";
                    }
                    @Override
                    public String getLocation() {
                        return REMOTE_LOCATION_NAME;
                    }
                }, new Properties()));

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(queuingservice, props));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:OSGI-INF/blueprint/blueprint-rpc-server.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        SnmpPeerFactory.setInstance(snmpPeerFactory);
        agentConfig = snmpPeerFactory.getAgentConfig(InetAddressUtils.addr("192.0.2.205"));
        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);
    }

    /**
     * Verifies that SNMP WALKs are successful when directly using SnmpUtils.
     *
     * Used a basis for comparison.
     */
    @Test
    public void canWalkIpAddressTableDirectly() throws InterruptedException {
        // Gather the list of IP addresses
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        try(SnmpWalker walker = SnmpUtils.createWalker(agentConfig, tracker.getDescription(), tracker)) {
            walker.start();
            walker.waitFor();
        }
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());

        // Now determine their interface indices using a different type of tracker
        final Set<SnmpInstId> ipAddrs = new TreeSet<>();
        for(final String ipAddr : tracker.getIpAddresses()) {
            ipAddrs.add(new SnmpInstId(InetAddressUtils.toOid(InetAddressUtils.addr(ipAddr))));
        }
        IpAddrTable ipAddrTable = new IpAddrTable(agentConfig.getAddress(), ipAddrs);
        try(SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ipAddrTable", ipAddrTable)) {
            walker.start();
            walker.waitFor();
        }
        ExpectedResults.compareToKnownIfIndices(ipAddrTable.getIfIndices());
    }

    /**
     * Verifies that SNMP GETs are successful when directly using SnmpUtils.
     *
     * Used a basis for comparison.
     */
    @Test
    public void canGetIpAddressTableEntriesDirectly() throws InterruptedException {
        assertEquals(1, SnmpUtils.get(agentConfig, SnmpObjId.get(".1.3.6.1.2.1.4.34.1.3.1.4.127.0.0.1")).toInt());
        assertEquals(7, SnmpUtils.get(agentConfig, SnmpObjId.get(".1.3.6.1.2.1.4.34.1.3.1.4.172.17.0.1")).toInt());
        assertEquals(SnmpObjId.get(".1.3.6.1.2.1.4.32.1.5.1.1.4.127.0.0.0.8"),
                SnmpUtils.get(agentConfig, SnmpObjId.get(".1.3.6.1.2.1.4.34.1.5.1.4.127.0.0.1")).toSnmpObjId());
    }

    /**
     * Verifies that SNMP WALKs are successful, and return the same results when using
     * the LocationAwareSnmpClient.
     */
    @Test
    public void canWalkIpAddressTableViaCurrentLocation() throws UnknownHostException, InterruptedException, ExecutionException {
        // Gather the list of IP addresses
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());

        // Now determine their interface indices using a different type of tracker
        final Set<SnmpInstId> ipAddrs = new TreeSet<>();
        for(final String ipAddr : tracker.getIpAddresses()) {
            ipAddrs.add(new SnmpInstId(InetAddressUtils.toOid(InetAddressUtils.addr(ipAddr))));
        }
        IpAddrTable ipAddrTable = new IpAddrTable(agentConfig.getAddress(), ipAddrs);
        locationAwareSnmpClient.walk(agentConfig, ipAddrTable)
            .withDescription(tracker.getDescription())
            .execute().get();
        ExpectedResults.compareToKnownIfIndices(ipAddrTable.getIfIndices());
    }

    /**
     * Verifies that SNMP GETs are successful, and return the same results when using
     * the LocationAwareSnmpClient.
     */
    @Test
    public void canGetIpAddressTableEntriesViaCurrentLocation() throws UnknownHostException, InterruptedException, ExecutionException {
        SnmpValue result = locationAwareSnmpClient.get(agentConfig,
                SnmpObjId.get(".1.3.6.1.2.1.4.34.1.3.1.4.127.0.0.1")).execute().get();
        assertEquals(1, result.toInt());

        result = locationAwareSnmpClient.get(agentConfig,
                SnmpObjId.get(".1.3.6.1.2.1.4.34.1.3.1.4.172.17.0.1")).execute().get();
        assertEquals(7, result.toInt());

        result = locationAwareSnmpClient.get(agentConfig,
                SnmpObjId.get(".1.3.6.1.2.1.4.34.1.5.1.4.127.0.0.1")).execute().get();
        assertEquals(SnmpObjId.get(".1.3.6.1.2.1.4.32.1.5.1.1.4.127.0.0.0.8"), result.toSnmpObjId());
    }

    /**
     * Verifies that the IP Address tables can be walked when using a remote location.
     *
     * This should invoke the route in the Camel context initialize in this blueprint.
     */
    @Test(timeout=60000)
    public void canWalkIpAddressTableViaAnotherLocation() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());

        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .withLocation(REMOTE_LOCATION_NAME)
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
    }
}
