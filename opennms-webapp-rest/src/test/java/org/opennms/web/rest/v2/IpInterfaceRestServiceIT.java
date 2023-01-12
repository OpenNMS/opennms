/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;

import org.jfree.util.Log;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class IpInterfaceRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(IpInterfaceRestServiceIT.class);

    public IpInterfaceRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    @JUnitTemporaryDatabase
    public void testFiqlSearch() throws Exception {
        // Add a node with an IP interface
        createNode(201);
        createIpInterface();

        String url = "/ipinterfaces";

        LOG.warn(sendRequest(GET, url, parseParamData("_s=ipAddress==10.10.10.10"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=node.label==*1"), 200));
        LOG.warn(sendRequest(GET, url, parseParamData("_s=snmpPrimary==P"), 200));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAllEndPoints() throws Exception {
        sendPost("/monitoringLocations", "<location location-name=\"location1\" monitoring-area=\"location1\" priority=\"1\"/>", 201);
        String node1 = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"JUnit\" foreignId=\"TestMachine1\">" +
                "<location>location1</location>" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/nodes", node1, 201);

        Log.warn(sendRequest(GET, "/nodes/1/ipinterfaces", 204));
        Log.warn(sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10", 404));
        Log.warn(sendRequest(GET, "/ipinterfaces", 204));
        Log.warn(sendRequest(GET, "/ipinterfaces/10.10.10.10", 404));

        String ipInterface1 = "<ipInterface snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<nodeId>1</nodeId>" +
                "<hostName>TestMachine1</hostName>" +
                "</ipInterface>";
        sendPost("/nodes/1/ipinterfaces", ipInterface1, 201);
        LOG.warn(sendRequest(GET, "/ipinterfaces", 200));
        LOG.warn(sendRequest(GET, "/ipinterfaces/10.10.10.10", 200)); // By IP Address

        // add another node in a different location with a duplicate IP
        sendPost("/monitoringLocations", "<location location-name=\"location2\" monitoring-area=\"location2\" priority=\"1\"/>", 201);
        String node2 = "<node type=\"A\" label=\"TestMachine2\" foreignSource=\"JUnit\" foreignId=\"TestMachine2\">" +
                "<location>location2</location>" +
                "<labelSource>H</labelSource>" +
                "<sysContact>The Owner</sysContact>" +
                "<sysDescription>" +
                "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                "</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>TestMachine1</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "</node>";
        sendPost("/nodes", node2, 201);

        String ipInterface2 = "<ipInterface snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<nodeId>2</nodeId>" +
                "<hostName>TestMachine2</hostName>" +
                "</ipInterface>";
        sendPost("/nodes/2/ipinterfaces", ipInterface2, 201);
        LOG.warn(sendRequest(GET, "/ipinterfaces", 200));

        // By IP address should fail if more than one interface maches, use the query format instead
        LOG.warn(sendRequest(GET, "/ipinterfaces/10.10.10.10", 400));

        final String jsonResponse = sendRequest(GET, "/ipinterfaces", parseParamData("_s=ipAddress==10.10.10.10"), 200);
        final JSONObject response = new org.json.JSONObject(jsonResponse);
        assertEquals(2, response.getInt("count"));
    }
}