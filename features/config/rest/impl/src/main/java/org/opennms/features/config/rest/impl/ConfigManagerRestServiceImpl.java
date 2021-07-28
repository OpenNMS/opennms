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

package org.opennms.features.config.rest.impl;

import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.rest.api.ConfigManagerRestService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * <b>Currently for testing OSGI integration</b>
 */
public class ConfigManagerRestServiceImpl implements ConfigManagerRestService {

    @Autowired
    private ConfigStoreDao configStoreDao;

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    public void setConfigStoreDao(ConfigStoreDao configStoreDao) {
        this.configStoreDao = configStoreDao;
    }

    public void setConfigurationManagerService(ConfigurationManagerService configurationManagerService) {
        this.configurationManagerService = configurationManagerService;
    }

    @Override
    public Set<String> listServices() {
       return (Set<String>) configStoreDao.getServiceIds().get();
    }

    /**
     * get or create a fake schema and return
     * @param serviceName
     * @return
     */
    @Override
    public ConfigSchema getSchema(String serviceName){
        try{
            Optional<ConfigSchema> schema = configurationManagerService.getRegisteredSchema(serviceName);
            if(schema.isEmpty()){
                configurationManagerService.registerSchema(serviceName, 29,0,0,ProvisiondConfiguration.class);
                schema = configurationManagerService.getRegisteredSchema(serviceName);
            }
            return schema.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
//            return Response.serverError().build();
        }
    }

    @Override
    public ConfigData getConfigFile(String serviceName, String filename) {
        return null;
    }

    @Override
    public ConfigData getView(String serviceName, String filename, Map<String, Object> inputParameters) {
        return null;
    }
}
