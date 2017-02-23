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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.category.AvailabilityNode;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class AvailabilityRestServiceIT extends AbstractSpringJerseyRestTestCase {
    @Autowired
    TransactionTemplate m_template;

    @Autowired
    DatabasePopulator m_populator;

    @Autowired
    private ServletContext m_servletContext;

    @Override
    protected void afterServletStart() {
        m_template.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                m_populator.populateDatabase();
            }
        });
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailability() throws Exception {
        String xml = sendRequest(GET, "/availability", new HashMap<String,String>(), 200);
        assertNotNull(xml);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityJson() throws Exception {
        String url = "/availability";

        // GET all items
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        // TODO: The comment and last-updated fields are blank in the objects that are
        // fetched. Figure out how to get them to populate so that we can test serialization
        // of those values.
        //
        JSONObject restObject = new JSONObject(json);
        JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/availability.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);

        // GET node item
        jsonRequest = createRequest(m_servletContext, GET, url  + "/nodes/" + m_populator.getNode1().getId());
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        json = sendRequest(jsonRequest, 200);

        restObject = new JSONObject(json);
        expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/availability_node.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetAvailabilityNode() throws Exception {
        final OnmsNode node = m_populator.getNode1();
        final AvailabilityRestService ars = new AvailabilityRestService();
        ars.setNodeDao(m_populator.getNodeDao());
        final AvailabilityNode an = ars.getAvailabilityNode(node.getId());
        assertNotNull(an);
        System.err.println(JaxbUtils.marshal(an));

        // Compare the object to the same node fetched via REST
        String url = "/availability/nodes/" + node.getId();
        AvailabilityNode restNode = getXmlObject(JaxbUtils.getContextFor(AvailabilityNode.class), url, 200, AvailabilityNode.class);
        Assert.assertNotNull(restNode);
        Assert.assertTrue(an.toString() + " != " + restNode.toString(), an.equals(restNode));
    }
}
