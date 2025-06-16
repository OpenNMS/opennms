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
package org.opennms.upgrade.implementations;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.web.api.Authentication;
import org.springframework.core.io.FileSystemResource;

/**
 * The Test Class for MagicUsersMigratorOffline.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class MagicUsersMigratorOfflineIT {

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test migration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMigration() throws Exception {
        MagicUsersMigratorOffline migrator = new MagicUsersMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();

        Assert.assertFalse(new File("target/home/etc/magic-users.properties").exists());
        Assert.assertTrue(new File("target/home/etc/magic-users.properties.zip").exists());
        Assert.assertTrue(new File("target/home/etc/magic-users.properties.zip").isFile());

        validateMigration();
    }

    /**
     * Test missing configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMissingConfigFile() throws Exception {
        FileUtils.deleteQuietly(new File("target/home/etc/magic-users.properties"));
        MagicUsersMigratorOffline migrator = new MagicUsersMigratorOffline();
        migrator.execute();
    }

    /**
     * Test RPM upgrade
     *
     * @throws Exception the exception
     */
    @Test
    public void testRpmUpgade() throws Exception {
        FileUtils.moveFile(new File("target/home/etc/magic-users.properties"), new File("target/home/etc/magic-users.properties.rpmsave"));

        MagicUsersMigratorOffline migrator = new MagicUsersMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();

        validateMigration();
    }

    /**
     * Test Debian upgrade
     *
     * @throws Exception the exception
     */
    @Test
    public void testDebianUpgade() throws Exception {
        FileUtils.moveFile(new File("target/home/etc/magic-users.properties"), new File("target/home/etc/magic-users.properties.dpkg-remove"));

        MagicUsersMigratorOffline migrator = new MagicUsersMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();

        validateMigration();
    }

    /**
     * Test upgrade with 2015 users.xml
     *
     * @throws Exception the exception
     */
    @Test
    public void test2015UsersXmlUpgrade() throws Exception {
        FileUtils.moveFile(new File("target/home/etc/magic-users.properties"), new File("target/home/etc/magic-users.properties.rpmsave"));
        FileUtils.deleteQuietly(new File("target/home/etc/users.xml"));
        FileUtils.moveFile(new File("target/home/etc/users-2015.xml"), new File("target/home/etc/users.xml"));

        MagicUsersMigratorOffline migrator = new MagicUsersMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();

        validateMigration();
    }

    /**
     * Validate the Migration by checking the updated users.xml
     *
     * @throws Exception the exception
     */
    private void validateMigration() throws Exception {
        Userinfo userInfo = JaxbUtils.unmarshal(Userinfo.class, new FileSystemResource(new File("target/home/etc/users.xml")));

        final User rtc = getUser(userInfo, "rtc");
        Assert.assertNotNull(rtc);
        Assert.assertEquals(1, rtc.getRoles().size());
        Assert.assertTrue(rtc.getRoles().contains(Authentication.ROLE_RTC));

        final User admin = getUser(userInfo, "admin");
        Assert.assertNotNull(admin);
        Assert.assertEquals(1, admin.getRoles().size());
        Assert.assertTrue(admin.getRoles().contains(Authentication.ROLE_ADMIN));

        final User jmx = getUser(userInfo, "jmx_operator");
        Assert.assertNotNull(jmx);
        Assert.assertEquals(1, admin.getRoles().size());
        Assert.assertTrue(jmx.getRoles().contains(Authentication.ROLE_JMX));

        final User agalue = getUser(userInfo, "agalue");
        Assert.assertNotNull(agalue);
        Assert.assertEquals(2, agalue.getRoles().size());
        Assert.assertTrue(agalue.getRoles().contains(Authentication.ROLE_USER));
        Assert.assertTrue(agalue.getRoles().contains("ROLE_MEASUREMENTS"));

        final User operator = getUser(userInfo, "operator");
        Assert.assertNotNull(operator);
        Assert.assertEquals(0, operator.getRoles().size());

        final User manager = getUser(userInfo, "manager");
        Assert.assertNotNull(manager);
        Assert.assertEquals(2, manager.getRoles().size());
        Assert.assertTrue(manager.getRoles().contains(Authentication.ROLE_USER));
        Assert.assertTrue(manager.getRoles().contains(Authentication.ROLE_READONLY));
    }

    private User getUser(final Userinfo userInfo, final String username) {
        for (final User user : userInfo.getUsers()) {
            if (user.getUserId().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
