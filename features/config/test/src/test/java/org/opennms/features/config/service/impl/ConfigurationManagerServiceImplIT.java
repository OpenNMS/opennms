/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.opennms.features.config.exception.ConfigRuntimeException;
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

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ConfigurationManagerServiceImplIT {
    private static final String CONFIG_NAME = "ConfigurationManagerServiceImplIT";
    private static final String CONFIG_NAME_MULTIPLE = "ConfigurationManagerServiceImplIT_multiple";
    private static final String CONFIG_ID_OK = DEFAULT_CONFIG_ID;
    private static final String CONFIG_ID_NOT_OK_FOR_NON_MULTIPLE = "someId";

    @Autowired
    private ConfigurationManagerService configManagerService;

    @Before
    public void init() throws Exception {
        ConfigDefinition def = XsdHelper.buildConfigDefinition(CONFIG_NAME, "provisiond-configuration.xsd",
                "provisiond-configuration", ConfigurationManagerService.BASE_PATH, false);

        ConfigDefinition def_M = XsdHelper.buildConfigDefinition(CONFIG_NAME_MULTIPLE, "provisiond-configuration.xsd",
                "provisiond-configuration", ConfigurationManagerService.BASE_PATH, true);

        configManagerService.registerConfigDefinition(CONFIG_NAME, def);
        configManagerService.registerConfigDefinition(CONFIG_NAME_MULTIPLE, def_M);

        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("provisiond-configuration.xml");
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        ConfigConverter converter = XsdHelper.getConverter(def);
        JsonAsString json = new JsonAsString(converter.xmlToJson(xmlStr));
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID_OK, json);
        configManagerService.registerConfiguration(CONFIG_NAME_MULTIPLE, CONFIG_ID_OK, json);
    }

    @After
    public void after() {
        configManagerService.unregisterSchema(CONFIG_NAME);
        configManagerService.unregisterSchema(CONFIG_NAME_MULTIPLE);
    }

    @Test
    public void testGetRegisterSchema() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        Assert.assertTrue(CONFIG_NAME + " fail to register", def.isPresent());
        Assert.assertTrue("Wrong converter", XsdHelper.getConverter(def.get()) instanceof JaxbXmlConverter);
    }

    @Test
    public void testRegisterExtraSchema() throws IOException {
        Assert.assertEquals("There must be 2 shemas initialy.", 2, configManagerService.getAllConfigDefinitions().size());
        String VACUUMD_CONFIG_NAME = "vacuumd";
        ConfigDefinition def = XsdHelper.buildConfigDefinition(VACUUMD_CONFIG_NAME, "vacuumd-configuration.xsd",
                "VacuumdConfiguration", ConfigurationManagerService.BASE_PATH, false);
        configManagerService.registerConfigDefinition(VACUUMD_CONFIG_NAME, def);
        Optional<ConfigDefinition> configSchema = configManagerService.getRegisteredConfigDefinition(VACUUMD_CONFIG_NAME);
        Assert.assertTrue(VACUUMD_CONFIG_NAME + " fail to register", configSchema.isPresent());

        Map<String, ConfigDefinition> map = configManagerService.getAllConfigDefinitions();
        Assert.assertTrue("Contains schema "+ CONFIG_NAME, map.keySet().contains(CONFIG_NAME));
        Assert.assertTrue("Contains schema "+ CONFIG_NAME_MULTIPLE, map.keySet().contains(CONFIG_NAME_MULTIPLE));
        Assert.assertTrue("Contains schema "+ VACUUMD_CONFIG_NAME, map.keySet().contains(VACUUMD_CONFIG_NAME));
    }

    @Test
    public void testRegisterConfiguration() throws IOException {
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config not found", configData.isPresent());
        Assert.assertEquals("Incorrect importThreads", 11,
                configData.get().getConfigs().get(CONFIG_ID_OK).get("importThreads"));
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
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID_OK + "_2", json);
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config should not store", configData.get().getConfigs().size() == 1);
    }

    @Test
    public void testUpdateConfiguration() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME);
        ConfigConverter converter = XsdHelper.getConverter(def.get());

        ProvisiondConfiguration pConfig = ConfigConvertUtil.jsonToObject(
                configManagerService.getJSONStrConfiguration(CONFIG_NAME, CONFIG_ID_OK).get(), ProvisiondConfiguration.class);

        pConfig.setImportThreads(12L);
        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID_OK,
                new JsonAsString(ConfigConvertUtil.objectToJson(pConfig)), false);
        Optional<JSONObject> jsonAfterUpdate = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID_OK);
        Assert.assertEquals("Incorrect importThreads", 12, jsonAfterUpdate.get().get("importThreads"));

        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID_OK,
                new JsonAsString("{\"rescanThreads\": 7}"), true);
        Optional<JSONObject> jsonAfterReplace = configManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID_OK);
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
                .getJSONConfiguration(CONFIG_NAME, CONFIG_ID_OK).get();
        configManagerService.registerEventHandler(UPDATE, new ConfigUpdateInfo(CONFIG_NAME, CONFIG_ID_OK), callback);
        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID_OK, new JsonAsString(json.toString()), false);
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
                configManagerService.getJSONStrConfiguration(CONFIG_NAME, CONFIG_ID_OK).get(), ProvisiondConfiguration.class);
        config.setImportThreads(-1L);

        configManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID_OK,
                new JsonAsString(ConfigConvertUtil.objectToJson(config)), false);
        Optional<ConfigData<JSONObject>> configData = configManagerService.getConfigData(CONFIG_NAME);
        Assert.assertTrue("Config not found", configData.isPresent());
    }

    @Test
    public void testRemoveEverything() throws Exception {
        Optional<ConfigDefinition> def = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME_MULTIPLE);
        //below extra configuration added, since it is not allowed to delete last configuration
        configManagerService.registerConfiguration(CONFIG_NAME_MULTIPLE, CONFIG_ID_OK + "_1", new JsonAsString("{}"));

        configManagerService.unregisterConfiguration(CONFIG_NAME_MULTIPLE, CONFIG_ID_OK);
        Optional<JSONObject> json = configManagerService.getJSONConfiguration(CONFIG_NAME_MULTIPLE, CONFIG_ID_OK);
        Assert.assertTrue("Fail to unregister config", json.isEmpty());
        configManagerService.registerConfiguration(CONFIG_NAME_MULTIPLE, CONFIG_ID_OK,
                new JsonAsString(ConfigConvertUtil.objectToJson(new ProvisiondConfiguration())));
        configManagerService.unregisterSchema(CONFIG_NAME_MULTIPLE);
        Optional<ConfigDefinition> schemaAfterDeregister = configManagerService.getRegisteredConfigDefinition(CONFIG_NAME_MULTIPLE);
        Optional<ConfigData<JSONObject>> configAfterDeregister = configManagerService.getConfigData(CONFIG_NAME_MULTIPLE);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }

    @Test(expected = ConfigRuntimeException.class)
    public void testErroneousConfigIdOnSchemaWithoutAllowMultiple() {

        //take existing config
        final String configStr = configManagerService.getJSONStrConfiguration(CONFIG_NAME, CONFIG_ID_OK).get();
        //and cleanup
        configManagerService.unregisterConfiguration(CONFIG_NAME, CONFIG_ID_OK);
        Assert.assertTrue("Must be initially empty", configManagerService.getConfigIds(CONFIG_NAME).isEmpty());
        Assert.assertFalse("Must be non multiple", configManagerService.getRegisteredConfigDefinition(CONFIG_NAME).get().getAllowMultiple());

        //Adding config wit ID other than org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID_OK + "someString",new JsonAsString(configStr));
    }

    @Test(expected = ConfigRuntimeException.class)
    public void testAddSecondConfigOnNonMultiple() {
        //take existing config
        final String configStr = configManagerService.getJSONStrConfiguration(CONFIG_NAME, CONFIG_ID_OK).get();
        //Adding config wit ID other than org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID
        configManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID_OK + "someString",new JsonAsString(configStr));
    }
}