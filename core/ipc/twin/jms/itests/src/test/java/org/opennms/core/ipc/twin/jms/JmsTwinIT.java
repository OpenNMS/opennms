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
import org.hamcrest.Matchers;
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
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-twin-jms-publisher.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DirtiesContextTestExecutionListener.class})
public class JmsTwinIT extends CamelBlueprintTest {

    private static final String REMOTE_LOCATION_NAME = "remote";

    private AtomicBoolean blueprintLoaded = new AtomicBoolean(false);

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
        // Blueprint gets loaded twice which leads to multiple copies of TwinSubscriber.
        // To avoid it, we try to load actual blueprint only once.
        if(blueprintLoaded.get()) {
            blueprintLoaded.set(true);
            return "classpath:/OSGI-INF/blueprint/blueprint-twin-subscriber.xml";
        }
        return "classpath:/OSGI-INF/blueprint/blueprint-empty.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        blueprintLoaded.set(false);
        subscriber = context.getRegistry().lookupByNameAndType("jmsTwinSubscriber", TwinSubscriber.class);
    }

    /**
     * Tests that an object can be published and the same object can be received by the subscriber.
     */
    @Test
    public void testPublishSubscribe() throws Exception {
        //subscriber = context.getRegistry().lookupByNameAndType("jmsTwinSubscriber", TwinSubscriber.class);
        assertThat(subscriber, notNullValue());
        final var session = publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        await().until(tracker::getLog, contains("Test1"));
    }


    /**
     * Tests that a publisher can update an object and the subscriber will receives all versions.
     */
    @Test
    public void testUpdates() throws Exception {
        //subscriber = context.getRegistry().lookupByNameAndType("jmsTwinSubscriber", TwinSubscriber.class);
        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker = AbstractTwinBrokerIT.Tracker.subscribe(this.subscriber, "test", String.class);

        await().until(tracker::getLog, contains("Test1"));
        session.publish("Test2");
        session.publish("Test3");
        await().until(tracker::getLog, contains("Test1", "Test2", "Test3"));
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
        await().until(tracker2::getLog, contains("Test1"));

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

        await().until(tracker::getLog, contains("Test1", "Test2"));
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

        assertThat(tracker1.getLog(), contains("Test1"));
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

        await().until(tracker::getLog, contains("Test1", "Test2"));

        ((JmsTwinPublisher) publisher).destroy();
        ((JmsTwinPublisher) publisher).init();
        session.publish("Test3");

        await().until(tracker::getLog, contains("Test1", "Test2", "Test3"));
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

        await().until(tracker1::getLog, contains("Test1", "Test2", "Test3"));
        await().until(tracker2::getLog, hasItems("Test2", "Test3"));
    }


    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }




}
