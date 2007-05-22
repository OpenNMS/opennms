package org.opennms.netmgt.ticketd;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class TicketerPluginFactory implements FactoryBean {

    private Class m_pluginClass;
    private TicketerPlugin m_ticketerPlugin;

    public void setPluginClass(Class pluginClass) {
        m_pluginClass = pluginClass;
    }
    
    public Object getObject() throws Exception {
        if (m_pluginClass == null) {
            throw new IllegalStateException("pluginClass must be set");
        }
        if (m_ticketerPlugin == null) {
            m_ticketerPlugin = (TicketerPlugin)m_pluginClass.newInstance();
        }
        return m_ticketerPlugin;
        
    }

    public Class getObjectType() {
        return (m_pluginClass == null ? TicketerPlugin.class : m_pluginClass);
    }

    public boolean isSingleton() {
        return true;
    }
    
}
