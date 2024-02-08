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
package org.opennms.core.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
public abstract class AbstractSpringJerseyRestJsonTestCase extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpringJerseyRestJsonTestCase.class);

    private static int jsonNodeCounter = 1;

    public static String ACCEPT = "Accept";

    @Override
    protected MockHttpServletResponse sendPost(String url, String xml, int statusCode, final String expectedUrlSuffix) throws Exception {
        LOG.debug("POST {}, expected status code = {}, expected URL suffix = {}", url, statusCode, expectedUrlSuffix);
        final MockHttpServletResponse response = sendData(POST, MediaType.APPLICATION_JSON, url, xml, statusCode);
        if (expectedUrlSuffix != null) {
            final Object header = response.getHeader("Location");
            assertNotNull("Location header is null", header);
            final String location = URLDecoder.decode(header.toString(), StandardCharsets.UTF_8.name());
            final String decodedExpectedUrlSuffix = URLDecoder.decode(expectedUrlSuffix, StandardCharsets.UTF_8.name());
            assertTrue("location '" + location + "' should end with '" + decodedExpectedUrlSuffix + "'", location.endsWith(decodedExpectedUrlSuffix));
        }
        return response;
    }

    @Override
    protected String sendRequest(MockHttpServletRequest request, int expectedStatus, final String expectedUrlSuffix) throws Exception, UnsupportedEncodingException {
        request.addHeader(ACCEPT, MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        final String json = response.getContentAsString();
        assertEquals(expectedStatus, response.getStatus());
        if (expectedUrlSuffix != null) {
            final String location = response.getHeader("Location").toString();
            assertTrue("location '" + location + "' should end with '" + expectedUrlSuffix + "'", location.endsWith(expectedUrlSuffix));
        }
        Thread.sleep(50);
        return json;
    }

    @Override
    protected void createNode(int statusCode) throws Exception {
        JSONObject node = new JSONObject();
        node.put("type", "A");
        node.put("label", "TestMachine" + jsonNodeCounter);
        node.put("labelSource", "H");
        node.put("sysContact", "The Owner");
        node.put("sysDescription", "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386");
        node.put("sysLocation", "DevJam");
        node.put("sysName", "TestMachine" + jsonNodeCounter);
        node.put("sysObjectId", ".1.3.6.1.4.1.8072.3.2.255");
        sendPost("/nodes", node.toString(), statusCode, "/nodes/" + jsonNodeCounter++);
    }

    @Override
    protected void createIpInterface() throws Exception {
        createNode();
        JSONObject ipInterface = new JSONObject();
        ipInterface.put("isManaged", "M");
        ipInterface.put("snmpPrimary", "P");
        ipInterface.put("ipAddress", "10.10.10.10");
        ipInterface.put("netMask", "255.255.255.0");
        ipInterface.put("hostName", "TestMachine");
        ipInterface.put("ipStatus", "1");
        sendPost("/nodes/1/ipinterfaces", ipInterface.toString(), 303, "/nodes/1/ipinterfaces/10.10.10.10");
    }

    @Override
    protected void createSnmpInterface() throws Exception {
        createIpInterface();
        JSONObject snmpInterface = new JSONObject();
        snmpInterface.put("ifIndex", "6");
        snmpInterface.put("ifAdminStatus", "1");
        snmpInterface.put("ifDescr", "en1");
        snmpInterface.put("ifName", "en1");
        snmpInterface.put("ifOperStatus", "1");
        snmpInterface.put("ifSpeed", "10000000");
        snmpInterface.put("ifType", "6");
        snmpInterface.put("physAddr", "001e5271136d");
        sendPost("/nodes/1/snmpinterfaces", snmpInterface.toString(), 303, "/nodes/1/snmpinterfaces/6");
    }

    @Override
    protected void createService() throws Exception {
        createIpInterface();
        JSONObject service = new JSONObject();
        service.put("source", "P");
        service.put("status", "N");
        service.put("notify", "Y");
        JSONObject serviceType = new JSONObject();
        serviceType.put("name", "ICMP");
        service.put("serviceType",serviceType);
        sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", service.toString(), 303, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP");
    }

    @Override
    protected void createCategory() throws Exception {
        createNode();
        JSONObject category = new JSONObject();
        category.put("name", "Routers");
        category.put("description", "Core Routers");
        sendPost("/categories", category.toString(), 303, "/categories/Routers");
    }

}
