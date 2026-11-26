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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.support.GenericIndexResourceType;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.cm.XmlDataCollectionConfigDaoCm;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.XmlCollectionConfigRestApi;
import org.opennms.web.rest.v2.mapper.XmlCollectionConfigMapper;
import org.opennms.web.rest.v2.model.DsTypeDto;
import org.opennms.web.rest.v2.model.XmlCollectionConfigDto;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlCollectionMetadataDto;
import org.opennms.web.rest.v2.model.XmlCollectionSettingsDto;
import org.opennms.web.rest.v2.model.XmlCollectionSummaryDto;
import org.opennms.web.rest.v2.model.XmlGroupFileDto;
import org.opennms.web.rest.v2.model.XmlGroupFileSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Edits the XML collector configuration held by the configuration manager: {@code xml-datacollection-config}
 * (settings and collections, one configuration) and {@code xml-datacollection-groups} (group files, one
 * configuration each).
 *
 * <p>Reads go straight to the configuration manager and return the configuration as stored, that is without the
 * {@code import-groups} resolution {@link XmlDataCollectionConfigDaoCm} applies for the collector. Writes are
 * serialized by a lock of this service, which is the only writer apart from upgrades. The collector picks every
 * change up by itself: {@link XmlDataCollectionConfigDaoCm} drops its cache on each configuration manager event.</p>
 */
