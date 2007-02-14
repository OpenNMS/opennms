
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

package org.opennms.web.acegisecurity;

import junit.framework.TestCase;

import org.acegisecurity.GrantedAuthority;
import org.opennms.test.ThrowableAnticipator;

public class UserDaoImplTest extends TestCase {

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

    public void testGetByUsernameWithoutUsersConfigFile() {
        UserDaoImpl dao = new UserDaoImpl();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("usersConfigurationFile parameter must be set to the location of the users.xml configuration file"));

        try {
            dao.getByUsername("test_user");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testGetByUsernameWithoutMagicUsersConfigFile() {
        UserDaoImpl dao = new UserDaoImpl();
        setUsersConfigurationFile(dao);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("magicUsersConfigurationFile parameter must be set to the location of the magic-users.properties configuration file"));

        try {
            dao.getByUsername("test_user");
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
        assertEquals("authorities size", 3, authorities.length);
        assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
        assertEquals("authorities 1 name", "ROLE_RTC", authorities[1].getAuthority());
        assertEquals("authorities 2 name", "ROLE_ADMIN", authorities[2].getAuthority());
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
        assertEquals("authorities size", 2, authorities.length);
        assertEquals("authorities 0 name", "ROLE_USER", authorities[0].getAuthority());
        assertEquals("authorities 1 name", "ROLE_RTC", authorities[1].getAuthority());
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

    private void setUsersConfigurationFile(UserDaoImpl dao) {
        //dao.setUsersConfigurationFile(ClassLoader.getSystemResource("org/opennms/web/acegisecurity/users.xml").getFile());
        dao.setUsersConfigurationFile("src/test/resources/org/opennms/web/acegisecurity/users.xml");
    }

    private void setMagicUsersConfigurationFile(UserDaoImpl dao) {
        //dao.setMagicUsersConfigurationFile(ClassLoader.getSystemResource("org/opennms/web/acegisecurity/magic-users.properties").getFile());
        dao.setMagicUsersConfigurationFile("src/test/resources/org/opennms/web/acegisecurity/magic-users.properties");
    }

}
