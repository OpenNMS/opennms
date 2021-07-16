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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.features.config.dao.api.ConfigData;

import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.distributed.kvstore.pgshared.AbstractPostgresKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class ConfigStoreDaoImpl<T> extends AbstractPostgresKeyValueStore<String,String> implements ConfigStoreDao<T> {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigStoreDao.class);
	public static final String CONTEXT = "config";
	private final ObjectMapper mapper;
	public ConfigStoreDaoImpl(DataSource dataSource) {
		super(dataSource);
		mapper = new ObjectMapper();
	}

	@Override
	public boolean register(ConfigData configData) {
		try {
			long timestamp = this.put(configData.getName(), mapper.writeValueAsString(configData), CONTEXT);
			return timestamp > 0;
		} catch (IOException e) {
			LOG.error("FAIL TO REGISTER: " + e.getMessage());
			return false;
		}
	}

	private boolean put(ConfigData configData) {
		try {
			long timestamp = this.put(configData.getName(), mapper.writeValueAsString(configData), CONTEXT);
			return timestamp > 0;
		} catch (IOException e) {
			LOG.error("FAIL TO REGISTER: " + e.getMessage());
			return false;
		}
	}

	@Override
	public Optional<ConfigData<T>> getConfigData(String serviceName) {
		Optional<String> json = this.get(serviceName, CONTEXT);
		if(json.isEmpty()){
			return Optional.empty();
		}
		try {
			ConfigData d = mapper.readValue(json.get(), ConfigData.class);
			return Optional.of(d);
		} catch (IOException e) {
			LOG.error("FAIL TO CONVERT JSON TO ConfigData. " + e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public boolean addOrUpdateConfig(String serviceName, String filename, T config) {
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return false;
		}
		Map<String,T> configs = configData.get().getConfigs();
		if(configs == null){
			configs = new HashMap<String, T>();
			configData.get().setConfigs(configs);
		}
		configs.put(filename, config);
		return this.put(configData.get());
	}

	@Override
	public boolean updateConfigs(String serviceName, Map<String, T> configs) {
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return false;
		}
		configData.get().setConfigs(configs);
		return this.put(configData.get());
	}

	@Override
	public boolean deleteConfig(String serviceName, String filename) {
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
	public void deregister(String serviceName) {
		this.delete(serviceName,CONTEXT);
	}

	@Override
	public Optional<Map<String, T>> getConfigs(String serviceName) {
		Optional<ConfigData<T>> configData = this.getConfigData(serviceName);
		if(configData.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(configData.get().getConfigs());
	}

	@Override
	protected String getTableName() {
		return "kvstore_jsonb";
	}

	@Override
	protected String getValueStatementPlaceholder() {
		return super.getValueStatementPlaceholder() + "::JSON";
	}

	@Override
	protected String getValueTypeFromSQLType(ResultSet resultSet, String columnName) throws SQLException {
		return resultSet.getString(columnName);
	}

	@Override
	protected String getPkConstraintName() {
		return "pk_kvstore_jsonb";
	}
}
