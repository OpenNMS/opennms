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

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import org.apache.camel.StringSource;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

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
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MetadataRestIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataRestIT.class);

    @Autowired
    private ServletContext m_context;

    public MetadataRestIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Before
    public void before() throws Exception {
        final String node = "<node type=\"A\" label=\"CCC\" foreignSource=\"AAA\" foreignId=\"BBB\">" +
                "<location>Default</location>" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/nodes", node, 201);

        final String ipInterface = "<ipInterface snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>TestMachine</hostName>" +
                "</ipInterface>";
        sendPost("/nodes/AAA:BBB/ipinterfaces", ipInterface, 201);

        final String service = "<service status=\"A\">" +
                "<serviceType>" +
                "<name>ICMP</name>" +
                "</serviceType>" +
                "</service>";
        sendPost("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services", service, 201);

        sendPut("/nodes/AAA:BBB/metadata/X-c1/k1/v1", service, 204);
        sendPut("/nodes/AAA:BBB/metadata/X-c2/k2/v2", service, 204);

        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata/X-c3/k3/v3", service, 204);
        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata/X-c4/k4/v4", service, 204);

        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata/X-c5/k5/v5", service, 204);
        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata/X-c6/k6/v6", service, 204);
    }

    @After
    public void after() throws Exception {
        sendRequest(DELETE, "/nodes/AAA:BBB", 204);
    }

    private String requestJson(final String url, final int status) throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = createRequest(m_context, GET, url);
        mockHttpServletRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        mockHttpServletRequest.addParameter("limit", "0");
        return sendRequest(mockHttpServletRequest, status);
    }

    private String requestXml(final String url, final int status) throws Exception {
        final MockHttpServletRequest mockHttpServletRequest = createRequest(m_context, GET, url);
        mockHttpServletRequest.addHeader("Accept", MediaType.APPLICATION_XML);
        mockHttpServletRequest.addParameter("limit", "0");
        return sendRequest(mockHttpServletRequest, status);
    }

    @Test
    public void testNodeMetadataJson() throws Exception {
        final String json = requestJson("/nodes/AAA:BBB/metadata", 200);
        JSONAssert.assertEquals(new JSONObject(
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"X-c1\", \"key\" : \"k1\", \"value\" : \"v1\" }, { \"context\" : \"X-c2\", \"key\" : \"k2\", \"value\" : \"v2\" }] }"
        ), new JSONObject(json), false);
    }

    @Test
    public void testNodeMetadataXml() throws Exception {
        final String xml = requestXml("/nodes/AAA:BBB/metadata", 200);
        Assert.assertEquals(JAXB.unmarshal(new StringSource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>X-c1</context><key>k1</key><value>v1</value></meta-data><meta-data><context>X-c2</context><key>k2</key><value>v2</value></meta-data></meta-data-list>"),
                OnmsMetaDataList.class
        ), JAXB.unmarshal(new StringSource(xml), OnmsMetaDataList.class));
    }

    @Test
    public void testInterfaceMetadataJson() throws Exception {
        final String json = requestJson("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata", 200);
        JSONAssert.assertEquals(new JSONObject(
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"X-c3\", \"key\" : \"k3\", \"value\" : \"v3\" }, { \"context\" : \"X-c4\", \"key\" : \"k4\", \"value\" : \"v4\" }] }"
        ), new JSONObject(json), false);
    }

    @Test
    public void testInterfaceMetadataXml() throws Exception {
        final String xml = requestXml("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata", 200);
        Assert.assertEquals(JAXB.unmarshal(new StringSource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>X-c3</context><key>k3</key><value>v3</value></meta-data><meta-data><context>X-c4</context><key>k4</key><value>v4</value></meta-data></meta-data-list>"),
                OnmsMetaDataList.class
        ), JAXB.unmarshal(new StringSource(xml), OnmsMetaDataList.class));
    }

    @Test
    public void testServiceMetadataJson() throws Exception {
        final String json = requestJson("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata", 200);
        JSONAssert.assertEquals(new JSONObject(
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"X-c5\", \"key\" : \"k5\", \"value\" : \"v5\" }, { \"context\" : \"X-c6\", \"key\" : \"k6\", \"value\" : \"v6\" }] }"
        ), new JSONObject(json), false);
    }

    @Test
    public void testServiceMetadataXml() throws Exception {
        final String xml = requestXml("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata", 200);
        Assert.assertEquals(JAXB.unmarshal(new StringSource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>X-c5</context><key>k5</key><value>v5</value></meta-data><meta-data><context>X-c6</context><key>k6</key><value>v6</value></meta-data></meta-data-list>"),
                OnmsMetaDataList.class
        ), JAXB.unmarshal(new StringSource(xml), OnmsMetaDataList.class));
    }
}
