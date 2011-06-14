package org.opennms.netmgt.correlation.drools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:META-INF/opennms/applicationContext-daemon.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:META-INF/opennms/applicationContext-correlator.xml",
        "classpath:test-context.xml"
})
@JUnitTemporaryDatabase(tempDbClass=TemporaryDatabase.class)
public class DroolsCorrelationEngineBuilderTest implements InitializingBean {
    @Autowired
    private DroolsCorrelationEngineBuilder m_droolsCorrelationEngineBuilder;
    @Autowired
    private CorrelationEngineRegistrar m_mockCorrelator;

    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_droolsCorrelationEngineBuilder);
        assertNotNull(m_mockCorrelator);
    }

    @Test
    public void testIt() throws Exception {
        List<CorrelationEngine> engines = m_mockCorrelator.getEngines();
        assertNotNull(engines);
        assertEquals(2, m_mockCorrelator.getEngines().size());
        assertTrue(engines.get(0) instanceof DroolsCorrelationEngine);
        assertTrue(m_mockCorrelator.findEngineByName("locationMonitorRules") instanceof DroolsCorrelationEngine);
        DroolsCorrelationEngine engine = (DroolsCorrelationEngine) m_mockCorrelator.findEngineByName("locationMonitorRules");
        assertEquals(2, engine.getInterestingEvents().size());
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI));
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI));
    }
}
