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
import org.opennms.netmgt.config.snmp.SnmpConfig;
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
public class SnmpConfigCmJaxbConfigDaoIT {
    @Autowired
    private SnmpConfigCmJaxbConfigTestDao snmpConfigCmJaxbConfigTestDao;

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Before
    public void init() throws Exception {
        ConfigDefinition def = XsdHelper.buildConfigDefinition(snmpConfigCmJaxbConfigTestDao.getConfigName(),
                "snmp-config.xsd", "snmp-config",
                ConfigurationManagerService.BASE_PATH, false);

        configurationManagerService.registerConfigDefinition(snmpConfigCmJaxbConfigTestDao.getConfigName(), def);
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("snmp-config.xml");
        Optional<ConfigDefinition> registeredDef = configurationManagerService.getRegisteredConfigDefinition(snmpConfigCmJaxbConfigTestDao.getConfigName());
        String xmlStr = Files.readString(Path.of(xmlPath.getPath()));
        ConfigConverter converter = XsdHelper.getConverter(registeredDef.get());

        JsonAsString configObject = new JsonAsString(converter.xmlToJson(xmlStr));
        configurationManagerService.registerConfiguration(snmpConfigCmJaxbConfigTestDao.getConfigName(),
                snmpConfigCmJaxbConfigTestDao.getDefaultConfigId(), configObject);
    }


    @After
    public void after() throws IOException {
        configurationManagerService.unregisterSchema(snmpConfigCmJaxbConfigTestDao.getConfigName());
    }

    @Test
    public void testProvisiondCmJaxbConfigDao() throws IOException {
        // test get config
        final SnmpConfig pconfig = snmpConfigCmJaxbConfigTestDao.loadConfig(snmpConfigCmJaxbConfigTestDao.getDefaultConfigId());
        Assert.assertTrue("getConfig fail!", pconfig != null);
        Assert.assertTrue("timeout is wrong", pconfig.getTimeout() == 800);

        // test callback update reference config entity object
        DefaultCmJaxbConfigDaoUpdateCallback updateCallback = Mockito.mock(DefaultCmJaxbConfigDaoUpdateCallback.class);
        Consumer<ConfigUpdateInfo> validateCallback = Mockito.mock(Consumer.class);

        snmpConfigCmJaxbConfigTestDao.addOnReloadedCallback(snmpConfigCmJaxbConfigTestDao.getDefaultConfigId(), updateCallback);
        snmpConfigCmJaxbConfigTestDao.addValidationCallback(validateCallback);

        snmpConfigCmJaxbConfigTestDao.updateConfig("{\"timeout\": 1600}");
        Mockito.verify(updateCallback, Mockito.times(1)).accept(Mockito.any(ConfigUpdateInfo.class));
        Mockito.verify(validateCallback, Mockito.times(1)).accept(Mockito.any(ConfigUpdateInfo.class));

        Assert.assertTrue("timeout is wrong (after callback)", pconfig.getTimeout() == 1600);
    }


}
