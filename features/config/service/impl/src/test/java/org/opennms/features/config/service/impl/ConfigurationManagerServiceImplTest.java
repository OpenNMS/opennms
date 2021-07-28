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

package org.opennms.features.config.service.impl;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ValidateUsingConverter;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
@FixMethodOrder(MethodSorters.JVM)
public class ConfigurationManagerServiceImplTest {
    private static final String SERVICE_NAME = "provisiond";
    private static final String CONFIG_ID = "test1";
    @Autowired
    private ConfigurationManagerService<JSONObject> configManagerService;

    @Test
    public void testRegisterSchema() throws IOException, ClassNotFoundException {
        configManagerService.registerSchema(SERVICE_NAME, 29, 0, 0, ProvisiondConfiguration.class);
        Optional<ConfigSchema<?>> configSchema = configManagerService.getRegisteredSchema(SERVICE_NAME);
        Assert.assertTrue(SERVICE_NAME + " fail to register", configSchema.isPresent());
        Assert.assertTrue(SERVICE_NAME + " fail to register", "29.0.0".equals(configSchema.get().getVersion()));
        Assert.assertTrue("Wrong converter", configSchema.get().getConverter() instanceof ValidateUsingConverter);
    }

    @Test
    public void testRegisterConfiguration() throws IOException, ClassNotFoundException {
        Optional<ConfigSchema<?>> configSchema = configManagerService.getRegisteredSchema(SERVICE_NAME);
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("provisiond-configuration.xml");
        configManagerService.registerConfiguration(SERVICE_NAME, CONFIG_ID, xmlPath.getPath());
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(SERVICE_NAME);
        Assert.assertTrue("Config not found", configData.isPresent());
        Assert.assertEquals("Incorrect importThreads", 11,
                configData.get().getConfigs().get(CONFIG_ID).get("importThreads"));
    }

    @Test
    public void testUpdateConfiguration() throws IOException, ClassNotFoundException {
        JSONObject json = configManagerService.getConfiguration(SERVICE_NAME, CONFIG_ID).get();
        json.put("importThreads", 12);
        configManagerService.updateConfiguration(SERVICE_NAME, CONFIG_ID, json);
        JSONObject jsonAfterUpdate = configManagerService.getConfiguration(SERVICE_NAME, CONFIG_ID).get();
        Assert.assertEquals("Incorrect importThreads", 12, jsonAfterUpdate.get("importThreads"));
    }

    @Test
    public void testRemoveConfiguration() throws IOException, ClassNotFoundException {
        configManagerService.unregisterConfiguration(SERVICE_NAME, CONFIG_ID);
        Optional<JSONObject> json = configManagerService.getConfiguration(SERVICE_NAME, CONFIG_ID);
        Assert.assertTrue("Fail to unregister config", json.isEmpty());
        configManagerService.registerConfiguration(SERVICE_NAME, CONFIG_ID, new JSONObject());
        configManagerService.unregisterSchema(SERVICE_NAME);
        Optional<ConfigSchema<?>> schemaAfterDeregister = configManagerService.getRegisteredSchema(SERVICE_NAME);
        Optional<ConfigData<JSONObject>> configAfterDeregister = configManagerService.getConfigData(SERVICE_NAME);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }
}
