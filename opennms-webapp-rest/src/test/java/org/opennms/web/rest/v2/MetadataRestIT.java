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

        sendPut("/nodes/AAA:BBB/metadata/c1/k1/v1", service, 204);
        sendPut("/nodes/AAA:BBB/metadata/c2/k2/v2", service, 204);

        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata/c3/k3/v3", service, 204);
        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata/c4/k4/v4", service, 204);

        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata/c5/k5/v5", service, 204);
        sendPut("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata/c6/k6/v6", service, 204);
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
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"c1\", \"key\" : \"k1\", \"value\" : \"v1\" }, { \"context\" : \"c2\", \"key\" : \"k2\", \"value\" : \"v2\" }] }"
        ), new JSONObject(json), false);
    }

    @Test
    public void testNodeMetadataXml() throws Exception {
        final String xml = requestXml("/nodes/AAA:BBB/metadata", 200);
        Assert.assertEquals(JAXB.unmarshal(new StringSource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>c1</context><key>k1</key><value>v1</value></meta-data><meta-data><context>c2</context><key>k2</key><value>v2</value></meta-data></meta-data-list>"),
                OnmsMetaDataList.class
        ), JAXB.unmarshal(new StringSource(xml), OnmsMetaDataList.class));
    }

    @Test
    public void testInterfaceMetadataJson() throws Exception {
        final String json = requestJson("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata", 200);
        JSONAssert.assertEquals(new JSONObject(
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"c3\", \"key\" : \"k3\", \"value\" : \"v3\" }, { \"context\" : \"c4\", \"key\" : \"k4\", \"value\" : \"v4\" }] }"
        ), new JSONObject(json), false);
    }

    @Test
    public void testInterfaceMetadataXml() throws Exception {
        final String xml = requestXml("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/metadata", 200);
        Assert.assertEquals(JAXB.unmarshal(new StringSource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>c3</context><key>k3</key><value>v3</value></meta-data><meta-data><context>c4</context><key>k4</key><value>v4</value></meta-data></meta-data-list>"),
                OnmsMetaDataList.class
        ), JAXB.unmarshal(new StringSource(xml), OnmsMetaDataList.class));
    }

    @Test
    public void testServiceMetadataJson() throws Exception {
        final String json = requestJson("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata", 200);
        JSONAssert.assertEquals(new JSONObject(
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"c5\", \"key\" : \"k5\", \"value\" : \"v5\" }, { \"context\" : \"c6\", \"key\" : \"k6\", \"value\" : \"v6\" }] }"
        ), new JSONObject(json), false);
    }

    @Test
    public void testServiceMetadataXml() throws Exception {
        final String xml = requestXml("/nodes/AAA:BBB/ipinterfaces/10.10.10.10/services/ICMP/metadata", 200);
        Assert.assertEquals(JAXB.unmarshal(new StringSource(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>c5</context><key>k5</key><value>v5</value></meta-data><meta-data><context>c6</context><key>k6</key><value>v6</value></meta-data></meta-data-list>"),
                OnmsMetaDataList.class
        ), JAXB.unmarshal(new StringSource(xml), OnmsMetaDataList.class));
    }
}
