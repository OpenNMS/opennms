package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class DroolsEngineFactoryBean implements FactoryBean, InitializingBean {

    EventIpcManager m_eventIpcManager;
    CorrelationEngine[] m_engines;

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
        m_engines = new CorrelationEngine[] {
                
        };
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

}
