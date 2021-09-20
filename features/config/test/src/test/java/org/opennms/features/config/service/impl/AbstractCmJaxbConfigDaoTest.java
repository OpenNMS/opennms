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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
    public void init() throws IOException, JAXBException {
        configurationManagerService.registerSchema(provisiondCmJaxbConfigTestDao.getConfigName(),
                "provisiond-configuration.xsd", "provisiond-configuration");
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("provisiond-configuration.xml");
        Optional<ConfigSchema<?>> configSchema = configurationManagerService.getRegisteredSchema(provisiondCmJaxbConfigTestDao.getConfigName());
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        JsonAsString configObject = new JsonAsString(configSchema.get().getConverter().xmlToJson(xmlStr));
        configurationManagerService.registerConfiguration(provisiondCmJaxbConfigTestDao.getConfigName(),
                provisiondCmJaxbConfigTestDao.getDefaultConfigId(), configObject);
    }

    @After
    public void after() throws IOException {
        configurationManagerService.unregisterSchema(provisiondCmJaxbConfigTestDao.getConfigName());
    }

    @Test
    public void testProvisiondCmJaxbConfigDao() {
        // test get config
        ProvisiondConfiguration pconfig = provisiondCmJaxbConfigTestDao.loadConfig(provisiondCmJaxbConfigTestDao.getDefaultConfigId());
        Assert.assertTrue("getConfig fail!", pconfig != null);

//        // test callback
//        ProvisiondCallback callback = Mockito.mock(ProvisiondCallback.class);
//        provisiondCmJaxbConfigTestDao.addOnReloadedCallback(callback);
//
//        doAnswer(invocationOnMock -> {
//            Assert.assertTrue("accept".equals(invocationOnMock.getMethod().getName()));
//            Assert.assertTrue(invocationOnMock.getArgument(0) instanceof ProvisiondConfiguration);
//            return null;
//        }).when(callback).accept(any());

//        provisiondCmJaxbConfigTestDao.loadConfig(provisiondCmJaxbConfigTestDao.getDefaultConfigId());
    }

    class ProvisiondCallback implements Consumer<ProvisiondConfiguration> {
        @Override
        public void accept(ProvisiondConfiguration provisiondConfiguration) {
            System.out.println("ProvisiondCallback fired!!!");
        }
    }
}
