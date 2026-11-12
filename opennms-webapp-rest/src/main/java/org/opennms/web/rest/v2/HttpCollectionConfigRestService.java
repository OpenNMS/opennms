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

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.dao.api.HttpCollectionConfigDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.HttpCollectionConfigRestApi;
import org.opennms.web.rest.v2.mapper.HttpCollectionConfigMapper;
import org.opennms.web.rest.v2.model.HttpCollectionConfigDto;
import org.opennms.web.rest.v2.model.HttpCollectionDto;
import org.opennms.web.rest.v2.model.HttpCollectionMetadataDto;
import org.opennms.web.rest.v2.model.HttpCollectionSettingsDto;
import org.opennms.web.rest.v2.model.HttpCollectionSummaryDto;
import org.opennms.web.rest.v2.model.HttpRrdDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REST service for the HTTP collector configuration, formerly {@code http-datacollection-config.xml}.
 *
 * <p>Every write reads the stored configuration, converts it to DTOs, changes the copy and stores the result
 * with {@link HttpCollectionConfigDao#replaceConfig}. The DAO's cached object is never modified, because the
 * collector may be reading it at the same time. Writes are serialized within the webapp, and the ETag check
 * catches the remaining case of a concurrent change made somewhere else.</p>
 *
 * <p>No event is sent after a write: the configuration manager notifies the DAO, which reloads, and the
 * collector looks the collection up on every collection.</p>
 */
@Service
public class HttpCollectionConfigRestService implements HttpCollectionConfigRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(HttpCollectionConfigRestService.class);

    static final String HTTP_COLLECTOR_CLASS = "org.opennms.netmgt.collectd.HttpCollector";
    /** Service parameters through which a collectd package selects an HTTP collection, as the collector reads them. */
    static final List<String> COLLECTION_PARAMETERS = List.of("collection", "http-collection");

    private static final String NO_CONFIG_MESSAGE = "HTTP collector configuration not found.";
    private static final String PERSIST_FAILED_MESSAGE = "Failed to persist the HTTP collector configuration.";

    static final List<String> SCHEMES = List.of("http", "https");
    static final List<String> DEFAULT_RRAS = List.of(
            "RRA:AVERAGE:0.5:1:2016",
            "RRA:AVERAGE:0.5:12:1488",
            "RRA:AVERAGE:0.5:288:366",
            "RRA:MAX:0.5:288:366",
            "RRA:MIN:0.5:288:366");
    static final int DEFAULT_STEP = 300;

    // The REST stack is wired to the codehaus JsonProvider, so the ETag and the download use the same mapper.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Object WRITE_LOCK = new Object();

    @Autowired
    private HttpCollectionConfigDao httpCollectionConfigDao;

    @Autowired(required = false)
    private CollectdConfigFactory collectdConfigFactory;

    // ---------------------------------------------------------------- settings

    @Override
    public Response getSettings(final SecurityContext securityContext) {
        try {
            final HttpDatacollectionConfig config = httpCollectionConfigDao.getConfig();
            if (config == null) {
                return notFound(NO_CONFIG_MESSAGE);
            }
            final HttpCollectionSettingsDto dto = settingsOf(config);
            final String etag = ThresholdRestSupport.etagOf(objectMapper, dto);
            dto.setVersion(etag);
            return Response.ok(dto).tag(etag).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the HTTP collector configuration.", e);
            return serverError("Failed to retrieve the HTTP collector configuration.");
        }
    }

    @Override
    public Response updateSettings(final HttpCollectionSettingsDto settings, final String ifMatch,
                                   final SecurityContext securityContext) {
        final String message = HttpCollectionConfigValidator.validateSettings(settings);
        if (message != null) {
            return badRequest(message);
        }

        try {
            synchronized (WRITE_LOCK) {
                final HttpDatacollectionConfig current = httpCollectionConfigDao.getConfig();
                if (current == null) {
                    return notFound(NO_CONFIG_MESSAGE);
                }
                final String etag = ThresholdRestSupport.etagOf(objectMapper, settingsOf(current));
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, etag)) {
                    return preconditionFailed("The HTTP collector settings have changed since they were read.");
                }

                final HttpCollectionConfigDto draft = HttpCollectionConfigMapper.toDto(current);
                draft.setRrdRepository(settings.getRrdRepository().trim());
                httpCollectionConfigDao.replaceConfig(HttpCollectionConfigMapper.toEntity(draft));
            }
        } catch (final ValidationException e) {
            LOG.warn("The HTTP collector settings failed schema validation.", e);
            return badRequest(e.getMessage());
        } catch (final Exception e) {
            LOG.error("Failed to update the HTTP collector settings.", e);
            return serverError(PERSIST_FAILED_MESSAGE);
        }
        return Response.noContent().build();
    }

    // ------------------------------------------------------------------- reads

    @Override
    public Response getCollections(final SecurityContext securityContext) {
        try {
            final HttpDatacollectionConfig config = httpCollectionConfigDao.getConfig();
            if (config == null) {
                return Response.ok(new ArrayList<HttpCollectionSummaryDto>()).build();
            }
            final List<HttpCollectionSummaryDto> summaries = config.getHttpCollection().stream()
                    .map(HttpCollectionConfigMapper::toSummaryDto)
                    .collect(Collectors.toList());
            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the HTTP collector configuration.", e);
            return serverError("Failed to retrieve the HTTP collector configuration.");
        }
    }

    @Override
    public Response getCollection(final String name, final SecurityContext securityContext) {
        try {
            final HttpDatacollectionConfig config = httpCollectionConfigDao.getConfig();
            final Optional<HttpCollectionDto> collection = config == null ? Optional.empty()
                    : find(HttpCollectionConfigMapper.toDto(config).getCollections(), name);
            if (collection.isEmpty()) {
                return notFound(unknownCollection(name));
            }
            final HttpCollectionDto dto = collection.get();
            final String etag = ThresholdRestSupport.etagOf(objectMapper, dto);
            dto.setVersion(etag);
            return Response.ok(dto).tag(etag).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve HTTP collection {}.", name, e);
            return serverError("Failed to retrieve the HTTP collector configuration.");
        }
    }

    @Override
    public Response getMetadata(final SecurityContext securityContext) {
        final HttpCollectionMetadataDto dto = new HttpCollectionMetadataDto();
        dto.setAttributeTypes(Arrays.stream(AttributeType.values()).map(AttributeType::getName).collect(Collectors.toList()));
        dto.setMethods(HttpCollectionConfigValidator.METHODS);
        dto.setHttpVersions(HttpCollectionConfigValidator.HTTP_VERSIONS);
        dto.setSchemes(SCHEMES);
        final HttpRrdDto rrd = new HttpRrdDto();
        rrd.setStep(DEFAULT_STEP);
        rrd.setRras(DEFAULT_RRAS);
        dto.setDefaultRrd(rrd);
        return Response.ok(dto).build();
    }

    // ------------------------------------------------------------------ writes

    @Override
    public Response createCollection(final HttpCollectionDto collection, final SecurityContext securityContext) {
        final String message = HttpCollectionConfigValidator.validateCollection(collection);
        if (message != null) {
            return badRequest(message);
        }
        final String name = collection.getName().trim();

        try {
            synchronized (WRITE_LOCK) {
                final HttpDatacollectionConfig current = httpCollectionConfigDao.getConfig();
                if (current == null) {
                    return notFound(NO_CONFIG_MESSAGE);
                }
                final HttpCollectionConfigDto draft = HttpCollectionConfigMapper.toDto(current);
                final Optional<HttpCollectionDto> existing = find(draft.getCollections(), name);
                if (existing.isPresent()) {
                    return conflict("HTTP collection '" + existing.get().getName() + "' already exists.");
                }
                draft.getCollections().add(collection);
                httpCollectionConfigDao.replaceConfig(HttpCollectionConfigMapper.toEntity(draft));
            }
        } catch (final ValidationException e) {
            LOG.warn("HTTP collection {} failed schema validation.", name, e);
            return badRequest(e.getMessage());
        } catch (final Exception e) {
            LOG.error("Failed to create HTTP collection {}.", name, e);
            return serverError(PERSIST_FAILED_MESSAGE);
        }
        return Response.created(URI.create("httpcollection/collections/" + name)).build();
    }

    @Override
    public Response updateCollection(final String name, final HttpCollectionDto collection, final String ifMatch,
                                     final SecurityContext securityContext) {
        final String message = HttpCollectionConfigValidator.validateCollection(collection);
        if (message != null) {
            return badRequest(message);
        }
        final String newName = collection.getName().trim();

        try {
            synchronized (WRITE_LOCK) {
                final HttpDatacollectionConfig current = httpCollectionConfigDao.getConfig();
                if (current == null) {
                    return notFound(NO_CONFIG_MESSAGE);
                }
                final HttpCollectionConfigDto draft = HttpCollectionConfigMapper.toDto(current);
                final int index = indexOf(draft.getCollections(), name);
                if (index < 0) {
                    return notFound(unknownCollection(name));
                }
                final HttpCollectionDto existing = draft.getCollections().get(index);

                final String etag = ThresholdRestSupport.etagOf(objectMapper, existing);
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, etag)) {
                    return preconditionFailed("HTTP collection '" + existing.getName() + "' has changed since it was read.");
                }

                if (!newName.equals(existing.getName())) {
                    final int clash = indexOf(draft.getCollections(), newName);
                    if (clash >= 0 && clash != index) {
                        return conflict("HTTP collection '" + draft.getCollections().get(clash).getName() + "' already exists.");
                    }
                    // collectd-configuration.xml is not managed here, so its references cannot be moved along.
                    final Set<String> users = packagesReferencing(existing.getName());
                    if (!users.isEmpty() && !HttpCollectionConfigValidator.normalizedName(newName)
                            .equals(HttpCollectionConfigValidator.normalizedName(existing.getName()))) {
                        return conflict("HTTP collection '" + existing.getName() + "' cannot be renamed: it is used by collectd packages: "
                                + String.join(", ", users) + ".");
                    }
                }

                draft.getCollections().set(index, collection);
                httpCollectionConfigDao.replaceConfig(HttpCollectionConfigMapper.toEntity(draft));
            }
        } catch (final ValidationException e) {
            LOG.warn("HTTP collection {} failed schema validation.", name, e);
            return badRequest(e.getMessage());
        } catch (final Exception e) {
            LOG.error("Failed to update HTTP collection {}.", name, e);
            return serverError(PERSIST_FAILED_MESSAGE);
        }
        return Response.noContent().build();
    }

    @Override
    public Response deleteCollection(final String name, final String ifMatch, final SecurityContext securityContext) {
        try {
            synchronized (WRITE_LOCK) {
                final HttpDatacollectionConfig current = httpCollectionConfigDao.getConfig();
                if (current == null) {
                    return notFound(NO_CONFIG_MESSAGE);
                }
                final HttpCollectionConfigDto draft = HttpCollectionConfigMapper.toDto(current);
                final int index = indexOf(draft.getCollections(), name);
                if (index < 0) {
                    return notFound(unknownCollection(name));
                }
                final HttpCollectionDto existing = draft.getCollections().get(index);

                final String etag = ThresholdRestSupport.etagOf(objectMapper, existing);
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, etag)) {
                    return preconditionFailed("HTTP collection '" + existing.getName() + "' has changed since it was read.");
                }

                final Set<String> users = packagesReferencing(existing.getName());
                if (!users.isEmpty()) {
                    return conflict("HTTP collection '" + existing.getName() + "' is used by collectd packages: "
                            + String.join(", ", users) + ".");
                }

                draft.getCollections().remove(index);
                httpCollectionConfigDao.replaceConfig(HttpCollectionConfigMapper.toEntity(draft));
            }
        } catch (final Exception e) {
            LOG.error("Failed to delete HTTP collection {}.", name, e);
            return serverError(PERSIST_FAILED_MESSAGE);
        }
        return Response.noContent().build();
    }

    // ------------------------------------------------------- download / upload

    @Override
    public Response downloadConfiguration(final String format, final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to download the HTTP collector configuration.").build();
        }
        final boolean isXml = format != null && format.equalsIgnoreCase("xml");
        if (!isXml && format != null && !format.equalsIgnoreCase("json")) {
            return badRequest("Invalid format parameter. Supported values are 'json' and 'xml'.");
        }

        final byte[] body;
        try {
            final HttpDatacollectionConfig config = httpCollectionConfigDao.getConfig();
            if (config == null) {
                return notFound(NO_CONFIG_MESSAGE);
            }
            body = (isXml
                    ? JaxbUtils.marshal(config)
                    : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(HttpCollectionConfigMapper.toDto(config)))
                    .getBytes(StandardCharsets.UTF_8);
        } catch (final Exception e) {
            LOG.error("Error serializing the HTTP collector configuration.", e);
            return serverError("Error retrieving the HTTP collector configuration.");
        }

        final String fileName = isXml ? "http-datacollection-config.xml" : "http-datacollection-config.json";
        final String contentType = isXml ? "application/xml" : "application/json";
        return Response.ok().type(contentType + ";charset=" + StandardCharsets.UTF_8)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Pragma", "public")
                .header("Cache-Control", "no-cache, must-revalidate")
                .entity(body).build();
    }

    @Override
    public Response uploadConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(attachment, securityContext, false);
    }

    @Override
    public Response uploadConfigurationXml(final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(attachment, securityContext, true);
    }

    private Response uploadInternal(final Attachment attachment, final SecurityContext securityContext, final boolean isXml) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to upload the HTTP collector configuration.").build();
        }
        final String fileType = isXml ? "XML" : "JSON";
        if (attachment == null) {
            return badRequest("Missing uploaded file for HTTP collector configuration " + fileType + " file upload.");
        }

        final HttpCollectionConfigDto uploaded;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            if (isXml) {
                // The validating overload: the two-argument one parses without checking the schema.
                final HttpDatacollectionConfig parsed = JaxbUtils.unmarshal(HttpDatacollectionConfig.class,
                        new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), true);
                uploaded = HttpCollectionConfigMapper.toDto(parsed);
            } else {
                uploaded = objectMapper.readValue(inputStream, HttpCollectionConfigDto.class);
            }
        } catch (final Exception e) {
            LOG.warn("Failed to parse the uploaded HTTP collector {} configuration.", fileType, e);
            return badRequest("Invalid HTTP collector " + fileType + " configuration.");
        }

        final String message = HttpCollectionConfigValidator.validateConfig(uploaded);
        if (message != null) {
            return badRequest(message);
        }
        if (uploaded.getCollections() == null) {
            uploaded.setCollections(new ArrayList<>());
        }

        // The same rule as for a delete: a collection a collectd package still uses must not disappear.
        final Map<String, Set<String>> missing = new LinkedHashMap<>();
        for (final Map.Entry<String, Set<String>> use : collectionUses().entrySet()) {
            if (find(uploaded.getCollections(), use.getKey()).isEmpty()) {
                missing.put(use.getKey(), use.getValue());
            }
        }
        if (!missing.isEmpty()) {
            return conflict("The uploaded configuration drops HTTP collections that collectd packages still use: "
                    + missing.entrySet().stream()
                            .map(e -> "'" + e.getKey() + "' (used by " + String.join(", ", e.getValue()) + ")")
                            .collect(Collectors.joining(", ")) + ".");
        }

        try {
            synchronized (WRITE_LOCK) {
                httpCollectionConfigDao.replaceConfig(HttpCollectionConfigMapper.toEntity(uploaded));
            }
        } catch (final ValidationException e) {
            LOG.warn("The uploaded HTTP collector configuration failed schema validation.", e);
            return badRequest(e.getMessage());
        } catch (final Exception e) {
            LOG.error("Failed to persist the uploaded HTTP collector configuration.", e);
            return serverError(PERSIST_FAILED_MESSAGE);
        }
        return Response.ok().build();
    }

    // ------------------------------------------------------------------ helpers

    private static HttpCollectionSettingsDto settingsOf(final HttpDatacollectionConfig config) {
        final HttpCollectionSettingsDto dto = new HttpCollectionSettingsDto();
        dto.setRrdRepository(config.getRrdRepository());
        return dto;
    }

    /** The collector looks collections up ignoring case, so this does too. */
    private static int indexOf(final List<HttpCollectionDto> collections, final String name) {
        if (collections == null || name == null) {
            return -1;
        }
        final String wanted = HttpCollectionConfigValidator.normalizedName(name);
        for (int i = 0; i < collections.size(); i++) {
            if (wanted.equals(HttpCollectionConfigValidator.normalizedName(collections.get(i).getName()))) {
                return i;
            }
        }
        return -1;
    }

    private static Optional<HttpCollectionDto> find(final List<HttpCollectionDto> collections, final String name) {
        final int index = indexOf(collections, name);
        return index < 0 ? Optional.empty() : Optional.of(collections.get(index));
    }

    /** Names of the collectd packages whose HTTP collector services use {@code collectionName}. */
    Set<String> packagesReferencing(final String collectionName) {
        final String wanted = HttpCollectionConfigValidator.normalizedName(collectionName);
        final Set<String> users = new LinkedHashSet<>();
        collectionUses().forEach((name, packages) -> {
            if (wanted.equals(HttpCollectionConfigValidator.normalizedName(name))) {
                users.addAll(packages);
            }
        });
        return users;
    }

    /**
     * Maps each collection name that collectd uses to the packages using it. Only services collected by the
     * HTTP collector count: other collectors have a {@code collection} parameter of their own.
     */
    Map<String, Set<String>> collectionUses() {
        final Map<String, Set<String>> uses = new LinkedHashMap<>();
        if (collectdConfigFactory == null) {
            return uses;
        }
        try {
            final Set<String> httpServices = collectdConfigFactory.getCollectors().stream()
                    .filter(collector -> HTTP_COLLECTOR_CLASS.equals(collector.getClassName()))
                    .map(Collector::getService)
                    .collect(Collectors.toSet());
            for (final Package pkg : collectdConfigFactory.getPackages()) {
                for (final org.opennms.netmgt.config.collectd.Service service : pkg.getServices()) {
                    if (!httpServices.contains(service.getName())) {
                        continue;
                    }
                    for (final String parameter : COLLECTION_PARAMETERS) {
                        final String value = service.getParameter(parameter);
                        if (value != null && !value.isBlank()) {
                            uses.computeIfAbsent(value.trim(), k -> new LinkedHashSet<>()).add(pkg.getName());
                            break;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOG.warn("Could not read the collectd configuration to find the HTTP collections in use.", e);
        }
        return uses;
    }

    private static String unknownCollection(final String name) {
        return "HTTP collection '" + name + "' does not exist.";
    }

    private static Response badRequest(final String message) {
        return Response.status(Status.BAD_REQUEST).entity(message).build();
    }

    private static Response notFound(final String message) {
        return Response.status(Status.NOT_FOUND).entity(message).build();
    }

    private static Response conflict(final String message) {
        return Response.status(Status.CONFLICT).entity(message).build();
    }

    private static Response preconditionFailed(final String message) {
        return Response.status(Status.PRECONDITION_FAILED).entity(message).build();
    }

    private static Response serverError(final String message) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }
}
