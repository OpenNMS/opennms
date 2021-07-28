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

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.*;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

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
public class ConfigStoreDaoImplTest {
    final String serviceName = "testServiceName";
    final int majorVersion = 29;
    final String filename = "testFilename";
    @Autowired
    private ConfigStoreDao configStoreDao;

    public static class FakeConvert<T> implements XmlConfigConverter {
        private Class<T> configurationClass;
        private ServiceSchema serviceSchema;
        private SCHEMA_TYPE schemaType = SCHEMA_TYPE.XML;
        public FakeConvert() {}

        @Override
        public Object xmlToJaxbObject(String xml) {
            return null;
        }

        @Override
        public String xmlTOJson(String xmlStr) {
            return null;
        }

        @Override
        public String jsonToXml(String jsonStr) {
            return null;
        }

        @Override
        public Object jsonToJaxbObject(String jsonStr) {
            return null;
        }

        @Override
        public Class getConfigurationClass() {
            return null;
        }

        @Override
        public ServiceSchema getServiceSchema() {
            return null;
        }

        @Override
        public URL getSchemaPath() throws IOException {
            return null;
        }

        @Override
        public SCHEMA_TYPE getSchemaType() {
            return null;
        }

        @Override
        public String getRawSchema() {
            return null;
        }
    }

    @Test
    public void testData() throws IOException, ClassNotFoundException {
        ConfigSchema<FakeConvert> configSchema = new ConfigSchema<>(serviceName, majorVersion, 0, 0, FakeConvert.class, new FakeConvert());
        JSONObject config = new JSONObject();
        config.put("test", "test");
        ConfigData configData = new ConfigData();
        configData.getConfigs().put(filename, config);
        // register
        boolean status = configStoreDao.register(configSchema);
        Assert.assertTrue("FAIL TO WRITE CONFIG", status);
        configStoreDao.addConfigs(serviceName, configData);
        Optional<ConfigData> configsInDb = configStoreDao.getConfigData(serviceName);
        Assert.assertTrue("FAIL TO getConfigData", configsInDb.isPresent());

        // get
        Optional<ConfigSchema> result = configStoreDao.getConfigSchema(serviceName);
        Assert.assertTrue("FAIL TO getConfigSchema", result.isPresent());

        // register more and update
        String serviceName2 = serviceName + "_2";
        ConfigSchema<FakeConvert> configSchema2 = new ConfigSchema<>(serviceName2, majorVersion, 0, 0, FakeConvert.class, new FakeConvert());
        configStoreDao.register(configSchema2);
        configSchema2.setMajorVersion(30);
        configStoreDao.updateConfigSchema(configSchema2);
        Optional<ConfigSchema> tmpConfigSchema2 = configStoreDao.getConfigSchema(serviceName2);
        Assert.assertEquals("FAIL TO updateConfigSchema", tmpConfigSchema2.get().getVersion(), "30.0.0");

        // list all
        Optional<Set<String>> all = configStoreDao.getServiceIds();
        Assert.assertEquals("FAIL TO getServices", all.get().size(), 2);

        // update
        JSONObject config2 = new JSONObject();
        config2.put("test2", "test2");
        status = configStoreDao.addConfig(serviceName, filename + "_2", config2);
        Assert.assertTrue("FAIL TO addOrUpdateConfig", status);
        Optional<ConfigData> resultAfterUpdate = configStoreDao.getConfigData(serviceName);
        Assert.assertTrue("FAIL configs count is not equal to 2", resultAfterUpdate.isPresent());

        // delete config
        status = configStoreDao.deleteConfig(serviceName, filename + "_2");
        Assert.assertTrue("FAIL TO deleteConfig", true);
        Optional<ConfigData> resultAfterDelete = configStoreDao.getConfigData(serviceName);
        Assert.assertTrue("FAIL configs count is not equal to 1", resultAfterDelete.get().getConfigs().size() == 1);

        // updateConfigs
        status = configStoreDao.updateConfigs(serviceName, new ConfigData());
        Assert.assertTrue("FAIL TO updateConfigs", true);
        Optional<ConfigData> resultAfterUpdateConfigs = configStoreDao.getConfigData(serviceName);
        Assert.assertTrue("FAIL configs count is not equal to 0", resultAfterUpdateConfigs.get().getConfigs().size() == 0);

        // deregister
        configStoreDao.unregister(serviceName);
        Optional<ConfigSchema<?>> schemaAfterDeregister = configStoreDao.getConfigSchema(serviceName);
        Optional<ConfigData> configAfterDeregister = configStoreDao.getConfigData(serviceName);
        Assert.assertTrue("FAIL TO deregister schema", schemaAfterDeregister.isEmpty());
        Assert.assertTrue("FAIL TO deregister config", configAfterDeregister.isEmpty());
    }
}
