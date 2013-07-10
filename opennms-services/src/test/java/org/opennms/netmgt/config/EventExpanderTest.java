package org.opennms.netmgt.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml"
})
@JUnitConfigurationEnvironment
public class EventExpanderTest {
    @Autowired
    private EventConfDao m_eventConfDao;

    private EventExpander m_eventExpander;

    @Before
    public void setUp() {
        m_eventExpander = new EventExpander();
        m_eventExpander.setEventConfDao(m_eventConfDao);
        m_eventExpander.afterPropertiesSet();
    }

    @Test
    public void testEventExpander() {
        final EventBuilder eb = new EventBuilder("uei.opennms.org/nodes/nodeDown", "EventExpanderTest");
        final Event event = eb.getEvent();
        m_eventExpander.process(null, event);
        assertNotNull(event.getDescr());
        assertTrue(event.getLogmsg().getContent().contains("is down"));
    }
}
