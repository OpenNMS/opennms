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

import org.hibernate.SessionFactory;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class EventConfEventDaoIT implements InitializingBean {

    @Autowired
    private EventConfEventDao m_eventDao;

    @Autowired
    private EventConfSourceDao m_eventSourceDao;

    private EventConfSource m_source;

    @Autowired
    private SessionFactory sessionFactory;


    private int defaultEventConfEventCount;
    @Before
    @Transactional
    public void setUp() {
        m_source = new EventConfSource();
        m_source.setName("test-source");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(m_eventSourceDao.nextFileOrder());
        m_source.setDescription("Test event source");
        m_source.setVendor("TestVendor");
        m_source.setUploadedBy("JUnitTest");
        m_source.setEventCount(0);
        m_source.setLastModified(new Date());

        List<EventConfEvent> event = m_eventDao.findAll();
        defaultEventConfEventCount = event.size();

        m_eventSourceDao.saveOrUpdate(m_source);
        m_eventSourceDao.flush();

        insertEvent("uei.opennms.org/internal/discoveryConfigChange",
                "Discovery configuration changed",
                "The discovery configuration has been changed and should be reloaded",
                "Normal");

        insertEvent("uei.opennms.org/internal/discovery/hardwareInventoryFailed",
                "Hardware discovery failed",
                "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.",
                "Minor");

        insertEvent("uei.opennms.org/internal/discovery/hardwareInventorySuccessful",
                "Hardware discovery successful",
                "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has been completed successfully.",
                "Normal");

        insertEvent("uei.opennms.org/internal/discovery/newSuspect",
                "New suspect discovered",
                "A new interface (%interface%) has been discovered in location %parm[location]% and is being queued for a services scan.",
                "Warning");
    }

    @After
    @Transactional
    public void tearDown() {
        var listofConfig = m_eventDao.findAll();
        var listOfSource = m_eventSourceDao.findAll();
        m_eventDao.deleteAll(listofConfig);
        m_eventSourceDao.deleteAll(listOfSource);
        m_eventDao.flush();
        m_eventSourceDao.flush();
    }

    private void insertEvent(String uei, String label, String description, String severity) {
        EventConfEvent event = new EventConfEvent();
        event.setUei(uei);
        event.setEventLabel(label);
        event.setDescription(description);
        event.setXmlContent("<event><uei>" + uei + "</uei></event>");
        event.setSource(m_source);
        event.setSeverity(severity);
        event.setEventOrder(m_eventDao.findMaxEventOrder(m_source.getId()) + 1);
        event.setEnabled(true);
        event.setCreatedTime(new Date());
        event.setLastModified(new Date());
        event.setModifiedBy("JUnitTest");

        m_eventDao.saveOrUpdate(event);
    }

    @Test
    @Transactional
    public void testFindAllEventConfEvents() {
        List<EventConfEvent> event = m_eventDao.findAll();
        int eventSize = event.size() - defaultEventConfEventCount;
        assertNotNull("Expected to find all events", event);
        assertEquals(4, eventSize);

    }

    @Test
    @Transactional
    public void testGetById() {
        List<EventConfEvent> events = m_eventDao.findAll();
        int eventSize = events.size() - defaultEventConfEventCount;
        assertNotNull("Events should not be null", events);
        assertEquals(4, eventSize);
        EventConfEvent result = m_eventDao.get(events.get(0).getId());
        assertNotNull("Fetched event should not be null", result);
        assertEquals(events.get(0).getUei(), result.getUei());
    }

    @Test
    @Transactional
    public void testFindBySourceId() {
        List<EventConfEvent> events = m_eventDao.findBySourceId(m_source.getId());
        assertNotNull(events);
        assertFalse(events.isEmpty());
    }

    @Test
    @Transactional
    public void testFindByUei() {
        EventConfEvent event = m_eventDao.findByUei("uei.opennms.org/internal/discoveryConfigChange");
        assertNotNull("Event with matching UEI should be found", event);
        assertEquals("uei.opennms.org/internal/discoveryConfigChange", event.getUei());
    }

    @Test
    @Transactional
    public void testFindEnabledEvents() {
        List<EventConfEvent> enabledEvents = m_eventDao.findEnabledEvents();
        int enabledEventsSize = enabledEvents.size() - defaultEventConfEventCount;
        assertNotNull("Enabled events should be found", enabledEvents);
        assertEquals(4, enabledEventsSize);

        EventConfEvent event = enabledEvents.get(0);
        event.setEnabled(false);
        m_eventDao.saveOrUpdate(event);

        List<EventConfEvent> updatedEnabled = m_eventDao.findEnabledEvents();
        int updatedEnabledSize = updatedEnabled.size() - defaultEventConfEventCount;
        assertEquals(3, updatedEnabledSize);
    }

    @Test
    @Transactional
    public void testDeleteBySourceId() {
        List<EventConfEvent> beforeDelete = m_eventDao.findBySourceId(m_source.getId());
        assertEquals(4, beforeDelete.size());

        m_eventDao.deleteBySourceId(m_source.getId());

        List<EventConfEvent> afterDelete = m_eventDao.findBySourceId(m_source.getId());
        assertEquals(0, afterDelete.size());
    }

    @Test
    @Transactional
    public void testUpdateEventEnabledFlag() {
        m_source = new EventConfSource();
        m_source.setName("testEventEnabledFlagName");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(m_eventSourceDao.nextFileOrder());
        m_source.setDescription("Test event source");
        m_source.setVendor("TestVendor1");
        m_source.setUploadedBy("JUnitTest");
        m_source.setEventCount(2);
        m_source.setLastModified(new Date());

        List<EventConfEvent> event = m_eventDao.findAll();
        defaultEventConfEventCount = event.size();

        m_eventSourceDao.saveOrUpdate(m_source);
        m_eventSourceDao.flush();

        insertEvent("uei.opennms.org/internal/discoveryConfigChange11", "Discovery configuration changed testing", "The discovery configuration has been changed and should be reloaded", "Normal");

        insertEvent("uei.opennms.org/internal/discovery/hardwareInventoryFailed22", "Hardware discovery failed testing", "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.", "Minor");

        EventConfSource source = m_eventSourceDao.findByName("testEventEnabledFlagName");

        EventConfEvent discoveryEvent = m_eventDao.findByUei("uei.opennms.org/internal/discoveryConfigChange11");
        EventConfEvent hardwareEvent = m_eventDao.findByUei("uei.opennms.org/internal/discovery/hardwareInventoryFailed22");

        // disable events
        m_eventDao.updateEventEnabledFlag(source.getId(), List.of(discoveryEvent.getId(), hardwareEvent.getId()), false);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // verify disabled state
        EventConfEvent refreshedDiscoveryEvent = m_eventDao.findByUei("uei.opennms.org/internal/discoveryConfigChange11");
        EventConfEvent refreshedHardwareEvent = m_eventDao.findByUei("uei.opennms.org/internal/discovery/hardwareInventoryFailed22");
        assertFalse(refreshedDiscoveryEvent.getEnabled());
        assertFalse(refreshedHardwareEvent.getEnabled());

        // enable events
        m_eventDao.updateEventEnabledFlag(source.getId(), List.of(discoveryEvent.getId(), hardwareEvent.getId()), true);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // verify enabled state
        refreshedDiscoveryEvent = m_eventDao.findByUei("uei.opennms.org/internal/discoveryConfigChange11");
        refreshedHardwareEvent = m_eventDao.findByUei("uei.opennms.org/internal/discovery/hardwareInventoryFailed22");
        assertTrue(refreshedDiscoveryEvent.getEnabled());
        assertTrue(refreshedHardwareEvent.getEnabled());
    }

    @Test
    @Transactional
    public void testDeleteByEventIds() {
        m_source = new EventConfSource();
        m_source.setName("testDeleteEvents");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(m_eventSourceDao.nextFileOrder());
        m_source.setDescription("Test events from a source");
        m_source.setVendor("TestVendor1");
        m_source.setUploadedBy("JUnitTest");
        m_source.setEventCount(3);
        m_source.setLastModified(new Date());

        List<EventConfEvent> event = m_eventDao.findAll();
        defaultEventConfEventCount = event.size();

        m_eventSourceDao.saveOrUpdate(m_source);
        m_eventSourceDao.flush();

        insertEvent("uei.opennms.org/internal/discovery/hardwareInventoryFailed11", "Hardware discovery failed testing11", "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed 11.", "Minor");
        insertEvent("uei.opennms.org/internal/discovery/hardwareInventoryFailed22", "Hardware discovery failed testing22", "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed 22.", "Minor");
        insertEvent("uei.opennms.org/internal/discovery/hardwareInventoryFailed33", "Hardware discovery failed testing33", "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed 33.", "Minor");
        EventConfSource source = m_eventSourceDao.findByName("testDeleteEvents");

        EventConfEvent hardwareInventoryFailed11 = m_eventDao.findByUei("uei.opennms.org/internal/discovery/hardwareInventoryFailed11");
        EventConfEvent hardwareInventoryFailed22 = m_eventDao.findByUei("uei.opennms.org/internal/discovery/hardwareInventoryFailed22");

        // delete events for source "testDeleteEvents"
        m_eventDao.deleteByEventIds(source.getId(), List.of(hardwareInventoryFailed11.getId(), hardwareInventoryFailed22.getId()));
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // verify deleted events
        List<EventConfEvent> updatedEvents = m_eventDao.findBySourceId(source.getId());
        assertEquals(1, updatedEvents.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventConfEvent reloadEvent(String uei) {
        return m_eventDao.findByUei(uei);
    }


    @Test
    @Transactional
    public void testSaveAllEvents() {
        int totalEvents = 55;
        List<EventConfEvent> bulkEvents = new ArrayList<>();
        for (int i = 0; i < totalEvents; i++) {
            EventConfEvent event = getEventConfEvent(i);
            bulkEvents.add(event);
        }

        m_eventDao.saveAll(bulkEvents);

        List<EventConfEvent> allEvents = m_eventDao.findBySourceId(m_source.getId());
        assertNotNull(allEvents);
        assertEquals(4 + totalEvents, allEvents.size());
    }

    private EventConfEvent getEventConfEvent(int i) {
        EventConfEvent event = new EventConfEvent();
        event.setUei("uei.opennms.org/test/bulk/" + i);
        event.setEventLabel("Bulk Event " + i);
        event.setDescription("Test bulk event " + i);
        event.setXmlContent("<event><uei>uei.opennms.org/test/bulk/" + i + "</uei></event>");
        event.setSource(m_source);
        event.setEnabled(true);
        event.setEventOrder(i);
        event.setSeverity("Normal");
        event.setCreatedTime(new Date());
        event.setLastModified(new Date());
        event.setModifiedBy("testUser");
        return event;
    }



    @Test
    @Transactional
    public void testFindBySourceIdAndEventId() {
        m_source = new EventConfSource();
        m_source.setName("sourceAndEventTesting");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(m_eventSourceDao.nextFileOrder());
        m_source.setDescription("Test event source");
        m_source.setVendor("TestVendor2");
        m_source.setUploadedBy("testCases");
        m_source.setEventCount(2);
        m_source.setLastModified(new Date());

        List<EventConfEvent> event = m_eventDao.findAll();
        defaultEventConfEventCount = event.size();

        m_eventSourceDao.saveOrUpdate(m_source);
        m_eventSourceDao.flush();

        insertEvent("uei.opennms.org/internal/trigger", "Trigger event", "Trigger event testing description", "Normal");

        insertEvent("uei.opennms.org/internal/clear", "Clear event testing", "The clear  (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.", "Minor");
        m_eventDao.flush();
        EventConfSource source = m_eventSourceDao.findByName("sourceAndEventTesting");
        EventConfEvent clearEvent = m_eventDao.findByUei("uei.opennms.org/internal/clear");

        EventConfEvent dbEvent = m_eventDao.findBySourceIdAndEventId(source.getId(),clearEvent.getId());
        assertEquals("uei.opennms.org/internal/clear", dbEvent.getUei());

    }

    @Test
    @Transactional
    public void testEventOrderIsAssignedAndPersisted() {
        m_eventDao.flush();
        List<EventConfEvent> events = m_eventDao.findBySourceId(m_source.getId());
        assertEquals(4, events.size());
        for (int i = 0; i < events.size(); i++) {
            assertEquals("events of a source are numbered 1..N", Integer.valueOf(i + 1), events.get(i).getEventOrder());
        }
        assertEquals(Integer.valueOf(4), m_eventDao.findMaxEventOrder(m_source.getId()));
    }

    @Test
    @Transactional
    public void testFindMaxEventOrderIsZeroForSourceWithoutEvents() {
        EventConfSource empty = new EventConfSource();
        empty.setName("empty-source");
        empty.setEnabled(true);
        empty.setCreatedTime(new Date());
        empty.setFileOrder(m_eventSourceDao.nextFileOrder());
        empty.setVendor("TestVendor");
        empty.setEventCount(0);
        m_eventSourceDao.saveOrUpdate(empty);
        m_eventSourceDao.flush();

        assertEquals(Integer.valueOf(0), m_eventDao.findMaxEventOrder(empty.getId()));
        assertEquals(Integer.valueOf(0), m_eventDao.findMaxEventOrder(-1L));
    }

    @Test
    @Transactional
    public void testNextEventOrderIsMaxPlusOne() {
        m_eventDao.flush();
        assertEquals(Integer.valueOf(5), m_eventDao.nextEventOrder(m_source.getId()));
        // nothing was inserted, so the value is stable until someone appends
        assertEquals(Integer.valueOf(5), m_eventDao.nextEventOrder(m_source.getId()));
    }

    @Test
    @Transactional
    public void testFindEnabledEventsFollowsEventOrderNotId() {
        m_eventDao.flush();
        List<EventConfEvent> events = m_eventDao.findBySourceId(m_source.getId());
        // Move the last inserted event (highest id) to the front of its source
        EventConfEvent last = events.get(events.size() - 1);
        last.setEventOrder(0);
        m_eventDao.saveOrUpdate(last);
        m_eventDao.flush();
        m_eventDao.clear();

        List<EventConfEvent> enabled = m_eventDao.findEnabledEvents().stream()
                .filter(e -> e.getSource().getId().equals(m_source.getId()))
                .toList();
        assertEquals(4, enabled.size());
        assertEquals("uei.opennms.org/internal/discovery/newSuspect", enabled.get(0).getUei());
        assertEquals("uei.opennms.org/internal/discoveryConfigChange", enabled.get(1).getUei());

        // findBySourceId (download) follows the same order
        assertEquals("uei.opennms.org/internal/discovery/newSuspect",
                m_eventDao.findBySourceId(m_source.getId()).get(0).getUei());
    }

    @Test
    @Transactional
    public void testCompactEventOrderRemovesGaps() {
        m_eventDao.flush();
        List<EventConfEvent> events = m_eventDao.findBySourceId(m_source.getId());
        // Delete the 2nd event (eventOrder 2) leaving 1,3,4
        m_eventDao.deleteByEventIds(m_source.getId(), List.of(events.get(1).getId()));
        m_eventDao.compactEventOrder(m_source.getId());
        m_eventDao.flush();
        m_eventDao.clear();

        List<EventConfEvent> remaining = m_eventDao.findBySourceId(m_source.getId());
        assertEquals(3, remaining.size());
        assertEquals("uei.opennms.org/internal/discoveryConfigChange", remaining.get(0).getUei());
        assertEquals("uei.opennms.org/internal/discovery/hardwareInventorySuccessful", remaining.get(1).getUei());
        assertEquals("uei.opennms.org/internal/discovery/newSuspect", remaining.get(2).getUei());
        for (int i = 0; i < remaining.size(); i++) {
            assertEquals(Integer.valueOf(i + 1), remaining.get(i).getEventOrder());
        }
    }

    @Test
    @Transactional
    public void testPagedFindBySourceIdDefaultsToEventOrder() {
        m_eventDao.flush();
        @SuppressWarnings("unchecked")
        List<EventConfEvent> page = (List<EventConfEvent>) m_eventDao
                .findBySourceId(m_source.getId(), "", null, null, 0, 0, 10).get("eventConfEventList");
        assertEquals(4, page.size());
        assertEquals(Integer.valueOf(1), page.get(0).getEventOrder());
        assertEquals(Integer.valueOf(4), page.get(3).getEventOrder());

        @SuppressWarnings("unchecked")
        List<EventConfEvent> desc = (List<EventConfEvent>) m_eventDao
                .findBySourceId(m_source.getId(), "", "eventOrder", "desc", 0, 0, 10).get("eventConfEventList");
        assertEquals(Integer.valueOf(4), desc.get(0).getEventOrder());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

}