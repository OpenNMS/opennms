/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
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
