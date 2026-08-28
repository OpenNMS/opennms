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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
            source.setFileOrder(1);
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
     * Two transactions appending to the same source must not be handed the same eventOrder:
     * the second allocation has to wait for the first transaction to commit and then see its insert.
     */
    @Test
    public void testNextEventOrderSerializesConcurrentAppenders() throws Exception {
        final Long sourceId = m_source.getId();
        final CountDownLatch firstHoldsLock = new CountDownLatch(1);
        final CountDownLatch secondAsked = new CountDownLatch(1);
        final AtomicInteger firstOrder = new AtomicInteger();
        final AtomicInteger secondOrder = new AtomicInteger();
        final AtomicLong secondObtainedAt = new AtomicLong();
        final AtomicLong firstCommittedAt = new AtomicLong();

        Thread first = new Thread(() -> m_transactionTemplate.execute(status -> {
            int order = m_eventDao.nextEventOrder(sourceId);
            firstOrder.set(order);
            EventConfEvent event = newEvent(m_source, "uei.opennms.org/test/concurrent/first", order);
            m_eventDao.save(event);
            m_eventDao.flush();
            firstHoldsLock.countDown();
            try {
                // keep the lock while the second transaction asks for its order
                assertTrue(secondAsked.await(10, TimeUnit.SECONDS));
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            firstCommittedAt.set(System.nanoTime());
            return null;
        }), "appender-1");

        Thread second = new Thread(() -> {
            try {
                assertTrue(firstHoldsLock.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            m_transactionTemplate.execute(status -> {
                secondAsked.countDown();
                int order = m_eventDao.nextEventOrder(sourceId); // blocks until appender-1 commits
                secondObtainedAt.set(System.nanoTime());
                secondOrder.set(order);
                m_eventDao.save(newEvent(m_source, "uei.opennms.org/test/concurrent/second", order));
                m_eventDao.flush();
                return null;
            });
        }, "appender-2");

        first.start();
        second.start();
        first.join(30_000);
        second.join(30_000);
        assertFalse(first.isAlive());
        assertFalse(second.isAlive());

        assertEquals(5, firstOrder.get());
        assertEquals("second appender must see the first insert", 6, secondOrder.get());
        assertTrue("second allocation must have waited for the first commit",
                secondObtainedAt.get() > firstCommittedAt.get());

        List<EventConfEvent> events = m_transactionTemplate.execute(status -> m_eventDao.findBySourceId(sourceId));
        assertEquals(6, events.size());
        assertEquals(6, events.stream().map(EventConfEvent::getEventOrder).distinct().count());
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
}
