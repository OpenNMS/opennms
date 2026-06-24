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
package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.netmgt.config.UserManagerCmImpl;
import org.opennms.netmgt.config.mock.MockGroupManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Integration test for UserManagerCmImpl verifying that users can be loaded,
 * saved, and deleted via ConfigurationManagerService (backed by the kvstore_jsonb table).
 *
 * Pattern follows SnmpConfigCmJaxbConfigDaoIT: registers the schema and imports the
 * test fixture manually in @Before, then exercises the UserManager API.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/META-INF/opennms/applicationContext-config-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-config-service.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class UserManagerCmImplIT {

    private static final String MOCK_GROUP_XML =
            "<groupinfo><groups><group><name>Admin</name><user>admin</user></group></groups></groupinfo>";

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    private UserManagerCmImpl userManager;

    @Before
    public void setUp() throws Exception {
        // Register schema and import test users.xml into CM
        ConfigDefinition def = XsdHelper.buildConfigDefinition(
                UserManagerCmImpl.CONFIG_NAME, "users.xsd", "userinfo",
                ConfigurationManagerService.BASE_PATH, false);
        configurationManagerService.registerConfigDefinition(UserManagerCmImpl.CONFIG_NAME, def);

        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("users-cm-test.xml");
        assertNotNull("Test fixture users-cm-test.xml not found on classpath", xmlPath);
        String xmlStr = Files.readString(Path.of(xmlPath.toURI()));

        Optional<ConfigDefinition> registeredDef =
                configurationManagerService.getRegisteredConfigDefinition(UserManagerCmImpl.CONFIG_NAME);
        String json = XsdHelper.getConverter(registeredDef.get()).xmlToJson(xmlStr);
        configurationManagerService.registerConfiguration(
                UserManagerCmImpl.CONFIG_NAME,
                ConfigDefinition.DEFAULT_CONFIG_ID,
                new JsonAsString(json));

        // Construct UserManagerCmImpl manually and inject the autowired CM service
        MockGroupManager groupManager = new MockGroupManager(MOCK_GROUP_XML);
        userManager = new UserManagerCmImpl(groupManager);
        ReflectionTestUtils.setField(userManager, "configurationManagerService", configurationManagerService);
        userManager.init();
    }

    @After
    public void tearDown() throws IOException {
        if (configurationManagerService.getRegisteredConfigDefinition(UserManagerCmImpl.CONFIG_NAME).isPresent()) {
            configurationManagerService.unregisterSchema(UserManagerCmImpl.CONFIG_NAME);
        }
    }

    @Test
    public void testLoadFromCm() throws Exception {
        User admin = userManager.getUser("admin");
        assertNotNull("admin user should be loadable from CM", admin);
        assertEquals("admin", admin.getUserId());
        assertEquals("Administrator", admin.getFullName().orElse(null));
        assertTrue("admin should have ROLE_ADMIN",
                admin.getRoles().contains("ROLE_ADMIN"));

        User rtc = userManager.getUser("rtc");
        assertNotNull("rtc user should be loadable from CM", rtc);
        assertEquals("rtc", rtc.getUserId());
    }

    @Test
    public void testSaveNewUserPersistsToCm() throws Exception {
        OnmsUser newUser = new OnmsUser("testuser");
        newUser.setFullName("Test User");
        newUser.setPassword("68154952C03D2DDF12AD5A8E291ADB57");
        newUser.setPasswordSalted(false);
        userManager.save(newUser);

        // Verify readable through UserManager
        assertTrue("testuser should exist after save", userManager.hasUser("testuser"));
        User saved = userManager.getUser("testuser");
        assertNotNull(saved);
        assertEquals("Test User", saved.getFullName().orElse(null));

        // Verify persisted in CM directly
        Optional<String> jsonInCm = configurationManagerService.getJSONStrConfiguration(
                UserManagerCmImpl.CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        assertTrue(jsonInCm.isPresent());
        assertTrue("CM JSON should contain new user id", jsonInCm.get().contains("testuser"));
    }

    @Test
    public void testDeleteUserPersistsToCm() throws Exception {
        assertTrue("admin should exist before delete", userManager.hasUser("admin"));
        userManager.deleteUser("admin");
        assertFalse("admin should not exist after delete", userManager.hasUser("admin"));

        // Verify removed in CM directly
        Optional<String> jsonInCm = configurationManagerService.getJSONStrConfiguration(
                UserManagerCmImpl.CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        assertTrue(jsonInCm.isPresent());
        assertFalse("CM JSON should no longer contain admin", jsonInCm.get().contains("\"admin\""));
    }

    @Test
    public void testLastModifiedBumpsOnSave() throws Exception {
        long before = userManager.getLastModified();

        // Small sleep so System.currentTimeMillis() is guaranteed to advance
        Thread.sleep(5);

        OnmsUser u = new OnmsUser("bumptestuser");
        u.setPassword("aabbcc");
        userManager.save(u);

        assertTrue("getLastModified() should increase after save",
                userManager.getLastModified() > before);
    }

    @Test
    public void testCmUpdateCallbackSetsReloadFlag() throws Exception {
        assertFalse("No reload needed initially", userManager.isUpdateNeeded());

        // Simulate an external CM write (e.g. from another node in a cluster)
        Optional<String> existingJson = configurationManagerService.getJSONStrConfiguration(
                UserManagerCmImpl.CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        // Re-save the same config to trigger the UPDATE event
        configurationManagerService.updateConfiguration(
                UserManagerCmImpl.CONFIG_NAME,
                ConfigDefinition.DEFAULT_CONFIG_ID,
                new JsonAsString(existingJson.get()),
                true);

        // The CM UPDATE callback should have set the reload flag
        assertTrue("CM UPDATE callback should set m_needsReload", userManager.isUpdateNeeded());
    }

    @Test
    public void testReloadAfterExternalCmUpdate() throws Exception {
        // Write a modified config directly to CM, bypassing UserManager
        Optional<ConfigDefinition> defOpt =
                configurationManagerService.getRegisteredConfigDefinition(UserManagerCmImpl.CONFIG_NAME);
        String updatedXml = "<?xml version=\"1.0\"?>\n" +
                "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">\n" +
                "  <header><rev>.9</rev><created>test</created><mstation>test</mstation></header>\n" +
                "  <users>\n" +
                "    <user>\n" +
                "      <user-id>external-user</user-id>\n" +
                "      <full-name>External User</full-name>\n" +
                "      <user-comments>added externally</user-comments>\n" +
                "      <password>AABB1122334455</password>\n" +
                "    </user>\n" +
                "  </users>\n" +
                "</userinfo>\n";
        String updatedJson = XsdHelper.getConverter(defOpt.get()).xmlToJson(updatedXml);
        configurationManagerService.updateConfiguration(
                UserManagerCmImpl.CONFIG_NAME,
                ConfigDefinition.DEFAULT_CONFIG_ID,
                new JsonAsString(updatedJson),
                true);

        // The callback set m_needsReload=true. The next operation should trigger doUpdate() → reload().
        assertTrue("external-user should be visible after CM update + lazy reload",
                userManager.hasUser("external-user"));
        assertFalse("admin should no longer exist after the config was replaced externally",
                userManager.hasUser("admin"));
    }
}
