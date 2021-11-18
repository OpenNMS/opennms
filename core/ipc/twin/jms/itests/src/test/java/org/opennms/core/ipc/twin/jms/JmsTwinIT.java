/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.jms;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.util.KeyValueHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.jms.publisher.JmsTwinPublisher;
import org.opennms.core.ipc.twin.test.AbstractTwinBrokerIT;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.test.camel.CamelBlueprintTest;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapListenerConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-twin-jms-publisher.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DirtiesContextTestExecutionListener.class})
public class JmsTwinIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private TwinPublisher publisher;

    @Autowired
    @Qualifier("twinSinkClient")
    private CamelContext sinkCamelContext;

    private TwinSubscriber subscriber;

    @Autowired
    @Qualifier("queuingservice")
    private ActiveMQComponent queuingservice;

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
        // Blueprint gets loaded by default from the dependencies.
        // Specifying blueprint again is leading multiple Twin subscribers.
        return "classpath:/OSGI-INF/blueprint/blueprint-empty.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        subscriber = context.getRegistry().lookupByNameAndType("jmsTwinSubscriber", TwinSubscriber.class);
    }

    /**
     * Tests that an object can be published and the same object can be received by the subscriber.
     */
    @Test
    public void testPublishSubscribe() throws Exception {
        final var session = publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        await().until(tracker::getLog, hasItems("Test1"));
    }


    /**
     * Tests that a publisher can update an object and the subscriber will receives all versions.
     */
    @Test
    public void testUpdates() throws Exception {
        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        await().until(tracker::getLog, hasItems("Test1"));
        session.publish("Test2");
        session.publish("Test3");
        await().until(tracker::getLog, hasItems("Test1", "Test2", "Test3"));
    }

    /**
     * Tests that subscription can be closed before registration.
     */
    @Test
    public void testSubscriberCloseBeforeRegister() throws Exception {

        final var tracker1 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);
        tracker1.close();

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker2 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);
        await().until(tracker2::getLog, hasItems("Test1"));

        assertThat(tracker1.getLog(), empty());
    }

    /**
     * Tests that a subscriber can register before a publisher exists.
     */
    @Test
    public void testSubscribeBeforePublish() throws Exception {
        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");
        session.publish("Test2");

        await().until(tracker::getLog, hasItems("Test1", "Test2"));
    }

    /**
     * Tests that subscription can be closed and reopened.
     */
    @Test
    public void testSubscriberClose() throws Exception {
        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker1 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);
        await().until(tracker1::getLog, contains("Test1"));

        tracker1.close();

        session.publish("Test2");
        session.publish("Test3");

        final var tracker2 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);
        await().until(tracker2::getLog, hasItems("Test3"));

        assertThat(tracker1.getLog(), hasItems("Test1"));
    }

    /**
     * Tests that subscription works if publisher gets restarted.
     */
    @Test
    public void testPublisherRestart() throws Exception {
        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");
        session.publish("Test2");

        await().until(tracker::getLog, hasItems("Test1", "Test2"));

        ((JmsTwinPublisher) publisher).close();
        ((JmsTwinPublisher) publisher).init();
        session.publish("Test3");

        await().until(tracker::getLog, hasItems("Test1", "Test2", "Test3"));
    }

    /**
     * Tests that multiple subscriptions exists for the same key.
     */
    @Test
    public void testMultipleSubscription() throws Exception {
        final var tracker1 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");
        session.publish("Test2");

        final var tracker2 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        session.publish("Test3");

        await().until(tracker1::getLog, hasItems("Test1", "Test2", "Test3"));
        await().until(tracker2::getLog, hasItems("Test2", "Test3"));
    }


    @Test
    public void testPublishWithDifferentLocations() throws IOException, InterruptedException {
        final var session = publisher.register("test", String.class, "INVALID-LOCATION");
        final var session2 = publisher.register("test", String.class, REMOTE_LOCATION_NAME);
        session.publish("Test1");
        session2.publish("Test2");

        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);
        await().atMost(10, TimeUnit.SECONDS).until(tracker::getLog, hasItems("Test2"));

        // Publishing without location is akin to publishing to all locations
        final var session3 = publisher.register("test", String.class);
        session3.publish("Test3");
        await().atMost(10, TimeUnit.SECONDS).until(tracker::getLog, hasItems("Test2", "Test3"));
    }

    @Test
    public void testPublishSubscribeWithTrapdConfig() throws IOException {
        final var session = this.publisher.register(TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class);
        final var tracker1 = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class);
        SnmpV3User user = new SnmpV3User("opennmsUser", "MD5", "0p3nNMSv3",
                "DES", "0p3nNMSv3");
        TrapListenerConfig trapListenerConfig = new TrapListenerConfig();
        ArrayList<SnmpV3User> users = new ArrayList<>();
        users.add(user);
        trapListenerConfig.setSnmpV3Users(users);
        session.publish(trapListenerConfig);
        await().until(tracker1::getLog, hasItem(trapListenerConfig));
        // Add two users and delete existing one.
        TrapListenerConfig updatedConfig = new TrapListenerConfig();
        SnmpV3User user1 = new SnmpV3User("opennmsUser1", "MD5", "0p3nNMSv1",
                "DES", "0p3nNMSv1");
        SnmpV3User user2 = new SnmpV3User("opennmsUser2", "MD5", "0p3nNMSv1",
                "DES", "0p3nNMSv2");
        users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        updatedConfig.setSnmpV3Users(users);
        session.publish(updatedConfig);
        await().until(tracker1::getLog, hasItem(updatedConfig));
    }


    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }




}
