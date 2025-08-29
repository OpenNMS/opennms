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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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
    private int defaultEventConfEventCount;
    @Before
    @Transactional
    public void setUp() {
        m_source = new EventConfSource();
        m_source.setName("test-source");
        m_source.setEnabled(true);
        m_source.setCreatedTime(new Date());
        m_source.setFileOrder(1);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

}
