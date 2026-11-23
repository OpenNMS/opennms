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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.apache.cxf.common.util.ClasspathScanner;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Produces the OpenAPI documents packaged under openapi/ in this module's jar.
 *
 * Each document has two sources, and both must match what is deployed:
 * <ul>
 * <li>The metadata and base packages must match
 * {@code opennms-webapp-rest/src/main/webapp/WEB-INF/applicationContext-cxf-rest-v*.xml}.</li>
 * <li>The OSGi resource classes must match the Blueprint services that publish into the same
 * path with an {@code application-path} property. {@code OpenApiResourceCoverageTest} checks
 * this list against the Blueprint files.</li>
 * </ul>
 */
public final class OpenApiDocGenerator {

    /** AbstractStaticOpenApiResource substitutes the caller's base URI for this. */
    public static final String BASE_URI_PLACEHOLDER = "__OPENNMS_BASE_URI__";

    static {
        ModelConverters.getInstance().addConverter(new WireFormatModelResolver());
    }

    public enum Api {
        V1("OpenNMS V1 RESTful API",
           "The original OpenNMS ReST API. Endpoints accept and return XML or JSON, "
                   + "and authenticate with HTTP basic.",
           "org.opennms.web.rest.v1",
           // Blueprint services with application-path /rest
           List.of(DeviceConfigRestService.class),
           "openapi-v1.json"),
        V2("OpenNMS V2 RESTful API",
           "The newer OpenNMS ReST API, where collection endpoints share a common query layer: "
                   + "`_s` takes a FIQL search expression, and `limit`, `offset`, `orderBy` and `order` "
                   + "page and sort the result. Endpoints accept and return XML or JSON, and "
                   + "authenticate with HTTP basic.",
           "org.opennms.web.rest.v2,org.opennms.web.rest.v2.bsm,,org.opennms.web.rest.v2.status",
           // Blueprint services with application-path /api/v2
           List.of(),
           "openapi-v2.json");

        private final String title;
        private final String description;
        private final String basePackages;
        private final List<Class<?>> osgiResourceClasses;
        private final String fileName;

        Api(final String title, final String description, final String basePackages,
            final List<Class<?>> osgiResourceClasses, final String fileName) {
            this.title = title;
            this.description = description;
            this.basePackages = basePackages;
            this.osgiResourceClasses = osgiResourceClasses;
            this.fileName = fileName;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getBasePackages() {
            return basePackages;
        }

        /**
         * ReST services that a Blueprint {@code <service>} publishes into this API's path through
         * its {@code application-path} property: {@code /rest} for V1, {@code /api/v2} for V2.
         * They live outside the scanned packages and declare their JAX-RS and swagger annotations
         * on an interface, so they are listed here and read through that interface. Add a
         * service's interface to the {@code Api} whose path matches its {@code application-path},
         * and its module as a provided dependency of this module.
         *
         * <p>Karaf serves these services with Jersey and the Jackson 2 provider, not with the
         * webapp's Jackson 1 / JAXB provider. The schemas of their DTOs still go through
         * {@link WireFormatModelResolver}, which models the webapp. That is correct only for DTOs
         * with no JAXB or Jackson 1 annotations. Also, Jackson 2 writes a {@code Date} as epoch
         * milliseconds, so annotate each {@code Date} property
         * {@code @Schema(type = "integer", format = "int64")}. {@code OpenApiDocsContentTest}
         * fails when an OSGi schema has a {@code date-time} property.</p>
         */
        public List<Class<?>> getOsgiResourceClasses() {
            return osgiResourceClasses;
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
                        .description(api.getDescription())
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

        final Set<Class<?>> scanned = findResourceClasses(api.getBasePackages());
        if (scanned.isEmpty()) {
            throw new IllegalStateException("no @Path resource classes found in " + api.getBasePackages()
                    + "; the generator classpath is missing the ReST modules");
        }

        final List<Class<?>> osgi = api.getOsgiResourceClasses();
        for (final Class<?> clazz : osgi) {
            if (!clazz.isAnnotationPresent(Path.class)) {
                throw new IllegalStateException(clazz.getName() + " has no @Path, so it would add nothing to the document");
            }
        }

        final Set<Class<?>> resourceClasses = Stream.concat(scanned.stream(), osgi.stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        final SwaggerConfiguration configuration = new SwaggerConfiguration()
                .openAPI(seed)
                .prettyPrint(Boolean.TRUE)
                // Describe methods with no @Operation, as OpenApiFeature does by default.
                .readAllResources(Boolean.TRUE)
                .resourceClasses(resourceClasses.stream()
                        .map(Class::getName)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));

        final Reader reader = new Reader(configuration);
        reader.read(scanned);
        // One at a time, so an OSGi operation that replaces one already read is detected.
        for (final Class<?> clazz : osgi) {
            final Map<String, Map<PathItem.HttpMethod, Operation>> before = operations(reader.getOpenAPI());
            reader.read(clazz);
            requireNoReplacedOperation(clazz, before, reader.getOpenAPI());
        }
        return serialize(reader.getOpenAPI());
    }

    private static Map<String, Map<PathItem.HttpMethod, Operation>> operations(final OpenAPI document) {
        final Map<String, Map<PathItem.HttpMethod, Operation>> operations = new HashMap<>();
        if (document.getPaths() != null) {
            document.getPaths().forEach((path, item) -> operations.put(path, item.readOperationsMap()));
        }
        return operations;
    }

    /**
     * Swagger's Reader merges path items with the same path, and an operation with the same method
     * replaces the earlier one without a warning. Refuse to drop an operation that way.
     */
    private static void requireNoReplacedOperation(final Class<?> clazz,
                                                   final Map<String, Map<PathItem.HttpMethod, Operation>> before,
                                                   final OpenAPI after) {
        before.forEach((path, methods) -> methods.forEach((method, operation) -> {
            final Operation current = after.getPaths().get(path).readOperationsMap().get(method);
            if (current != operation) {
                throw new IllegalStateException(clazz.getName() + " also declares " + method + " " + path
                        + ", which replaces the operation already read for it");
            }
        }));
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
