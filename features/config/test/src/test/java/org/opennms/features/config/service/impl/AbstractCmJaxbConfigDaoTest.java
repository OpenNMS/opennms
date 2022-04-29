/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.api.callback.DefaultCmJaxbConfigDaoUpdateCallback;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:mock-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AbstractCmJaxbConfigDaoTest {
    @Autowired
    private ProvisiondCmJaxbConfigTestDao provisiondCmJaxbConfigTestDao;

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Before
    public void init() throws Exception {
        ConfigDefinition def = XsdHelper.buildConfigDefinition(provisiondCmJaxbConfigTestDao.getConfigName(),
                "provisiond-configuration.xsd", "provisiond-configuration",
                ConfigurationManagerService.BASE_PATH, false);

        configurationManagerService.registerConfigDefinition(provisiondCmJaxbConfigTestDao.getConfigName(), def);
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("provisiond-configuration.xml");
        Optional<ConfigDefinition> registeredDef = configurationManagerService.getRegisteredConfigDefinition(provisiondCmJaxbConfigTestDao.getConfigName());
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        ConfigConverter converter = XsdHelper.getConverter(registeredDef.get());

        JsonAsString configObject = new JsonAsString(converter.xmlToJson(xmlStr));
        configurationManagerService.registerConfiguration(provisiondCmJaxbConfigTestDao.getConfigName(),
                provisiondCmJaxbConfigTestDao.getDefaultConfigId(), configObject);
    }

    @After
    public void after() throws IOException {
        configurationManagerService.unregisterSchema(provisiondCmJaxbConfigTestDao.getConfigName());
    }

    @Test
    public void testProvisiondCmJaxbConfigDao() throws IOException {
        // test get config
        ProvisiondConfiguration pconfig = provisiondCmJaxbConfigTestDao.loadConfig(provisiondCmJaxbConfigTestDao.getDefaultConfigId());
        Assert.assertTrue("getConfig fail!", pconfig != null);
        Assert.assertTrue("import thread is wrong", pconfig.getImportThreads() == 11);

        // test callback update reference config entity object
        DefaultCmJaxbConfigDaoUpdateCallback updateCallback = Mockito.mock(DefaultCmJaxbConfigDaoUpdateCallback.class);
        Consumer<ConfigUpdateInfo> validateCallback = Mockito.mock(Consumer.class);

        provisiondCmJaxbConfigTestDao.addOnReloadedCallback(provisiondCmJaxbConfigTestDao.getDefaultConfigId(), updateCallback);
        provisiondCmJaxbConfigTestDao.addValidationCallback(validateCallback);

        provisiondCmJaxbConfigTestDao.updateConfig("{\"importThreads\": 12}");
        Mockito.verify(updateCallback, Mockito.times(1)).accept(Mockito.any(ConfigUpdateInfo.class));
        Mockito.verify(validateCallback, Mockito.times(1)).accept(Mockito.any(ConfigUpdateInfo.class));

        Assert.assertTrue("import thread is wrong (after callback)", pconfig.getImportThreads() == 12);
    }
}