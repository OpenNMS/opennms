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
package org.opennms.features.config.dao.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.KeyHandler;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.impl.ProvisiondCmJaxbConfigTestDao;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableMap;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:mock-dao.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class KeyHandlerTest {
    @Autowired
    private ProvisiondCmJaxbConfigTestDao provisiondCmJaxbConfigTestDao;

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Autowired
    private KeyHandler keyHandler;

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
    public void canScheduleRequisitionViaKeysAtRuntime() {
        // Some requisitions are scheduled by default
        ProvisiondConfiguration config = provisiondCmJaxbConfigTestDao.getConfig("default");
        assertThat(config.getRequisitionDefs(), hasSize(2));

        // No keys
        assertThat(keyHandler.getKeysMatchingPrefix("/wowzers"), empty());

        // Let's add one using keys
        Map<String, String> keysToSet = ImmutableMap.<String,String>builder()
                .put("/wowzers/import-name", "wowzers")
                .put("/wowzers/import-url-resource", "dns://localhost/localhost")
                .put("/wowzers/cron-schedule", "0 0 0 * * ? *")
                .build();
        keyHandler.setKeys(keysToSet);

        // Verify
        config = provisiondCmJaxbConfigTestDao.getConfig("default");
        assertThat(config.getRequisitionDefs(), hasSize(3));
        assertThat(keyHandler.getKeysMatchingPrefix("/wowzers"), equalTo(keysToSet));
    }

    @Test
    @JUnitConfigurationEnvironment(systemProperties = {
            "wowzers.import-name=wowzers",
            "wowzers.import-url-resource=dns://localhost/localhost",
            "wowzers.cron-schedule=\"0 0 0 * * ? *\"",
    })
    public void canScheduleRequisitionViaKeysWithSysProps() {
        ProvisiondConfiguration config = provisiondCmJaxbConfigTestDao.getConfig("default");
        assertThat(config.getRequisitionDefs(), hasSize(3));
    }

}
