package org.opennms.netmgt.dao;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.EventConfigDao;
import org.opennms.netmgt.dao.api.EventConfigSourceDao;
import org.opennms.netmgt.model.EventConfEvents;
import org.opennms.netmgt.model.EventConfSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase
public class EventConfigDaoIT {

    @Autowired
    private EventConfigDao m_eventDao;

    @Autowired
    private EventConfigSourceDao m_eventSourceDao;

    private EventConfSource m_source;

    @Before
    @Transactional
    public void setUp() {
        // Create and persist an event source
        m_source = new EventConfSource();
        m_source.setName("test-source");
        m_source.setEnabled(true);
        m_eventSourceDao.saveOrUpdate(m_source);
        m_eventSourceDao.flush();

        // Populate the DB with events parsed from the eventconf.xml
        insertEvent("uei.opennms.org/internal/discoveryConfigChange",
                "OpenNMS-defined internal event: discovery configuration changed",
                "The discovery configuration has been changed and should be reloaded", "Normal");

        insertEvent("uei.opennms.org/internal/discovery/hardwareInventoryFailed",
                "OpenNMS-defined internal event: reload specified daemon configuration failed",
                "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has failed.",
                "Minor");

        insertEvent("uei.opennms.org/internal/discovery/hardwareInventorySuccessful",
                "OpenNMS-defined internal event: hardware discovery successful",
                "The hardware discovery (%parm[method]%) on node %nodelabel% (IP address %interface%) has been completed successfuly.",
                "Normal");

        insertEvent("uei.opennms.org/internal/discovery/newSuspect",
                "OpenNMS-defined internal event: discovery newSuspect",
                "A new interface (%interface%) has been discovered in location %parm[location]% and is being queued for a services scan.",
                "Warning");
    }

    private void insertEvent(String uei, String label, String logMsg, String severity) {
        EventConfEvents event = new EventConfEvents();
        event.setUei(uei);
        event.setEventLabel(label);
//        event.setLogmsg(logMsg);
//        event.setSeverity(severity);
        event.setSource(m_source);
//        event.setCreationTime(new Date());
        m_eventDao.saveOrUpdate(event);
    }

  /*  @Test
    @Transactional
    public void testFindByUeiAndSourceId() {
        EventConfEvent event = m_eventDao.findByUeiAndSourceId("uei.opennms.org/internal/discovery/newSuspect", m_source.getId());
        assertNotNull("Expected to find event by UEI and source ID", event);
        assertEquals("uei.opennms.org/internal/discovery/newSuspect", event.getUei());
        assertEquals("Warning", event.getSeverity());
    }

    @Test
    @Transactional
    public void testFindAllForSource() {
        List<EventConfEvent> events = m_eventDao.findAllForSource(m_source);
        assertNotNull(events);
        assertEquals("Should find 4 events", 4, events.size());
    }

    @Test
    @Transactional
    public void testFieldsMatch() {
        EventConfEvent event = m_eventDao.findByUeiAndSourceId("uei.opennms.org/internal/discovery/hardwareInventorySuccessful", m_source.getId());
        assertNotNull(event);
        assertEquals("OpenNMS-defined internal event: hardware discovery successful", event.getEventLabel());
        assertEquals("Normal", event.getSeverity());
    }*/
}
