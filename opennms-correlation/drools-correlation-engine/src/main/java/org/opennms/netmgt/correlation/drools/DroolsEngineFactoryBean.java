package org.opennms.netmgt.correlation.drools;

import java.beans.PropertyEditor;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.drools.config.EngineConfiguration;
import org.opennms.netmgt.correlation.drools.config.Global;
import org.opennms.netmgt.correlation.drools.config.RuleSet;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class DroolsEngineFactoryBean extends PropertyEditorRegistrySupport implements FactoryBean, InitializingBean, ApplicationContextAware {
    // injected
    private ApplicationContext m_applicationContext;
    private Resource m_configResource;
    private EventIpcManager m_eventIpcManager;

    // built
    private CorrelationEngine[] m_engines;
    private List<RuleSetConfiguration> m_ruleSets;
    
    public DroolsEngineFactoryBean() {
        registerDefaultEditors();
    }

    public Object getObject() throws Exception {
        return m_engines;
    }

    public Class getObjectType() {
        return CorrelationEngine[].class;
    }

    public boolean isSingleton() {
        return true;
    }
    
    public void assertSet(Object obj, String name) {
        Assert.state(obj != null, name+" required for DroolsEngineFactoryBean");
    }
    
    public void afterPropertiesSet() throws Exception {
        assertSet(m_configResource, "configurationResource");
        assertSet(m_applicationContext, "applicationContext");
        assertSet(m_eventIpcManager, "eventIpcManager");
        
        readConfiguration();
        
        List<CorrelationEngine> engineList = new LinkedList<CorrelationEngine>();
        for (RuleSetConfiguration ruleSet : m_ruleSets) {
           engineList.add(constructEngine(ruleSet));
        }
        
        m_engines = (CorrelationEngine[]) engineList.toArray(new CorrelationEngine[engineList.size()]);
    }

    private CorrelationEngine constructEngine(RuleSetConfiguration ruleSet) throws Exception {
        DroolsCorrelationEngine engine = new DroolsCorrelationEngine();
        engine.setEventIpcManager(m_eventIpcManager);
        engine.setScheduler(new Timer());
        engine.setInterestingEvents(ruleSet.getInterestingEvents());
        engine.setRulesResources(ruleSet.getRuleResources());
        engine.setGlobals(ruleSet.getGlobals());
        engine.initialize();
        return engine;
    }
    
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    public void setConfigurationResource(Resource configResource) {
        m_configResource = configResource;
    }
    
    private void readConfiguration() throws Exception {
        
        Reader rdr = null;
        try {
            rdr = new InputStreamReader( m_configResource.getInputStream() );

            EngineConfiguration configuration = (EngineConfiguration) Unmarshaller.unmarshal( EngineConfiguration.class, rdr );
            
            List<RuleSetConfiguration> ruleSets = new LinkedList<RuleSetConfiguration>();
            for (RuleSet ruleSet : configuration.getRuleSet()) {
                ruleSets.add(new RuleSetConfiguration(ruleSet));
            }
            
            m_ruleSets = ruleSets;
            
        } finally {
            IOUtils.closeQuietly(rdr);
        }
        
    }
    
    public class RuleSetConfiguration {
        private List<String> m_interestingEvents;
        private List<Resource> m_ruleResources;
        private Map<String, Object> m_globals;
        
        public RuleSetConfiguration(RuleSet ruleSet) {
            m_interestingEvents = Arrays.asList(ruleSet.getEvent());
            
            m_ruleResources = new LinkedList<Resource>();
            
            for (String resourceName : ruleSet.getRuleFile()) {
                m_ruleResources.add( createResource(resourceName) );
            }
            
            m_globals = new HashMap<String, Object>();
            
            for (Global global : ruleSet.getGlobal()) {
                m_globals.put(global.getName(), constructValue(global.getValue(), global.getType()));
            }
            
        }

        private Resource createResource(String resourceName) {
            String finalName = PropertiesUtils.substitute(resourceName, System.getProperties());
            return m_applicationContext.getResource( finalName );
        }
        
        private Object constructValue(String value, String type) {
            PropertyEditor classEditor = getDefaultEditor(Class.class);
            classEditor.setAsText(type);
            Class typeClass = (Class)classEditor.getValue();
            
            PropertyEditor valueEditor = getDefaultEditor(typeClass);
            valueEditor.setAsText(value);
            Object val = valueEditor.getValue();
            
            return val;
            
        }

        public Map<String, Object> getGlobals() {
            return m_globals;
        }

        public void setGlobals(Map<String, Object> globals) {
            m_globals = globals;
        }

        public List<String> getInterestingEvents() {
            return m_interestingEvents;
        }

        public void setInterestingEvents(List<String> interestingEvents) {
            m_interestingEvents = interestingEvents;
        }

        public List<Resource> getRuleResources() {
            return m_ruleResources;
        }

        public void setRuleResources(List<Resource> ruleResources) {
            m_ruleResources = ruleResources;
        }
    }

}
