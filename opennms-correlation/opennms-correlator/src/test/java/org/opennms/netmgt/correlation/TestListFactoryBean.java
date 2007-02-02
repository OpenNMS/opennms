package org.opennms.netmgt.correlation;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class TestListFactoryBean implements FactoryBean, InitializingBean {
    
    
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
        
    }
    
    CorrelationEngine[] m_engines;
    EventIpcManager m_eventIpcManager;
    
    public Object getObject() throws Exception {
        return m_engines;
    }

    public Class getObjectType() {
        return CorrelationEngine[].class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        MyEngine engine = new MyEngine();
        engine.setEventIpcManager(m_eventIpcManager);
        
        m_engines = new CorrelationEngine[] {
                engine
        };
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

}
