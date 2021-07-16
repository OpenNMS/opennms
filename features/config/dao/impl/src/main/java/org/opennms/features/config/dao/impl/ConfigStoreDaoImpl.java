/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.config.dao.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.features.config.dao.api.ConfigData;

import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigStoreDaoImpl<T> implements ConfigStoreDao<T> {
	//private static final Logger LOG = LoggerFactory.getLogger(ConfigStoreDao.class);
	public static final String CONTEXT = "config";
	private final ObjectMapper mapper;

	@Autowired
	private JsonStore jsonStore;

	public ConfigStoreDaoImpl() {
		mapper = new ObjectMapper();
	}

	@Override
	public boolean register(ConfigData configData) throws IOException{
		long timestamp = jsonStore.put(configData.getName(), mapper.writeValueAsString(configData), CONTEXT);
		return timestamp > 0;
	}

	@Override
	public Optional<ConfigData<T>> getConfigData(String serviceName) throws IOException{
		Optional<String> json = jsonStore.get(serviceName, CONTEXT);
		if(json.isEmpty()){
			return Optional.empty();
		}
		ConfigData d = mapper.readValue(json.get(), ConfigData.class);
		return Optional.of(d);
	}

	@Override
	public boolean addOrUpdateConfig(String serviceName, String filename, T config) throws IOException{
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return false;
		}
		Map<String,T> configs = configData.get().getConfigs();
		if(configs == null){
			configs = new HashMap<>();
			configData.get().setConfigs(configs);
		}
		configs.put(filename, config);
		return this.put(configData.get());
	}

	@Override
	public boolean updateConfigs(String serviceName, Map<String, T> configs) throws IOException{
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return false;
		}
		configData.get().setConfigs(configs);
		return this.put(configData.get());
	}

	@Override
	public boolean deleteConfig(String serviceName, String filename) throws IOException{
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return false;
		}
		if(configData.get().getConfigs().remove(filename) == null){
			return false;
		}
		return this.put(configData.get());
	}

	@Override
	public void deregister(String serviceName){
		jsonStore.delete(serviceName,CONTEXT);
	}

	@Override
	public Optional<Map<String, T>> getConfigs(String serviceName) throws IOException{
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(configData.get().getConfigs());
	}

	private boolean put(ConfigData configData) throws IOException{
		long timestamp = jsonStore.put(configData.getName(), mapper.writeValueAsString(configData), CONTEXT);
		return timestamp > 0;
	}
}
