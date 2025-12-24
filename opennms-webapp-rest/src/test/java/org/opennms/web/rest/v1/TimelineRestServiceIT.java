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
package org.opennms.web.rest.v1;

import javax.xml.bind.JAXB;

import org.apache.camel.StringSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.model.OnmsServiceType;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-timeformat.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
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

        final String serviceXml = sendRequest(GET, "/nodes/1/ipinterfaces/10.10.10.10/services/test-%2Ffoo%2Fbar", 200);
        final int serviceId = JAXB.unmarshal(new StringSource(serviceXml), OnmsServiceType.class).getId();
        final String xml = sendRequest(GET, "/timeline/html/1/10.10.10.10/" + serviceId + "/1559556000/1559642400/300", 200);
        Assert.assertEquals("<img src=\"/opennms/rest/timeline/image/1/10.10.10.10/" + serviceId + "/1559556000/1559642400/300\" usemap=\"#1-10.10.10.10-" + serviceId + "\"><map name=\"1-10.10.10.10-" + serviceId + "\"></map>", xml);
    }
}