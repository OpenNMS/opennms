package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
import org.springframework.mock.web.MockExpressionEvaluator;

import junit.framework.TestCase;

public class DroolsCorrelationEngineTest extends TestCase {
    
    MockEventIpcManager m_eventIpcMgr;      

    public DroolsCorrelationEngineTest() {
        System.setProperty("opennms.home", "src/test/opennms-home");
        
        m_eventIpcMgr = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcMgr);
    }

    public void testDroolsEngine() throws Exception {
        
        DroolsCorrelationEngine engine = new DroolsCorrelationEngine();
        engine.setEventIpcManager(m_eventIpcMgr);
        engine.afterPropertiesSet();
        
        EventBuilder bldr = new EventBuilder("uei", "test");
        
        engine.correlate(bldr.getEvent());
        
    }

}
