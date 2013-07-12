package org.opennms.netmgt.dao.mock;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        //"classpath:/META-INF/opennms/applicationContext-daemon.xml",
        //"classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        //"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
        //"classpath:/META-INF/opennms/applicationContext-mockEventd.xml"
})
@JUnitConfigurationEnvironment
public class MockEventWriterTest {
    private MockEventWriter m_eventWriter;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Before
    public void setUp() throws Exception {
        m_eventWriter = new MockEventWriter();
        m_eventWriter.setEventDao(m_eventDao);
        m_eventWriter.setDistPollerDao(m_distPollerDao);
        m_eventWriter.setNodeDao(m_nodeDao);
        m_eventWriter.setServiceTypeDao(m_serviceTypeDao);
        m_eventWriter.afterPropertiesSet();
    }

    @Test
    public void testWrite() throws EventProcessorException {
        final EventBuilder eb = new EventBuilder("uei.opennms.org/nodes/nodeDown", "EventExpanderTest");
        final Event event = eb.getEvent();
        m_eventWriter.process(null, event);

        System.err.println(m_eventDao.findAll());
        assertTrue(m_eventDao.findAll().get(0).getId() > 0);
    }

}
