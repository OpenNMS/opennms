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
import org.json.JSONArray;
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
public class SnmpInterfaceRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpInterfaceRestServiceIT.class);

    public SnmpInterfaceRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
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

        Log.warn(sendRequest(GET, "/nodes/1/snmpinterfaces", 204));
        Log.warn(sendRequest(GET, "/nodes/1/snmpinterfaces/6", 404));
        Log.warn(sendRequest(GET, "/snmpinterfaces", 204));
        Log.warn(sendRequest(GET, "/snmpinterfaces/1", 404));

        String snmpInterface1 = "<snmpInterface ifIndex=\"6\">" +
                "<ifAdminStatus>1</ifAdminStatus>" +
                "<ifDescr>en1</ifDescr>" +
                "<ifName>en1</ifName>" +
                "<ifOperStatus>1</ifOperStatus>" +
                "<ifSpeed>10000000</ifSpeed>" +
                "<ifType>6</ifType>" +
                "<netMask>255.255.255.0</netMask>" +
                "<physAddr>001e5271136d</physAddr>" +
                "</snmpInterface>";
        sendPost("/nodes/1/snmpinterfaces", snmpInterface1, 201, "/nodes/1/snmpinterfaces/6");

        LOG.warn(sendRequest(GET, "/snmpinterfaces", parseParamData("_s=ifIndex==6"), 200));
        LOG.warn(sendRequest(GET, "/snmpinterfaces", parseParamData("_s=node.label==*1"), 200));
        LOG.warn(sendRequest(GET, "/snmpinterfaces", parseParamData("_s=ifName==en1"), 200));

        JSONObject response = new JSONObject(sendRequest(GET, "/snmpinterfaces", 200));
        assertEquals(1, response.getInt("count"));
        JSONArray objects = response.getJSONArray("snmpInterface");
        JSONObject object = objects.getJSONObject(0);
        LOG.warn(sendRequest(GET, "/snmpinterfaces/" + object.getInt("id"), 200)); // By ID

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

        String snmpInterface2 = "<snmpInterface ifIndex=\"6\">" +
                "<ifAdminStatus>1</ifAdminStatus>" +
                "<ifDescr>en1</ifDescr>" +
                "<ifName>en1</ifName>" +
                "<ifOperStatus>1</ifOperStatus>" +
                "<ifSpeed>10000000</ifSpeed>" +
                "<ifType>6</ifType>" +
                "<netMask>255.255.255.0</netMask>" +
                "<physAddr>001e5271136d</physAddr>" +
                "</snmpInterface>";
        sendPost("/nodes/2/snmpinterfaces", snmpInterface2, 201, "/nodes/2/snmpinterfaces/6");
        LOG.warn(sendRequest(GET, "/snmpinterfaces", 200));

        String jsonResponse = sendRequest(GET, "/snmpinterfaces", parseParamData("_s=ifIndex==6"), 200);
        response = new org.json.JSONObject(jsonResponse);
        assertEquals(2, response.getInt("count"));
    }
}