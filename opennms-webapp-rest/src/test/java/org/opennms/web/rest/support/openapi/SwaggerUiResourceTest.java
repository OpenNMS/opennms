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

import javax.ws.rs.core.Response;

import org.junit.Test;

public class SwaggerUiResourceTest {

    private static class StubResource extends AbstractSwaggerUiResource {
    }

    private static String body(final String resource) {
        final Response response = new StubResource().getResource(resource);
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
            final Response response = new StubResource().getResource(hostile);
            assertEquals("'" + hostile + "' should not resolve to a resource",
                    404, response.getStatus());
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
                    404, new StubResource().getResource(name).getStatus());
        }
    }

    @Test
    public void assetsAreServedWithTheirOwnMediaType() {
        final StubResource resource = new StubResource();
        assertEquals("image/png", resource.getResource("favicon-32x32.png").getMediaType().toString());
        assertEquals("text/css", resource.getResource("swagger-ui.css").getMediaType().toString());
        assertEquals("text/css", resource.getResource("index.css").getMediaType().toString());
        assertEquals("application/javascript",
                resource.getResource("swagger-ui-bundle.js").getMediaType().toString());
    }
}
