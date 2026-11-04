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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.FilterOperator;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Parameter;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.ThresholdingConfigRestApi;
import org.opennms.web.rest.v2.mapper.ThresholdingConfigMapper;
import org.opennms.web.rest.v2.model.DsTypeDto;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;
import org.opennms.web.rest.v2.model.ThresholdGroupSummaryDto;
import org.opennms.web.rest.v2.model.ThresholdingConfigDto;
import org.opennms.web.rest.v2.model.ThresholdingMetadataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * REST service for the thresholding configuration, formerly {@code thresholds.xml}.
 *
 * <p>The threshold group is the unit of transfer. That is not just an API-shape preference: a threshold has
 * no identity in the schema and can only be addressed by its index, and every {@code saveConfig()} triggers
 * a synchronous reload that replaces the whole object graph. Index-addressed sub-resources would therefore
 * hand out positions that go stale the moment anyone else writes.</p>
 */
@Service
public class ThresholdingConfigRestService implements ThresholdingConfigRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingConfigRestService.class);


    /** Service parameter through which a threshd package binds a service to a threshold group. */
    static final String THRESHOLDING_GROUP_PARAMETER = "thresholding-group";
    private static final String NO_CONFIG_MESSAGE = "Thresholding configuration not found.";

    // The REST stack is wired to the codehaus JsonProvider, so the download endpoint has to serialize with
    // the same mapper the response bodies use.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WriteableThresholdingDao thresholdingDao;

    @Autowired
    private WriteableThreshdDao threshdDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private ThresholdUeiService thresholdUeiService;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy eventProxy;

    // ------------------------------------------------------------------- reads

    @Override
    public Response getThresholdGroups(final SecurityContext securityContext) {
        try {
            final ThresholdingConfig merged = thresholdingDao.getReadOnlyConfig();
            if (merged == null) {
                return Response.ok(new ArrayList<ThresholdGroupSummaryDto>()).build();
            }

            final Set<String> writeableNames = writeableGroupNames();
            final List<ThresholdGroupSummaryDto> summaries = merged.getGroups().stream()
                    .sorted(Comparator.comparing(Group::getName, Comparator.nullsLast(String::compareTo)))
                    .map(group -> ThresholdingConfigMapper.toSummaryDto(group, !writeableNames.contains(group.getName())))
                    .collect(Collectors.toList());

            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve thresholding configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve thresholding configuration.").build();
        }
    }

    @Override
    public Response getThresholdGroup(final String groupName, final SecurityContext securityContext) {
        try {
            final ThresholdingConfig merged = thresholdingDao.getReadOnlyConfig();
            final Optional<Group> group = findGroup(merged, groupName);
            if (group.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity(unknownGroup(groupName)).build();
            }

            final boolean readOnly = !writeableGroupNames().contains(groupName);
            final ThresholdGroupDto dto = ThresholdingConfigMapper.toDto(group.get(), readOnly);
            final String etag = ThresholdRestSupport.etagOf(objectMapper, dto);
            dto.setVersion(etag);

            return Response.ok(dto).tag(etag).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve threshold group {}.", groupName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve thresholding configuration.").build();
        }
    }

    @Override
    public Response getThresholdingMetadata(final SecurityContext securityContext) {
        final ThresholdingMetadataDto dto = new ThresholdingMetadataDto();
        dto.setDsTypes(buildDsTypes());
        dto.setThresholdTypes(java.util.Arrays.stream(ThresholdType.values())
                .map(ThresholdType::getEnumName)
                .collect(Collectors.toList()));
        dto.setFilterOperators(java.util.Arrays.stream(FilterOperator.values())
                .map(FilterOperator::getEnumName)
                .collect(Collectors.toList()));
        return Response.ok(dto).build();
    }

    /**
     * Builds the datasource type list exactly as the JSP editor did: the two built-in types first, then every
     * generic index resource type sorted by label, disambiguated with its name when two share a label.
     */
    private List<DsTypeDto> buildDsTypes() {
        final List<DsTypeDto> dsTypes = new ArrayList<>();
        dsTypes.add(DsTypeDto.of("node", "Node"));
        dsTypes.add(DsTypeDto.of("if", "Interface")); // "interface" is a wrong word

        final Multimap<String, String> genericDsTypes = TreeMultimap.create();
        final Collection<OnmsResourceType> resourceTypes = resourceDao.getResourceTypes();
        if (resourceTypes != null) {
            for (final OnmsResourceType resourceType : resourceTypes) {
                if (!(resourceType instanceof GenericIndexResourceType)) {
                    continue;
                }
                final String label = resourceType.getLabel();
                final String name = resourceType.getName();
                if (label == null) {
                    LOG.warn("Label should not be null for resource {}", resourceType);
                    genericDsTypes.put(name, name);
                } else {
                    genericDsTypes.put(label, name);
                }
            }
        }

        for (final String label : new LinkedHashSet<>(genericDsTypes.keys())) {
            final Collection<String> names = genericDsTypes.get(label);
            for (final String name : names) {
                dsTypes.add(DsTypeDto.of(name, names.size() > 1 ? label + " [" + name + "]" : label));
            }
        }

        return dsTypes;
    }

    // ------------------------------------------------------------------ writes

    @Override
    public Response createThresholdGroup(final ThresholdGroupDto group, final SecurityContext securityContext) {
        final String validationMessage = ThresholdConfigValidator.validateGroup(group);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        final String groupName = group.getName().trim();
        final AtomicReference<Response> failure = new AtomicReference<>();
        final AtomicBoolean eventsCreated = new AtomicBoolean();

        try {
            thresholdingDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }
                final Response conflict = nameConflict(config, groupName);
                if (conflict != null) {
                    failure.set(conflict);
                    return;
                }

                final Group entity = ThresholdingConfigMapper.toEntity(group);
                eventsCreated.set(thresholdUeiService.ensureUeisInEventConf(entity.getThresholdsAndExpressions()));

                final List<Group> groups = new ArrayList<>(config.getGroups());
                groups.add(entity);
                // setGroups rebuilds the name index; mutating the list in place would leave it stale.
                config.setGroups(groups);

                thresholdingDao.saveConfig();
            });
        } catch (final ValidationException e) {
            LOG.warn("Threshold group {} failed schema validation.", groupName, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to create threshold group {}.", groupName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist thresholding configuration.").build();
        } finally {
            // Also after a failed save: the definitions are committed either way, and until the reload
            // eventconf would neither use them nor know not to create them again.
            reloadEventConfIf(eventsCreated.get());
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendConfigChanged();
        return Response.created(URI.create("thresholding/groups/" + groupName)).build();
    }

    @Override
    public Response updateThresholdGroup(final String groupName, final ThresholdGroupDto group, final String ifMatch,
                                         final SecurityContext securityContext) {
        final String validationMessage = ThresholdConfigValidator.validateGroup(group);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        final String newName = group.getName().trim();
        final boolean renamed = !newName.equals(groupName);
        final AtomicReference<Response> failure = new AtomicReference<>();
        final AtomicBoolean eventsCreated = new AtomicBoolean();

        try {
            thresholdingDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }

                final Optional<Group> existing = findGroup(config, groupName);
                if (existing.isEmpty()) {
                    failure.set(readOnlyOrNotFound(groupName));
                    return;
                }
                if (renamed) {
                    final Response conflict = nameConflict(config, newName);
                    if (conflict != null) {
                        failure.set(conflict);
                        return;
                    }
                    // Packages contributed by extensions cannot be rewritten, so they would be left pointing
                    // at a group that no longer exists.
                    final Set<String> extensionUsers = extensionPackagesReferencing(groupName);
                    if (!extensionUsers.isEmpty()) {
                        failure.set(Response.status(Status.CONFLICT)
                                .entity("Threshold group '" + groupName + "' cannot be renamed: it is used by threshd "
                                        + "packages provided by an extension: " + String.join(", ", extensionUsers) + ".")
                                .build());
                        return;
                    }
                }

                final String currentEtag = ThresholdRestSupport.etagOf(objectMapper,
                        ThresholdingConfigMapper.toDto(existing.get(), false));
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, currentEtag)) {
                    failure.set(Response.status(Status.PRECONDITION_FAILED)
                            .entity("Threshold group '" + groupName + "' has changed since it was read.").build());
                    return;
                }

                final Group entity = ThresholdingConfigMapper.toEntity(group);
                eventsCreated.set(thresholdUeiService.ensureUeisInEventConf(entity.getThresholdsAndExpressions()));

                final List<Group> previousGroups = new ArrayList<>(config.getGroups());
                final List<Group> groups = new ArrayList<>();
                for (final Group candidate : previousGroups) {
                    groups.add(groupName.equals(candidate.getName()) ? entity : candidate);
                }
                config.setGroups(groups);

                if (renamed) {
                    // Lock order: thresholding, then threshd. Nothing takes them the other way round.
                    threshdDao.withWriteLock(threshd -> {
                        final boolean referenced = renameReferences(threshd, groupName, newName);
                        thresholdingDao.saveConfig();
                        if (!referenced) {
                            return;
                        }
                        try {
                            threshdDao.saveConfig();
                        } catch (final RuntimeException e) {
                            // The group is already stored under its new name; put the old one back so the
                            // packages that still use it keep working.
                            restoreGroups(config, previousGroups, e);
                            throw e;
                        }
                    });
                } else {
                    thresholdingDao.saveConfig();
                }
            });
        } catch (final ValidationException e) {
            LOG.warn("Threshold group {} failed schema validation.", groupName, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to update threshold group {}.", groupName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist thresholding configuration.").build();
        } finally {
            // Also after a failed save: the definitions are committed either way, and until the reload
            // eventconf would neither use them nor know not to create them again.
            reloadEventConfIf(eventsCreated.get());
        }

        if (failure.get() != null) {
            return failure.get();
        }

        // One notification covers both documents: the thresholding sets are rebuilt from both either way.
        sendConfigChanged();
        return Response.noContent().build();
    }

    @Override
    public Response deleteThresholdGroup(final String groupName, final String ifMatch,
                                         final SecurityContext securityContext) {
        final AtomicReference<Response> failure = new AtomicReference<>();

        try {
            thresholdingDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }

                final Optional<Group> existing = findGroup(config, groupName);
                if (existing.isEmpty()) {
                    failure.set(readOnlyOrNotFound(groupName));
                    return;
                }

                final String currentEtag = ThresholdRestSupport.etagOf(objectMapper,
                        ThresholdingConfigMapper.toDto(existing.get(), false));
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, currentEtag)) {
                    failure.set(Response.status(Status.PRECONDITION_FAILED)
                            .entity("Threshold group '" + groupName + "' has changed since it was read.").build());
                    return;
                }

                final Set<String> users = packagesReferencing(threshdDao.getReadOnlyConfig(), groupName);
                if (!users.isEmpty()) {
                    failure.set(Response.status(Status.CONFLICT)
                            .entity("Threshold group '" + groupName + "' is used by threshd packages: "
                                    + String.join(", ", users) + ".").build());
                    return;
                }

                final List<Group> groups = config.getGroups().stream()
                        .filter(candidate -> !groupName.equals(candidate.getName()))
                        .collect(Collectors.toList());
                config.setGroups(groups);

                thresholdingDao.saveConfig();
            });
        } catch (final Exception e) {
            LOG.error("Failed to delete threshold group {}.", groupName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist thresholding configuration.").build();
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendConfigChanged();
        return Response.noContent().build();
    }

    // ------------------------------------------------------- download / upload

    @Override
    public Response downloadThresholdingConfiguration(final String format, final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to download the thresholding configuration.").build();
        }

        final boolean isXml = format != null && format.equalsIgnoreCase("xml");
        if (!isXml && format != null && !format.equalsIgnoreCase("json")) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid format parameter. Supported values are 'json' and 'xml'.").build();
        }

        final byte[] body;
        try {
            final ThresholdingConfig config = thresholdingDao.getWriteableConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build();
            }

            if (isXml) {
                body = JaxbUtils.marshal(config).getBytes(StandardCharsets.UTF_8);
            } else {
                final ThresholdingConfigDto dto = new ThresholdingConfigDto();
                dto.setGroups(config.getGroups().stream()
                        .map(group -> ThresholdingConfigMapper.toDto(group, false))
                        .collect(Collectors.toList()));
                body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto).getBytes(StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            LOG.error("Error serializing the thresholding configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving the thresholding configuration.").build();
        }

        final String fileName = isXml ? "thresholds.xml" : "thresholds.json";
        final String contentType = isXml ? "application/xml" : "application/json";

        return Response.ok().type(contentType + ";charset=" + StandardCharsets.UTF_8)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Pragma", "public")
                .header("Cache-Control", "no-cache, must-revalidate")
                .entity(body).build();
    }

    @Override
    public Response uploadThresholdingConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(attachment, securityContext, false);
    }

    @Override
    public Response uploadThresholdingConfigurationXml(final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(attachment, securityContext, true);
    }

    private Response uploadInternal(final Attachment attachment, final SecurityContext securityContext, final boolean isXml) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to upload the thresholding configuration.").build();
        }

        final String fileType = isXml ? "XML" : "JSON";
        if (attachment == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Missing uploaded file for thresholding configuration " + fileType + " file upload.").build();
        }

        final List<Group> groups = new ArrayList<>();

        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            if (isXml) {
                // The validating overload: the two-argument one parses without checking the schema.
                final ThresholdingConfig parsed = JaxbUtils.unmarshal(ThresholdingConfig.class,
                        new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), true);
                groups.addAll(parsed.getGroups());
            } else {
                final ThresholdingConfigDto dto = objectMapper.readValue(inputStream, ThresholdingConfigDto.class);
                if (dto.getGroups() != null) {
                    for (final ThresholdGroupDto groupDto : dto.getGroups()) {
                        final String message = ThresholdConfigValidator.validateGroup(groupDto);
                        if (message != null) {
                            return Response.status(Status.BAD_REQUEST).entity(message).build();
                        }
                        groups.add(ThresholdingConfigMapper.toEntity(groupDto));
                    }
                }
            }
        } catch (final Exception e) {
            LOG.warn("Failed to parse the uploaded thresholding {} configuration.", fileType, e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid thresholding " + fileType + " configuration.").build();
        }

        final Set<String> uploadedNames = new HashSet<>();
        for (final Group group : groups) {
            if (!uploadedNames.add(group.getName())) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("The uploaded configuration contains threshold group '" + group.getName() + "' more than once.")
                        .build();
            }
        }

        final AtomicReference<Response> failure = new AtomicReference<>();
        final AtomicBoolean eventsCreated = new AtomicBoolean();

        try {
            thresholdingDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }

                final Set<String> extensionGroups = extensionGroupNames(config);
                for (final String name : uploadedNames) {
                    if (extensionGroups.contains(name)) {
                        failure.set(extensionConflict(name));
                        return;
                    }
                }

                // The same rule as for a delete: a group a package still uses must not disappear.
                final Map<String, Set<String>> missing = new LinkedHashMap<>();
                final ThreshdConfiguration threshd = threshdDao.getReadOnlyConfig();
                if (threshd != null) {
                    for (final Package pkg : threshd.getPackages()) {
                        for (final String name : groupsReferencedBy(pkg)) {
                            if (!uploadedNames.contains(name) && !extensionGroups.contains(name)) {
                                missing.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(pkg.getName());
                            }
                        }
                    }
                }
                if (!missing.isEmpty()) {
                    failure.set(Response.status(Status.CONFLICT)
                            .entity("The uploaded configuration drops threshold groups that threshd packages still use: "
                                    + missing.entrySet().stream()
                                            .map(e -> "'" + e.getKey() + "' (used by " + String.join(", ", e.getValue()) + ")")
                                            .collect(Collectors.joining(", ")) + ".")
                            .build());
                    return;
                }

                final List<Basethresholddef> defs = new ArrayList<>();
                groups.forEach(group -> defs.addAll(group.getThresholdsAndExpressions()));
                eventsCreated.set(thresholdUeiService.ensureUeisInEventConf(defs));

                config.setGroups(groups);
                thresholdingDao.saveConfig();
            });
        } catch (final ValidationException e) {
            LOG.warn("The uploaded thresholding configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to persist the uploaded thresholding configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist thresholding configuration.").build();
        } finally {
            reloadEventConfIf(eventsCreated.get());
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendConfigChanged();
        return Response.ok().build();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Reloads eventconf into memory once per request, and only when the request created event definitions.
     * Done here rather than per event: the reload is asynchronous and re-reads every event from the database.
     */
    private void reloadEventConfIf(final boolean eventsCreated) {
        if (eventsCreated) {
            eventConfPersistenceService.reloadEventsIntoMemory();
        }
    }

    private Set<String> writeableGroupNames() {
        final ThresholdingConfig writeable = thresholdingDao.getWriteableConfig();
        if (writeable == null) {
            return Set.of();
        }
        return writeable.getGroups().stream()
                .map(Group::getName)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * {@code ThresholdingConfig.getGroup} throws on an unknown name rather than returning null, which would
     * turn every typo into a 500.
     */
    private static Optional<Group> findGroup(final ThresholdingConfig config, final String groupName) {
        if (config == null || groupName == null) {
            return Optional.empty();
        }
        return config.getGroups().stream()
                .filter(group -> groupName.equals(group.getName()))
                .findFirst();
    }

    /**
     * Distinguishes "there is no such group" from "the group exists but is contributed by an extension", so
     * the client is not told to create something that is already there.
     */
    private Response readOnlyOrNotFound(final String groupName) {
        final ThresholdingConfig merged = thresholdingDao.getReadOnlyConfig();
        if (findGroup(merged, groupName).isPresent()) {
            return Response.status(Status.FORBIDDEN)
                    .entity("Threshold group '" + groupName + "' is provided by an extension and cannot be modified.").build();
        }
        return Response.status(Status.NOT_FOUND).entity(unknownGroup(groupName)).build();
    }

    private static String unknownGroup(final String groupName) {
        return "Threshold group '" + groupName + "' does not exist.";
    }

    /**
     * Returns a 409 when {@code name} is taken, either by a stored group or by one an extension provides. The
     * merged view keeps whichever duplicate comes last, so a clash with an extension group would silently
     * shadow one of the two definitions.
     */
    private Response nameConflict(final ThresholdingConfig draft, final String name) {
        if (findGroup(draft, name).isPresent()) {
            return Response.status(Status.CONFLICT).entity("Threshold group '" + name + "' already exists.").build();
        }
        if (extensionGroupNames(draft).contains(name)) {
            return extensionConflict(name);
        }
        return null;
    }

    private static Response extensionConflict(final String name) {
        return Response.status(Status.CONFLICT)
                .entity("Threshold group '" + name + "' already exists and is provided by an extension.").build();
    }

    /** Names of the groups only extensions provide: in the merged view, but not stored. */
    private Set<String> extensionGroupNames(final ThresholdingConfig draft) {
        final ThresholdingConfig merged = thresholdingDao.getReadOnlyConfig();
        if (merged == null) {
            return Set.of();
        }
        final Set<String> stored = new HashSet<>();
        final ThresholdingConfig writeable = thresholdingDao.getWriteableConfig();
        if (writeable != null) {
            writeable.getGroups().forEach(group -> stored.add(group.getName()));
        }
        if (draft != null) {
            draft.getGroups().forEach(group -> stored.add(group.getName()));
        }
        return merged.getGroups().stream()
                .map(Group::getName)
                .filter(name -> name != null && !stored.contains(name))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Names of the threshd packages, stored or provided by an extension, whose services use {@code groupName}. */
    static Set<String> packagesReferencing(final ThreshdConfiguration threshd, final String groupName) {
        if (threshd == null) {
            return Set.of();
        }
        return threshd.getPackages().stream()
                .filter(pkg -> groupsReferencedBy(pkg).contains(groupName))
                .map(Package::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> extensionPackagesReferencing(final String groupName) {
        final Set<String> users = new LinkedHashSet<>(packagesReferencing(threshdDao.getReadOnlyConfig(), groupName));
        users.removeAll(packagesReferencing(threshdDao.getWriteableConfig(), groupName));
        return users;
    }

    private static Set<String> groupsReferencedBy(final Package pkg) {
        final Set<String> names = new LinkedHashSet<>();
        for (final org.opennms.netmgt.config.threshd.Service service : pkg.getServices()) {
            for (final Parameter parameter : service.getParameters()) {
                if (THRESHOLDING_GROUP_PARAMETER.equals(parameter.getKey()) && parameter.getValue() != null) {
                    names.add(parameter.getValue());
                }
            }
        }
        return names;
    }

    /**
     * Points every {@code thresholding-group} parameter in the threshd draft that names {@code oldName} at
     * {@code newName}. The draft is a private copy, so editing it in place is safe.
     *
     * @return whether any parameter was changed
     */
    private static boolean renameReferences(final ThreshdConfiguration threshd, final String oldName, final String newName) {
        if (threshd == null) {
            return false;
        }
        boolean changed = false;
        for (final Package pkg : threshd.getPackages()) {
            for (final org.opennms.netmgt.config.threshd.Service service : pkg.getServices()) {
                for (final Parameter parameter : service.getParameters()) {
                    if (THRESHOLDING_GROUP_PARAMETER.equals(parameter.getKey()) && oldName.equals(parameter.getValue())) {
                        parameter.setValue(newName);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private void restoreGroups(final ThresholdingConfig config, final List<Group> previousGroups, final RuntimeException cause) {
        try {
            config.setGroups(previousGroups);
            thresholdingDao.saveConfig();
        } catch (final RuntimeException e) {
            LOG.error("Could not restore the thresholding configuration after the threshd save failed; threshd "
                    + "packages may now reference a threshold group that no longer exists.", e);
            cause.addSuppressed(e);
        }
    }

    private void sendConfigChanged() {
        try {
            ThresholdRestSupport.sendConfigChangedEvent(eventProxy);
        } catch (final Exception e) {
            // The configuration is already stored at this point, so failing the request would be misleading.
            LOG.warn("Saved the thresholding configuration but could not tell the thresholding service it changed.", e);
        }
    }
}
