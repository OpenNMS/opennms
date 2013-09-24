/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;

public class UserRestServiceTest extends AbstractSpringJerseyRestTestCase  {

	private static void validateNoPasswordHash(List<OnmsUser> onmsUser) {
		for (OnmsUser eachUser : onmsUser) {
			validateNoPasswordHash(eachUser);
		}
	}
	
	private static void validateNoPasswordHash(OnmsUser user) {
		assertNull(user.getPassword());;
		assertFalse(user.getPasswordSalted());
	}

    @Test
    public void testUser() throws Exception {
        String url = "/users";

        // GET all users
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("admin"));
        OnmsUserList list = JaxbUtils.unmarshal(OnmsUserList.class, xml);
        assertEquals(1, list.getUsers().size());
        assertEquals(xml, "admin", list.getUsers().get(0).getUsername());
        validateNoPasswordHash(list);

        // GET admin user
        xml = sendRequest(GET, url + "/admin", 200);
        assertTrue(xml.contains(">admin<"));
        validateNoPasswordHash(JaxbUtils.unmarshal(OnmsUser.class, xml));

        // GET invalid URL
        sendRequest(GET, url + "/idontexist", 404);
    }
        
    @Test
    public void testWriteUser() throws Exception {
    	createUser("test");

    	// validate creation
    	String xml = sendRequest(GET, "/users/test", 200);
        assertTrue(xml.contains("<user><user-id>test</user-id>"));
        validateNoPasswordHash(JaxbUtils.unmarshal(OnmsUser.class, xml));

        // change password and email
        sendPut("/users/test", "password=MONKEYS&email=test@opennms.org", 303, "/users/test"); 
        
        // validate change of password
        xml = sendRequest(GET, "/users/test", 200); 
        OnmsUser testUser = JaxbUtils.unmarshal(OnmsUser.class,  xml);
        validateNoPasswordHash(testUser); // password should not be in response...
        // ... but in xml-file
        User castorUser = getWebAppContext().getBean(UserManager.class).getUser("test");
        assertEquals(castorUser.getPassword().getContent(), "MONKEYS");
        
        // validate change of email
        assertEquals("test@opennms.org", testUser.getEmail());
    }
    
    @Test
    public void testWriteUserWithEmail() throws Exception {
    	createUser("test123", "test123@opennms.org");
    	
    	// validate creation
    	String xml = sendRequest(GET, "/users/test123", 200);
    	assertNotNull(xml);
    	OnmsUser testUser = JaxbUtils.unmarshal(OnmsUser.class,  xml);
    	assertNotNull(testUser);
    	assertEquals("test123", testUser.getUsername());
    	assertEquals("test123@opennms.org", testUser.getEmail());
    	validateNoPasswordHash(testUser);
    }

    @Test
    public void testWriteALotOfUsers() throws Exception {
        int userCount = 40;

        ExecutorService pool = Executors.newCachedThreadPool();
        List<Future<?>> createFutures = new ArrayList<Future<?>>();
        for (int i = 0; i < userCount; i++) {
            final String userName = "test" + i;
            createFutures.add(pool.submit(Executors.callable(new Runnable() {
                @Override
                public void run() {
                    try {
                        createUser(userName, userName + "@opennms.org");
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail(e.getMessage());
                    }
                }
            })));
        }

        // Wait for all of the REST operations to complete
        for(Future<?> future : createFutures) {
            future.get();
        }

        // validate list
        OnmsUserList users = JaxbUtils.unmarshal(OnmsUserList.class,  sendRequest(GET, "/users", 200));
        assertNotNull(users);
        assertEquals(userCount + 1, users.size()); //+1 because user "admin" is there before creating all the new users.

        // Try changing the password for every user to make sure that they
        // are properly accessible in the UserManager
        for (int i = 0; i < userCount; i++) {
        	// validate each created user
            String xml = sendRequest(GET, "/users/test" + i, 200);
            OnmsUser eachUser = JaxbUtils.unmarshal(OnmsUser.class, xml);
            assertEquals("test" + i, eachUser.getUsername());
            assertEquals("test" + i + " Full Name", eachUser.getFullName());
            assertEquals("test" + i + "@opennms.org", eachUser.getEmail());
            assertEquals("Autogenerated by a unit test...", eachUser.getComments());
            validateNoPasswordHash(eachUser);
            
            // change
            sendPut("/users/test" + i, "password=MONKEYS&email=TEST@OPENNMS.COM", 303, "/users/test" + i);

            // validate change of password
            eachUser = JaxbUtils.unmarshal(OnmsUser.class, sendRequest(GET, "/users/test" + i, 200));
            validateNoPasswordHash(eachUser);
            User castorUser = getWebAppContext().getBean(UserManager.class).getUser("test" + i);
            assertEquals(castorUser.getPassword().getContent(), "MONKEYS");
            
            // validate change of email
            assertEquals("TEST@OPENNMS.COM", eachUser.getEmail());
        }
    }

    @Test
    public void testDeleteUser() throws Exception {
        createUser("deleteMe");
        
        String xml = sendRequest(GET, "/users", 200);
        assertTrue(xml.contains("deleteMe"));

        sendRequest(DELETE, "/users/idontexist", 400);
        
        sendRequest(DELETE, "/users/deleteMe", 200);

        sendRequest(GET, "/users/deleteMe", 404);
    }

    protected void createUser(final String username) throws Exception {
    	createUser(username, null);
    }
    
    protected void createUser(final String username, final String email) throws Exception {
        String userXml = "<user>" +
                "<user-id>" + username + "</user-id>" +
                "<full-name>" + username + " Full Name</full-name>" +
                "{EMAIL}" + 
                "<user-comments>Autogenerated by a unit test...</user-comments>" +
                "<password>21232F297A57A5A743894A0E4A801FC3</password>" +
                "</user>";
        userXml = userXml.replace("{EMAIL}", email != null ?  "<email>" + email + "</email>": "");
        sendPost("/users", userXml, 303, "/users/" + username);
    }
}
