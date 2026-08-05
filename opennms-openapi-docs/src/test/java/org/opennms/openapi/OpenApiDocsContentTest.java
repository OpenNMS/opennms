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
package org.opennms.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads the committed documents off the classpath, the way the webapp will.
 *
 * The paths asserted here are sentinels, one per contributing module. If a
 * dependency goes missing from this module's pom, generation still succeeds and
 * those endpoints quietly disappear; this is what catches that.
 */
public class OpenApiDocsContentTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** A regenerate run writes after resources were copied, so the classpath is stale. */
    @Before
    public void skipWhileRegenerating() {
        org.junit.Assume.assumeFalse(Boolean.getBoolean("openapi.regenerate"));
    }

    @Test
    public void v1DescribesEveryContributingModule() throws Exception {
        final JsonNode document = load("openapi-v1.json");

        assertEquals("OpenNMS V1 RESTful API", document.at("/info/title").asText());
        assertBaseUriPlaceholder(document);
        assertBasicAuth(document);

        // opennms-webapp-rest
        assertHasPath(document, "/nodes");
        assertHasPath(document, "/alarms");
        assertHasPath(document, "/events");
        assertHasPath(document, "/requisitions");
        // org.opennms.features.measurements.rest
        assertHasPath(document, "/measurements");

        assertTrue("v1 document describes suspiciously few paths: " + document.get("paths").size(),
                document.get("paths").size() > 150);
    }

    @Test
    public void v2DescribesEveryContributingModule() throws Exception {
        final JsonNode document = load("openapi-v2.json");

        assertEquals("OpenNMS V2 RESTful API", document.at("/info/title").asText());
        assertBaseUriPlaceholder(document);
        assertBasicAuth(document);

        // opennms-webapp-rest
        assertHasPath(document, "/nodes");
        assertHasPath(document, "/alarms");
        assertHasPath(document, "/events");
        // org.opennms.features.bsm.rest.impl
        assertHasPath(document, "/business-services");
        // org.opennms.features.status.rest
        assertHasPath(document, "/status/applications");
        // org.opennms.features.geolocation.rest
        assertHasPath(document, "/geolocation");

        assertTrue("v2 document describes suspiciously few paths: " + document.get("paths").size(),
                document.get("paths").size() > 150);
    }

    private static void assertBaseUriPlaceholder(final JsonNode document) {
        assertEquals("the serving resource rewrites this per request",
                OpenApiDocGenerator.BASE_URI_PLACEHOLDER, document.at("/servers/0/url").asText());
    }

    private static void assertBasicAuth(final JsonNode document) {
        assertEquals("http", document.at("/components/securitySchemes/basicAuth/type").asText());
        assertEquals("basic", document.at("/components/securitySchemes/basicAuth/scheme").asText());
    }

    private static void assertHasPath(final JsonNode document, final String path) {
        assertNotNull("document is missing " + path, document.get("paths").get(path));
    }

    private static JsonNode load(final String fileName) throws Exception {
        try (InputStream in = OpenApiDocsContentTest.class.getResourceAsStream("/openapi/" + fileName)) {
            assertNotNull("/openapi/" + fileName + " is not on the classpath", in);
            return MAPPER.readTree(in);
        }
    }
}
