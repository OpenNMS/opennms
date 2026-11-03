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
package org.opennms.netmgt.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
/**
 * Runs without a test-managed transaction so that two real, committed transactions can race
 * for the same source; see {@link EventConfEventDao#nextEventOrder(Long)}.
 */
public class EventConfEventOrderConcurrencyIT implements InitializingBean {

    @Autowired
    private EventConfEventDao m_eventDao;

    @Autowired
    private EventConfSourceDao m_eventSourceDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    private EventConfSource m_source;

    @Before
    public void setUp() {
        m_source = m_transactionTemplate.execute(status -> {
            EventConfSource source = new EventConfSource();
            source.setName("concurrency-source");
            source.setEnabled(true);
            source.setCreatedTime(new Date());
            source.setLastModified(new Date());
            source.setFileOrder(m_eventSourceDao.nextFileOrder());
            source.setVendor("TestVendor");
            source.setUploadedBy("JUnitTest");
            source.setEventCount(4);
            m_eventSourceDao.saveOrUpdate(source);
            for (int i = 1; i <= 4; i++) {
                m_eventDao.save(newEvent(source, "uei.opennms.org/test/concurrent/seed" + i, i));
            }
            m_eventDao.flush();
            return source;
        });
    }

    @After
    public void tearDown() {
        m_transactionTemplate.execute(status -> {
            EventConfSource source = m_eventSourceDao.get(m_source.getId());
            if (source != null) {
                m_eventDao.deleteBySourceId(source.getId());
                m_eventSourceDao.delete(source);
            }
            return null;
        });
    }

