/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import static org.opennms.core.test.MockLogAppender.assertLogAtLevel;
import static org.opennms.core.test.MockLogAppender.assertNoWarningsOrGreater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.FileAnticipator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
        "classpath:/org/opennms/web/springframework/security/AuthenticationIntegrationTest-context.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SpringSecurityUserDaoImplTest extends TestCase implements InitializingBean {

    private static final String MAGIC_USERS_FILE = "src/test/resources/org/opennms/web/springframework/security/magic-users.properties";
    private static final String USERS_XML_FILE = "src/test/resources/org/opennms/web/springframework/security/users.xml";

    @Autowired
    SpringSecurityUserDao m_springSecurityDao;

    @Autowired
    UserManager m_userManager;

    @Autowired
    GroupManager m_groupManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    @Override
    public void setUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testGetByUsernameAdmin() {
        SpringSecurityUser user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("admin");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "admin", user.getUsername());
        assertEquals("Full name", "Administrator", user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "21232F297A57A5A743894A0E4A801FC3", user.getPassword());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 2, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
        assertContainsAuthority(Authentication.ROLE_ADMIN, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    public void testGetByUsernameBogus() {
        assertNull("user object should be null", m_springSecurityDao.getByUsername("bogus"));
        assertNoWarningsOrGreater();
    }

    @Test
    public void testGetByUsernameRtc() {
        SpringSecurityUser user = m_springSecurityDao.getByUsername("rtc");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "rtc", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertTrue("Password", m_userManager.checkSaltedPassword("rtc", user.getPassword()));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_RTC, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    @DirtiesContext
    public void testGetByUsernameTempUser() throws Exception {
        final OnmsUser newUser = new OnmsUser("tempuser");
        newUser.setPassword("18126E7BD3F84B3F3E4DF094DEF5B7DE");
        m_userManager.save(newUser);

        final SpringSecurityUser user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("tempuser");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "tempuser", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "18126E7BD3F84B3F3E4DF094DEF5B7DE", user.getPassword());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    @DirtiesContext
    public void testGetByUsernameDashboard() throws Exception {
        final OnmsUser newUser = new OnmsUser("dashboard");
        newUser.setPassword("DC7161BE3DBF2250C8954E560CC35060");
        m_userManager.save(newUser);

        SpringSecurityUser user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("dashboard");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "dashboard", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "DC7161BE3DBF2250C8954E560CC35060", user.getPassword());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_DASHBOARD, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    @DirtiesContext
    public void testMagicUsersReload() throws Exception {
        final OnmsUser newUser = new OnmsUser("dashboard");
        newUser.setPassword("DC7161BE3DBF2250C8954E560CC35060");
        m_userManager.save(newUser);

        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();

        try {
            File users = fa.tempFile("users.xml");
            File magicUsers = fa.tempFile("magic-users.properties");

            writeTemporaryFile(users, getUsersXmlContents());
            writeTemporaryFile(magicUsers, getMagicUsersContents());

            ((SpringSecurityUserDaoImpl) m_springSecurityDao).setUsersConfigurationFile(users.getAbsolutePath());
            ((SpringSecurityUserDaoImpl) m_springSecurityDao).setMagicUsersConfigurationFile(magicUsers.getAbsolutePath());

            SpringSecurityUser user;
            Collection<? extends GrantedAuthority> authorities;

            user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.size());
            assertContainsAuthority(Authentication.ROLE_DASHBOARD, authorities);

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writeTemporaryFile(magicUsers, getMagicUsersContents().replace("role.dashboard.users=dashboard", "role.dashboard.users="));

            user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.size());
            assertContainsAuthority(Authentication.ROLE_USER, authorities);
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
        assertNoWarningsOrGreater();
    }

    /**
     * Test for bugzilla bug #1810.  This is the case:
     * <ol>
     * <li>Both users and magic users files are loaded</li>
     * <li>Magic users file is changed</li>
     * <li>Magic users file is reloaded on the next call to getByUsername</li>
     * <li>Subsequent calls to getByUsername call causes a reload because the
     *     last update time for the users file is stored when magic users is
     *     reloaded</li>
     * </ol>
     * 
     * @param file
     * @param content
     * @throws IOException
     */
    @Test
    @DirtiesContext
    public void testMagicUsersReloadUpdateLastModified() throws Exception {
        final OnmsUser newUser = new OnmsUser("dashboard");
        newUser.setPassword("DC7161BE3DBF2250C8954E560CC35060");
        m_userManager.save(newUser);

        /*
         * We're not going to use the anticipator functionality, but it's
         * handy for handling temporary directories.
         */
        FileAnticipator fa = new FileAnticipator();

        try {
            File users = fa.tempFile("users.xml");
            File magicUsers = fa.tempFile("magic-users.properties");

            writeTemporaryFile(users, getUsersXmlContents());
            writeTemporaryFile(magicUsers, getMagicUsersContents());

            ((SpringSecurityUserDaoImpl) m_springSecurityDao).setUsersConfigurationFile(users.getAbsolutePath());
            ((SpringSecurityUserDaoImpl) m_springSecurityDao).setMagicUsersConfigurationFile(magicUsers.getAbsolutePath());

            SpringSecurityUser user;
            Collection<? extends GrantedAuthority> authorities;

            user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.size());
            assertContainsAuthority(Authentication.ROLE_DASHBOARD, authorities);

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writeTemporaryFile(magicUsers, getMagicUsersContents().replace("role.dashboard.users=dashboard", "role.dashboard.users="));

            user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.size());
            assertContainsAuthority(Authentication.ROLE_USER, authorities);

            long ourLastModifiedTime = magicUsers.lastModified();
            long daoLastModifiedTime = ((SpringSecurityUserDaoImpl) m_springSecurityDao).getMagicUsersLastModified();

            assertEquals("last modified time of magic users file does not match what the DAO stored after reloading the file", ourLastModifiedTime, daoLastModifiedTime);
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
        assertNoWarningsOrGreater();
    }

    @DirtiesContext
    @Test
    public void testMissingMagicUsersProperties() {
        ((SpringSecurityUserDaoImpl) m_springSecurityDao).setMagicUsersConfigurationFile("src/test/resources/org/opennms/web/springframework/security/magic-users-bad.properties");
        ((SpringSecurityUserDaoImpl) m_springSecurityDao).parseMagicUsers();
        assertLogAtLevel(Level.WARN);
    }

    private void writeTemporaryFile(File file, String content) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        writer.write(content);
        writer.close();
    }

    private String getUsersXmlContents() throws IOException {
        return getFileContents(new File(USERS_XML_FILE));
    }

    private String getMagicUsersContents() throws IOException {
        return getFileContents(new File(MAGIC_USERS_FILE));
    }

    private String getFileContents(File file) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

        try {
            StringBuffer contents = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                contents.append(line);
                contents.append("\n");
            }
    
            return contents.toString();
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void assertContainsAuthority(final String role, final Collection<? extends GrantedAuthority> authorities) {
        for (final GrantedAuthority authority : authorities) {
            if (role.equals(authority.getAuthority())) {
                return;
            }
        }

        throw new AssertionError("role " + role + " was not found in " + authorities);
    }
}
