/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset;


import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Persists Asset Graph Definitions using the OSGi configuration admin service
 * @author admin
 *
 */
public class AssetGraphDefinitionRepositoryImpl implements AssetGraphDefinitionRepository {

	public static final String DEFINITION_PREFIX="asset.graph.definition.";

	//<reference id="configurationAdmin" interface="org.osgi.service.cm.ConfigurationAdmin"/> 
	private ConfigurationAdmin configurationAdmin=null;

	// name of config  file <persistentId>.cfg
	private String persistentId="org.opennms.features.topology.plugins.topo.asset";

	public ConfigurationAdmin getConfigurationAdmin() {
		return configurationAdmin;
	}

	public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
		this.configurationAdmin = configurationAdmin;
	}

	public String getPersistentId() {
		return persistentId;
	}

	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	/* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository#getConfigDefinition(java.lang.String)
	 */
	@Override
	public GeneratorConfig getConfigDefinition(String providerId){
		String propId=DEFINITION_PREFIX+providerId;
		GeneratorConfig generatorConfig=null;
		Configuration config;
		try {
			config = configurationAdmin.getConfiguration(persistentId);
			Dictionary<String, Object> props = config.getProperties();

			// if null, there is no configuration
			if (props == null) return null;

			String graphDefinitionUri = (String) props.get(propId);
			if(graphDefinitionUri == null) return null;

			generatorConfig = new GeneratorConfigBuilder()
			.withGraphDefinitionUri(graphDefinitionUri)
			.build();

		} catch (Exception e) {
			throw new RuntimeException("problem loading graph definition "+propId+ " from "+persistentId+".cfg",e);
		}
		return generatorConfig;
	}

	/* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository#exists()
	 */
	@Override
	public boolean exists(String providerId){
		String propId=DEFINITION_PREFIX+providerId;
		Configuration config;
		try {
			config = configurationAdmin.getConfiguration(persistentId);
			Dictionary<String, Object> props = config.getProperties();

			// if null, there is no configuration
			if (props == null) return false;

			String graphDefinitionUri = (String) props.get(propId);
			if(graphDefinitionUri == null) return false;

			return true;

		} catch (Exception e) {
			throw new RuntimeException("problem checking if definition exists  "+propId+ " from "+persistentId+".cfg",e);
		}

	}

	/* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository#getAllConfigDefinitions()
	 */
	@Override
	public Map<String,GeneratorConfig> getAllConfigDefinitions(){
		Map<String,GeneratorConfig> generatorConfigs = new LinkedHashMap<String,GeneratorConfig>();

		try {
			Configuration config = configurationAdmin.getConfiguration(persistentId);
			Dictionary<String, Object> props = config.getProperties();

			// if null, there are no configurations
			if (props == null) return generatorConfigs;

			Enumeration<String> keys = props.keys();
			while(keys.hasMoreElements()){
				String propId = keys.nextElement();
				if(propId.startsWith(DEFINITION_PREFIX)){
					String providerId=propId.replace(DEFINITION_PREFIX,"");
					String graphDefinitionUri = (String) props.get(propId);
					GeneratorConfig generatorConfig = new GeneratorConfigBuilder()
					.withGraphDefinitionUri(graphDefinitionUri)
					.build();
					generatorConfigs.put(providerId, generatorConfig);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("problem loading all graph definitions from "+persistentId+".cfg",e);
		}
		return generatorConfigs;
	}

	/* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository#removeConfigDefinition(java.lang.String)
	 */
	@Override
	public void removeConfigDefinition(String providerId){
		String propId=DEFINITION_PREFIX+providerId;

		Configuration config;
		try {
			config = configurationAdmin.getConfiguration(persistentId);
			Dictionary<String, Object> props = config.getProperties();

			// if null, there is no configuration
			if (props == null) return;

			String graphDefinitionUri = (String) props.get(propId);
			if(graphDefinitionUri == null) return;

			props.remove(propId);
			config.update(props);

		} catch (Exception e) {
			throw new RuntimeException("problem removing graph definition "+propId+ " from "+persistentId+".cfg",e);
		}
	}

	/* (non-Javadoc)
	 * @see org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository#addConfigDefinition(org.opennms.features.topology.plugins.topo.asset.GeneratorConfig)
	 */
	@Override
	public void addConfigDefinition(GeneratorConfig generatorConfig){

		String propId=DEFINITION_PREFIX+generatorConfig.getProviderId();

		Configuration config;
		try {
			config = configurationAdmin.getConfiguration(persistentId);
			Dictionary<String, Object> props = config.getProperties();

			// if null, the configuration is new
			if (props == null) {
				props = new Hashtable<String, Object>();
			}

			String graphDefinitionUri = (String) props.get(propId);
			if(graphDefinitionUri != null) throw new IllegalArgumentException("A configuration for providerId "
					+generatorConfig.getProviderId() +" already exists");

			String graphDefinitionUriString = GeneratorConfigBuilder.toGraphDefinitionUriString(generatorConfig);

			props.put(propId,graphDefinitionUriString);
			config.update(props);

		} catch (Exception e) {
			throw new RuntimeException("problem adding graph definition "+propId+ " to "+persistentId+".cfg",e);
		}
	}

}
