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
public class AbstractCmJaxbConfigDaoIT {
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