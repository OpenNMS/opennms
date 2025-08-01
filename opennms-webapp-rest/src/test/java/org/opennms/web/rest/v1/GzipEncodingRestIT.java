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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.io.CharStreams;

/**
 * Verifies that gzip encoding is supported when making
 * REST API calls.
 *
 * @author jwhite
 */
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
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class GzipEncodingRestIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private ServletContext m_context;

    @Before
    @Override
    public void setUp() throws Throwable {
        super.setUp();
        Assert.assertNotNull(populator);
        Assert.assertNotNull(applicationDao);

        populator.populateDatabase();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        populator.resetDatabase();
    }

    @Test
    public void testGzippedEncodedReponse() throws Exception {
        // Retrieve the results of request without any encoding headers set
        final MockHttpServletRequest request = createRequest(m_context, GET, "/nodes");
        String xml = sendRequest(request, 200);

        // Now set the header, and re-issue that same request
        request.addHeader("Accept-Encoding", "gzip");
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(200, response.getStatus());

        // Decompress the response
        assertEquals("gzip", response.getHeader("Content-Encoding"));
        InputStream gzip = new GZIPInputStream(new ByteArrayInputStream(response.getContentAsByteArray()));
        String ungzippedXml = CharStreams.toString(new InputStreamReader(gzip, StandardCharsets.UTF_8));

        // The resulting strings from both requests should match
        assertEquals(xml, ungzippedXml);
    }
}
