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

package org.opennms.web.rest.v1;

import org.junit.Assert;
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
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-timeformat.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TimelineRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(NodeRestServiceIT.class);

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    @JUnitTemporaryDatabase
    public void testTimelineHtml() throws Exception {
        final String nodeXml = "<node type=\"A\" label=\"test-node\">" +
                "<labelSource>H</labelSource>" +
                "<sysContact>Me</sysContact>" +
                "<sysDescription>Big computer</sysDescription>" +
                "<sysLocation>DevJam</sysLocation>" +
                "<sysName>test-node</sysName>" +
                "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                "<createTime>2019-06-04T09:00:00.123-04:00</createTime>" +
                "<lastCapsdPoll>2019-06-04T10:10:30.456-04:00</lastCapsdPoll>" +
                "</node>";

        sendPost("/nodes", nodeXml, 201, null);

        final String ipInterfaceXml = "<ipInterface isManaged=\"M\" snmpPrimary=\"P\">" +
                "<ipAddress>10.10.10.10</ipAddress>" +
                "<hostName>test-node</hostName>" +
                "</ipInterface>";

        sendPost("/nodes/1/ipinterfaces", ipInterfaceXml, 201, null);

        final String icmpServiceXml = "<service source=\"P\" status=\"N\">" +
                "<notify>Y</notify>" +
                "<serviceType>" +
                "<name>ICMP</name>" +
                "</serviceType>" +
                "</service>";

        sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", icmpServiceXml, 201, "/nodes/1/ipinterfaces/10.10.10.10/services/ICMP");

        final String foobarServiceXml = "<service source=\"P\" status=\"N\">" +
                "<notify>Y</notify>" +
                "<serviceType>" +
                "<name>test-/foo/bar</name>" +
                "</serviceType>" +
                "</service>";

        sendPost("/nodes/1/ipinterfaces/10.10.10.10/services", foobarServiceXml, 201, "/nodes/1/ipinterfaces/10.10.10.10/services/test-%2Ffoo%2Fbar");

        final String xml = sendRequest(GET, "/timeline/html/1/10.10.10.10/test-%2Ffoo%2Fbar/1559556000/1559642400/300", 200);

        Assert.assertEquals("<img src=\"/opennms/rest/timeline/image/1/10.10.10.10/test-%2Ffoo%2Fbar/1559556000/1559642400/300\" usemap=\"#1-10.10.10.10-test-%2Ffoo%2Fbar\"><map name=\"1-10.10.10.10-test-%2Ffoo%2Fbar\"></map>", xml);
    }
}