@Service
public class XmlCollectionConfigRestService implements XmlCollectionConfigRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(XmlCollectionConfigRestService.class);

    static final String CONFIG_NAME = XmlDataCollectionConfigDaoCm.CONFIG_NAME;
    static final String GROUPS_CONFIG_NAME = XmlDataCollectionConfigDaoCm.GROUPS_CONFIG_NAME;
    static final String CONFIG_ID = ConfigDefinition.DEFAULT_CONFIG_ID;

    private static final String READ_FAILED = "Failed to retrieve the XML collector configuration.";
    private static final String WRITE_FAILED = "Failed to persist the XML collector configuration.";
    private static final String XML_EXTENSION = ".xml";

    // The REST stack is wired to the codehaus JsonProvider; ETags and the download must use the same mapper.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConfigurationManagerService configurationManagerService;

    @Autowired
    private ResourceDao resourceDao;

    private final ReentrantLock writeLock = new ReentrantLock();

    // ---------------------------------------------------------------- settings

    @Override
    public Response getSettings(final SecurityContext securityContext) {
        try {
            final XmlCollectionSettingsDto dto = new XmlCollectionSettingsDto();
            dto.setRrdRepository(readConfig().getRrdRepository());
            return Response.ok(dto).build();
        } catch (final Exception e) {
            LOG.error(READ_FAILED, e);
            return serverError(READ_FAILED);
        }
    }

    @Override
    public Response updateSettings(final XmlCollectionSettingsDto settings, final SecurityContext securityContext) {
        final String message = XmlCollectionConfigValidator.validateRrdRepository(settings == null ? null : settings.getRrdRepository());
        if (message != null) {
            return badRequest(message);
        }
        return write(() -> {
            final XmlDataCollectionConfig config = readConfig();
            config.setRrdRepository(settings.getRrdRepository().trim());
            writeConfig(config);
            return Response.noContent().build();
        });
    }

    // ------------------------------------------------------------- collections

    @Override
    public Response getCollections(final SecurityContext securityContext) {
        try {
            final List<XmlCollectionSummaryDto> summaries = readConfig().getXmlDataCollections().stream()
                    .sorted(Comparator.comparing(XmlDataCollection::getName, Comparator.nullsLast(String::compareTo)))
                    .map(XmlCollectionConfigMapper::toSummaryDto)
                    .collect(Collectors.toList());
            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error(READ_FAILED, e);
            return serverError(READ_FAILED);
        }
    }

    @Override
    public Response getCollection(final String name, final SecurityContext securityContext) {
        try {
            final Optional<XmlDataCollection> collection = findCollection(readConfig(), name);
            if (collection.isEmpty()) {
                return notFound(unknownCollection(name));
            }
            final XmlCollectionDto dto = XmlCollectionConfigMapper.toDto(collection.get());
            final String etag = ConfigEtags.etagOf(objectMapper, dto);
            dto.setVersion(etag);
            return Response.ok(dto).tag(etag).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve XML data collection {}.", name, e);
            return serverError(READ_FAILED);
        }
    }

    @Override
    public Response createCollection(final XmlCollectionDto collection, final SecurityContext securityContext) {
        return write(() -> {
            final String message = XmlCollectionConfigValidator.validateCollection(collection, groupFileIds());
            if (message != null) {
                return badRequest(message);
            }
            final String name = collection.getName().trim();
            final XmlDataCollectionConfig config = readConfig();
            if (findCollection(config, name).isPresent()) {
                return conflict("XML data collection '" + name + "' already exists.");
            }
            final List<XmlDataCollection> collections = new ArrayList<>(config.getXmlDataCollections());
            collections.add(XmlCollectionConfigMapper.toEntity(collection));
            config.setXmlDataCollections(collections);
            writeConfig(config);
            return Response.created(UriBuilder.fromPath("xml-collection/collections/{name}").build(name)).build();
        });
    }

    @Override
    public Response updateCollection(final String name, final XmlCollectionDto collection, final String ifMatch,
                                     final SecurityContext securityContext) {
        return write(() -> {
            final String message = XmlCollectionConfigValidator.validateCollection(collection, groupFileIds());
            if (message != null) {
                return badRequest(message);
            }
            final String newName = collection.getName().trim();
            final XmlDataCollectionConfig config = readConfig();
            final Optional<XmlDataCollection> existing = findCollection(config, name);
            if (existing.isEmpty()) {
                return notFound(unknownCollection(name));
            }
            if (!newName.equals(name) && findCollection(config, newName).isPresent()) {
                return conflict("XML data collection '" + newName + "' already exists.");
            }
            if (!ConfigEtags.matchesIfMatch(ifMatch, ConfigEtags.etagOf(objectMapper, XmlCollectionConfigMapper.toDto(existing.get())))) {
                return preconditionFailed("XML data collection '" + name + "' has changed since it was read.");
            }
            final XmlDataCollection entity = XmlCollectionConfigMapper.toEntity(collection);
            config.setXmlDataCollections(config.getXmlDataCollections().stream()
                    .map(candidate -> candidate == existing.get() ? entity : candidate)
                    .collect(Collectors.toCollection(ArrayList::new)));
            writeConfig(config);
            return Response.noContent().build();
        });
    }

    @Override
    public Response deleteCollection(final String name, final String ifMatch, final SecurityContext securityContext) {
        return write(() -> {
            final XmlDataCollectionConfig config = readConfig();
            final Optional<XmlDataCollection> existing = findCollection(config, name);
            if (existing.isEmpty()) {
                return notFound(unknownCollection(name));
            }
            if (!ConfigEtags.matchesIfMatch(ifMatch, ConfigEtags.etagOf(objectMapper, XmlCollectionConfigMapper.toDto(existing.get())))) {
                return preconditionFailed("XML data collection '" + name + "' has changed since it was read.");
            }
            config.setXmlDataCollections(config.getXmlDataCollections().stream()
                    .filter(candidate -> candidate != existing.get())
                    .collect(Collectors.toCollection(ArrayList::new)));
            writeConfig(config);
            return Response.noContent().build();
        });
    }

    // ------------------------------------------------------------- group files

    @Override
    public Response getGroupFiles(final SecurityContext securityContext) {
        try {
            final Map<String, Set<String>> users = groupFileUsers(readConfig());
            final List<XmlGroupFileSummaryDto> summaries = new ArrayList<>();
            readGroupFiles().forEach((id, groups) -> summaries.add(XmlCollectionConfigMapper.toGroupFileSummaryDto(
                    id, groups, new ArrayList<>(users.getOrDefault(id, Set.of())))));
            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error(READ_FAILED, e);
            return serverError(READ_FAILED);
        }
    }

    @Override
    public Response getGroupFile(final String id, final SecurityContext securityContext) {
        try {
            final Optional<XmlGroups> groups = readGroupFile(id);
            if (groups.isEmpty()) {
                return notFound(unknownGroupFile(id));
            }
            final XmlGroupFileDto dto = XmlCollectionConfigMapper.toGroupFileDto(id, groups.get());
            final String etag = ConfigEtags.etagOf(objectMapper, dto);
            dto.setVersion(etag);
            return Response.ok(dto).tag(etag).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve XML group file {}.", id, e);
            return serverError(READ_FAILED);
        }
    }

    @Override
    public Response createGroupFile(final XmlGroupFileDto groupFile, final SecurityContext securityContext) {
        final String message = XmlCollectionConfigValidator.validateGroupFile(groupFile);
        if (message != null) {
            return badRequest(message);
        }
        final String id = groupFile.getId().trim();
        return write(() -> {
            if (groupFileIds().contains(id)) {
                return conflict("Group file '" + id + "' already exists.");
            }
            configurationManagerService.registerConfiguration(GROUPS_CONFIG_NAME, id,
                    toJson(XmlCollectionConfigMapper.toEntity(groupFile)));
            return Response.created(UriBuilder.fromPath("xml-collection/groups/{id}").build(id)).build();
        });
    }

    @Override
    public Response updateGroupFile(final String id, final XmlGroupFileDto groupFile, final String ifMatch,
                                    final SecurityContext securityContext) {
        if (groupFile != null && groupFile.getId() != null && !groupFile.getId().isBlank() && !groupFile.getId().trim().equals(id)) {
            return badRequest("Group file '" + id + "' cannot be renamed; create a new group file instead.");
        }
        if (groupFile != null) {
            groupFile.setId(id);
        }
        final String message = XmlCollectionConfigValidator.validateGroupFile(groupFile);
        if (message != null) {
            return badRequest(message);
        }
        return write(() -> {
            final Optional<XmlGroups> existing = readGroupFile(id);
            if (existing.isEmpty()) {
                return notFound(unknownGroupFile(id));
            }
            if (!ConfigEtags.matchesIfMatch(ifMatch, ConfigEtags.etagOf(objectMapper, XmlCollectionConfigMapper.toGroupFileDto(id, existing.get())))) {
                return preconditionFailed("Group file '" + id + "' has changed since it was read.");
            }
            configurationManagerService.updateConfiguration(GROUPS_CONFIG_NAME, id,
                    toJson(XmlCollectionConfigMapper.toEntity(groupFile)), true);
            return Response.noContent().build();
        });
    }

    @Override
    public Response deleteGroupFile(final String id, final String ifMatch, final SecurityContext securityContext) {
        return write(() -> {
            final Optional<XmlGroups> existing = readGroupFile(id);
            if (existing.isEmpty()) {
                return notFound(unknownGroupFile(id));
            }
            if (!ConfigEtags.matchesIfMatch(ifMatch, ConfigEtags.etagOf(objectMapper, XmlCollectionConfigMapper.toGroupFileDto(id, existing.get())))) {
                return preconditionFailed("Group file '" + id + "' has changed since it was read.");
            }
            final Set<String> users = groupFileUsers(readConfig()).getOrDefault(id, Set.of());
            if (!users.isEmpty()) {
                return conflict("Group file '" + id + "' is imported by collections: " + String.join(", ", users) + ".");
            }
            configurationManagerService.unregisterConfiguration(GROUPS_CONFIG_NAME, id);
            return Response.noContent().build();
        });
    }

    // ---------------------------------------------------------------- metadata

    @Override
    public Response getMetadata(final SecurityContext securityContext) {
        try {
            final XmlCollectionMetadataDto dto = new XmlCollectionMetadataDto();
            dto.setAttributeTypes(Arrays.stream(AttributeType.values()).map(AttributeType::getName).collect(Collectors.toList()));
            dto.setResourceTypes(buildResourceTypes());
            dto.setGroupFiles(new ArrayList<>(groupFileIds()));
            return Response.ok(dto).build();
        } catch (final Exception e) {
            LOG.error("Failed to build the XML collector metadata.", e);
            return serverError(READ_FAILED);
        }
    }

    /**
     * The resource types a group can store its values under: {@code node} first, then every generic index resource
     * type sorted by label.
     */
    private List<DsTypeDto> buildResourceTypes() {
        final List<DsTypeDto> types = new ArrayList<>();
        types.add(DsTypeDto.of("node", "Node"));
        final Collection<OnmsResourceType> resourceTypes = resourceDao.getResourceTypes();
        if (resourceTypes != null) {
            resourceTypes.stream()
                    .filter(GenericIndexResourceType.class::isInstance)
                    .map(type -> DsTypeDto.of(type.getName(), type.getLabel() == null ? type.getName() : type.getLabel() + " [" + type.getName() + "]"))
                    .sorted(Comparator.comparing(DsTypeDto::getLabel, String.CASE_INSENSITIVE_ORDER))
                    .forEach(types::add);
        }
        return types;
    }

    // ------------------------------------------------------- download / upload

    @Override
    public Response downloadConfiguration(final SecurityContext securityContext) {
        if (!isAdmin(securityContext)) {
            return forbidden("Admin role required to download the XML collector configuration.");
        }
        try {
            final XmlDataCollectionConfig config = readConfig();
            final XmlCollectionConfigDto dto = new XmlCollectionConfigDto();
            dto.setRrdRepository(config.getRrdRepository());
            dto.setCollections(config.getXmlDataCollections().stream()
                    .map(XmlCollectionConfigMapper::toDto)
                    .collect(Collectors.toList()));
            final List<XmlGroupFileDto> groupFiles = new ArrayList<>();
            readGroupFiles().forEach((id, groups) -> groupFiles.add(XmlCollectionConfigMapper.toGroupFileDto(id, groups)));
            dto.setGroupFiles(groupFiles);
            final byte[] body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto).getBytes(StandardCharsets.UTF_8);
            return Response.ok().type("application/json;charset=" + StandardCharsets.UTF_8)
                    .header("Content-Disposition", "attachment; filename=xml-collection.json")
                    .header("Pragma", "public")
                    .header("Cache-Control", "no-cache, must-revalidate")
                    .entity(body).build();
        } catch (final Exception e) {
            LOG.error("Error serializing the XML collector configuration.", e);
            return serverError(READ_FAILED);
        }
    }

    @Override
    public Response uploadConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        if (!isAdmin(securityContext)) {
            return forbidden("Admin role required to upload the XML collector configuration.");
        }
        if (attachment == null) {
            return badRequest("Missing uploaded file.");
        }

        final XmlCollectionConfigDto dto;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            dto = objectMapper.readValue(inputStream, XmlCollectionConfigDto.class);
        } catch (final Exception e) {
            LOG.warn("Failed to parse the uploaded XML collector configuration.", e);
            return badRequest("Invalid XML collector JSON configuration.");
        }

        // Validate everything before anything is written.
        final String rrdMessage = XmlCollectionConfigValidator.validateRrdRepository(dto.getRrdRepository());
        if (rrdMessage != null) {
            return badRequest(rrdMessage);
        }
        final List<XmlGroupFileDto> groupFiles = dto.getGroupFiles() == null ? List.of() : dto.getGroupFiles();
        final Set<String> uploadedIds = new LinkedHashSet<>();
        for (final XmlGroupFileDto groupFile : groupFiles) {
            final String message = XmlCollectionConfigValidator.validateGroupFile(groupFile);
            if (message != null) {
                return badRequest(message);
            }
            if (!uploadedIds.add(groupFile.getId().trim())) {
                return badRequest("The uploaded configuration contains group file '" + groupFile.getId().trim() + "' more than once.");
            }
        }
        final List<XmlCollectionDto> collections = dto.getCollections() == null ? List.of() : dto.getCollections();
        final String collectionsMessage = validateCollections(collections, uploadedIds);
        if (collectionsMessage != null) {
            return badRequest(collectionsMessage);
        }

        return write(() -> {
            final Set<String> existingIds = groupFileIds();
            for (final XmlGroupFileDto groupFile : groupFiles) {
                writeGroupFile(groupFile.getId().trim(), XmlCollectionConfigMapper.toEntity(groupFile), existingIds);
            }
            final XmlDataCollectionConfig config = new XmlDataCollectionConfig();
            config.setRrdRepository(dto.getRrdRepository().trim());
            config.setXmlDataCollections(collections.stream().map(XmlCollectionConfigMapper::toEntity).collect(Collectors.toCollection(ArrayList::new)));
            writeConfig(config);
            // Last, so that no collection ever refers to a group file that is gone.
            for (final String id : existingIds) {
                if (!uploadedIds.contains(id)) {
                    configurationManagerService.unregisterConfiguration(GROUPS_CONFIG_NAME, id);
                }
            }
            return Response.ok().build();
        });
    }

    @Override
    public Response uploadXml(final Attachment attachment, final String id, final SecurityContext securityContext) {
        if (!isAdmin(securityContext)) {
            return forbidden("Admin role required to upload the XML collector configuration.");
        }
        if (attachment == null) {
            return badRequest("Missing uploaded file.");
        }

        final String xml;
        final String rootElement;
        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            rootElement = rootElementOf(xml);
        } catch (final Exception e) {
            LOG.warn("Failed to read the uploaded XML collector file.", e);
            return badRequest("The uploaded file is not a well-formed XML document.");
        }

        try {
            if ("xml-datacollection-config".equals(rootElement)) {
                return importCollectionsXml(JaxbUtils.unmarshal(XmlDataCollectionConfig.class, xml, false, true));
            }
            if ("xml-groups".equals(rootElement)) {
                final String groupFileId = id != null && !id.isBlank() ? id.trim() : groupFileIdOf(attachment);
                return importGroupFileXml(groupFileId, JaxbUtils.unmarshal(XmlGroups.class, xml, false, true));
            }
        } catch (final RuntimeException e) {
            LOG.warn("Failed to parse the uploaded XML collector file.", e);
            return badRequest("The uploaded <" + rootElement + "> document is invalid: " + e.getMessage());
        }
        return badRequest("Unsupported document <" + rootElement + ">; expected <xml-datacollection-config> or <xml-groups>.");
    }

    private Response importCollectionsXml(final XmlDataCollectionConfig uploaded) {
        final String rrdMessage = XmlCollectionConfigValidator.validateRrdRepository(uploaded.getRrdRepository());
        if (rrdMessage != null) {
            return badRequest(rrdMessage);
        }
        final List<XmlCollectionDto> collections = uploaded.getXmlDataCollections().stream()
                .map(XmlCollectionConfigMapper::toDto)
                .collect(Collectors.toList());
        return write(() -> {
            final String message = validateCollections(collections, groupFileIds());
            if (message != null) {
                return badRequest(message + (message.contains("does not exist") ? " Upload the group files first." : ""));
            }
            final XmlDataCollectionConfig config = new XmlDataCollectionConfig();
            config.setRrdRepository(uploaded.getRrdRepository().trim());
            config.setXmlDataCollections(collections.stream().map(XmlCollectionConfigMapper::toEntity).collect(Collectors.toCollection(ArrayList::new)));
            writeConfig(config);
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("type", "collections");
            result.put("count", collections.size());
            return Response.ok(result).build();
        });
    }

    private Response importGroupFileXml(final String id, final XmlGroups uploaded) {
        final XmlGroupFileDto dto = XmlCollectionConfigMapper.toGroupFileDto(id, uploaded);
        final String message = XmlCollectionConfigValidator.validateGroupFile(dto);
        if (message != null) {
            return badRequest(message);
        }
        final String groupFileId = id.trim();
        return write(() -> {
            writeGroupFile(groupFileId, XmlCollectionConfigMapper.toEntity(dto), groupFileIds());
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("type", "groupFile");
            result.put("id", groupFileId);
            return Response.ok(result).build();
        });
    }

    private static String validateCollections(final List<XmlCollectionDto> collections, final Set<String> groupFileIds) {
        final Set<String> names = new HashSet<>();
        for (final XmlCollectionDto collection : collections) {
            final String message = XmlCollectionConfigValidator.validateCollection(collection, groupFileIds);
            if (message != null) {
                return message;
            }
            if (!names.add(collection.getName().trim())) {
                return "XML data collection '" + collection.getName().trim() + "' is defined more than once.";
            }
        }
        return null;
    }

    /** The group file id of an uploaded file: its name without directory and {@code .xml}. */
    private static String groupFileIdOf(final Attachment attachment) {
        final String fileName = attachment.getContentDisposition() == null ? null : attachment.getContentDisposition().getParameter("filename");
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        return XmlDataCollectionConfigDaoCm.toGroupsConfigId(fileName);
    }

    /** Reads just far enough to tell the document type, with DTDs and external entities disabled. */
    private static String rootElementOf(final String xml) throws Exception {
        final XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        final XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        try {
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    return reader.getLocalName();
                }
            }
        } finally {
            reader.close();
        }
        throw new IllegalArgumentException("The document has no root element.");
    }

    // ------------------------------------------------------------------ storage

    /**
     * @return the stored configuration as is, without resolved {@code import-groups}; an empty one when none is stored
     */
    private XmlDataCollectionConfig readConfig() {
        return configurationManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID)
                .map(json -> ConfigConvertUtil.jsonToObject(XmlDataCollectionConfigDaoCm.withoutEmptyContainers(json).toString(),
                        XmlDataCollectionConfig.class))
                .orElseGet(XmlDataCollectionConfig::new);
    }

    private void writeConfig(final XmlDataCollectionConfig config) {
        final JsonAsString json = toJson(config);
        if (configurationManagerService.getJSONConfiguration(CONFIG_NAME, CONFIG_ID).isPresent()) {
            configurationManagerService.updateConfiguration(CONFIG_NAME, CONFIG_ID, json, true);
        } else {
            configurationManagerService.registerConfiguration(CONFIG_NAME, CONFIG_ID, json);
        }
    }

    private Set<String> groupFileIds() {
        return new TreeSet<>(configurationManagerService.getConfigIds(GROUPS_CONFIG_NAME));
    }

    private Optional<XmlGroups> readGroupFile(final String id) {
        if (id == null || !groupFileIds().contains(id)) {
            return Optional.empty();
        }
        return configurationManagerService.getJSONConfiguration(GROUPS_CONFIG_NAME, id)
                .map(json -> ConfigConvertUtil.jsonToObject(XmlDataCollectionConfigDaoCm.withoutEmptyContainers(json).toString(),
                        XmlGroups.class));
    }

    /** @return every group file, sorted by id */
    private Map<String, XmlGroups> readGroupFiles() {
        final Map<String, XmlGroups> groupFiles = new TreeMap<>();
        for (final String id : groupFileIds()) {
            readGroupFile(id).ifPresent(groups -> groupFiles.put(id, groups));
        }
        return groupFiles;
    }

    private void writeGroupFile(final String id, final XmlGroups groups, final Set<String> existingIds) {
        if (existingIds.contains(id)) {
            configurationManagerService.updateConfiguration(GROUPS_CONFIG_NAME, id, toJson(groups), true);
        } else {
            configurationManagerService.registerConfiguration(GROUPS_CONFIG_NAME, id, toJson(groups));
        }
    }

    private static JsonAsString toJson(final Object config) {
        return new JsonAsString(ConfigConvertUtil.objectToJson(config));
    }

    /** @return for each group file id, the names of the collections that import it */
    private static Map<String, Set<String>> groupFileUsers(final XmlDataCollectionConfig config) {
        final Map<String, Set<String>> users = new TreeMap<>();
        for (final XmlDataCollection collection : config.getXmlDataCollections()) {
            for (final XmlSource source : collection.getXmlSources()) {
                for (final String id : XmlCollectionConfigMapper.importGroupIds(source)) {
                    users.computeIfAbsent(id, k -> new TreeSet<>()).add(collection.getName());
                }
            }
        }
        return users;
    }

    private static Optional<XmlDataCollection> findCollection(final XmlDataCollectionConfig config, final String name) {
        if (name == null) {
            return Optional.empty();
        }
        return config.getXmlDataCollections().stream()
                .filter(collection -> Objects.equals(name, collection.getName()))
                .findFirst();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Runs a write while holding the write lock, so read-modify-write cycles of concurrent requests do not
     * overwrite each other, and maps the configuration manager's failures to responses.
     */
    private Response write(final Supplier<Response> operation) {
        writeLock.lock();
        try {
            return operation.get();
        } catch (final ValidationException e) {
            LOG.warn("The XML collector configuration failed schema validation.", e);
            return badRequest(e.getMessage());
        } catch (final Exception e) {
            LOG.error(WRITE_FAILED, e);
            return serverError(WRITE_FAILED);
        } finally {
            writeLock.unlock();
        }
    }

    private static boolean isAdmin(final SecurityContext securityContext) {
        return securityContext != null && securityContext.isUserInRole(Authentication.ROLE_ADMIN);
    }

    private static String unknownCollection(final String name) {
        return "XML data collection '" + name + "' does not exist.";
    }

    private static String unknownGroupFile(final String id) {
        return "Group file '" + id + "' does not exist.";
    }

    private static Response badRequest(final String message) {
        return Response.status(Status.BAD_REQUEST).entity(message).build();
    }

    private static Response forbidden(final String message) {
        return Response.status(Status.FORBIDDEN).entity(message).build();
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
