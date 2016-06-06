/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
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

import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.activemq.camel.component.ActiveMQComponent;
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
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.proxy.common.DelegatingLocationAwareSnmpClientImpl;
import org.opennms.netmgt.snmp.proxy.common.testutils.ExpectedResults;
import org.opennms.netmgt.snmp.proxy.common.testutils.IPAddressGatheringTracker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * Used to test the Camel context defined in blueprint-snmp-proxy.xml.
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
public class LocationAwareSnmpClientBlueprintIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    private DelegatingLocationAwareSnmpClientImpl locationAwareSnmpClient;

    private SnmpAgentConfig agentConfig;

    /**
     * Register a mock OSGi {@link SchedulerService} so that we can make sure that the scheduler
     * whiteboard is working properly.
     */
    @SuppressWarnings( "rawtypes" )
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
        services.put(Component.class.getName(),
                new KeyValueHolder<Object, Dictionary>(ActiveMQComponent.activeMQComponent("vm://localhost?create=false"),
                                                       props));
    }

    // The location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

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
        final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, tracker.getDescription(), tracker);
        walker.start();
        walker.waitFor();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
    }

    /**
     * Verifies that the IP Address tables can be walked when using the current location.
     */
    @Test(timeout=60000)
    public void canWalkIpAddressTableViaCurrentLocation() throws UnknownHostException, InterruptedException, ExecutionException {
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .atLocation(identity.getLocation())
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
    }

    /**
     * Verifies that the IP Address tables can be walked when using a remote location.
     *
     * This should invoke the route in the Camel context initialize in this blueprint.
     */
    @Test(timeout=60000)
    public void canWalkIpAddressTableViaAnotherLocation() throws Exception {
        final IPAddressGatheringTracker tracker = new IPAddressGatheringTracker();
        locationAwareSnmpClient.walk(agentConfig, tracker)
            .withDescription(tracker.getDescription())
            .atLocation(REMOTE_LOCATION_NAME)
            .execute().get();
        ExpectedResults.compareToKnownIpAddressList(tracker.getIpAddresses());
    }
}
