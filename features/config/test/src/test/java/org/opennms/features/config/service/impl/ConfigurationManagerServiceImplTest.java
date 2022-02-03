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

package org.opennms.features.config.service.impl;

import static org.opennms.features.config.service.api.EventType.UPDATE;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.JaxbXmlConverter;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
    public void init() throws Exception {
        if (configManagerService.getRegisteredConfigDefinition(CONFIG_NAME).isEmpty()) {
            ConfigDefinition def = XsdHelper.buildConfigDefinition(CONFIG_NAME, "provisiond-configuration.xsd",
                    "provisiond-configuration", ConfigurationManagerService.BASE_PATH);
            configManagerService.registerConfigDefinition(CONFIG_NAME, def);
        }
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("provisiond-configuration.xml");
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        ConfigConverter converter = XsdHelper.getConverter(def.get());
        JsonAsString json = new JsonAsString(converter.xmlToJson(xmlStr));
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID, json);
    }

    @After
    public void after() throws IOException {
        configManagerService.unregisterSchema(CONFIG_NAME);
    }

    @Test
    public void testGetRegisterSchema() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        Assert.assertTrue(CONFIG_NAME + " fail to register", def.isPresent());
        Assert.assertTrue("Wrong converter", XsdHelper.getConverter(def.get()) instanceof JaxbXmlConverter);
    }

    @Test
    public void testRegisterExtraSchema() throws IOException {
        String VACUUMD_CONFIG_NAME = "vacuumd";
        ConfigDefinition def = XsdHelper.buildConfigDefinition(VACUUMD_CONFIG_NAME, "vacuumd-configuration.xsd",
                "VacuumdConfiguration", ConfigurationManagerService.BASE_PATH);
        configManagerService.registerConfigDefinition(VACUUMD_CONFIG_NAME, def);
        Optional<ConfigDefinition> configSchema = configManagerService.getRegisteredConfigDefinition(VACUUMD_CONFIG_NAME);
        Assert.assertTrue(VACUUMD_CONFIG_NAME + " fail to register", configSchema.isPresent());

        Map<String, ConfigDefinition> map = configManagerService.getAllConfigDefinitions();
        Assert.assertArrayEquals("It should contain 2 schemas.", new String[]{CONFIG_NAME, VACUUMD_CONFIG_NAME}, map.keySet().toArray());
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
     *
     * @throws IOException
     */
    @Test(expected = RuntimeException.class)
    public void testRegisterInvalidConfiguration() throws Exception {
        ProvisiondConfiguration config = new ProvisiondConfiguration();
        config.setImportThreads(-1L);
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        ConfigConverter converter = XsdHelper.getConverter(def.get());
        JsonAsString json = new JsonAsString(converter.xmlToJson(ConfigConvertUtil.objectToJson(config)));
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID + "_2", json);
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config should not store", configData.get().getConfigs().size() == 1);
    }

    @Test
    public void testUpdateConfiguration() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        ConfigConverter converter = XsdHelper.getConverter(def.get());

        ProvisiondConfiguration pConfig = ConfigConvertUtil.jsonToObject(
                configManagerService.getJSONStrConfiguration(CONFIG_NAME, CONFIG_ID).get(), ProvisiondConfiguration.class);

        pConfig.setImportThreads(12L);
        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID,
                new JsonAsString(ConfigConvertUtil.objectToJson(pConfig)), false);
        Optional<JSONObject> jsonAfterUpdate = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID);
        Assert.assertEquals("Incorrect importThreads", 12, jsonAfterUpdate.get().get("importThreads"));

        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID,
                new JsonAsString("{\"rescanThreads\": 7}"), true);
        Optional<JSONObject> jsonAfterReplace = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID);
        Assert.assertEquals("Incorrect rescanThreads", 7, jsonAfterReplace.get().get("rescanThreads"));
        Assert.assertEquals("importThreads should be default", 8, jsonAfterReplace.get().get("importThreads"));
    }

    private class TestCallback implements Consumer<ConfigUpdateInfo> {
        @Override
        public void accept(ConfigUpdateInfo info) {
        }
    }

    @Test
    public void testRegisterNewCallback() throws IOException {
        TestCallback callback = Mockito.mock(TestCallback.class);
        JSONObject json = configManagerService
                .getJSONConfiguration(CONFIG_NAME, CONFIG_ID).get();
        configManagerService.registerEventHandler(UPDATE, new ConfigUpdateInfo(CONFIG_NAME, CONFIG_ID), callback);
        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID, new JsonAsString(json.toString()), false);
        Mockito.verify(callback, Mockito.atLeastOnce()).accept(Mockito.any());
    }

    /**
     * it is expected to have exception due to not xsd validation. importThreads > 0
     *
     * @throws IOException
     */
    @Test(expected = RuntimeException.class)
    public void testUpdateInvalidateConfiguration() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        ConfigConverter converter = XsdHelper.getConverter(def.get());
        ProvisiondConfiguration config = ConfigConvertUtil.jsonToObject(
                configManagerService.getJSONStrConfiguration(CONFIG_NAME, CONFIG_ID).get(), ProvisiondConfiguration.class);
        config.setImportThreads(-1L);

        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID,
                new JsonAsString(ConfigConvertUtil.objectToJson(config)), false);
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config not found", configData.isPresent());
    }

    @Test
    public void testRemoveEverything() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        ConfigConverter converter = XsdHelper.getConverter(def.get());
        //below extra configuration added, since it is not allowed to delete last configuration
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID + "_1", new JsonAsString("{}"));

        configManagerService.unregisterConfiguration(CONFIG_NAME, CONFIG_ID);
        Optional<JSONObject> json = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID);
        Assert.assertTrue("Fail to unregister config", json.isEmpty());
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID,
                new JsonAsString(ConfigConvertUtil.objectToJson(new ProvisiondConfiguration())));
        configManagerService.unregisterSchema(CONFIG_NAME);
        Optional<ConfigDefinition> schemaAfterDeregister = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        Optional<ConfigData<JSONObject>> configAfterDeregister = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }
}
