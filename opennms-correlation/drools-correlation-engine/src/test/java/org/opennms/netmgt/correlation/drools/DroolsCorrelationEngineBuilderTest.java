package org.opennms.netmgt.correlation.drools;

import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.DaoTestConfigBean;

public class DroolsCorrelationEngineBuilderTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private DroolsCorrelationEngineBuilder m_droolsCorrelationEngineBuilder;
    private CorrelationEngineRegistrar m_mockCorrelator;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/opennms-home");
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/mockEventIpcManager.xml",
                "classpath:META-INF/opennms/applicationContext-correlator.xml",
                "classpath:META-INF/opennms/correlation-engine.xml",
        };
    }

    public void testIt() throws Exception {
        assertNotNull(m_droolsCorrelationEngineBuilder);
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
    
    public void setDroolsCorrelationEngineBuilder(DroolsCorrelationEngineBuilder bean) {
        m_droolsCorrelationEngineBuilder = bean;
    }
    
    public void setMockCorrelator(CorrelationEngineRegistrar mockCorrelator) {
        m_mockCorrelator = mockCorrelator;
    }
}
