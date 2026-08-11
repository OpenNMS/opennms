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
package org.opennms.web.rest.support.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

public class SwaggerUiResourceTest {

    private static final String DIRECTORY = "/opennms/rest/api-docs";

    private static class StubResource extends AbstractSwaggerUiResource {
    }

    private static UriInfo requestFor(final String path) {
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create(path));
        return uriInfo;
    }

    private static Response get(final String resource) {
        return new StubResource().getResource(resource, requestFor(DIRECTORY + "/"));
    }

    private static String body(final String resource) {
        final Response response = get(resource);
        assertEquals(200, response.getStatus());
        return response.getEntity() instanceof String
                ? (String) response.getEntity()
                : new String((byte[]) response.getEntity());
    }

    @Test
    public void theInitializerPointsAtTheLocalDocument() {
        final String js = body("swagger-initializer.js");
        assertFalse("the webjar's petstore url must not reach the browser",
                js.contains("petstore.swagger.io"));
        assertTrue(js.contains("url: \"../openapi.json\""));
    }

    @Test
    public void indexHtmlLoadsTheInitializer() {
        final String html = body("index.html");
        assertFalse(html.contains("petstore.swagger.io"));
        assertTrue(html.contains("swagger-initializer.js"));
    }

    /**
     * Nothing may reach swagger-ui's own spec-url handling, which is what
     * GHSA-qrmm-w75w-3wpx and CVE-2018-25031 are about.
     */
    @Test
    public void thereIsNoWayToRequestADifferentSpec() {
        for (final String hostile : new String[]{
                "?url=//evil.example.com/spec.json",
                "?url=http://evil.example.com/spec.json",
                "swagger-initializer.js?url=//evil.example.com/spec.json"}) {
            assertEquals("'" + hostile + "' should not resolve to a resource",
                    404, get(hostile).getStatus());
        }
        assertTrue(body("swagger-initializer.js").contains("url: \"../openapi.json\""));
    }

    @Test
    public void pathTraversalIsRejected() {
        for (final String name : new String[]{
                "../../WEB-INF/web.xml",
                "..\\..\\web.xml",
                "openapi/swagger-ui.properties",
                "does-not-exist.js"}) {
            assertEquals("'" + name + "' should not resolve",
                    404, get(name).getStatus());
        }
    }

    @Test
    public void assetsAreServedWithTheirOwnMediaType() {
        assertEquals("image/png", get("favicon-32x32.png").getMediaType().toString());
        assertEquals("text/css", get("swagger-ui.css").getMediaType().toString());
        assertEquals("text/css", get("index.css").getMediaType().toString());
        assertEquals("application/javascript", get("swagger-ui-bundle.js").getMediaType().toString());
    }

    /**
     * index.html is reachable at both spellings, but only from the one ending in a
     * slash do its relative references resolve to the right directory.
     */
    @Test
    public void theDirectoryUrlWithoutItsSlashRedirects() {
        final Response response = new StubResource().getResource("", requestFor(DIRECTORY));
        assertEquals(303, response.getStatus());
        assertEquals(DIRECTORY + "/", response.getLocation().toString());
    }

    @Test
    public void theDirectoryUrlWithItsSlashServesTheIndex() {
        final Response response = new StubResource().getResource("", requestFor(DIRECTORY + "/"));
        assertEquals(200, response.getStatus());
        assertTrue(new String((byte[]) response.getEntity()).contains("swagger-initializer.js"));
    }
}
