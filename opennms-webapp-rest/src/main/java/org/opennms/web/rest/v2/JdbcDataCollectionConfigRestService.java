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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ConfigAlreadyExistsException;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.jaxb.JdbcDataCollectionConfigDaoCm;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.JdbcDataCollectionConfigRestApi;
import org.opennms.web.rest.v2.mapper.JdbcDataCollectionConfigMapper;
import org.opennms.web.rest.v2.model.JdbcCollectionDto;
import org.opennms.web.rest.v2.model.JdbcDataCollectionMetadataDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;
import org.opennms.web.rest.v2.model.JdbcSourceSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REST service for the JDBC data collection configuration, formerly {@code jdbc-datacollection-config.xml} and
 * {@code jdbc-datacollection.d/*.xml}, now one configuration manager document per source.
 *
 * <p>The collection is the unit of transfer below a source, for the same reason as the threshold group: queries
 * and columns have no identity besides their position. Every write stores the whole source, so the entity tag is
 * computed over the source and guards writes to any of its collections.</p>
 *
 * <p>No reload event is sent after a write: the collector reads the DAO it shares with this service on every
 * collection, and the DAO rebuilds its merged view from the configuration manager's update notification.</p>
 */
@Service
public class JdbcDataCollectionConfigRestService implements JdbcDataCollectionConfigRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataCollectionConfigRestService.class);

    /** Service parameter through which a collectd service selects its JDBC collection. */
    static final String COLLECTION_PARAMETER = "collection";

    private static final String JDBC_COLLECTOR_CLASS = "JdbcCollector";
    private static final String DEFAULT_RRD_REPOSITORY = "${install.share.dir}/rrd/snmp/";
    private static final int DEFAULT_RRD_STEP = 300;
    private static final List<String> DEFAULT_RRAS = List.of(
            "RRA:AVERAGE:0.5:1:2016",
            "RRA:AVERAGE:0.5:12:1488",
            "RRA:AVERAGE:0.5:288:366",
            "RRA:MAX:0.5:288:366",
            "RRA:MIN:0.5:288:366");
    private static final List<String> IF_TYPES = List.of("ignore", "all");

    /** File name suffixes an upload may carry, including the timestamp of a file from etc_archive. */
    private static final Pattern FILE_SUFFIX = Pattern.compile("\\.(xml|json)(\\.\\d+)?$", Pattern.CASE_INSENSITIVE);

    private static final String PERSIST_FAILED = "Failed to persist the JDBC data collection configuration.";

    // The REST stack is wired to the codehaus JsonProvider; the entity tag and the downloads use the same mapper.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** Serializes check-then-write sequences; the configuration manager has no transactions spanning both. */
    private final ReentrantLock writeLock = new ReentrantLock();

    @Autowired
    private JdbcDataCollectionConfigDaoCm jdbcDataCollectionConfigDao;

    @Autowired
    private CollectdConfigFactory collectdConfigFactory;

    @Autowired
    private ResourceDao resourceDao;

    // ------------------------------------------------------------------- reads

    @Override
    public Response getSources(final SecurityContext securityContext) {
        try {
            final List<JdbcSourceSummaryDto> summaries = new ArrayList<>();
            for (final String id : jdbcDataCollectionConfigDao.getConfigIds()) {
                summaries.add(JdbcDataCollectionConfigMapper.toSummaryDto(id, jdbcDataCollectionConfigDao.getConfig(id)));
            }
            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the JDBC data collection sources.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve the JDBC data collection configuration.").build();
        }
    }

    @Override
    public Response getSource(final String sourceId, final SecurityContext securityContext) {
        try {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            final String etag = etagOf(sourceId, config);

            final JdbcSourceDto dto = JdbcDataCollectionConfigMapper.toDto(sourceId, config);
            dto.setVersion(etag);
            dto.setEffectiveRrdRepository(jdbcDataCollectionConfigDao.getConfig().getRrdRepository());
            for (final JdbcCollectionDto collection : dto.getCollections()) {
                decorate(sourceId, collection);
            }
            return Response.ok(dto).tag(etag).build();
        } catch (final ConfigNotFoundException e) {
            return unknownSource(sourceId);
        } catch (final Exception e) {
            LOG.error("Failed to retrieve JDBC data collection source {}.", sourceId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve the JDBC data collection configuration.").build();
        }
    }

    @Override
    public Response getCollection(final String sourceId, final String collectionName, final SecurityContext securityContext) {
        try {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            final JdbcDataCollection collection = findCollection(config, collectionName);
            if (collection == null) {
                return unknownCollection(sourceId, collectionName);
            }
            final JdbcCollectionDto dto = JdbcDataCollectionConfigMapper.toDto(collection);
            decorate(sourceId, dto);
            return Response.ok(dto).tag(etagOf(sourceId, config)).build();
        } catch (final ConfigNotFoundException e) {
            return unknownSource(sourceId);
        } catch (final Exception e) {
            LOG.error("Failed to retrieve JDBC collection {} of source {}.", collectionName, sourceId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve the JDBC data collection configuration.").build();
        }
    }

    @Override
    public Response getMetadata(final SecurityContext securityContext) {
        final JdbcDataCollectionMetadataDto dto = new JdbcDataCollectionMetadataDto();
        dto.setAttributeTypes(Arrays.stream(AttributeType.values()).map(AttributeType::getName).collect(Collectors.toList()));
        dto.setIfTypes(IF_TYPES);
        dto.setResourceTypes(resourceTypeNames());
        dto.setDefaultRrdRepository(DEFAULT_RRD_REPOSITORY);
        dto.setDefaultRrdStep(DEFAULT_RRD_STEP);
        dto.setDefaultRras(DEFAULT_RRAS);
        return Response.ok(dto).build();
    }

    private List<String> resourceTypeNames() {
        final Set<String> names = new TreeSet<>();
        try {
            final Collection<OnmsResourceType> resourceTypes = resourceDao.getResourceTypes();
            if (resourceTypes != null) {
                for (final OnmsResourceType resourceType : resourceTypes) {
                    if (resourceType instanceof GenericIndexResourceType && resourceType.getName() != null) {
                        names.add(resourceType.getName());
                    }
                }
            }
        } catch (final Exception e) {
            // only suggestions; the editor still works without them
            LOG.warn("Could not list the resource types.", e);
        }
        return new ArrayList<>(names);
    }

    // ---------------------------------------------------------- source writes

    @Override
    public Response createSource(final JdbcSourceDto source, final SecurityContext securityContext) {
        if (source == null) {
            return badRequest("The source is required.");
        }
        final String sourceId = source.getId() == null ? null : source.getId().trim();
        final String message = firstNonNull(
                () -> JdbcDataCollectionConfigValidator.validateSourceId(sourceId),
                () -> JdbcDataCollectionConfigValidator.validateSource(source));
        if (message != null) {
            return badRequest(message);
        }
        final JdbcDataCollectionConfig config = JdbcDataCollectionConfigMapper.toEntity(source);

        return locked(() -> {
            if (exists(sourceId)) {
                return sourceExists(sourceId);
            }
            final Response conflict = collectionNameConflicts(sourceId, config);
            if (conflict != null) {
                return conflict;
            }
            jdbcDataCollectionConfigDao.createConfig(sourceId, config);
            return Response.created(URI.create("jdbc-datacollection/sources/" + sourceId)).build();
        }, "create source " + sourceId);
    }

    @Override
    public Response updateSource(final String sourceId, final JdbcSourceDto source, final String ifMatch,
                                 final SecurityContext securityContext) {
        if (source == null) {
            return badRequest("The source is required.");
        }
        return locked(() -> {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            final Response stale = checkIfMatch(sourceId, config, ifMatch);
            if (stale != null) {
                return stale;
            }
            final String repository = source.getRrdRepository();
            config.setRrdRepository(repository == null || repository.trim().isEmpty() ? null : repository.trim());
            jdbcDataCollectionConfigDao.replaceConfig(sourceId, config);
            return Response.noContent().build();
        }, "update source " + sourceId);
    }

    @Override
    public Response deleteSource(final String sourceId, final String ifMatch, final SecurityContext securityContext) {
        if (ConfigDefinition.DEFAULT_CONFIG_ID.equals(sourceId)) {
            return badRequest("The source '" + ConfigDefinition.DEFAULT_CONFIG_ID + "' cannot be deleted.");
        }
        return locked(() -> {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final Response stale = checkIfMatch(sourceId, jdbcDataCollectionConfigDao.copyOfConfig(sourceId), ifMatch);
            if (stale != null) {
                return stale;
            }
            jdbcDataCollectionConfigDao.deleteConfig(sourceId);
            return Response.noContent().build();
        }, "delete source " + sourceId);
    }

    // ------------------------------------------------------ collection writes

    @Override
    public Response createCollection(final String sourceId, final JdbcCollectionDto collection, final String ifMatch,
                                     final SecurityContext securityContext) {
        final String message = JdbcDataCollectionConfigValidator.validateCollection(collection);
        if (message != null) {
            return badRequest(message);
        }
        final JdbcDataCollection entity = JdbcDataCollectionConfigMapper.toEntity(collection);

        return locked(() -> {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            final Response stale = checkIfMatch(sourceId, config, ifMatch);
            if (stale != null) {
                return stale;
            }
            final Response conflict = collectionNameConflict(entity.getName(), null);
            if (conflict != null) {
                return conflict;
            }
            final List<JdbcDataCollection> collections = new ArrayList<>(config.getJdbcDataCollections());
            collections.add(entity);
            config.setJdbcDataCollections(collections);
            jdbcDataCollectionConfigDao.replaceConfig(sourceId, config);
            return Response.created(URI.create("jdbc-datacollection/sources/" + sourceId + "/collections/" + entity.getName())).build();
        }, "create collection " + entity.getName() + " in source " + sourceId);
    }

    @Override
    public Response updateCollection(final String sourceId, final String collectionName, final JdbcCollectionDto collection,
                                     final String ifMatch, final SecurityContext securityContext) {
        final String message = JdbcDataCollectionConfigValidator.validateCollection(collection);
        if (message != null) {
            return badRequest(message);
        }
        final JdbcDataCollection entity = JdbcDataCollectionConfigMapper.toEntity(collection);

        return locked(() -> {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            if (findCollection(config, collectionName) == null) {
                return unknownCollection(sourceId, collectionName);
            }
            final Response stale = checkIfMatch(sourceId, config, ifMatch);
            if (stale != null) {
                return stale;
            }
            if (!entity.getName().equals(collectionName)) {
                final Response conflict = collectionNameConflict(entity.getName(), null);
                if (conflict != null) {
                    return conflict;
                }
            }
            config.setJdbcDataCollections(config.getJdbcDataCollections().stream()
                    .map(candidate -> collectionName.equals(candidate.getName()) ? entity : candidate)
                    .collect(Collectors.toList()));
            jdbcDataCollectionConfigDao.replaceConfig(sourceId, config);
            return Response.noContent().build();
        }, "update collection " + collectionName + " of source " + sourceId);
    }

    @Override
    public Response deleteCollection(final String sourceId, final String collectionName, final String ifMatch,
                                     final SecurityContext securityContext) {
        return locked(() -> {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            if (findCollection(config, collectionName) == null) {
                return unknownCollection(sourceId, collectionName);
            }
            final Response stale = checkIfMatch(sourceId, config, ifMatch);
            if (stale != null) {
                return stale;
            }
            config.setJdbcDataCollections(config.getJdbcDataCollections().stream()
                    .filter(candidate -> !collectionName.equals(candidate.getName()))
                    .collect(Collectors.toList()));
            jdbcDataCollectionConfigDao.replaceConfig(sourceId, config);
            return Response.noContent().build();
        }, "delete collection " + collectionName + " of source " + sourceId);
    }

    // ------------------------------------------------------- download / upload

    @Override
    public Response downloadSource(final String sourceId, final String format, final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to download the JDBC data collection configuration.").build();
        }
        final boolean isJson = format != null && format.equalsIgnoreCase("json");
        if (!isJson && format != null && !format.equalsIgnoreCase("xml")) {
            return badRequest("Invalid format parameter. Supported values are 'json' and 'xml'.");
        }

        final byte[] body;
        try {
            if (!exists(sourceId)) {
                return unknownSource(sourceId);
            }
            final JdbcDataCollectionConfig config = jdbcDataCollectionConfigDao.copyOfConfig(sourceId);
            if (isJson) {
                body = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(JdbcDataCollectionConfigMapper.toDto(sourceId, config))
                        .getBytes(StandardCharsets.UTF_8);
            } else {
                body = JaxbUtils.marshal(config).getBytes(StandardCharsets.UTF_8);
            }
        } catch (final ConfigNotFoundException e) {
            return unknownSource(sourceId);
        } catch (final Exception e) {
            LOG.error("Error serializing JDBC data collection source {}.", sourceId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving the JDBC data collection configuration.").build();
        }

        final String fileName = sourceId + (isJson ? ".json" : ".xml");
        final String contentType = isJson ? "application/json" : "application/xml";
        return Response.ok().type(contentType + ";charset=" + StandardCharsets.UTF_8)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Pragma", "public")
                .header("Cache-Control", "no-cache, must-revalidate")
                .entity(body).build();
    }

    @Override
    public Response uploadSource(final Attachment attachment, final String requestedId, final boolean replace,
                                 final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to upload the JDBC data collection configuration.").build();
        }
        if (attachment == null) {
            return badRequest("Missing uploaded file for the JDBC data collection configuration upload.");
        }

        final String fileName = attachment.getContentDisposition() == null ? null
                : attachment.getContentDisposition().getParameter("filename");
        final JdbcSourceDto source;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            final String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            source = parseUpload(content, fileName);
        } catch (final Exception e) {
            LOG.warn("Failed to parse the uploaded JDBC data collection configuration {}.", fileName, e);
            return badRequest("The uploaded file is neither a JDBC data collection XML file nor its JSON export.");
        }

        final String sourceId = firstNonBlank(requestedId, source.getId(), idFromFileName(fileName));
        final String message = firstNonNull(
                () -> sourceId == null ? "The source id is required: pass it as the 'id' parameter." : null,
                () -> JdbcDataCollectionConfigValidator.validateSourceId(sourceId),
                () -> JdbcDataCollectionConfigValidator.validateSource(source));
        if (message != null) {
            return badRequest(message);
        }
        final JdbcDataCollectionConfig config = JdbcDataCollectionConfigMapper.toEntity(source);

        return locked(() -> {
            final boolean existing = exists(sourceId);
            if (existing && !replace) {
                return Response.status(Status.CONFLICT)
                        .entity("JDBC data collection source '" + sourceId + "' already exists; upload with replace to overwrite it.").build();
            }
            final Response conflict = collectionNameConflicts(sourceId, config);
            if (conflict != null) {
                return conflict;
            }
            if (existing) {
                jdbcDataCollectionConfigDao.replaceConfig(sourceId, config);
            } else {
                jdbcDataCollectionConfigDao.createConfig(sourceId, config);
            }
            return Response.ok(JdbcDataCollectionConfigMapper.toSummaryDto(sourceId, config)).build();
        }, "upload source " + sourceId);
    }

    private static JdbcSourceDto parseUpload(final String content, final String fileName) throws Exception {
        final String trimmed = content.trim();
        final boolean json = trimmed.startsWith("{") || (fileName != null && fileName.toLowerCase().endsWith(".json"));
        if (json) {
            return objectMapper.readValue(trimmed, JdbcSourceDto.class);
        }
        final JdbcDataCollectionConfig parsed = JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, trimmed);
        if (parsed == null) {
            throw new IllegalArgumentException("Empty document.");
        }
        return JdbcDataCollectionConfigMapper.toDto(null, parsed);
    }

    static String idFromFileName(final String fileName) {
        if (fileName == null) {
            return null;
        }
        String name = fileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        return FILE_SUFFIX.matcher(name).replaceFirst("");
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Runs a write under the lock and maps the exceptions of the configuration manager to responses.
     */
    private Response locked(final Supplier<Response> write, final String description) {
        writeLock.lock();
        try {
            return write.get();
        } catch (final ValidationException e) {
            LOG.warn("JDBC data collection configuration failed schema validation: {}.", description, e);
            return badRequest(e.getMessage());
        } catch (final ConfigAlreadyExistsException e) {
            return Response.status(Status.CONFLICT).entity(e.getMessage()).build();
        } catch (final ConfigNotFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to {}.", description, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(PERSIST_FAILED).build();
        } finally {
            writeLock.unlock();
        }
    }

    private boolean exists(final String sourceId) {
        return sourceId != null && jdbcDataCollectionConfigDao.getConfigIds().contains(sourceId);
    }

    private static String etagOf(final String sourceId, final JdbcDataCollectionConfig config) {
        return ThresholdRestSupport.etagOf(objectMapper, JdbcDataCollectionConfigMapper.toDto(sourceId, config));
    }

    private static Response checkIfMatch(final String sourceId, final JdbcDataCollectionConfig config, final String ifMatch) {
        if (ThresholdRestSupport.matchesIfMatch(ifMatch, etagOf(sourceId, config))) {
            return null;
        }
        return Response.status(Status.PRECONDITION_FAILED)
                .entity("JDBC data collection source '" + sourceId + "' has changed since it was read.").build();
    }

    private static JdbcDataCollection findCollection(final JdbcDataCollectionConfig config, final String name) {
        return config.getJdbcDataCollections().stream()
                .filter(collection -> Objects.equals(collection.getName(), name))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return the id of the first source, in merge order, that defines a collection of that name; null if none
     *         other than {@code excludedSourceId} does
     */
    private String sourceDefining(final String collectionName, final String excludedSourceId) {
        for (final String id : jdbcDataCollectionConfigDao.getConfigIds()) {
            if (id.equals(excludedSourceId)) {
                continue;
            }
            try {
                if (findCollection(jdbcDataCollectionConfigDao.getConfig(id), collectionName) != null) {
                    return id;
                }
            } catch (final ConfigNotFoundException e) {
                LOG.debug("Source {} was deleted while looking for collection {}.", id, collectionName);
            }
        }
        return null;
    }

    /** A 409 when any source, including the one written to, already defines {@code name}. */
    private Response collectionNameConflict(final String name, final String excludedSourceId) {
        final String owner = sourceDefining(name, excludedSourceId);
        if (owner == null) {
            return null;
        }
        return Response.status(Status.CONFLICT)
                .entity("Collection '" + name + "' already exists in source '" + owner + "'.").build();
    }

    /** A 409 when another source defines one of the collections of a source that is created or replaced. */
    private Response collectionNameConflicts(final String sourceId, final JdbcDataCollectionConfig config) {
        for (final JdbcDataCollection collection : config.getJdbcDataCollections()) {
            final Response conflict = collectionNameConflict(collection.getName(), sourceId);
            if (conflict != null) {
                return conflict;
            }
        }
        return null;
    }

    private void decorate(final String sourceId, final JdbcCollectionDto collection) {
        collection.setUsedBy(collectdServicesUsing(collection.getName()));
        final String first = sourceDefining(collection.getName(), null);
        collection.setShadowedBy(first == null || first.equals(sourceId) ? null : first);
    }

    /** @return the collectd services, as {@code package/service}, that collect with this JDBC collection */
    private List<String> collectdServicesUsing(final String collectionName) {
        final List<String> users = new ArrayList<>();
        try {
            final Set<String> jdbcServices = collectdConfigFactory.getCollectors().stream()
                    .filter(collector -> collector.getClassName() != null && collector.getClassName().endsWith(JDBC_COLLECTOR_CLASS))
                    .map(Collector::getService)
                    .collect(Collectors.toSet());
            for (final Package pkg : collectdConfigFactory.getPackages()) {
                for (final org.opennms.netmgt.config.collectd.Service service : pkg.getServices()) {
                    if (jdbcServices.contains(service.getName())
                            && Objects.equals(collectionName, service.getParameter(COLLECTION_PARAMETER))) {
                        users.add(pkg.getName() + "/" + service.getName());
                    }
                }
            }
        } catch (final Exception e) {
            // informational only
            LOG.warn("Could not determine the collectd services using JDBC collection {}.", collectionName, e);
        }
        return users;
    }

    @SafeVarargs
    private static String firstNonNull(final Supplier<String>... checks) {
        for (final Supplier<String> check : checks) {
            final String message = check.get();
            if (message != null) {
                return message;
            }
        }
        return null;
    }

    private static String firstNonBlank(final String... values) {
        for (final String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Response badRequest(final String message) {
        return Response.status(Status.BAD_REQUEST).entity(message).build();
    }

    private static Response unknownSource(final String sourceId) {
        return Response.status(Status.NOT_FOUND).entity("JDBC data collection source '" + sourceId + "' does not exist.").build();
    }

    private static Response unknownCollection(final String sourceId, final String collectionName) {
        return Response.status(Status.NOT_FOUND)
                .entity("Collection '" + collectionName + "' does not exist in JDBC data collection source '" + sourceId + "'.").build();
    }

    private static Response sourceExists(final String sourceId) {
        return Response.status(Status.CONFLICT).entity("JDBC data collection source '" + sourceId + "' already exists.").build();
    }
}
