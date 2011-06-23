/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.drools;

import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.xml.CastorUtils;
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

/**
 * <p>DroolsCorrelationEngineBuilder class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngineBuilder extends PropertyEditorRegistrySupport implements InitializingBean, ApplicationListener<ApplicationEvent> {
    // injected
    private Resource m_configResource;
    private EventIpcManager m_eventIpcManager;
    //private ResourceLoader m_resourceLoader;

    // built
    private List<RuleSetConfiguration> m_ruleSets;
    private CorrelationEngineRegistrar m_correlator;
    
    /**
     * <p>Constructor for DroolsCorrelationEngineBuilder.</p>
     */
    public DroolsCorrelationEngineBuilder() {
        registerDefaultEditors();
    }

    /**
     * <p>assertSet</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param name a {@link java.lang.String} object.
     */
    public void assertSet(final Object obj, final String name) {
        Assert.state(obj != null, name+" required for DroolsEngineFactoryBean");
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        assertSet(m_configResource, "configurationResource");
        assertSet(m_eventIpcManager, "eventIpcManager");
        assertSet(m_correlator, "correlator");
        
        
        //m_resourceLoader = new ConfigFileResourceLoader();
        
        readConfiguration();
    }

    private void registerEngines(final ApplicationContext appContext) {
        
        for (final RuleSetConfiguration ruleSet : m_ruleSets) {
            m_correlator.addCorrelationEngine(ruleSet.constructEngine(appContext));
        }
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventIpcManager(final EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    /**
     * <p>setConfigurationResource</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public void setConfigurationResource(final Resource configResource) {
        m_configResource = configResource;
    }
    
    /**
     * <p>setCorrelationEngineRegistrar</p>
     *
     * @param correlator a {@link org.opennms.netmgt.correlation.CorrelationEngineRegistrar} object.
     */
    public void setCorrelationEngineRegistrar(final CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }
    
    private void readConfiguration() throws Exception {
    	final EngineConfiguration configuration = CastorUtils.unmarshal(EngineConfiguration.class, m_configResource);

    	final List<RuleSetConfiguration> ruleSets = new LinkedList<RuleSetConfiguration>();
        for (final RuleSet ruleSet : configuration.getRuleSet()) {
            ruleSets.add(new RuleSetConfiguration(ruleSet));
        }

        m_ruleSets = ruleSets;
    }
    
    private static class ConfigFileApplicationContext extends AbstractXmlApplicationContext {
        
        private String m_configFileLocation;
        
        public ConfigFileApplicationContext(final String configFileLocation, final ApplicationContext parent) {
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
        protected Resource getResourceByPath(final String path) {
            return new FileSystemResource(path);
        }
        
    }
    
    public static class ResourceConfiguration {
        private String m_resourcePath;
        
        public ResourceConfiguration(final String resourcePath) {
            m_resourcePath = resourcePath;
        }
        
        public Resource getResource(final ResourceLoader loader) {
        	final String finalName = PropertiesUtils.substitute( m_resourcePath, System.getProperties() );
            return loader.getResource( finalName );

        }
    }
    
    public static class GlobalConfiguration extends PropertyEditorRegistrySupport {

        private final String m_name;
        private final String m_type;
        private final String m_value;
        private final String m_ref;
        
        public GlobalConfiguration(final Global global) {
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

        Object constructValue(final ApplicationContext context) {
            
        	final String type = getType();
        	Class<?> typeClass = Object.class;
            if (type != null) {
            	final PropertyEditor classEditor = getDefaultEditor(Class.class);
                classEditor.setAsText(type);
                typeClass = (Class<?>)classEditor.getValue();
            }
        
            final String value = getValue();
            if (value != null) {
            	final PropertyEditor valueEditor = getDefaultEditor(typeClass);
                valueEditor.setAsText(value);
                return valueEditor.getValue();
            }
            
            final String ref = getRef();
            if (ref != null) {
            	return context.getBean(ref, typeClass);
            }
            
            throw new IllegalArgumentException("One of either the value or the ref must be specified");
            
        }

    }

    public class RuleSetConfiguration {
        private final String m_name;
        private final String m_appContextLocation;
        private final List<String> m_interestingEvents;
        private final List<ResourceConfiguration> m_resourceConfigurations;
        private final List<GlobalConfiguration> m_globalConfig;
        
        public RuleSetConfiguration(final RuleSet ruleSet) {

            m_name = ruleSet.getName();
            m_interestingEvents = Arrays.asList(ruleSet.getEvent());
            
            m_resourceConfigurations = new LinkedList<ResourceConfiguration>();
            for(final String resourcePath : ruleSet.getRuleFile()) {
                m_resourceConfigurations.add( new ResourceConfiguration( resourcePath ) );
            }
            
            m_appContextLocation = ruleSet.getAppContext();
            
            m_globalConfig = new LinkedList<GlobalConfiguration>();
            for(final Global global : ruleSet.getGlobal()) {
                m_globalConfig.add(new GlobalConfiguration(global));
            }
            
            
        }

        public String getName() {
            return m_name;
        }

        public Map<String, Object> getGlobals(final ApplicationContext context) {
        	final Map<String, Object> globals = new HashMap<String, Object>();
            
            for (final GlobalConfiguration globalConfig : m_globalConfig) {
                globals.put(globalConfig.getName(), globalConfig.constructValue(context));
            }
            
            return globals;
        }

        public List<String> getInterestingEvents() {
            return m_interestingEvents;
        }

        public List<Resource> getRuleResources(final ResourceLoader resourceLoader) {
        	final List<Resource> resources = new LinkedList<Resource>();
            for (final ResourceConfiguration resConfig : m_resourceConfigurations) {
                resources.add(resConfig.getResource(resourceLoader));
                
            }
            return resources;
        }
        
        CorrelationEngine constructEngine(final ApplicationContext parent) {
        	final ApplicationContext configContext = new ConfigFileApplicationContext(getConfigLocation(), parent);
            
        	final DroolsCorrelationEngine engine = new DroolsCorrelationEngine();
            engine.setName(getName());
            engine.setEventIpcManager(m_eventIpcManager);
            engine.setScheduler(new Timer(getName()+"-Timer"));
            engine.setInterestingEvents(getInterestingEvents());
            engine.setRulesResources(getRuleResources(configContext));
            engine.setGlobals(getGlobals(configContext));
            try {
                engine.initialize();
                return engine;
            } catch (final Throwable e) {
                throw new RuntimeException("Unable to initialize Drools engine "+getName(), e);
            }
        }

        private String getConfigLocation() {
            return PropertiesUtils.substitute(m_appContextLocation, System.getProperties());
        }
    }

    /** {@inheritDoc} */
    public void onApplicationEvent(final ApplicationEvent appEvent) {
        if (appEvent instanceof ContextRefreshedEvent) {
            final ApplicationContext appContext = ((ContextRefreshedEvent)appEvent).getApplicationContext();
            if (!(appContext instanceof ConfigFileApplicationContext)) {
                registerEngines(appContext);
            }
        }
        
    }

}
