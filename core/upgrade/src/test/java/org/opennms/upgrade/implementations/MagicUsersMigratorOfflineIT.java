/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.CastorUtils;
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
        Userinfo userInfo = CastorUtils.unmarshal(Userinfo.class, new FileSystemResource(new File("target/home/etc/users.xml")));

        final User rtc = getUser(userInfo, "rtc");
        Assert.assertNotNull(rtc);
        Assert.assertEquals(1, rtc.getRoleCount());
        Assert.assertTrue(rtc.getRoleCollection().contains(Authentication.ROLE_RTC));

        final User admin = getUser(userInfo, "admin");
        Assert.assertNotNull(admin);
        Assert.assertEquals(1, admin.getRoleCount());
        Assert.assertTrue(admin.getRoleCollection().contains(Authentication.ROLE_ADMIN));

        final User jmx = getUser(userInfo, "jmx_operator");
        Assert.assertNotNull(jmx);
        Assert.assertEquals(1, admin.getRoleCount());
        Assert.assertTrue(jmx.getRoleCollection().contains(Authentication.ROLE_JMX));

        final User agalue = getUser(userInfo, "agalue");
        Assert.assertNotNull(agalue);
        Assert.assertEquals(2, agalue.getRoleCount());
        Assert.assertTrue(agalue.getRoleCollection().contains(Authentication.ROLE_USER));
        Assert.assertTrue(agalue.getRoleCollection().contains("ROLE_MEASUREMENTS"));

        final User operator = getUser(userInfo, "operator");
        Assert.assertNotNull(operator);
        Assert.assertEquals(0, operator.getRoleCount());

        final User manager = getUser(userInfo, "manager");
        Assert.assertNotNull(manager);
        Assert.assertEquals(2, manager.getRoleCount());
        Assert.assertTrue(manager.getRoleCollection().contains(Authentication.ROLE_USER));
        Assert.assertTrue(manager.getRoleCollection().contains(Authentication.ROLE_READONLY));
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
     * Gets the user.
     *
     * @param userInfo the user info
     * @param username the user name
     * @return the user
     */
    private User getUser(Userinfo userInfo, String username) {
        for (final User user : userInfo.getUsers().getUserCollection()) {
            if (user.getUserId().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
