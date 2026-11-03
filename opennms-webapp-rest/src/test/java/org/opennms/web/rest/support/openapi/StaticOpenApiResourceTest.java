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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class StaticOpenApiResourceTest {

    private static class StubResource extends AbstractStaticOpenApiResource {
        StubResource(final String path) {
            super(path);
        }
    }

    private static UriInfo uriInfo(final String baseUri) {
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(URI.create(baseUri));
        return uriInfo;
    }

    @Test
    public void jsonCarriesTheRequestBaseUri() throws Exception {
        final Response response = new StubResource("/openapi/openapi-stub.json")
                .getOpenApi(uriInfo("http://onms.example.com/opennms/api/v2/"), "json");

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getMediaType().toString());

        final String body = (String) response.getEntity();
        assertFalse("placeholder should have been substituted",
                body.contains(AbstractStaticOpenApiResource.BASE_URI_PLACEHOLDER));

        // The trailing slash is dropped, matching what dynamicBasePath produced.
        assertEquals("http://onms.example.com/opennms/api/v2",
                new ObjectMapper().readTree(body).at("/servers/0/url").asText());
    }

    @Test
    public void baseUriWithoutTrailingSlashIsLeftAlone() throws Exception {
        final Response response = new StubResource("/openapi/openapi-stub.json")
                .getOpenApi(uriInfo("http://onms.example.com/opennms/rest"), "json");

        assertEquals("http://onms.example.com/opennms/rest",
                new ObjectMapper().readTree((String) response.getEntity()).at("/servers/0/url").asText());
    }

    @Test
    public void yamlIsDerivedFromTheSameDocument() throws Exception {
        final Response response = new StubResource("/openapi/openapi-stub.json")
                .getOpenApi(uriInfo("http://onms.example.com/opennms/rest"), "yaml");

        assertEquals(200, response.getStatus());
        assertEquals(AbstractStaticOpenApiResource.YAML_MEDIA_TYPE, response.getMediaType().toString());

        final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        assertEquals("http://onms.example.com/opennms/rest",
                yaml.readTree((String) response.getEntity()).at("/servers/0/url").asText());
    }

    @Test
    public void missingDocumentIsReportedRatherThanThrown() {
        final Response response = new StubResource("/openapi/does-not-exist.json")
                .getOpenApi(uriInfo("http://onms.example.com/opennms/rest"), "json");

        assertEquals(404, response.getStatus());
        assertTrue(((String) response.getEntity()).contains("No OpenAPI document"));
    }
}
