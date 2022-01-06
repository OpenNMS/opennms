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

package org.opennms.core.ipc.twin.test;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.distributed.core.api.MinionIdentity;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapListenerConfig;

public abstract class AbstractTwinBrokerIT {

    protected abstract TwinPublisher createPublisher() throws Exception;
    protected abstract TwinSubscriber createSubscriber(final MinionIdentity identity) throws Exception;

    public TwinPublisher publisher;
    public TwinSubscriber subscriber;

    private final MinionIdentity standardIdentity = new MockMinionIdentity("remote");

    @Before
    public void setup() throws Exception {
        this.publisher = this.createPublisher();
        this.subscriber = this.createSubscriber(this.standardIdentity);
    }

    @After
    public void teardown() throws Exception {
        this.subscriber.close();
        this.publisher.close();
    }

    /**
     * Tests that an object can be published and the same object can be received by the subscriber.
     */
    @Test
    public void testPublishSubscribe() throws Exception {
        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker = Tracker.subscribe(this.subscriber, "test", String.class);

        await().until(tracker::getLog, contains("Test1"));
    }

    /**
     * Tests that a publisher can update an object and the subscriber will receives all versions.
     */
    @Test
    public void testUpdates() throws Exception {
        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker = Tracker.subscribe(this.subscriber, "test", String.class);
        // Ensure Test1 is received.
        await().until(tracker::getLog, contains("Test1"));

        session.publish("Test2");
        session.publish("Test3");

        await().until(tracker::getLog, contains("Test1", "Test2", "Test3"));
    }

    /**
     * Tests that a subscriber can register before a publisher exists.
     */
    @Test
    public void testSubscribeBeforePublish() throws Exception {
        final var tracker = Tracker.subscribe(this.subscriber, "test", String.class);

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");
        await().until(tracker::getLog, contains("Test1"));

        session.publish("Test2");
        await().until(tracker::getLog, contains("Test1", "Test2"));
    }

    /**
     * Tests that multiple subscriptions exists for the same key.
     */
    @Test
    public void testMultipleSubscription() throws Exception {
        final var tracker1 = Tracker.subscribe(this.subscriber, "test", String.class);

        final var session = this.publisher.register("test", String.class);

        session.publish("Test1");
        await().until(tracker1::getLog, contains("Test1"));

        session.publish("Test2");
        await().until(tracker1::getLog, contains("Test1", "Test2"));

        final var tracker2 = Tracker.subscribe(this.subscriber, "test", String.class);
        await().until(tracker2::getLog, contains("Test2"));

        session.publish("Test3");
        await().until(tracker1::getLog, contains("Test1", "Test2", "Test3"));
        await().until(tracker2::getLog, contains("Test2", "Test3"));
    }

    /**
     * Tests that multiple subscribers get the same update.
     */
    @Test
    public void testMultipleSubscribers() throws Exception {
        final var session = this.publisher.register("test", String.class);

        final var subscriber1 = this.createSubscriber(new MockMinionIdentity("location_a", "minion_1"));
        final var tracker1 = Tracker.subscribe(subscriber1, "test", String.class);
        await().until(tracker1::getLog, empty());

        session.publish("Test1");
        await().until(tracker1::getLog, contains("Test1"));

        session.publish("Test2");
        await().until(tracker1::getLog, contains("Test1", "Test2"));

        final var subscriber2 = this.createSubscriber(new MockMinionIdentity("location_a", "minion_2"));
        final var tracker2 = Tracker.subscribe(subscriber2, "test", String.class);
        await().until(tracker2::getLog, contains("Test2"));

        session.publish("Test3");
        await().until(tracker1::getLog, contains("Test1", "Test2", "Test3"));
        await().until(tracker2::getLog, contains("Test2", "Test3"));

        final var subscriber3 = this.createSubscriber(new MockMinionIdentity("location_b", "minion_3"));
        final var tracker3 = Tracker.subscribe(subscriber3, "test", String.class);
        await().until(tracker3::getLog, contains("Test3"));

        subscriber1.close();
        subscriber2.close();
        subscriber3.close();
    }

    /**
     * Tests that subscription works if publisher gets restarted.
     */
    @Test
    public void testPublisherRestart() throws Exception {
        final var tracker = Tracker.subscribe(this.subscriber, "test", String.class);

        final var session1 = this.publisher.register("test", String.class);
        session1.publish("Test1");
        session1.publish("Test2");

        await().until(tracker::getLog, contains("Test1", "Test2"));

        this.publisher.close();
        this.publisher = this.createPublisher();

        final var session2 = this.publisher.register("test", String.class);
        session2.publish("Test3");

        await().until(tracker::getLog, contains("Test1", "Test2", "Test3"));
    }

    /**
     * Tests that subscription can be closed and reopened.
     */
    @Test
    public void testSubscriberClose() throws Exception {
        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker1 = Tracker.subscribe(this.subscriber, "test", String.class);
        await().until(tracker1::getLog, contains("Test1"));

        tracker1.close();

        session.publish("Test2");
        session.publish("Test3");

        final var tracker2 = Tracker.subscribe(this.subscriber, "test", String.class);
        // Maybe contains elements from the `publish`s above.
        // Due to transport latency, the subscriber receive the elements even if published before subscribe.
        await().until(tracker2::getLog, hasItem("Test3"));

        assertThat(tracker1.getLog(), not(hasItem("Test2")));
        assertThat(tracker1.getLog(), not(hasItem("Test3")));
    }

    /**
     * Tests that subscription can be closed before registration.
     */
    @Test
    public void testSubscriberCloseBeforeRegister() throws Exception {
        final var tracker1 = Tracker.subscribe(this.subscriber, "test", String.class);
        tracker1.close();

        final var session = this.publisher.register("test", String.class);
        session.publish("Test1");

        final var tracker2 = Tracker.subscribe(this.subscriber, "test", String.class);
        await().until(tracker2::getLog, contains("Test1"));

        assertThat(tracker1.getLog(), empty());
    }

    @Test
    public void testPublishSubscribeWithTrapdConfig() throws IOException {
        final var session = this.publisher.register(TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class);
        final var tracker1 = Tracker.subscribe(this.subscriber, TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class);
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

    public static class Tracker<T> implements Closeable {
        private final List<T> log;
        private final Closeable subscription;

        private Tracker(final List<T> log,
                        final Closeable subscription) {
            this.log = Objects.requireNonNull(log);
            this.subscription = Objects.requireNonNull(subscription);
        }

        public List<T> getLog() {
            return this.log;
        }

        @Override
        public void close() throws IOException {
            this.subscription.close();
        }

        public static <T> Tracker<T> subscribe(final TwinSubscriber subscriber, final String key, final Class<T> clazz) {
            final var log = new ArrayList<T>();
            final var subscription = subscriber.subscribe(key, clazz, log::add);

            return new Tracker<>(log, subscription);
        }
    }
}
