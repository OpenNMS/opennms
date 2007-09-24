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
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.correlation.drools.config.EngineConfiguration;
import org.opennms.netmgt.correlation.drools.config.Global;
import org.opennms.netmgt.correlation.drools.config.RuleSet;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

public class DroolsCorrelationEngineBuilder extends PropertyEditorRegistrySupport implements InitializingBean, ApplicationListener {
    // injected
    private Resource m_configResource;
    private EventIpcManager m_eventIpcManager;
    //private ResourceLoader m_resourceLoader;

    // built
    private List<RuleSetConfiguration> m_ruleSets;
    private CorrelationEngineRegistrar m_correlator;
    
    public DroolsCorrelationEngineBuilder() {
        registerDefaultEditors();
    }

    public void assertSet(Object obj, String name) {
        Assert.state(obj != null, name+" required for DroolsEngineFactoryBean");
    }
    
    public void afterPropertiesSet() throws Exception {
        assertSet(m_configResource, "configurationResource");
        assertSet(m_eventIpcManager, "eventIpcManager");
        assertSet(m_correlator, "correlator");
        
        
        //m_resourceLoader = new ConfigFileResourceLoader();
        
        readConfiguration();
    }

    private void registerEngines(ApplicationContext appContext) {
        
        for (RuleSetConfiguration ruleSet : m_ruleSets) {
            m_correlator.addCorrelationEngine(ruleSet.constructEngine(appContext));
        }
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public void setConfigurationResource(Resource configResource) {
        m_configResource = configResource;
    }
    
    public void setCorrelationEngineRegistrar(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
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
    
    private static class ConfigFileApplicationContext extends AbstractXmlApplicationContext {
        
        private String m_configFileLocation;
        
        public ConfigFileApplicationContext(String configFileLocation, ApplicationContext parent) {
            super(parent);
            m_configFileLocation = configFileLocation;
            refresh();
        }
        
        @Override
        protected String[] getConfigLocations() {
            if ( m_configFileLocation == null ) {
                return null;
            }
            return new String[] { m_configFileLocation };
        }

        @Override
        protected Resource getResourceByPath(String path) {
            return new FileSystemResource(path);
        }
        
    }
    
    public static class ResourceConfiguration {
        private String m_resourcePath;
        
        public ResourceConfiguration(String resourcePath) {
            m_resourcePath = resourcePath;
        }
        
        public Resource getResource(ResourceLoader loader) {
            String finalName = PropertiesUtils.substitute( m_resourcePath, System.getProperties() );
            return loader.getResource( finalName );

        }
    }
    
    public static class GlobalConfiguration extends PropertyEditorRegistrySupport {

        private String m_name;
        private String m_type;
        private String m_value;
        private String m_ref;
        
        public GlobalConfiguration(Global global) {
            registerDefaultEditors();
            m_name = global.getName();
            m_type = global.getType();
            m_value = global.getValue();
            m_ref = global.getRef();
        }

        public String getName() {
            return m_name;
        }

        public String getRef() {
            return m_ref;
        }

        public String getType() {
            return m_type;
        }

        public String getValue() {
            return m_value;
        }

        Object constructValue(ApplicationContext context) {
            
            String type = getType();
            Class<?> typeClass = Object.class;
            if (type != null) {
                PropertyEditor classEditor = getDefaultEditor(Class.class);
                classEditor.setAsText(type);
                typeClass = (Class<?>)classEditor.getValue();
            }
        
            String value = getValue();
            if (value != null) {
                PropertyEditor valueEditor = getDefaultEditor(typeClass);
                valueEditor.setAsText(value);
                return valueEditor.getValue();
            }
            
            String ref = getRef();
            if (ref != null) {
                Object bean = context.getBean(ref, typeClass);
                return bean;
            }
            
            throw new IllegalArgumentException("One of either the value or the ref must be specified");
            
        }

    }

    public class RuleSetConfiguration {
        private String m_name;
        private String m_appContextLocation;
        private List<String> m_interestingEvents;
        private List<ResourceConfiguration> m_resourceConfigurations;
        private List<GlobalConfiguration> m_globalConfig;
        
        public RuleSetConfiguration(RuleSet ruleSet) {

            m_name = ruleSet.getName();
            m_interestingEvents = Arrays.asList(ruleSet.getEvent());
            
            m_resourceConfigurations = new LinkedList<ResourceConfiguration>();
            for(String resourcePath : ruleSet.getRuleFile()) {
                m_resourceConfigurations.add( new ResourceConfiguration( resourcePath ) );
            }
            
            m_appContextLocation = ruleSet.getAppContext();
            
            m_globalConfig = new LinkedList<GlobalConfiguration>();
            for(Global global : ruleSet.getGlobal()) {
                m_globalConfig.add(new GlobalConfiguration(global));
            }
            
            
        }

        public String getName() {
            return m_name;
        }

        public Map<String, Object> getGlobals(ApplicationContext context) {
            Map<String, Object> globals = new HashMap<String, Object>();
            
            for (GlobalConfiguration globalConfig : m_globalConfig) {
                globals.put(globalConfig.getName(), globalConfig.constructValue(context));
            }
            
            return globals;
        }

        public List<String> getInterestingEvents() {
            return m_interestingEvents;
        }

        public List<Resource> getRuleResources(ResourceLoader resourceLoader) {
            List<Resource> resources = new LinkedList<Resource>();
            for (ResourceConfiguration resConfig : m_resourceConfigurations) {
                resources.add(resConfig.getResource(resourceLoader));
                
            }
            return resources;
        }
        CorrelationEngine constructEngine(ApplicationContext parent) {
            ApplicationContext configContext = new ConfigFileApplicationContext(getConfigLocation(), parent);
            
            
            DroolsCorrelationEngine engine = new DroolsCorrelationEngine();
            engine.setName(getName());
            engine.setEventIpcManager(m_eventIpcManager);
            engine.setScheduler(new Timer(getName()+"-Timer"));
            engine.setInterestingEvents(getInterestingEvents());
            engine.setRulesResources(getRuleResources(configContext));
            engine.setGlobals(getGlobals(configContext));
            try {
                engine.initialize();
                return engine;
            } catch (Exception e) {
                throw new RuntimeException("Unable to initialize Drools engine "+getName(), e);
            }
        }

        private String getConfigLocation() {
            return PropertiesUtils.substitute(m_appContextLocation, System.getProperties());
        }
    }

    public void onApplicationEvent(ApplicationEvent appEvent) {
        if (appEvent instanceof ContextRefreshedEvent) {
            ApplicationContext appContext = ((ContextRefreshedEvent)appEvent).getApplicationContext();
            if (!(appContext instanceof ConfigFileApplicationContext)) {
                registerEngines(appContext);
            }
        }
        
    }

}
