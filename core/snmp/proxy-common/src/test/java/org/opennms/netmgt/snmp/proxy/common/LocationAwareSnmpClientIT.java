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

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.testutils.ExpectedResults;
import org.opennms.netmgt.snmp.proxy.common.testutils.IPAddressGatheringTracker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
@TestExecutionListeners(JUnitSnmpAgentExecutionListener.class)
@JUnitSnmpAgent(host="192.0.2.205", resource="classpath:loadSnmpDataTest.properties")
public class LocationAwareSnmpClientIT {

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    private SnmpAgentConfig agentConfig;

    private LocationAwareSnmpClient locationAwareSnmpClient;

    @Before
    public void setUp() {
        SnmpPeerFactory.setInstance(snmpPeerFactory);
        agentConfig = snmpPeerFactory.getAgentConfig(InetAddressUtils.addr("192.0.2.205"));
        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);

        SnmpRequestExecutorLocalImpl snmpRequestExecutor = new SnmpRequestExecutorLocalImpl();
        DelegatingLocationAwareSnmpClientImpl locationAwareSnmpClient = new DelegatingLocationAwareSnmpClientImpl();
        locationAwareSnmpClient.setLocalSnmpRequestExecutor(snmpRequestExecutor);
        this.locationAwareSnmpClient = locationAwareSnmpClient;
    }

    /**
     * Verifies that SNMP WALKs are successful when directly using SnmpUtils.
     *
     * Used a basis for comparison.
     */
    @Test
    public void canWalkIpAddressTableDirectly() throws InterruptedException {
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, tracker.getDescription(), tracker);
        walker.start();
        walker.waitFor();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
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
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
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
}
