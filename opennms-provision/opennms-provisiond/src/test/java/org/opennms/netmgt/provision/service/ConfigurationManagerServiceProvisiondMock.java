/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.quartz.CronExpression;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;

public class ConfigurationManagerServiceProvisiondMock implements ConfigurationManagerService {
    @Override
    public <ENTITY> void registerSchema(String configName, int majorVersion, int minorVersion, int patchVersion, Class<ENTITY> entityClass) throws IOException, JAXBException {

    }

    @Override
    public <ENTITY> void registerSchema(String configName, Version version, Class<ENTITY> entityClass) throws IOException, JAXBException {

    }

    @Override
    public void registerSchema(String configName, int majorVersion, int minorVersion, int patchVersion, ConfigConverter converter) throws IOException {

    }

    @Override
    public Optional<ConfigSchema<?>> getRegisteredSchema(String configName) throws IOException {
        return Optional.empty();
    }

    @Override
    public void registerConfiguration(String configName, String configId, Object configObject) throws IOException {

    }

    @Override
    public void unregisterConfiguration(String configName, String configId) throws IOException {

    }

    @Override
    public void updateConfiguration(String configName, String configId, Object configObject) throws IOException {

    }

    @Override
    public <ENTITY> Optional<ENTITY> getConfiguration(String configName, String configId, Class<ENTITY> entityClass) throws IOException {
        if (entityClass.equals(ProvisiondConfiguration.class)) {
            Optional<ProvisiondConfiguration> entity = Optional.of(new ProvisiondConfiguration());
            RequisitionDef requisitionDef =  new RequisitionDef();
            requisitionDef.setImportName("test");
            requisitionDef.setCronSchedule("1 * * * * *");
            entity.get().addRequisitionDef(requisitionDef);
            return (Optional<ENTITY>) entity;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(String configName, String configId) throws IOException {
        return Optional.empty();
    }

    @Override
    public String getJSONStrConfiguration(String configName, String configId) throws IOException {
        return null;
    }

    @Override
    public Optional<String> getXmlConfiguration(String configName, String configId) throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) throws IOException {
        return Optional.empty();
    }

    @Override
    public Set<String> getConfigNames() throws IOException {
        return null;
    }

    @Override
    public void unregisterSchema(String configName) throws IOException {

    }

    @Override
    public Set<String> getConfigIds(String configName) throws IOException {
        return null;
    }
}
