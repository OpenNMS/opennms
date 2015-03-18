/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.test.JUnitConfigurationEnvironment;
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
        "classpath:/META-INF/opennms/applicationContext-mockEventProxy.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml",

        // Use this to prevent us from overwriting users.xml and groups.xml
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class CategoryRestServiceTest extends AbstractSpringJerseyRestTestCase {

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testCategories() throws Exception {
        // get initial categories
        String xml = sendRequest("GET", "/categories", 200);
        assertNotNull(xml);
        OnmsCategoryCollection categories = JAXB.unmarshal(new StringReader(xml), OnmsCategoryCollection.class);
        int initialSize = categories.size();
        assertNotNull(categories);
        assertEquals(initialSize,categories.size());

        // add category
        createCategory("testCategory");
        xml = sendRequest("GET", "/categories", 200);
        categories = JAXB.unmarshal(new StringReader(xml), OnmsCategoryCollection.class);
        assertEquals(initialSize + 1, categories.size());
        assertTrue(xml.contains("name=\"testCategory\""));

        // add again (should fail)
        sendData("POST", MediaType.APPLICATION_XML,  "/categories", JaxbUtils.marshal(new OnmsCategory("testCategory")), 400);
        xml = sendRequest("GET", "/categories", 200);
        categories = JAXB.unmarshal(new StringReader(xml), OnmsCategoryCollection.class);
        assertEquals(initialSize + 1, categories.size());
        assertTrue(xml.contains("name=\"testCategory\""));
        
        // delete
        sendRequest("DELETE", "/categories/testCategory", 303);
        xml = sendRequest("GET", "/categories", 200);
        categories = JAXB.unmarshal(new StringReader(xml), OnmsCategoryCollection.class);
        assertEquals(initialSize, categories.size());
        assertFalse(xml.contains("name=\"testCategory\""));
    }
    
    @Test
    public void testAddCategory() throws Exception {
        // add with description
        OnmsCategory createMe = new OnmsCategory();
        createMe.setDescription("This is a description");
        createMe.setName("myName");
        createCategory(createMe);

        // verify
        String xml = sendRequest("GET", "/categories/myName", 200);
        OnmsCategory category = JAXB.unmarshal(new StringReader(xml), OnmsCategory.class);
        assertNotNull(category.getId());
        createMe.setId(category.getId());
        assertTrue(category.getId().equals(createMe.getId()));
        assertEquals(createMe, category);
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testUpdateCategory() throws Exception {
        // create
        OnmsCategory createMe = new OnmsCategory();
        createMe.setDescription("This is a description");
        createMe.setName("myName");
        createCategory(createMe);
        
        // verify creation
        String url = "/categories";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<description>This is a description</description>"));
        assertTrue(xml.contains("name=\"myName\""));
        
        // change
        url += "/myName";
        sendPut(url, "description=My Equipment&name=NewCategory", 303, url);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<description>My Equipment</description>"));
        assertTrue(xml.contains("name=\"NewCategory\""));
        xml = sendRequest(DELETE, url, 303);
        assertFalse(xml.contains("<description>My Equipment</description>"));
        assertFalse(xml.contains("name=\"NewCategory\""));
        assertFalse(xml.contains("name=\"myName\""));
        assertFalse(xml.contains("<description>This is a description</description>"));
        sendRequest(GET, url, 200);
    }
    
    @Test
    // does not test in detail, just checks if methods are still there
    // the tests should be done in NodeRestServiceTest
    public void testNodeCategories() throws Exception {
        createNode(); 
        sendRequest(GET, "/categories/nodes/1", 200); // list all categories of node 1
        sendRequest(PUT, "/categories/Routers/nodes/1", 303); // add category to node 1
        sendRequest(DELETE, "/nodes/1/categories/Routers", 200); // remove category from node 1
    }
    
    @Test
    // does not test in detail, just checks if methods are there.
    // the tests should be done in GroupRestServiceTest
    public void testGroupCategories() throws Exception {
        sendRequest(GET, "/categories/groups/Admin", 200); // list all categories of group Admin
        sendRequest(PUT, "/categories/Routers/groups/Admin", 200); // add category Routers to group Admin
        sendRequest(DELETE, "/categories/Routers/groups/Admin", 200); // remove category from Group 
    }
    
    protected void createCategory(final OnmsCategory category) throws Exception {
        sendPost("/categories", JaxbUtils.marshal(category), 303, "/categories/" + category.getName());
    }

    protected void createCategory(final String categoryName) throws Exception {
        OnmsCategory cat = new OnmsCategory();
        cat.setName(categoryName);
        createCategory(cat);
    }
}
