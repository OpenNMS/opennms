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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;
import org.opennms.netmgt.correlation.drools.config.EngineConfiguration;
import org.opennms.netmgt.correlation.drools.config.RuleSet;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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
public class DroolsCorrelationEngineBuilder extends PropertyEditorRegistrySupport implements InitializingBean, EventListener {
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
			LOG.info("Creating drools engines for configuration {}.", m_configResource);

			return m_configuration.constructEngines(m_configResource, appContext, eventIpcManager, metricRegistry);
		}

		public List<RuleSet> getRuleSets() {
		    if (m_configuration == null) return Collections.emptyList();
		    return m_configuration.getRuleSetCollection();
		}

		public Resource getConfigResource() {
		    return m_configResource;
                }
	}
	
	// injected
	@javax.annotation.Resource(name="droolsCorrelationEngineBuilderConfigurationDirectory")
	private File m_configDirectory;
        @javax.annotation.Resource(name="droolsCorrelationEngineBuilderConfigurationResource")
    private Resource m_configResource;
        @Autowired
        @Qualifier("eventIpcManager")
    private EventIpcManager m_eventIpcManager;
        @Autowired
        @Qualifier("correlator")
    private CorrelationEngineRegistrar m_correlator;
        @Autowired
        @Qualifier("metricRegistry")
    private MetricRegistry m_metricRegistry;
        @Autowired
    private ApplicationContext m_appContext;

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
        assertSet(m_appContext, "applicationContext");
        
        Assert.state(!m_configDirectory.exists() || m_configDirectory.isDirectory(), m_configDirectory+" must be a directory!");
        
        m_eventIpcManager.addEventListener(this, EventConstants.RELOAD_DAEMON_CONFIG_UEI);
        m_eventIpcManager.addEventListener(this, EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI);
        readConfiguration();
        registerEngines();
    }

    private void registerEngines() {
    	for(PluginConfiguration pluginConfig : m_pluginConfigurations) {
    		m_correlator.addCorrelationEngines(pluginConfig.constructEngines(m_appContext, m_eventIpcManager, m_metricRegistry));
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
		List<PluginConfiguration> pluginConfigs = new LinkedList<>();
		
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


    @Override
    public String getName() {
        return "DroolsCorrelationEngine";
    }

    @Override
    public void onEvent(Event e) {
        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_UEI)) {
            final String daemonName = getDaemonNameFromReloadDaemonEvent(e);
            if (daemonName != null && daemonName.equals(getName())) {
                doAddAndRemoveEngines();
                return;
            }
        }
        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI)) {
            final String daemonName = getDaemonNameFromReloadDaemonEvent(e);
            Matcher m = Pattern.compile(getName() + "-(.+)$").matcher(daemonName);
            if (m.find()) {
                final String engineName = m.group(1);
                LOG.warn("An error was detected while reloading engine {}, this engine will be removed. Fix the error and try again.", engineName);
                m_correlator.removeCorrelationEngine(engineName);
                for (PluginConfiguration p : m_pluginConfigurations) {
                    final RuleSet set = p.getRuleSets().stream().filter(r -> r.getName().equals(engineName)).findFirst().orElse(null);
                    if (set != null) {
                        p.getRuleSets().remove(set);
                    }
                }
            }
        }
    }

    private String getDaemonNameFromReloadDaemonEvent(Event e) {
        List<Parm> parmCollection = e.getParmCollection();
        for (Parm parm : parmCollection) {
            String parmName = parm.getParmName();
            if (EventConstants.PARM_DAEMON_NAME.equals(parmName)) {
                if (parm.getValue() == null || parm.getValue().getContent() == null) {
                    LOG.warn("The daemonName parameter has no value, ignoring.");
                    return null;
                }
                return parm.getValue().getContent();
            }
        }
        return null;
    }

    // This handles only the addition of new engines and the removal of existing engines.
    // For updating existing engines, use the appropriate value for the daemonName
    private void doAddAndRemoveEngines() {
        LOG.info("Analyzing directory {} to add/remove drools engines...", m_configDirectory);
        EventBuilder ebldr = null;
        try {
            final PluginConfiguration[] newPlugins = locatePluginConfigurations();
            final List<RuleSet> newEngines = Arrays.stream(newPlugins).peek(PluginConfiguration::readConfig).flatMap(pc -> pc.getRuleSets().stream()).collect(Collectors.toList());
            final List<RuleSet> currentEngines = Arrays.stream(m_pluginConfigurations).flatMap(pc -> pc.getRuleSets().stream()).collect(Collectors.toList());
            LOG.debug("Current engines: {}", currentEngines);
            LOG.debug("New engines: {}", newEngines);
            // Find old engines to remove
            currentEngines.stream().filter(en -> !newEngines.contains(en)).forEach(en -> {
                LOG.warn("Deleting engine {}", en);
                m_correlator.removeCorrelationEngine(en.getName());
            });
            // Find new engines to add
            newEngines.stream().filter(en -> !currentEngines.contains(en)).forEach(en -> {
                LOG.warn("Adding engine {}", en);
                addEngine(newPlugins, en);
            });
            m_pluginConfigurations = newPlugins;
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
        } catch (Exception ex) {
            LOG.error("Cannot process reloadDaemonConfig", ex);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, getName());
            ebldr.addParam(EventConstants.PARM_REASON, ex.getMessage());
        } finally {
            if (ebldr != null)
                try {
                    m_eventIpcManager.send(ebldr.getEvent());
                } catch (EventProxyException epx) {
                    LOG.error("Can't send reloadDaemonConfig status event", epx);
                }
        }
    }

    private void addEngine(final PluginConfiguration[] newPlugins, RuleSet ruleSet) {
        Arrays.stream(newPlugins).filter(p -> p.getRuleSets().contains(ruleSet)).findFirst().ifPresent(p -> {
            try {
                LOG.debug("addEngine: adding engine {} using {}", ruleSet.getName(), p.getConfigResource());
                m_correlator.addCorrelationEngine(ruleSet.constructEngine(p.getConfigResource(), m_appContext, m_eventIpcManager, m_metricRegistry));
            } catch (Exception e) {
                p.getRuleSets().remove(ruleSet);
            }
        });
    }

}
