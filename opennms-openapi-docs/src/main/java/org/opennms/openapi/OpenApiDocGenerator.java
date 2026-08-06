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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.apache.cxf.common.util.ClasspathScanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Produces the OpenAPI documents packaged under openapi/ in this module's jar.
 *
 * The metadata and base packages here must match
 * {@code opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-rest-v*.xml},
 * or the documents stop describing what is deployed.
 */
public final class OpenApiDocGenerator {

    /** AbstractStaticOpenApiResource substitutes the caller's base URI for this. */
    public static final String BASE_URI_PLACEHOLDER = "__OPENNMS_BASE_URI__";

    public enum Api {
        V1("OpenNMS V1 RESTful API",
           "org.opennms.web.rest.v1",
           "openapi-v1.json"),
        V2("OpenNMS V2 RESTful API",
           "org.opennms.web.rest.v2,org.opennms.web.rest.v2.bsm,,org.opennms.web.rest.v2.status",
           "openapi-v2.json");

        private final String title;
        private final String basePackages;
        private final String fileName;

        Api(final String title, final String basePackages, final String fileName) {
            this.title = title;
            this.basePackages = basePackages;
            this.fileName = fileName;
        }

        public String getTitle() {
            return title;
        }

        public String getBasePackages() {
            return basePackages;
        }

        public String getFileName() {
            return fileName;
        }
    }

    private OpenApiDocGenerator() {
    }

    public static String generate(final Api api) throws Exception {
        final OpenAPI seed = new OpenAPI()
                .info(new Info()
                        .title(api.getTitle())
                        .description(api.getTitle())
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("OpenNMS")
                                .url("http://www.opennms.com/"))
                        .license(new License()
                                .name("OpenNMS(R) Licensing (GNU Affero General Public License)")
                                .url("http://www.gnu.org/licenses/agpl.html")))
                .addServersItem(new Server().url(BASE_URI_PLACEHOLDER))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));

        final Set<Class<?>> resourceClasses = findResourceClasses(api.getBasePackages());
        if (resourceClasses.isEmpty()) {
            throw new IllegalStateException("no @Path resource classes found in " + api.getBasePackages()
                    + "; the generator classpath is missing the ReST modules");
        }

        final SwaggerConfiguration configuration = new SwaggerConfiguration()
                .openAPI(seed)
                .prettyPrint(Boolean.TRUE)
                // Describe methods with no @Operation, as OpenApiFeature does by default.
                .readAllResources(Boolean.TRUE)
                .resourceClasses(resourceClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));

        return serialize(new Reader(configuration).read(resourceClasses));
    }

    /**
     * Jackson derives schema properties from reflection, whose order is not stable
     * across JVM runs, so the map keys have to be sorted or the documents differ
     * from build to build.
     */
    private static String serialize(final OpenAPI document) throws JsonProcessingException {
        final ObjectMapper mapper = Json.mapper().copy()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);
    }

    /**
     * Mirrors how {@code jaxrs:server basePackages} picks up root resources.
     * Interfaces and abstract classes are dropped because CXF cannot register
     * them, even though several carry {@code @Path} (the
     * {@code org.opennms.web.rest.v2.api} contracts, for instance).
     *
     * Sorted so the document is byte-stable across classpath orderings.
     */
    private static Set<Class<?>> findResourceClasses(final String basePackages) throws Exception {
        final Collection<Class<?>> annotated = ClasspathScanner
                .findClasses(ClasspathScanner.parsePackages(basePackages), Path.class)
                .get(Path.class);

        if (annotated == null) {
            return Set.of();
        }

        return annotated.stream()
                .filter(clazz -> !clazz.isInterface())
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
