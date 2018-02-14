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

package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-icmp.xml",
        "classpath:/pinger.xml"
})
@JUnitConfigurationEnvironment
public class LocationAwarePingClientIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private LocationAwarePingClient locationAwarePingClient;

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(),
                new KeyValueHolder<>(new MinionIdentity() {
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
        services.put(Component.class.getName(), new KeyValueHolder<>(queuingservice, props));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-rpc-server.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        BeanUtils.assertAutowiring(this);
        Assert.assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());
    }

    /**
     * Verifies that Pings are successful when invoked from localhost (JVM mode)
     */
    @Test
    public void canPingViaLocalhost() throws InterruptedException, ExecutionException {
        CompletableFuture<PingSummary> future = locationAwarePingClient.ping(InetAddress.getLoopbackAddress()).execute();
        PingSummary pingSummary = future.get();
        verify(pingSummary, 1);
    }

    /**
     * Verifies that Pings are successful when executed on remote location (RPC mode)
     */
    @Test
    public void canPingViaRemoteLocation() throws InterruptedException, ExecutionException {
        final CompletableFuture<PingSummary> future = locationAwarePingClient.ping(
                InetAddress.getLoopbackAddress()).withLocation(REMOTE_LOCATION_NAME).execute();
        PingSummary pingSummary = future.get();
        verify(pingSummary, 1);
    }

    @Test
    public void canMultiPingViaRemoteLocation() throws ExecutionException, InterruptedException {
        final Integer[] invoked = new Integer[]{0};
        verify(locationAwarePingClient.ping(InetAddress.getLoopbackAddress())
                .withLocation(REMOTE_LOCATION_NAME)
                .withNumberOfRequests(10)
                .withProgressCallback((newSequence, summary) -> {
                    invoked[0]++;
                }).execute().get(), 10);
        // Since the default retries are 2 for every invocation
        Assert.assertEquals(Integer.valueOf(20), invoked[0]);
    }

    private void verify(PingSummary pingSummary, int numberSequences) {
        Assert.assertEquals(Boolean.TRUE, pingSummary.isSuccess());
        Assert.assertEquals(Boolean.TRUE, pingSummary.isComplete());
        Assert.assertEquals(numberSequences, pingSummary.getSequences().size());
        pingSummary.getSequences().forEach(eachSequence -> {
            Assert.assertTrue(eachSequence.getResponse().getRtt() != Double.POSITIVE_INFINITY);
        });
    }
}
