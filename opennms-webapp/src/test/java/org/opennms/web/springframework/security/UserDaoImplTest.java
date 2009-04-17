
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.web.springframework.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.security.GrantedAuthority;

public class UserDaoImplTest extends TestCase {

    private static final String MAGIC_USERS_FILE = "src/test/resources/org/opennms/web/springframework/security/magic-users.properties";
    private static final String USERS_XML_FILE = "src/test/resources/org/opennms/web/springframework/security/users.xml";

    public void testConfigSetter() {
        String usersConfigurationFile = "users.xml";
        String magicUsersConfigurationFile = "magic-users.properties";
        UserDaoImpl dao = new UserDaoImpl();

        dao.setUsersConfigurationFile(usersConfigurationFile);
        dao.setMagicUsersConfigurationFile(magicUsersConfigurationFile);

    }

    public void testConfigGetter() {
        String usersConfigurationFile = "users.xml";
        String magicUsersConfigurationFile = "magic-users.properties";
        UserDaoImpl dao = new UserDaoImpl();

        dao.setUsersConfigurationFile(usersConfigurationFile);
        dao.setMagicUsersConfigurationFile(magicUsersConfigurationFile);
        assertEquals("getUsersConfigurationFile returned what we passed to setUsersConfigurationFile", usersConfigurationFile, dao.getUsersConfigurationFile());
        assertEquals("getMagicUsersConfigurationFile returned what we passed to setMagicUsersConfigurationFile", magicUsersConfigurationFile, dao.getMagicUsersConfigurationFile());
    }

