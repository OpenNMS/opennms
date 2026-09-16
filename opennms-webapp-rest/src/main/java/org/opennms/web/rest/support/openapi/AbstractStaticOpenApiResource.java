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

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * Serves an OpenAPI document generated at build time by opennms-openapi-docs.
 *
 * Subclasses supply the document and carry the {@code @Path}, so each CXF
 * servlet's base package scan picks up exactly one of them.
 */
public abstract class AbstractStaticOpenApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStaticOpenApiResource.class);

    public static final String YAML_MEDIA_TYPE = "application/yaml";

    /** Must match OpenApiDocGenerator.BASE_URI_PLACEHOLDER. */
    static final String BASE_URI_PLACEHOLDER = "__OPENNMS_BASE_URI__";

    private final String resourcePath;

    private volatile String jsonTemplate;
    private volatile String yamlTemplate;

    protected AbstractStaticOpenApiResource(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, YAML_MEDIA_TYPE})
    public Response getOpenApi(@Context final UriInfo uriInfo, @PathParam("type") final String type) {
        final boolean yaml = "yaml".equals(type);

        final String template;
        try {
            template = yaml ? yamlTemplate() : jsonTemplate();
        } catch (final IOException e) {
            LOG.error("Could not read the OpenAPI document {}", resourcePath, e);
            return Response.serverError().type(MediaType.TEXT_PLAIN)
                    .entity("The OpenAPI document could not be read.\n").build();
        }

        if (template == null) {
            LOG.warn("The OpenAPI document {} is not on the classpath; the opennms-openapi-docs"
                    + " artifact is missing from this deployment", resourcePath);
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
                    .entity("No OpenAPI document is available in this build.\n").build();
        }

        return Response.ok(template.replace(BASE_URI_PLACEHOLDER, baseUri(uriInfo)))
                .type(yaml ? YAML_MEDIA_TYPE : MediaType.APPLICATION_JSON)
                .build();
    }

    /** servers[0].url carries no trailing slash. */
    private static String baseUri(final UriInfo uriInfo) {
        final String baseUri = uriInfo.getBaseUri().toString();
        final String trimmed = baseUri.endsWith("/")
                ? baseUri.substring(0, baseUri.length() - 1) : baseUri;
        // Substituted into a quoted JSON string or YAML scalar.
        return trimmed.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String jsonTemplate() throws IOException {
        String template = jsonTemplate;
        if (template == null) {
            synchronized (this) {
                template = jsonTemplate;
                if (template == null) {
                    template = read();
                    jsonTemplate = template;
                }
            }
        }
        return template;
    }

    /**
     * Only JSON is generated at build time. Converting half a megabyte per
     * request would be wasteful, so the rendering is cached with the placeholder
     * still in it; it survives the round trip as a plain scalar.
     */
    private String yamlTemplate() throws IOException {
        String template = yamlTemplate;
        if (template == null) {
            synchronized (this) {
                template = yamlTemplate;
                if (template == null) {
                    final String json = jsonTemplate();
                    if (json == null) {
                        return null;
                    }
                    template = toYaml(json);
                    yamlTemplate = template;
                }
            }
        }
        return template;
    }

    private static String toYaml(final String json) throws IOException {
        final YAMLFactory factory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.SPLIT_LINES);
        final ObjectMapper yamlMapper = new ObjectMapper(factory);
        final String yaml = yamlMapper.writeValueAsString(new ObjectMapper().readTree(json));
        // MINIMIZE_QUOTES leaves the placeholder a plain scalar, which a context
        // path with YAML-significant characters could break out of.
        return yaml.replace(BASE_URI_PLACEHOLDER, '"' + BASE_URI_PLACEHOLDER + '"');
    }

    private String read() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
