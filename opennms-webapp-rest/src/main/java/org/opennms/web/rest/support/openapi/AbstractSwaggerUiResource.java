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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves the Swagger UI webjar.
 *
 * The webjar ships pointing at the Swagger petstore, so the spec URL is
 * rewritten to the sibling openapi.json. There is deliberately no way to ask
 * for a different spec: swagger-ui's own {@code ?url=} support is the subject of
 * GHSA-qrmm-w75w-3wpx and CVE-2018-25031, and is disabled by default from 4.1.3
 * on.
 */
public abstract class AbstractSwaggerUiResource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSwaggerUiResource.class);

    private static final String WEBJAR_ROOT = "/META-INF/resources/webjars/swagger-ui/";

    /** The one file in the webjar that carries the spec URL. */
    private static final String INITIALIZER = "swagger-initializer.js";

    private static final String PLACEHOLDER_SPEC_URL = "https://petstore.swagger.io/v2/swagger.json";

    /** Resolves to the openapi.json of whichever ReST context serves api-docs/. */
    private static final String SPEC_URL = "../openapi.json";

    private static final String JAVASCRIPT = "application/javascript";

    private static final Map<String, String> MEDIA_TYPES = Map.of(
            "html", MediaType.TEXT_HTML,
            "js", JAVASCRIPT,
            "css", "text/css",
            "png", "image/png",
            "json", MediaType.APPLICATION_JSON);

    private static final String VERSION = readVersion();

    @GET
    @Path("{resource:.*}")
    public Response getResource(@PathParam("resource") final String resource) {
        if (VERSION == null) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
                    .entity("Swagger UI is not available in this build.\n").build();
        }

        final String name = (resource == null || resource.isEmpty() || "/".equals(resource))
                ? "index.html" : resource;

        // The webjar is a flat directory, so any separator is an escape attempt.
        if (name.contains("/") || name.contains("\\") || name.contains("..")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final byte[] content;
        try {
            content = read(WEBJAR_ROOT + VERSION + "/" + name);
        } catch (final IOException e) {
            LOG.error("Could not read Swagger UI resource {}", name, e);
            return Response.serverError().build();
        }

        if (content == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (INITIALIZER.equals(name)) {
            final String js = new String(content, StandardCharsets.UTF_8)
                    .replace(PLACEHOLDER_SPEC_URL, SPEC_URL);
            return Response.ok(js).type(JAVASCRIPT).build();
        }

        return Response.ok(content).type(mediaType(name)).build();
    }

    private static String mediaType(final String name) {
        final int dot = name.lastIndexOf('.');
        final String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase();
        return MEDIA_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM);
    }

    private static byte[] read(final String path) throws IOException {
        try (InputStream in = AbstractSwaggerUiResource.class.getResourceAsStream(path)) {
            return in == null ? null : in.readAllBytes();
        }
    }

    /**
     * The webjar nests its content under a version directory. Returns null
     * rather than throwing, so a packaging slip costs the Swagger UI page
     * instead of the whole ReST context.
     */
    private static String readVersion() {
        final Properties properties = new Properties();
        try (InputStream in = AbstractSwaggerUiResource.class
                .getResourceAsStream("/openapi/swagger-ui.properties")) {
            if (in == null) {
                LOG.error("/openapi/swagger-ui.properties is missing; Swagger UI will not be served");
                return null;
            }
            properties.load(in);
        } catch (final IOException e) {
            LOG.error("Could not read /openapi/swagger-ui.properties", e);
            return null;
        }
        return properties.getProperty("version");
    }
}
