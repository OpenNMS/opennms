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
package org.opennms.core.ipc.sink.camel.itests;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.camel.itests.heartbeat.Heartbeat;
import org.opennms.core.ipc.sink.camel.itests.heartbeat.HeartbeatModule;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-server.xml",
        "classpath:/META-INF/opennms/applicationContext-ipc-sink-camel-client.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath:/META-INF/opennms/applicationContext-opennms-identity.xml"
})
@JUnitConfigurationEnvironment
@org.springframework.test.annotation.IfProfileValue(name="runFlappers", value="true")
public class SinkBlueprintMessageFailureIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker();

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private MessageConsumerManager consumerManager;
    
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
                    @Override
                    public String getType() {
                        return SystemType.Minion.name();
                    }
                }, new Properties()));

        Properties props = new Properties();
        props.setProperty("alias", "opennms.broker");
        services.put(Component.class.getName(), new KeyValueHolder<Object, Dictionary>(queuingservice, props));
    }

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-ipc-client.xml";
    }

    @Test(timeout=60000)
    public void doesntLogMessageBody() throws Exception {
        HeartbeatModule module = new HeartbeatModule();

        final CountDownLatch consumed = new CountDownLatch(1);
        // Create a consumer that just throws exceptions during the exchange
        MessageConsumer<Heartbeat, Heartbeat> consumer = new MessageConsumer<Heartbeat, Heartbeat>() {
            @Override
            public SinkModule<Heartbeat, Heartbeat> getModule() {
                return module;
            }

            @Override
            public void handleMessage(Heartbeat heartbeat) {
                consumed.countDown();
                throw new IllegalStateException();
            }
        };
        consumerManager.registerConsumer(consumer);

        Thread.sleep(500);

        // Fetch the remote dispatcher from the blueprint context
        MessageDispatcherFactory remoteMessageDispatcherFactory = context.getRegistry().lookupByNameAndType("camelRemoteMessageDispatcherFactory", MessageDispatcherFactory.class);
        assertNotNull(remoteMessageDispatcherFactory);
        SyncDispatcher<Heartbeat> dispatcher = remoteMessageDispatcherFactory.createSyncDispatcher(HeartbeatModule.INSTANCE);

        dispatcher.send(new Heartbeat());
        consumed.await();

        // Sleep slightly longer to allow the body to be logged on the sink consumer listener thread
        Thread.sleep(2000);

        // Verify that the exchange error was logged
        MockLogAppender.assertLogMatched(Level.ERROR, "Message History");
        MockLogAppender.assertLogMatched(Level.ERROR, "queuingservice://" + SystemInfoUtils.DEFAULT_INSTANCE_ID + ".Sink.Heartbeat");
        // Verify that the message body was suppressed
        MockLogAppender.assertNoLogMatched(Level.ERROR, "<heartbeat/>");

        consumerManager.unregisterConsumer(consumer);
    }
}
