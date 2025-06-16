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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSourceCollection;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ForeignSourceRestServiceIT extends AbstractSpringJerseyRestTestCase {
    
    @Test
    public void testGetDefaultForeignSources() throws Exception {
        String url = "/foreignSources/default";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("name=\"default\""));
        assertTrue(xml.contains("ICMP"));

        sendRequest(DELETE, url, 202);
        sendRequest(DELETE, "/foreignSources/deployed/default", 202);
    }
    
    @Test
    public void testGetDeployedForeignSources() throws Exception {
        String url = "/foreignSources/deployed";
        String xml = sendRequest(GET, url, 200);

        url = "/foreignSources/deployed/count";
        xml = sendRequest(GET, url, 200);
        assertEquals(xml, "0", xml);
    }
    
    @Test
    public void testForeignSources() throws Exception {
        createForeignSource();
        String url = "/foreignSources";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("ICMP"));

        url = "/foreignSources/count";
        xml = sendRequest(GET, url, 200);
        assertEquals(xml, "1", xml);

        url = "/foreignSources/test";
        sendPut(url, "scanInterval=1h", 202, "/foreignSources/test");
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1h</scan-interval>"));
        
        url = "/foreignSources/test";
        sendPut(url, "scanInterval=1h", 202, "/foreignSources/test");
        sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1d</scan-interval>"));
        
        sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 200);
        assertTrue(xml.contains("<scan-interval>1d</scan-interval>"));
    }
    
    @Test
    public void testDetectors() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/detectors";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("<detectors "));
        assertTrue(xml, xml.contains("<detector "));
        assertTrue(xml, xml.contains("name=\"ICMP\""));
        
        url = "/foreignSources/test/detectors/HTTP";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("org.opennms.netmgt.provision.detector.simple.HttpDetector"));

        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
    }

    @Test
    public void testPolicies() throws Exception {
        createForeignSource();

        String url = "/foreignSources/test/policies";
        String xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("<policies "));
        assertTrue(xml, xml.contains("<policy "));
        assertTrue(xml, xml.contains("name=\"lower-case-node\""));
        assertTrue(xml, xml.contains("value=\"Lower-Case-Nodes\""));
        
        url = "/foreignSources/test/policies/all-ipinterfaces";
        xml = sendRequest(GET, url, 200);
        assertTrue(xml, xml.contains("org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        
        xml = sendRequest(DELETE, url, 202);
        xml = sendRequest(GET, url, 404);
    }

    private void createForeignSource() throws Exception {
        // Be sure there are no foreign-sources defined
        System.err.println("[createForeignSource] : deleting existing foreign source definitions");
        ForeignSourceCollection collection = JaxbUtils.unmarshal(ForeignSourceCollection.class, sendRequest(GET, "/foreignSources", 200));
        collection.getForeignSources().forEach(f -> {
            try {
                System.err.printf("[createForeignSource] : deleting %s\n", f.getName());
                sendRequest(DELETE, "/foreignSources/" + f.getName(), 202);
                sendRequest(DELETE, "/requisitions/" + f.getName(), 202);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
        // Create sample foreign source
        System.err.println("[createForeignSource] : creating sample foreign source");
        String fs =
            "<foreign-source xmlns=\"http://xmlns.opennms.org/xsd/config/foreign-source\" name=\"test\">" +
                "<scan-interval>1d</scan-interval>" +
                "<detectors>" + 
                    "<detector class=\"org.opennms.netmgt.provision.detector.datagram.DnsDetector\" name=\"DNS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpDetector\" name=\"HTTP\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.simple.HttpsDetector\" name=\"HTTPS\"/>" +
                    "<detector class=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" name=\"ICMP\"/>" +
                "</detectors>" +
                "<policies>" +
                    "<policy name=\"lower-case-node\" class=\"org.opennms.netmgt.provision.persist.policies.NodeCategoryPolicy\">" +
                        "<parameter key=\"label\" value=\"~^[a-z]$\" />" +
                        "<parameter key=\"category\" value=\"Lower-Case-Nodes\" />" +
                    "</policy>" +
                    "<policy name=\"all-ipinterfaces\" class=\"org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy\" />" +
                "</policies>" +
            "</foreign-source>";
        MockHttpServletResponse response = sendPost("/foreignSources", fs, 202, "/foreignSources/test");
        System.err.println("response = " + stringifyResponse(response));
    }
    
}
