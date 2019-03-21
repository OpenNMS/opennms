/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class UserDefinedLinkRestServiceIT extends AbstractSpringJerseyRestTestCase {

    public UserDefinedLinkRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private ServletContext m_servletContext;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void canCreateAndReadUserDefinedLinks() throws Exception {
        // GET all UDLs, there shouldn't be any yet
        MockHttpServletRequest request = createRequest(m_servletContext, GET, "/userdefinedlinks");
        sendRequest(request, 204);

        // Create a UDL
        final OnmsNode node1 = m_databasePopulator.getNode1();
        final OnmsNode node2 = m_databasePopulator.getNode2();
        String linkJson = "{\n" +
                "      \"node-id-a\": " + node1.getId() + ",\n" +
                "      \"component-label-a\": \"labela\",\n" +
                "      \"node-id-z\": " + node2.getId() + ",\n" +
                "      \"component-label-z\": \"labelb\",\n" +
                "      \"link-id\": \"linkid\",\n" +
                "      \"link-label\": \"my link\",\n" +
                "      \"owner\": \"me\"\n" +
                "}\n";
        sendData(POST, MediaType.APPLICATION_JSON, "/userdefinedlinks", linkJson, 201);

        // Now retrieve all of the UDLs again
        List<UserDefinedLink> udls = getAllUserDefinedLinks();
        // There should be one
        assertThat(udls, hasSize(1));
        // It should be the one we just created
        assertThat(udls.get(0).getLinkLabel(), equalTo("my link"));
        assertThat(udls.get(0).getOwner(), equalTo("me"));
    }

    private List<UserDefinedLink> getAllUserDefinedLinks() throws Exception {
        // GET
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, "/userdefinedlinks");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        // Unmarshal
        final ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
        UserDefinedLinkRestService.UserDefinedLinkCollection udls = mapper.readValue(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                UserDefinedLinkRestService.UserDefinedLinkCollection.class);
        return udls.getObjects();
    }

}
