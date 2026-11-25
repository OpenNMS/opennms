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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ConfigAlreadyExistsException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.ResourceTypesDaoCm;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.ResourceTypesConfigRestApi;
import org.opennms.web.rest.v2.mapper.ResourceTypesConfigMapper;
import org.opennms.web.rest.v2.model.ResourceTypesConfigDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigSummaryDto;
import org.opennms.web.rest.v2.model.ResourceTypesMetadataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REST service for the resource types stored in the configuration manager, formerly {@code etc/resource-types.d/}.
 *
 * <p>A configuration is the unit of transfer. Each write replaces a whole configuration in the configuration manager,
 * which validates it and reloads {@link ResourceTypesDaoCm} before it returns, so the change is in effect when the
 * response is sent. Writes are serialized here, so the checks against the other configurations and the entity tag
 * still hold when the write happens.</p>
 */
@Service
public class ResourceTypesConfigRestService implements ResourceTypesConfigRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypesConfigRestService.class);

    /** The storage strategies shipped with OpenNMS. */
    static final List<String> STORAGE_STRATEGIES = List.of(
            "org.opennms.netmgt.collection.support.IndexStorageStrategy",
            "org.opennms.netmgt.collection.support.JexlIndexStorageStrategy",
            "org.opennms.netmgt.collection.support.ObjectNameStorageStrategy",
            "org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy",
            "org.opennms.netmgt.dao.support.FrameRelayStorageStrategy",
            "org.opennms.netmgt.dao.support.HostFileSystemStorageStrategy",
            "org.opennms.protocols.xml.collector.XmlStorageStrategy");

    /** The persistence selector strategies shipped with OpenNMS. */
    static final List<String> PERSISTENCE_SELECTOR_STRATEGIES = List.of(
            "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy",
            "org.opennms.netmgt.collection.support.PersistRegexSelectorStrategy");

    private static final String READ_FAILED = "Failed to retrieve the resource types configuration.";
    private static final String WRITE_FAILED = "Failed to persist the resource types configuration.";

    // The REST stack is wired to the codehaus JsonProvider, so the download and the entity tag have to serialize
    // with the same mapper the response bodies use.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ReentrantLock writeLock = new ReentrantLock();

    @Autowired
    private ResourceTypesDaoCm resourceTypesDao;

    // ------------------------------------------------------------------- reads

    @Override
    public Response getConfigs(final SecurityContext securityContext) {
        try {
            final List<ResourceTypesConfigSummaryDto> summaries = resourceTypesDao.getConfigurations().entrySet().stream()
                    .map(e -> ResourceTypesConfigMapper.toSummaryDto(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the resource types configurations.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(READ_FAILED).build();
        }
    }

    @Override
    public Response getConfig(final String configId, final SecurityContext securityContext) {
        try {
            final Optional<ResourceTypes> config = resourceTypesDao.getConfiguration(configId);
            if (config.isEmpty()) {
                return notFound(configId);
            }
            final ResourceTypesConfigDto dto = ResourceTypesConfigMapper.toDto(configId, config.get());
            final String etag = ThresholdRestSupport.etagOf(objectMapper, dto);
            dto.setVersion(etag);
            return Response.ok(dto).tag(etag).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve resource types configuration {}.", configId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(READ_FAILED).build();
        }
    }

    @Override
    public Response getMetadata(final SecurityContext securityContext) {
        final ResourceTypesMetadataDto dto = new ResourceTypesMetadataDto();
        dto.setStorageStrategies(new ArrayList<>(STORAGE_STRATEGIES));
        dto.setPersistenceSelectorStrategies(new ArrayList<>(PERSISTENCE_SELECTOR_STRATEGIES));
        try {
            dto.setDataCollectionTypeNames(new ArrayList<>(resourceTypesDao.getDataCollectionResourceTypeNames()));
            dto.setExtensionTypeNames(new ArrayList<>(resourceTypesDao.getExtensionResourceTypeNames()));
        } catch (final Exception e) {
            // only used for warnings, the editor works without them
            LOG.warn("Failed to determine the resource types defined outside of the configuration manager.", e);
        }
        return Response.ok(dto).build();
    }

    // ------------------------------------------------------------------ writes

    @Override
    public Response createConfig(final ResourceTypesConfigDto config, final SecurityContext securityContext) {
        final String message = ResourceTypesConfigValidator.validateConfig(config);
        if (message != null) {
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        }
        final String configId = config.getId().trim();
        final ResourceTypes entity = ResourceTypesConfigMapper.toEntity(config);

        writeLock.lock();
        try {
            final SortedMap<String, ResourceTypes> configs = resourceTypesDao.getConfigurations();
            if (configs.containsKey(configId)) {
                return alreadyExists(configId);
            }
            final Response clash = nameClash(configs, entity, configId);
            if (clash != null) {
                return clash;
            }
            resourceTypesDao.createConfiguration(configId, entity);
        } catch (final ConfigAlreadyExistsException e) {
            return alreadyExists(configId);
        } catch (final ValidationException e) {
            LOG.warn("Resource types configuration {} failed validation.", configId, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to create resource types configuration {}.", configId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(WRITE_FAILED).build();
        } finally {
            writeLock.unlock();
        }
        return Response.created(UriBuilder.fromPath("resource-types/configs/{id}").build(configId)).build();
    }

    @Override
    public Response updateConfig(final String configId, final ResourceTypesConfigDto config, final String ifMatch,
                                 final SecurityContext securityContext) {
        final String message = ResourceTypesConfigValidator.validateConfig(config);
        if (message != null) {
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        }
        final String newId = config.getId().trim();
        final boolean renamed = !newId.equals(configId);
        final ResourceTypes entity = ResourceTypesConfigMapper.toEntity(config);

        writeLock.lock();
        try {
            final SortedMap<String, ResourceTypes> configs = resourceTypesDao.getConfigurations();
            final ResourceTypes existing = configs.get(configId);
            if (existing == null) {
                return notFound(configId);
            }
            if (!matches(configId, existing, ifMatch)) {
                return changed(configId);
            }
            if (renamed && configs.containsKey(newId)) {
                return alreadyExists(newId);
            }
            final Response clash = nameClash(configs, entity, configId);
            if (clash != null) {
                return clash;
            }

            if (renamed) {
                // Store the new one first: if that fails, nothing is lost.
                resourceTypesDao.createConfiguration(newId, entity);
                try {
                    resourceTypesDao.deleteConfiguration(configId);
                } catch (final RuntimeException e) {
                    rollBackRename(newId, e);
                    throw e;
                }
            } else {
                resourceTypesDao.updateConfiguration(configId, entity);
            }
        } catch (final ConfigAlreadyExistsException e) {
            return alreadyExists(newId);
        } catch (final ValidationException e) {
            LOG.warn("Resource types configuration {} failed validation.", configId, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to update resource types configuration {}.", configId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(WRITE_FAILED).build();
        } finally {
            writeLock.unlock();
        }
        return Response.noContent().build();
    }

    @Override
    public Response deleteConfig(final String configId, final String ifMatch, final SecurityContext securityContext) {
        writeLock.lock();
        try {
            final Optional<ResourceTypes> existing = resourceTypesDao.getConfiguration(configId);
            if (existing.isEmpty()) {
                return notFound(configId);
            }
            if (!matches(configId, existing.get(), ifMatch)) {
                return changed(configId);
            }
            resourceTypesDao.deleteConfiguration(configId);
        } catch (final Exception e) {
            LOG.error("Failed to delete resource types configuration {}.", configId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(WRITE_FAILED).build();
        } finally {
            writeLock.unlock();
        }
        return Response.noContent().build();
    }

    // ------------------------------------------------------- download / upload

    @Override
    public Response downloadConfig(final String configId, final String format, final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to download a resource types configuration.").build();
        }
        final boolean isXml = format != null && format.equalsIgnoreCase("xml");
        if (!isXml && format != null && !format.equalsIgnoreCase("json")) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid format parameter. Supported values are 'json' and 'xml'.").build();
        }

        final byte[] body;
        try {
            final Optional<ResourceTypes> config = resourceTypesDao.getConfiguration(configId);
            if (config.isEmpty()) {
                return notFound(configId);
            }
            if (isXml) {
                body = JaxbUtils.marshal(config.get()).getBytes(StandardCharsets.UTF_8);
            } else {
                body = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(ResourceTypesConfigMapper.toDto(configId, config.get()))
                        .getBytes(StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            LOG.error("Error serializing resource types configuration {}.", configId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(READ_FAILED).build();
        }

        // the id is checked on every write, so it is safe as a file name
        final String fileName = configId + (isXml ? ".xml" : ".json");
        final String contentType = isXml ? "application/xml" : "application/json";
        return Response.ok().type(contentType + ";charset=" + StandardCharsets.UTF_8)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .header("Pragma", "public")
                .header("Cache-Control", "no-cache, must-revalidate")
                .entity(body).build();
    }

    @Override
    public Response uploadConfig(final String configId, final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(configId, attachment, securityContext, false);
    }

    @Override
    public Response uploadConfigXml(final String configId, final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(configId, attachment, securityContext, true);
    }

    private Response uploadInternal(final String configId, final Attachment attachment,
                                    final SecurityContext securityContext, final boolean isXml) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to upload a resource types configuration.").build();
        }
        final String idMessage = ResourceTypesConfigValidator.validateConfigId(configId);
        if (idMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(idMessage).build();
        }
        final String fileType = isXml ? "XML" : "JSON";
        if (attachment == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Missing uploaded file for resource types configuration " + fileType + " file upload.").build();
        }

        final ResourceTypesConfigDto dto;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            if (isXml) {
                // The validating overload: the two-argument one parses without checking the schema.
                final ResourceTypes parsed = JaxbUtils.unmarshal(ResourceTypes.class,
                        new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), true);
                dto = ResourceTypesConfigMapper.toDto(configId, parsed);
            } else {
                dto = objectMapper.readValue(inputStream, ResourceTypesConfigDto.class);
            }
        } catch (final Exception e) {
            LOG.warn("Failed to parse the uploaded resource types {} configuration.", fileType, e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid resource types " + fileType + " configuration.").build();
        }
        if (dto == null) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid resource types " + fileType + " configuration.").build();
        }
        // the path names the configuration, whatever the file says
        dto.setId(configId);
        final String message = ResourceTypesConfigValidator.validateConfig(dto);
        if (message != null) {
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        }
        final ResourceTypes entity = ResourceTypesConfigMapper.toEntity(dto);

        writeLock.lock();
        try {
            final SortedMap<String, ResourceTypes> configs = resourceTypesDao.getConfigurations();
            final Response clash = nameClash(configs, entity, configId);
            if (clash != null) {
                return clash;
            }
            if (configs.containsKey(configId)) {
                resourceTypesDao.updateConfiguration(configId, entity);
            } else {
                resourceTypesDao.createConfiguration(configId, entity);
            }
        } catch (final ValidationException e) {
            LOG.warn("Uploaded resource types configuration {} failed validation.", configId, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to store uploaded resource types configuration {}.", configId, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(WRITE_FAILED).build();
        } finally {
            writeLock.unlock();
        }
        return Response.ok().build();
    }

    // ----------------------------------------------------------------- helpers

    private boolean matches(final String configId, final ResourceTypes existing, final String ifMatch) {
        final String currentEtag = ThresholdRestSupport.etagOf(objectMapper, ResourceTypesConfigMapper.toDto(configId, existing));
        return ThresholdRestSupport.matchesIfMatch(ifMatch, currentEtag);
    }

    /**
     * @return a 409 if a resource type of {@code entity} is defined by a configuration other than {@code ownId}
     */
    private static Response nameClash(final Map<String, ResourceTypes> configs, final ResourceTypes entity, final String ownId) {
        for (final ResourceType type : entity.getResourceTypes()) {
            for (final Map.Entry<String, ResourceTypes> other : configs.entrySet()) {
                if (other.getKey().equals(ownId)) {
                    continue;
                }
                if (other.getValue().getResourceTypes().stream().anyMatch(t -> type.getName().equals(t.getName()))) {
                    return Response.status(Status.CONFLICT)
                            .entity("Resource type '" + type.getName() + "' is already defined in configuration '" + other.getKey() + "'.")
                            .build();
                }
            }
        }
        return null;
    }

    private void rollBackRename(final String newId, final RuntimeException cause) {
        try {
            resourceTypesDao.deleteConfiguration(newId);
        } catch (final RuntimeException e) {
            cause.addSuppressed(e);
            LOG.error("Renaming left resource types configuration {} behind next to the old one.", newId, e);
        }
    }

    private static Response notFound(final String configId) {
        return Response.status(Status.NOT_FOUND).entity("Resource types configuration '" + configId + "' does not exist.").build();
    }

    private static Response alreadyExists(final String configId) {
        return Response.status(Status.CONFLICT).entity("Resource types configuration '" + configId + "' already exists.").build();
    }

    private static Response changed(final String configId) {
        return Response.status(Status.PRECONDITION_FAILED)
                .entity("Resource types configuration '" + configId + "' has changed since it was read.").build();
    }
}
