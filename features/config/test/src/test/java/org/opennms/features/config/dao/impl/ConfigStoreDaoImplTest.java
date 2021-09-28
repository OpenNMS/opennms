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
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.dao.impl.util.XmlConverter;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ConfigStoreDaoImplTest {
    final String configName = "testConfigName";
    final String filename = "testFilename";
    @Autowired
    private ConfigStoreDao configStoreDao;

    @Test
    public void testData() throws IOException, JAXBException {
        // register
        XmlConverter converter = new XmlConverter("provisiond-configuration.xsd", "provisiond-configuration");
        ConfigSchema<XmlConverter> configSchema = new ConfigSchema<>(configName, XmlConverter.class, converter);

        configStoreDao.register(configSchema);

        // config
        JSONObject config = new JSONObject();
        config.put("importThreads", 11);
        ConfigData configData = new ConfigData();
        configData.getConfigs().put(filename, config);

        // register
        configStoreDao.register(configSchema);
        configStoreDao.addConfigs(configName, configData);
        Optional<ConfigData> configsInDb = configStoreDao.getConfigData(configName);
        Assert.assertTrue("FAIL TO getConfigData", configsInDb.isPresent());

        // get
        Optional<ConfigSchema> result = configStoreDao.getConfigSchema(configName);
        Assert.assertTrue("FAIL TO getConfigSchema", result.isPresent());

        // register more and update
        String configName2 = configName + "_2";
        ConfigSchema<XmlConverter> configSchema2 = new ConfigSchema<>(configName2, XmlConverter.class, converter);
        configStoreDao.register(configSchema2);
        configStoreDao.updateConfigSchema(configSchema2);
        Optional<ConfigSchema> tmpConfigSchema2 = configStoreDao.getConfigSchema(configName2);

        // list all
        Optional<Set<String>> all = configStoreDao.getConfigNames();
        Assert.assertEquals("FAIL TO getServices", all.get().size(), 2);

        // add config
        ProvisiondConfiguration config2 = new ProvisiondConfiguration();
        config2.setImportThreads(20L);
        JSONObject config2AsJson = new JSONObject(converter.xmlToJson(JaxbUtils.marshal(config2)));
        configStoreDao.addConfig(configName, filename + "_2", config2AsJson);
        Optional<ConfigData> resultAfterUpdate = configStoreDao.getConfigData(configName);
        Assert.assertTrue("FAIL configs count is not equal to 2", resultAfterUpdate.isPresent());

        // delete config
        configStoreDao.deleteConfig(configName, filename + "_2");
        Optional<ConfigData> resultAfterDelete = configStoreDao.getConfigData(configName);
        Assert.assertTrue("FAIL configs count is not equal to 1", resultAfterDelete.get().getConfigs().size() == 1);

        // updateConfigs
        configStoreDao.updateConfigs(configName, new ConfigData());
        Optional<ConfigData> resultAfterUpdateConfigs = configStoreDao.getConfigData(configName);
        Assert.assertTrue("FAIL configs count is not equal to 0", resultAfterUpdateConfigs.get().getConfigs().size() == 0);

        // deregister
        configStoreDao.unregister(configName);
        Optional<ConfigSchema<?>> schemaAfterDeregister = configStoreDao.getConfigSchema(configName);
        Optional<ConfigData> configAfterDeregister = configStoreDao.getConfigData(configName);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }
}
