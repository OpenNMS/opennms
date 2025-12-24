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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.io.Files;

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
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class KscRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private ServletContext m_servletContext;

    private File m_configFile = new File("target/test-classes/ksc-performance-reports.xml");

    @Override
    protected void beforeServletStart() throws Exception {
        /* make sure the file is reset every time, otherwise we're reliant on test ordering */
        final File sourceFile = new File("src/test/resources/ksc-performance-reports.xml");
        Files.copy(sourceFile, m_configFile);
        KSC_PerformanceReportFactory.setConfigFile(m_configFile);
        KSC_PerformanceReportFactory.getInstance().reload();
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testReadOnly() throws Exception {
        // Testing GET Collection
        String xml = sendRequest(GET, "/ksc", 200);
        assertTrue(xml, xml.contains("Test 2"));

        xml = sendRequest(GET, "/ksc/0", 200);
        assertTrue(xml, xml.contains("label=\"Test\""));

        sendRequest(GET, "/ksc/3", 404);
    }

    @Test
    public void testKscJson() throws Exception {
        String url = "/ksc/0";

        // GET all users
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);

        JSONObject restObject = new JSONObject(json);
        JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/ksc.json")));
        JSONAssert.assertEquals(expectedObject, restObject, true);
    }

    @Test
    public void testAddGraph() throws Exception {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("title", "foo");
        params.put("reportName", "bar");
        params.put("resourceId", "baz");
        sendRequest(PUT, "/ksc/0", params, 204);

        final String xml = slurp(m_configFile);
        assertTrue(xml, xml.contains("title=\"foo\""));
    }

    @Test
    public void testAddNewGraph() throws Exception {
        final String kscReport = "<kscReport id=\"3\" label=\"foo2\">"
                +"<kscGraph title=\"Title1\" resourceId=\"node[2].responseTime[127.0.0.1]\" timespan=\"1_hour\" graphtype=\"icmp\"/>"
                +"</kscReport>";

        sendPost("/ksc", kscReport, 201, "/ksc/3");

        final String xml = slurp(m_configFile);
        assertTrue(xml, xml.contains("title=\"foo2\""));
    }

    private static String slurp(final File file) throws Exception {
        Reader fileReader = null;
        BufferedReader reader = null;

        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            final StringBuilder sb = new StringBuilder();
            while (reader.ready()) {
                final String line = reader.readLine();
                System.err.println(line);
                sb.append(line).append('\n');
            }

            return sb.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(fileReader);
        }
    }
}
