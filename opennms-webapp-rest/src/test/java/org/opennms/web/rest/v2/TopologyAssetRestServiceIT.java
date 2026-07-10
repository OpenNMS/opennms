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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Drives {@code /api/v2/topology/assets} end to end through the CXF test
 * harness: raw-body image upload, metadata listing/filtering, byte serving
 * with ETag revalidation, deletion, and the validation/error statuses
 * (400 missing name/kind, 413 over the per-kind cap, 415 non-image type,
 * 404 unknown id).
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
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class TopologyAssetRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String BASE = "/topology/assets";

    private static final byte[] IMAGE = "png-bytes-stand-in".getBytes(StandardCharsets.UTF_8);

    private final ObjectMapper m_mapper = new ObjectMapper();

    public TopologyAssetRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "WARN");
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    /**
     * POSTs raw image bytes. The harness's sendData() can't carry query
     * parameters, so this sets the query string and body itself.
     */
    private MockHttpServletResponse postAsset(final String query, final String contentType, final byte[] body,
                                              final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(POST, BASE);
        request.setQueryString(query);
        for (final String pair : query.split("&")) {
            final int eq = pair.indexOf('=');
            if (eq > 0) {
                // The query string carries encoded values; parameters carry decoded
                // ones (as a real servlet container would present them).
                request.addParameter(pair.substring(0, eq),
                        java.net.URLDecoder.decode(pair.substring(eq + 1), java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        request.setContentType(contentType);
        request.setContent(body);
        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(response.getErrorMessage(), expectedStatus, response.getStatus());
        return response;
    }

    private MockHttpServletResponse getRaw(final String url, final String ifNoneMatch, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        if (ifNoneMatch != null) {
            request.addHeader("If-None-Match", ifNoneMatch);
        }
        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(response.getErrorMessage(), expectedStatus, response.getStatus());
        return response;
    }

    private static String lastPathSegment(final String location) {
        return location == null ? null : location.substring(location.lastIndexOf('/') + 1);
    }

    @Test
    @JUnitTemporaryDatabase
    public void uploadServeAndDeleteLifecycle() throws Exception {
        // Empty catalog to start.
        assertEquals(0, m_mapper.readTree(sendRequest(GET, BASE, 200)).size());

        // Upload a background.
        final MockHttpServletResponse post = postAsset("name=DC+floor+plan&kind=background", "image/png", IMAGE, 201);
        final String id = lastPathSegment(post.getHeader("Location"));
        assertNotNull("POST should return a Location with an id", id);
        final JsonNode created = m_mapper.readTree(post.getContentAsString());
        assertEquals("DC floor plan", created.get("name").asText());
        assertEquals("background", created.get("kind").asText());
        assertEquals("image/png", created.get("mimeType").asText());
        assertEquals(IMAGE.length, created.get("sizeBytes").asInt());
        assertEquals("admin", created.get("owner").asText());

        // Serve the bytes back: body, content type, cacheability.
        final MockHttpServletResponse bytes = getRaw(BASE + "/" + id, null, 200);
        assertArrayEquals(IMAGE, bytes.getContentAsByteArray());
        assertTrue(bytes.getContentType().startsWith("image/png"));
        final String etag = bytes.getHeader("ETag");
        assertNotNull("byte responses must carry an ETag", etag);
        assertNotNull(bytes.getHeader("Cache-Control"));

        // Conditional GET with the ETag revalidates as a 304 without a body.
        final MockHttpServletResponse notModified = getRaw(BASE + "/" + id, etag, 304);
        assertEquals(0, notModified.getContentAsByteArray().length);

        // Metadata endpoint and listing.
        final JsonNode meta = m_mapper.readTree(sendRequest(GET, BASE + "/" + id + "/meta", 200));
        assertEquals(id, meta.get("id").asText());
        final JsonNode all = m_mapper.readTree(sendRequest(GET, BASE, 200));
        assertEquals(1, all.size());

        // Delete removes metadata and bytes; a second delete is a 404.
        sendRequest(DELETE, BASE + "/" + id, 204);
        sendRequest(GET, BASE + "/" + id, 404);
        sendRequest(DELETE, BASE + "/" + id, 404);
    }

    @Test
    @JUnitTemporaryDatabase
    public void listsCanFilterByKind() throws Exception {
        postAsset("name=floor&kind=background", "image/png", IMAGE, 201);
        postAsset("name=glyph&kind=icon", "image/webp", IMAGE, 201);

        assertEquals(2, m_mapper.readTree(sendRequest(GET, BASE, 200)).size());

        final MockHttpServletRequest request = createRequest(GET, BASE);
        request.setQueryString("kind=icon");
        request.addParameter("kind", "icon");
        final MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(200, response.getStatus());
        final JsonNode icons = m_mapper.readTree(response.getContentAsString());
        assertEquals(1, icons.size());
        assertEquals("glyph", icons.get(0).get("name").asText());
    }

    @Test
    @JUnitTemporaryDatabase
    public void rejectsBadUploads() throws Exception {
        // Missing name / missing or unknown kind.
        postAsset("kind=background", "image/png", IMAGE, 400);
        postAsset("name=x", "image/png", IMAGE, 400);
        postAsset("name=x&kind=wallpaper", "image/png", IMAGE, 400);

        // Non-image content types are rejected by @Consumes.
        postAsset("name=x&kind=icon", "text/plain", IMAGE, 415);
        postAsset("name=x&kind=icon", "image/svg+xml", IMAGE, 415);

        // Per-kind size caps: this payload is over the icon cap (512 KiB)
        // but under the background cap (10 MiB).
        final byte[] big = new byte[600 * 1024];
        Arrays.fill(big, (byte) 'a');
        postAsset("name=big&kind=icon", "image/png", big, 413);
        postAsset("name=big&kind=background", "image/png", big, 201);

        // Empty body.
        postAsset("name=x&kind=icon", "image/png", new byte[0], 400);

        // Unknown asset id.
        sendRequest(GET, BASE + "/no-such-id", 404);
        sendRequest(GET, BASE + "/no-such-id/meta", 404);
    }
}
