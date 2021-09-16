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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.impl.util.ValidateUsingConverter;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ConfigurationManagerServiceImplTest {
    private static final String CONFIG_NAME = "provisiond";
    private static final String CONFIG_ID = "test1";
    @Autowired
    private ConfigurationManagerService configManagerService;

    @Before
    public void init() throws IOException, JAXBException {
        if(configManagerService.getRegisteredSchema(CONFIG_NAME).isEmpty()) {
            configManagerService.registerSchema(CONFIG_NAME, 29, 0, 0, ProvisiondConfiguration.class);
        }
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("provisiond-configuration.xml");
        Optional<ConfigSchema<?>> configSchema = configManagerService.getRegisteredSchema(CONFIG_NAME);
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        Object configObject = configSchema.get().getConverter().xmlToJaxbObject(xmlStr);
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID, configObject);
    }

    @After
    public void after() throws IOException{
        configManagerService.unregisterSchema(CONFIG_NAME);
    }

    @Test
    public void testRegisterSchema() throws IOException {
        Optional<ConfigSchema<?>> configSchema = configManagerService.getRegisteredSchema(CONFIG_NAME);
        Assert.assertTrue(CONFIG_NAME + " fail to register", configSchema.isPresent());
        Assert.assertTrue(CONFIG_NAME + " fail to register", "29.0.0".equals(configSchema.get().getVersion()));
        Assert.assertTrue("Wrong converter", configSchema.get().getConverter() instanceof ValidateUsingConverter);
    }

    @Test
    public void testRegisterConfiguration() throws IOException {
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config not found", configData.isPresent());
        Assert.assertEquals("Incorrect importThreads", 11,
                configData.get().getConfigs().get(CONFIG_ID).get("importThreads"));
    }

    /**
     * it is expected to have exception due to not xsd validation. importThreads > 0
     * @throws IOException
     */
    @Test(expected = RuntimeException.class)
    public void testRegisterInvalidConfiguration() throws IOException {
        ProvisiondConfiguration config = new ProvisiondConfiguration();
        config.setImportThreads(-1L);
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID + "_2", config);
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config should not store", configData.get().getConfigs().size() == 1);
    }

    @Test
    public void testUpdateConfiguration() throws IOException {
        ProvisiondConfiguration pConfig = configManagerService.getConfiguration(CONFIG_NAME, CONFIG_ID, ProvisiondConfiguration.class).get();
        pConfig.setImportThreads(12L);
        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID, pConfig);
        Optional<JSONObject> jsonAfterUpdate = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID);
        Assert.assertEquals("Incorrect importThreads", 12, jsonAfterUpdate.get().get("importThreads"));
    }

    /**
     * it is expected to have exception due to not xsd validation. importThreads > 0
     * @throws IOException
     */
    @Test(expected = RuntimeException.class)
    public void testUpdateInvalidateConfiguration() throws IOException {
        ProvisiondConfiguration config = configManagerService.getConfiguration(CONFIG_NAME, CONFIG_ID, ProvisiondConfiguration.class).get();
        config.setImportThreads(-1L);
        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID, config);
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config not found", configData.isPresent());
    }

    @Test
    public void testRemoveEverything() throws IOException {
        configManagerService.unregisterConfiguration(CONFIG_NAME, CONFIG_ID);
        Optional<JSONObject> json = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID);
        Assert.assertTrue("Fail to unregister config", json.isEmpty());
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID, new ProvisiondConfiguration());
        configManagerService.unregisterSchema(CONFIG_NAME);
        Optional<ConfigSchema<?>> schemaAfterDeregister = configManagerService.getRegisteredSchema(CONFIG_NAME);
        Optional<ConfigData<JSONObject>> configAfterDeregister = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }
}
