package org.opennms.netmgt.correlation;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

public class MultiEngineBuilder implements InitializingBean {
    
    
    private static class MyEngine extends AbstractCorrelationEngine {

        public void correlate(Event e) {
            EventBuilder bldr = new EventBuilder("listLoaded", "TestEngine");
            sendEvent(bldr.getEvent());
        }

        public List<String> getInterestingEvents() {
            String[] ueis = {
              "isListLoaded"      
            };
            return Arrays.asList(ueis);
        }

        @Override
        protected void timerExpired(Integer timerId) {
            
        }

        public String getName() {
           return "MyEngine";
        }
        
    }
    
    CorrelationEngine[] m_engines;
    CorrelationEngineRegistrar m_correlator;
    EventIpcManager m_eventIpcManager;
    
    public void afterPropertiesSet() throws Exception {
        MyEngine engine = new MyEngine();
        engine.setEventIpcManager(m_eventIpcManager);
        
        m_correlator.addCorrelationEngine(engine);
    }
    
    public void setCorrelator(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

}
