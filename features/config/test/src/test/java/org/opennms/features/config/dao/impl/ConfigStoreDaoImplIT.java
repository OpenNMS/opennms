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
package org.opennms.features.config.dao.impl;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.Set;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ConfigStoreDaoImplIT {
    final String configName = "testConfigName";
    final String filename = "testFilename";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private ConfigStoreDao configStoreDao;

    @Test
    public void testData() {
        // register
        ConfigDefinition def = XsdHelper.buildConfigDefinition(configName, "provisiond-configuration.xsd",
                "provisiond-configuration", ConfigurationManagerService.BASE_PATH, false);
        configStoreDao.register(def);

        // config
        JSONObject config = new JSONObject();
        config.put("importThreads", 11);
        ConfigData configData = new ConfigData();
        configData.getConfigs().put(filename, config);

        // register
        configStoreDao.register(def);
        configStoreDao.addConfigs(configName, configData);
        Optional<ConfigData> configsInDb = configStoreDao.getConfigs(configName);
        Assert.assertTrue("FAIL TO getConfigs", configsInDb.isPresent());

        // get
        Optional<ConfigDefinition> result = configStoreDao.getConfigDefinition(configName);
        Assert.assertTrue("FAIL TO getConfigSchema", result.isPresent());

        // register more and update
        String configName2 = configName + "_2";
        ConfigDefinition def2 = XsdHelper.buildConfigDefinition(configName2, "provisiond-configuration.xsd",
                "provisiond-configuration", ConfigurationManagerService.BASE_PATH, false);
        configStoreDao.register(def2);
        configStoreDao.updateConfigDefinition(def2);
        Optional<ConfigDefinition> tmpConfigSchema2 = configStoreDao.getConfigDefinition(configName2);

        // list all
        Set<String> all = configStoreDao.getConfigNames();
        Assert.assertEquals("FAIL TO getServices", 2, all.size());

        // add config
        ProvisiondConfiguration config2 = new ProvisiondConfiguration();
        config2.setImportThreads(20L);

        JSONObject config2AsJson = new JSONObject(ConfigConvertUtil.objectToJson(config2));
        configStoreDao.addConfig(configName, filename + "_2", config2AsJson);
        Optional<ConfigData> resultAfterUpdate = configStoreDao.getConfigs(configName);
        Assert.assertTrue("FAIL configs count is not equal to 2", resultAfterUpdate.isPresent());

        // delete config
        configStoreDao.deleteConfig(configName, filename + "_2");
        Optional<ConfigData> resultAfterDelete = configStoreDao.getConfigs(configName);
        Assert.assertEquals("FAIL configs count is not equal to 1", 1, resultAfterDelete.get().getConfigs().size());

        //check if its last config, deletion not allowed if it is last config
        expectedException.expect(ConfigRuntimeException.class);
        expectedException.expectMessage("Deletion of the last config is not allowed. testConfigName, configId testFilename");
        configStoreDao.deleteConfig(configName, filename);

        // updateConfigs
        configStoreDao.updateConfigs(configName, new ConfigData());
        Optional<ConfigData> resultAfterUpdateConfigs = configStoreDao.getConfigs(configName);
        Assert.assertEquals("FAIL configs count is not equal to 0", 0,
                resultAfterUpdateConfigs.get().getConfigs().size());

        // deregister
        configStoreDao.unregister(configName);
        Optional<ConfigDefinition> schemaAfterDeregister = configStoreDao.getConfigDefinition(configName);
        Optional<ConfigData> configAfterDeregister = configStoreDao.getConfigs(configName);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }
}