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
package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-jms.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-icmp.xml",
        "classpath:/pinger.xml" })
@JUnitConfigurationEnvironment
public class LocationAwarePingSweepClientIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private LocationAwarePingClient client;

    @SuppressWarnings("rawtypes")
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(MinionIdentity.class.getName(), new KeyValueHolder<>(new MinionIdentity() {
            @Override
            public String getId() {
                return "0";
            }

            @Override
            public String getLocation() {
                return REMOTE_LOCATION_NAME;
            }

            @Override
            public String getType() {
                return SystemType.Minion.name();
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
     * 
     * @throws UnknownHostException
     */
    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void canPingViaLocalhost() throws InterruptedException, ExecutionException, UnknownHostException {
        CompletableFuture<PingSweepSummary> future = client.sweep()
                .withRange(InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.10")).execute();
        PingSweepSummary pingSummary = future.get();
        Assert.assertEquals(10, pingSummary.numberOfPingsReturned());
    }

    /**
     * Verifies that Pings are successful when executed on remote location (RPC
     * mode)
     * 
     * @throws UnknownHostException
     */
    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void canPingViaRemoteLocation() throws InterruptedException, ExecutionException, UnknownHostException {
        final CompletableFuture<PingSweepSummary> future = client.sweep()
                .withRange(InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.10"))
                .withLocation(REMOTE_LOCATION_NAME).execute();
        PingSweepSummary pingSummary = future.get();
        Assert.assertEquals(10, pingSummary.numberOfPingsReturned());
    }

}
