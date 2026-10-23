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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sends real multipart requests through the CXF servlet, so the part binding of the upload
 * endpoint is exercised and not only the service method behind it.
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
        "classpath:mock-cm-dao.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/applicationContext-config-service.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class EventConfUploadRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String BOUNDARY = "----nms19813boundary";

    private static final String EVENT_FILE = "<events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">\n"
            + "  <event>\n"
            + "    <uei>uei.opennms.org/test/upload/nms19813</uei>\n"
            + "    <event-label>Upload test event</event-label>\n"
            + "    <descr>Upload test event</descr>\n"
            + "    <logmsg dest=\"logndisplay\">Upload test event</logmsg>\n"
            + "    <severity>Normal</severity>\n"
            + "  </event>\n"
            + "</events>\n";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    public EventConfUploadRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        setUser("admin", new String[]{"ROLE_ADMIN", "ROLE_REST"});
    }

    private MockHttpServletResponse upload(final String partName, final String filename, final String content) throws Exception {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"" + partName + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: application/xml\r\n\r\n"
                + content + "\r\n"
                + "--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8));

        final MockHttpServletRequest request = createRequest(POST, "/eventconf/upload");
        request.setContentType(MediaType.MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
        request.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        request.setContent(body.toByteArray());

        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        return response;
    }

    @Test
    @Transactional
    public void testUploadWithoutUploadPart_ExplainsTheExpectedPartName() throws Exception {
        // curl -F "file=@x.events.xml": the part is not named "upload"
        final MockHttpServletResponse response = upload("file", "nms19813.misnamed.events.xml", EVENT_FILE);

        assertEquals(400, response.getStatus());
        final String body = response.getContentAsString();
        assertTrue("a rejected upload must say why, got: '" + body + "'", body != null && !body.isBlank());
        @SuppressWarnings("unchecked") final Map<String, Object> error = mapper.readValue(body, Map.class);
        assertTrue(String.valueOf(error.get("error")).contains("upload"));
        assertEquals("nothing may be stored", null, eventConfSourceDao.findByName("nms19813.misnamed.events"));
    }

    @Test
    @Transactional
    public void testUploadWithUploadPart_StoresTheSource() throws Exception {
        final MockHttpServletResponse response = upload("upload", "nms19813.events.xml", EVENT_FILE);

        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked") final Map<String, Object> report = mapper.readValue(response.getContentAsString(), Map.class);
        assertEquals(List.of(), report.get("errors"));
        assertEquals(1, ((List<?>) report.get("success")).size());
        assertNotNull(eventConfSourceDao.findByName("nms19813.events"));
    }
}
