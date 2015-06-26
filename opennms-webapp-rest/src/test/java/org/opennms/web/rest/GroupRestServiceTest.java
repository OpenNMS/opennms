/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml",

        // Use this to prevent us from overwriting users.xml and groups.xml
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class GroupRestServiceTest extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(GroupRestServiceTest.class);

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testGroup() throws Exception {
        // Testing GET Collection
        String xml = sendRequest(GET, "/groups", 200);
        assertTrue(xml.contains("Admin"));
        LOG.debug("testGroup: XML = " + xml);
        OnmsGroupList list = JaxbUtils.unmarshal(OnmsGroupList.class, xml);
        assertEquals(1, list.getGroups().size());
        assertEquals(xml, "Admin", list.getGroups().get(0).getName());

        xml = sendRequest(GET, "/groups/Admin", 200);
        assertTrue(xml.contains(">Admin<"));
        sendRequest(GET, "/groups/idontexist", 404);
    }
    
    @Test
    public void testWriteGroup() throws Exception {
        createGroup("test");
        
        String xml = sendRequest(GET, "/groups/test", 200);
        assertTrue(xml.contains("<group><name>test</name>"));

        sendPut("/groups/test", "comments=MONKEYS", 303, "/groups/test");

        xml = sendRequest(GET, "/groups/test", 200);
        assertTrue(xml.contains(">MONKEYS<"));
    }

    @Test
    public void testDeleteGroup() throws Exception {
        createGroup("deleteMe");
        
        String xml = sendRequest(GET, "/groups", 200);
        assertTrue(xml.contains("deleteMe"));

        sendRequest(DELETE, "/groups/idontexist", 400);
        
        sendRequest(DELETE, "/groups/deleteMe", 200);

        sendRequest(GET, "/groups/deleteMe", 404);
    }

    @Test
    public void testUsers() throws Exception {
        createGroup("deleteMe");

        // test add user which does not exist to group
        sendRequest(PUT, "/groups/deleteMe/users/totallyUniqueUser", 400);
        
        // create user first
        createUser("totallyUniqueUser");

        LOG.debug("add totallyUniqueUser to deleteMe");
        // add user to group
        sendRequest(PUT, "/groups/deleteMe/users/totallyUniqueUser", 303);
        String xml = sendRequest(GET, "/groups/deleteMe", 200);
        assertTrue(xml.contains("totallyUniqueUser"));

        LOG.debug("add totallyUniqueUser to deleteMe a second time");
        // add user to group twice
        sendRequest(PUT, "/groups/deleteMe/users/totallyUniqueUser", 400); // already added

        LOG.debug("get the list of users");
        xml = sendRequest(GET, "/groups/deleteMe/users", 200);
        assertEquals(1, StringUtils.countMatches(xml,  "totallyUniqueUser"));
        assertEquals(1, JaxbUtils.unmarshal(OnmsUserList.class, xml).size());
        
        LOG.debug("get the totallyUniqueUser");
        //get specific user
        xml = sendRequest(GET, "/groups/deleteMe/users/totallyUniqueUser", 200);
        assertNotNull(xml);
        OnmsUser user = JaxbUtils.unmarshal(OnmsUser.class,  xml);
        assertNotNull(user);
        assertEquals("totallyUniqueUser", user.getUsername());
        
        LOG.debug("delete temporary users");
        // clean up
        sendRequest(DELETE, "/groups/deleteMe/users/totallyBogusUser", 400);
        sendRequest(DELETE, "/groups/deleteMe/users/totallyUniqueUser", 200);
        xml = sendRequest(GET, "/groups/deleteMe", 200);
        assertFalse(xml.contains("totallyUniqueUser"));
        
        LOG.debug("check that users were deleted");
        // list all users of group
        xml = sendRequest(GET, "/groups/deleteMe/users", 200);
        assertNotNull(xml);
        assertEquals(0, JaxbUtils.unmarshal(OnmsUserList.class, xml).size());
        
        LOG.debug("check specific get when no users");
        // get specific user
        sendRequest(GET, "/groups/deleteMe/users/totallyUniqueUser", 404);
    }

    @Test
    public void testAddGroup() throws Exception {
        OnmsGroupList groups = JaxbUtils.unmarshal(OnmsGroupList.class, sendRequest(GET, "/groups", 200));
        assertNotNull(groups);
        assertTrue(groups.size() > 0);
        int initialGroupSize = groups.size();

        createGroup("My little Test group");         // add group
        String xml = sendRequest(GET, "/groups", 200);
        groups = JaxbUtils.unmarshal(OnmsGroupList.class, xml);
        assertEquals(initialGroupSize+1, groups.size());
        assertTrue(xml.contains(">My little Test group<"));
    }

    @Test
    public void testCategories() throws Exception {
        
        createGroup("testGroup");
        String xml = sendRequest(GET, "/groups/testGroup/categories", 200);
        assertNotNull(xml);
        OnmsCategoryCollection categories = JAXB.unmarshal(new StringReader(xml), OnmsCategoryCollection.class);
        assertNotNull(categories);
        assertTrue(categories.getCategories().isEmpty());

        // add category to group
        sendRequest(PUT, "/groups/testGroup/categories/testCategory", 400); // fails, because Category is not there
        createCategory("testCategory"); // create category
        sendRequest(PUT, "/groups/testGroup/categories/testCategory", 200); // should not fail, because Category is now there
        xml = sendRequest(GET, "/groups/testGroup/categories/testCategory", 200); // get data
        assertNotNull(xml);
        OnmsCategory category = JAXB.unmarshal(new StringReader(xml), OnmsCategory.class);
        assertNotNull(category);
        assertEquals("testCategory", category.getName());

        // add again (fails)
        sendRequest(PUT, "/groups/testGroup/categories/testCategory", 400); // should fail, because Category is already there

        // remove category from group
        sendRequest(DELETE, "/groups/testGroup/categories/testCategory", 200); // should not fail
        sendRequest(DELETE, "/groups/testGroup/categories/testCategory", 400); // should fail, because category is already removed

        // test that all categories for group "testGroup" have been removed
        xml = sendRequest(GET, "/groups/testGroup/categories", 200);
        assertNotNull(xml);
        categories = JaxbUtils.unmarshal(OnmsCategoryCollection.class, xml);
        assertNotNull(categories);
        assertTrue(categories.getCategories().isEmpty());
    }

    protected void createCategory(final String categoryName) throws Exception {
        OnmsCategory cat = new OnmsCategory(categoryName);
        sendPost("/categories", JaxbUtils.marshal(cat), 303, "/categories/" + categoryName);
    }
    
    private void createUser(final String userName) throws Exception {
        OnmsUser user = new OnmsUser();
        user.setUsername(userName);
        sendPost("/users", JaxbUtils.marshal(user), 303, "/users/" + userName);
    }
    
    protected void createGroup(final String groupname) throws Exception {
        String group = "<group>" +
                "<name>" + groupname + "</name>" +
                "<comments>" + groupname + "</comments>" +
                "</group>";
        sendPost("/groups", group, 303, "/groups/" + groupname);
    }
}