    /**
     * Two transactions appending to the same source must not be handed the same eventOrder: the second
     * allocation must block while the first transaction is open, and see its insert once it committed.
     * Blocking is verified with latches (the second appender provably makes no progress while the first
     * holds its transaction open), not with timestamps, which would race the moment of the commit itself.
     */
    @Test
    public void testNextEventOrderSerializesConcurrentAppenders() throws Exception {
        final Long sourceId = m_source.getId();
        final CountDownLatch firstHoldsLock = new CountDownLatch(1);
        final CountDownLatch secondAboutToAllocate = new CountDownLatch(1);
        final CountDownLatch releaseFirst = new CountDownLatch(1);
        final AtomicInteger firstOrder = new AtomicInteger();
        final AtomicInteger secondOrder = new AtomicInteger();

        final Thread first = worker("appender-1", () -> m_transactionTemplate.execute(status -> {
            int order = m_eventDao.nextEventOrder(sourceId);
            firstOrder.set(order);
            m_eventDao.save(newEvent(m_source, "uei.opennms.org/test/concurrent/first", order));
            m_eventDao.flush();
            firstHoldsLock.countDown();
            try {
                // hold the transaction (and with it the source-row lock) open until the main thread
                // has verified that the second appender is blocked
                assertTrue(releaseFirst.await(20, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }));

        final Thread second = worker("appender-2", () -> {
            assertTrue(firstHoldsLock.await(10, TimeUnit.SECONDS));
            m_transactionTemplate.execute(status -> {
                secondAboutToAllocate.countDown();
                int order = m_eventDao.nextEventOrder(sourceId); // blocks until appender-1 commits
                secondOrder.set(order);
                m_eventDao.save(newEvent(m_source, "uei.opennms.org/test/concurrent/second", order));
                m_eventDao.flush();
                return null;
            });
        });

        first.start();
        second.start();

        // While the first transaction is open, the second appender must not obtain an order.
        assertTrue(secondAboutToAllocate.await(10, TimeUnit.SECONDS));
        Thread.sleep(1_000);
        assertEquals("second appender must be blocked while the first transaction is open", 0, secondOrder.get());

        releaseFirst.countDown();
        joinAndRethrow(first, second);

        assertEquals(5, firstOrder.get());
        assertEquals("second appender must see the first insert", 6, secondOrder.get());

        List<EventConfEvent> events = m_transactionTemplate.execute(status -> m_eventDao.findBySourceId(sourceId));
        assertEquals(6, events.size());
        assertEquals(6, events.stream().map(EventConfEvent::getEventOrder).distinct().count());
    }


    /**
     * Two transactions creating sources at the same time must not be handed the same fileOrder either.
     * Allocation comes from a sequence, so neither has to wait for the other: the first creator holds its
     * transaction open while the second allocates, commits and is done, and both values are distinct and
     * above everything that existed before.
     */
    @Test
    public void testNextFileOrderIsUniqueAndLockFreeAcrossCreators() throws Exception {
        final CountDownLatch firstAllocated = new CountDownLatch(1);
        final CountDownLatch secondCommitted = new CountDownLatch(1);
        final AtomicInteger firstOrder = new AtomicInteger();
        final AtomicInteger secondOrder = new AtomicInteger();
        final int maxBefore = m_transactionTemplate.execute(status -> m_eventSourceDao.findMaxFileOrder());

        final Thread first = worker("creator-1", () -> m_transactionTemplate.execute(status -> {
            int order = m_eventSourceDao.nextFileOrder();
            firstOrder.set(order);
            m_eventSourceDao.save(newSource("concurrency-source-a", order));
            m_eventSourceDao.flush();
            firstAllocated.countDown();
            try {
                // the second creator must get through while this transaction is still open
                assertTrue("second creator must not wait for the first one", secondCommitted.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        }));

        final Thread second = worker("creator-2", () -> {
            assertTrue(firstAllocated.await(10, TimeUnit.SECONDS));
            m_transactionTemplate.execute(status -> {
                int order = m_eventSourceDao.nextFileOrder();
                secondOrder.set(order);
                m_eventSourceDao.save(newSource("concurrency-source-b", order));
                m_eventSourceDao.flush();
                return null;
            });
            secondCommitted.countDown();
        });

        first.start();
        second.start();
        joinAndRethrow(first, second);

        assertTrue("both allocations must be above the previous maximum", firstOrder.get() > maxBefore && secondOrder.get() > maxBefore);
        assertTrue("allocations must be distinct and increasing", secondOrder.get() > firstOrder.get());

        m_transactionTemplate.execute(status -> {
            for (String name : List.of("concurrency-source-a", "concurrency-source-b")) {
                EventConfSource s = m_eventSourceDao.findByName(name);
                if (s != null) {
                    m_eventSourceDao.delete(s);
                }
            }
            return null;
        });
    }

    private EventConfSource newSource(String name, int fileOrder) {
        EventConfSource source = new EventConfSource();
        source.setName(name);
        source.setEnabled(true);
        source.setCreatedTime(new Date());
        source.setLastModified(new Date());
        source.setFileOrder(fileOrder);
        source.setVendor("TestVendor");
        source.setUploadedBy("JUnitTest");
        source.setEventCount(0);
        return source;
    }

    private EventConfEvent newEvent(EventConfSource source, String uei, int order) {
        EventConfEvent event = new EventConfEvent();
        event.setUei(uei);
        event.setEventLabel(uei);
        event.setDescription(uei);
        event.setXmlContent("<event><uei>" + uei + "</uei></event>");
        event.setSource(source);
        event.setSeverity("Normal");
        event.setEnabled(true);
        event.setCreatedTime(new Date());
        event.setLastModified(new Date());
        event.setModifiedBy("JUnitTest");
        event.setEventOrder(order);
        return event;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** A worker whose failure (assertion or exception) is captured and rethrown by {@link #joinAndRethrow}. */
    private static Thread worker(final String name, final ThrowingRunnable body) {
        final Thread t = new Thread(() -> {
            try {
                body.run();
            } catch (Throwable e) {
                FAILURES.put(Thread.currentThread(), e);
            }
        }, name);
        return t;
    }

    private static void joinAndRethrow(final Thread... threads) throws Exception {
        for (Thread t : threads) {
            t.join(30_000);
            assertFalse(t.getName() + " is still running", t.isAlive());
        }
        for (Thread t : threads) {
            final Throwable failure = FAILURES.remove(t);
            if (failure instanceof Exception) {
                throw (Exception) failure;
            } else if (failure != null) {
                throw new AssertionError(t.getName() + " failed", failure);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final Map<Thread, Throwable> FAILURES = new ConcurrentHashMap<>();
}
