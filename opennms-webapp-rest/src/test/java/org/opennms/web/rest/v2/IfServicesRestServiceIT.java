/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class IfServicesRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(IfServicesRestServiceIT.class);

    @Autowired
    private ServletContext m_context;

    public IfServicesRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    public void testJsonIds() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = createRequest(m_context, GET, "/ifservices");
        mockHttpServletRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        mockHttpServletRequest.addParameter("limit", "0");
        final JSONObject responseObject = new JSONObject(sendRequest(mockHttpServletRequest, 200));
        final JSONArray serviceArray = responseObject.getJSONArray("service");
        for (final Object jsonObject : serviceArray) {
            assertTrue(((JSONObject) jsonObject).getInt("ipInterfaceId") != 0);
            assertTrue(((JSONObject) jsonObject).getInt("id") != 0);
        }
    }

    @Test
    @Transactional
    public void testXmlIds() throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = createRequest(m_context, GET, "/ifservices");
        mockHttpServletRequest.addHeader("Accept", MediaType.APPLICATION_XML);
        mockHttpServletRequest.addParameter("limit", "0");

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new ByteArrayInputStream(sendRequest(mockHttpServletRequest, 200).getBytes("UTF-8")));

        final NodeList serviceList = document.getElementsByTagName("service");
        assertTrue(serviceList.getLength() > 0);

        for (int i = 0; i < serviceList.getLength(); i++) {
            final Node node = serviceList.item(i).getAttributes().getNamedItem("id");
            assertTrue(Integer.valueOf(node.getTextContent()) != 0);
        }

        final NodeList ipInterfaceIdList = document.getElementsByTagName("ipInterfaceId");
        assertTrue(ipInterfaceIdList.getLength() > 0);

        for (int i = 0; i < ipInterfaceIdList.getLength(); i++) {
            assertTrue(Integer.valueOf(ipInterfaceIdList.item(i).getTextContent()) != 0);
        }
    }
}
