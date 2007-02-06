package org.opennms.netmgt.correlation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

public class CorrelationEngineFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {
    
    private List<CorrelationEngine> m_correlationEngines = Collections.EMPTY_LIST;
	private ApplicationContext m_applicationContext;

    public Object getObject() throws Exception {
        return m_correlationEngines;
    }

    public Class getObjectType() {
        return m_correlationEngines.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
    
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_applicationContext != null, "applicationContext must be set");
        
        Map<String, CorrelationEngine> beans = 
            BeanFactoryUtils.beansOfTypeIncludingAncestors(m_applicationContext, CorrelationEngine.class);
        
        // put them in a set to deduplicate the beans
        System.err.println("Deduplicating engines");
        HashSet<CorrelationEngine> engineSet = new HashSet<CorrelationEngine>(beans.values()); 
        
        m_correlationEngines = new LinkedList<CorrelationEngine>(engineSet);
        
        System.err.println("Found "+m_correlationEngines.size()+" engines");
    }
    

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	    m_applicationContext = applicationContext;
	}

}
