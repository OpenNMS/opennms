/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.drools;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.correlation.drools.config.EngineConfiguration;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.codahale.metrics.MetricRegistry;

/**
 * <p>DroolsCorrelationEngineBuilder class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngineBuilder extends PropertyEditorRegistrySupport implements InitializingBean, ApplicationListener<ApplicationEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsCorrelationEngineBuilder.class);

	public static final String PLUGIN_CONFIG_FILE_NAME = "drools-engine.xml";
	
	private static class PluginConfiguration {
		private Resource m_configResource;
		private EngineConfiguration m_configuration;
		
		public PluginConfiguration(Resource configResource) {
			m_configResource = configResource;
		}
		
		public void readConfig() {
			LOG.info("Parsing drools engine configuration at {}.", m_configResource);
			m_configuration = JaxbUtils.unmarshal(EngineConfiguration.class, m_configResource);
		}

		public CorrelationEngine[] constructEngines(ApplicationContext appContext, EventIpcManager eventIpcManager, MetricRegistry metricRegistry) {
			LOG.info("Creating drools engins for configuration {}.", m_configResource);

			return m_configuration.constructEngines(m_configResource, appContext, eventIpcManager, metricRegistry);
		}

	}
	
	// injected
	private File m_configDirectory;
    private Resource m_configResource;
    private EventIpcManager m_eventIpcManager;
    private CorrelationEngineRegistrar m_correlator;
    private MetricRegistry m_metricRegistry;

    // built
    private PluginConfiguration[] m_pluginConfigurations;

    
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
    @Override
    public void afterPropertiesSet() throws Exception {
        assertSet(m_configDirectory, "configurationDirectory");
        assertSet(m_eventIpcManager, "eventIpcManager");
        assertSet(m_correlator, "correlator");
        assertSet(m_metricRegistry, "metricRegistry");
        
        Assert.state(!m_configDirectory.exists() || m_configDirectory.isDirectory(), m_configDirectory+" must be a directory!");
        
        readConfiguration();
    }

    private void registerEngines(final ApplicationContext appContext) {
    	for(PluginConfiguration pluginConfig : m_pluginConfigurations) {
    		m_correlator.addCorrelationEngines(pluginConfig.constructEngines(appContext, m_eventIpcManager, m_metricRegistry));
    	}

    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventIpcManager(final EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    /**
     * <p>setMetricRegistry</p>
     *
     * @param metricRegistry a {@link com.codahale.metrics.MetricRegistry} object.
     */
    public void setMetricRegistry(final MetricRegistry metricRegistry) {
        m_metricRegistry = metricRegistry;
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
     * <p>setConfigurationDirectory</p>
     * 
     * @param configDirectory a {@link java.io.File} object.
     */
    public void setConfigurationDirectory(final File configDirectory) {
        m_configDirectory = configDirectory;
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
    	m_pluginConfigurations = locatePluginConfigurations();
    	
    	// now parse all of the configuration files
    	for(PluginConfiguration pluginCofig : m_pluginConfigurations) {
    		pluginCofig.readConfig();
    	}
    	
    }

	private PluginConfiguration[] locatePluginConfigurations() throws Exception {
		List<PluginConfiguration> pluginConfigs = new LinkedList<PluginConfiguration>();
		
		// first we see if the config is etc exists 
    	if (m_configResource != null && m_configResource.isReadable()) {
			LOG.info("Found Drools Plugin config file {}.", m_configResource);
    		pluginConfigs.add(new PluginConfiguration(m_configResource));
    	}

    	// then we look in each plugin dir for a config
    	File[] pluginDirs = getPluginDirs();
    	
    	for(File pluginDir : pluginDirs) {
    		File configFile = new File(pluginDir, PLUGIN_CONFIG_FILE_NAME);
    		if (!configFile.exists()) {
			LOG.error("Drools Plugin directory {} does not contains a {} config file.  Ignoring plugin.", pluginDir, PLUGIN_CONFIG_FILE_NAME);
    		} else {
			LOG.info("Found Drools Plugin directory {} containing a {} config file.", pluginDir, PLUGIN_CONFIG_FILE_NAME);
    			pluginConfigs.add(new PluginConfiguration(new FileSystemResource(configFile)));
    		}
    	}
    	
    	return pluginConfigs.toArray(new PluginConfiguration[0]);
	}

	private File[] getPluginDirs() throws Exception {

	LOG.debug("Checking {} for drools correlation plugins", m_configDirectory);
    	

		if (!m_configDirectory.exists()) {
			LOG.debug("Plugin configuration directory does not exists.");
			return new File[0];
		}

		File[] pluginDirs = m_configDirectory.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
	LOG.debug("Found {} drools correlation plugin sub directories", pluginDirs.length);
    	
		return pluginDirs;
	}

	/** {@inheritDoc} */
        @Override
    public void onApplicationEvent(final ApplicationEvent appEvent) {
        if (appEvent instanceof ContextRefreshedEvent) {
            final ApplicationContext appContext = ((ContextRefreshedEvent)appEvent).getApplicationContext();
            if (!(appContext instanceof ConfigFileApplicationContext)) {
                registerEngines(appContext);
            }
        }
        
    }

}