    public void testAfterPropertiesSetWithoutUsersConfigFile() {
        UserDaoImpl dao = new UserDaoImpl();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("usersConfigurationFile parameter must be set to the location of the users.xml configuration file"));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testAfterPropertiesSetWithoutMagicUsersConfigFile() {
        UserDaoImpl dao = new UserDaoImpl();
        setUsersConfigurationFile(dao);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("magicUsersConfigurationFile parameter must be set to the location of the magic-users.properties configuration file"));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testGetByUsernameAdmin() {
        UserDaoImpl dao = new UserDaoImpl();

        setUsersConfigurationFile(dao);
        setMagicUsersConfigurationFile(dao);

        User user = dao.getByUsername("admin");
        assertNotNull("user object should not be null", user);
        assertEquals("User name", "admin", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "21232F297A57A5A743894A0E4A801FC3", user.getPassword());

        GrantedAuthority[] authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 2, authorities.length);
        assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
        assertEquals("authorities 2 name", "ROLE_ADMIN", authorities[1].getAuthority());
    }

    public void testGetByUsernameBogus() {
        UserDaoImpl dao = new UserDaoImpl();
        setUsersConfigurationFile(dao);
        setMagicUsersConfigurationFile(dao);

        User user = dao.getByUsername("bogus");
        assertNull("user object should be null", user);
    }

    public void testGetByUsernameRtc() {
        UserDaoImpl dao = new UserDaoImpl();
        setUsersConfigurationFile(dao);
        setMagicUsersConfigurationFile(dao);

        User user = dao.getByUsername("rtc");
        assertNotNull("user object should not be null", user);
        assertEquals("User name", "rtc", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "68154466F81BFB532CD70F8C71426356", user.getPassword());

        GrantedAuthority[] authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.length);
        assertEquals("authorities 0 name", "ROLE_RTC", authorities[0].getAuthority());
    }

    public void testGetByUsernameTempUser() {
        UserDaoImpl dao = new UserDaoImpl();
        setUsersConfigurationFile(dao);
        setMagicUsersConfigurationFile(dao);

        User user = dao.getByUsername("tempuser");
        assertNotNull("user object should not be null", user);
        assertEquals("User name", "tempuser", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "18126E7BD3F84B3F3E4DF094DEF5B7DE", user.getPassword());

        GrantedAuthority[] authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.length);
        assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
    }
    
    public void testGetByUsernameDashoard() {
        UserDaoImpl dao = new UserDaoImpl();
        setUsersConfigurationFile(dao);
        setMagicUsersConfigurationFile(dao);

        User user = dao.getByUsername("dashboard");
        assertNotNull("user object should not be null", user);
        assertEquals("User name", "dashboard", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "DC7161BE3DBF2250C8954E560CC35060", user.getPassword());

        GrantedAuthority[] authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.length);
        assertEquals("authorities 0 name", "ROLE_DASHBOARD", authorities[0].getAuthority());
    }
    
    public void testUsersReload() throws Exception {
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

            UserDaoImpl dao = new UserDaoImpl();
            dao.setUsersConfigurationFile(users.getAbsolutePath());
            dao.setMagicUsersConfigurationFile(magicUsers.getAbsolutePath());

            User user;
            
            user = dao.getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            
            user = dao.getByUsername("dashboard-foo");
            assertNull("dashboard-foo user should not exist and the object should be null", user);

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writeTemporaryFile(users, getUsersXmlContents().replace("<user-id>dashboard</user-id>", "<user-id>dashboard-foo</user-id>"));

            user = dao.getByUsername("dashboard");
            assertNull("dashboard user should no longer exist and the object should be null", user);
            
            user = dao.getByUsername("dashboard-foo");
            assertNotNull("dashboard-foo user should now exist and the object should not be null", user);
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
    }


    public void testMagicUsersReload() throws Exception {
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

            UserDaoImpl dao = new UserDaoImpl();
            dao.setUsersConfigurationFile(users.getAbsolutePath());
            dao.setMagicUsersConfigurationFile(magicUsers.getAbsolutePath());

            User user;
            GrantedAuthority[] authorities;
            
            user = dao.getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.length);
            assertEquals("user GrantedAuthorities[0]", "ROLE_DASHBOARD", authorities[0].getAuthority());

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writeTemporaryFile(magicUsers, getMagicUsersContents().replace("role.dashboard.users=dashboard", "role.dashboard.users="));

            user = dao.getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.length);
            assertEquals("user GrantedAuthorities[0]", "ROLE_USER", authorities[0].getAuthority());
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
    }
    
    /**
     * Test for bugzilla bug #1810.  This is the case:
     * <ol>
     * <li>Both users and magic users files are loaded</li>
     * <li>Magic users file is changed</li>
     * <li>Magic users file is reloaded on the next call to getByUsername</li>
     * <li>Subsequent calls to getByUsername call caues a reload because the
     *     last update time for the users file is stored when magic users is
     *     reloaded</li>
     * </ol>
     * 
     * @param file
     * @param content
     * @throws IOException
     */
    public void testMagicUsersReloadUpdateLastModified() throws Exception {
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

            UserDaoImpl dao = new UserDaoImpl();
            dao.setUsersConfigurationFile(users.getAbsolutePath());
            dao.setMagicUsersConfigurationFile(magicUsers.getAbsolutePath());

            User user;
            GrantedAuthority[] authorities;
            
            user = dao.getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.length);
            assertEquals("user GrantedAuthorities[0]", "ROLE_DASHBOARD", authorities[0].getAuthority());

            /*
             *  On UNIX, the resolution of the last modified time is 1 second,
             *  so we need to wait at least that long before rewriting the
             *  file to ensure that we have crossed over into the next second.
             *  At least we're not crossing over with John Edward.
             */
            Thread.sleep(1100);

            writeTemporaryFile(magicUsers, getMagicUsersContents().replace("role.dashboard.users=dashboard", "role.dashboard.users="));

            user = dao.getByUsername("dashboard");
            assertNotNull("dashboard user should exist and the object should not be null", user);
            authorities = user.getAuthorities(); 
            assertNotNull("user GrantedAuthorities[] object should not be null", authorities);
            assertEquals("user GrantedAuthorities[] object should have only one entry", 1, authorities.length);
            assertEquals("user GrantedAuthorities[0]", "ROLE_USER", authorities[0].getAuthority());

            long ourLastModifiedTime = magicUsers.lastModified();
            long daoLastModifiedTime = dao.getMagicUsersLastModified();
            
            assertEquals("last modified time of magic users file does not match what the DAO stored after reloading the file", ourLastModifiedTime, daoLastModifiedTime);
        } finally {
            fa.deleteExpected();
            fa.tearDown();
        }
    }
    private void writeTemporaryFile(File file, String content) throws IOException {
        FileWriter writer = new FileWriter(file);
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
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        StringBuffer contents = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            contents.append(line);
            contents.append("\n");
        }
        
        return contents.toString();
    }

    private void setUsersConfigurationFile(UserDaoImpl dao) {
        dao.setUsersConfigurationFile(USERS_XML_FILE);
    }

    private void setMagicUsersConfigurationFile(UserDaoImpl dao) {
        dao.setMagicUsersConfigurationFile(MAGIC_USERS_FILE);
    }

}
