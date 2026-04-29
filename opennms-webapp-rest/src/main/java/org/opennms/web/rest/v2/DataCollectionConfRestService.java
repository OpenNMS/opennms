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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.DatacollectionJsonHelper;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.dao.support.SnmpDataCollectionConfigLoader;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionProfileDto;
import org.opennms.netmgt.model.SnmpCollectionSourceDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.opennms.netmgt.model.SnmpCollectionMibGroupDto;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;


import org.opennms.web.rest.v2.api.DataCollectionConfRestApi;
import org.opennms.web.rest.v2.model.SnmpCollectionSourceNamesAndIdsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Date;

@Component
public class DataCollectionConfRestService  implements DataCollectionConfRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfRestService.class);

    @Autowired
    private DataCollectionConfPersistenceService dataCollectionConfPersistenceService;

    @Autowired
    private SnmpCollectionSourceDao snmpCollectionSourceDao;

    @Autowired
    private SnmpDataCollectionConfigLoader snmpDataCollectionConfigLoader;

    @Override
    public Response uploadSnmpDataCollectionConfFiles(List<Attachment> attachments,
                                                      List<Attachment> profileNameParts,
                                                      SecurityContext securityContext) throws Exception {

        final String username = getUsername(securityContext);
        final Date now = new Date();
        final List<String> profileNames = readProfileNamesParts(profileNameParts);

        final Map<String, Attachment> fileMap = new LinkedHashMap<>();
        for (Attachment attachment : attachments) {
            String filename = attachment.getContentDisposition().getParameter("filename");
            String basename = stripPathAndExtension(filename);

            if (basename == null || basename.isEmpty()) {
                LOG.warn("Skipping attachment with invalid filename: {}", filename);
                continue;
            }

            if (fileMap.containsKey(basename)) {
                String existingFilename = fileMap.get(basename).getContentDisposition().getParameter("filename");
                LOG.warn("Duplicate basename detected: '{}' and '{}' resolve to same name '{}'. Keeping first file.",
                        existingFilename, filename, basename);
                continue;
            }

            fileMap.put(basename, attachment);
        }
        List<String> orderedFiles = new ArrayList<>(fileMap.keySet());

        // Two-pass: first parse every file and route by root element, since a
        // batch can mix <datacollection-group> source files with at most one
        // <datacollection-config> profiles file. We track the original upload
        // filename for every parsed source so the response schema can stay
        // consistent ({file, ...} for success and errors) regardless of which
        // path handled the file.
        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();
        final LinkedHashMap<String, DatacollectionGroup> parsedSources =
                new LinkedHashMap<>(); // sourceName -> parsed group
        final Map<String, String> sourceNameToFile = new LinkedHashMap<>();
        DatacollectionConfig parsedConfig = null;
        String parsedConfigFileName = null;

        for (final String fileName : orderedFiles) {
            final Attachment attachment = fileMap.get(fileName);
            if (attachment == null) {
                continue;
            }
            try (InputStream stream = attachment.getObject(InputStream.class)) {
                final byte[] bytes = stream.readAllBytes();
                final String rootElement = peekRootElement(bytes);
                if ("datacollection-config".equals(rootElement)) {
                    if (parsedConfig != null) {
                        errorList.add(buildErrorResponse(fileName,
                                new IllegalArgumentException(
                                        "Only one <datacollection-config> file may be uploaded per request; '"
                                                + parsedConfigFileName + "' was already accepted.")));
                        continue;
                    }
                    parsedConfig = parseDatacollectionConfig(new ByteArrayInputStream(bytes));
                    parsedConfigFileName = fileName;
                } else {
                    final DatacollectionGroup dcg =
                            parseDataCollectionFile(new ByteArrayInputStream(bytes));
                    final String sourceName = dcg.getName() != null ? dcg.getName() : fileName;
                    parsedSources.put(sourceName, dcg);
                    sourceNameToFile.put(sourceName, fileName);
                }
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
            }
        }

        // If only sources were uploaded (the existing flow), retain the
        // "must pick at least one profile" contract so newly-created sources
        // don't end up orphaned. When a <datacollection-config> is part of
        // the upload, it drives source-to-profile attachment instead.
        if (parsedConfig == null && !parsedSources.isEmpty() && profileNames.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "At least one profileNames value is required to upload sources."))
                    .build();
        }

        if (parsedConfig != null) {
            // Bulk path: persist all sources first, then upsert profiles from
            // the config XML. profileNames from the form is ignored because the
            // config's <include-collection> entries are authoritative. Any
            // mid-batch DB failure aborts the whole transaction (see
            // bulkUploadConfig() Javadoc), reported here as a single
            // {file: <config>, error: <msg>} entry.
            final String configFile = parsedConfigFileName != null ? parsedConfigFileName : "datacollection-config.xml";
            try {
                final var bulkResult = dataCollectionConfPersistenceService.bulkUploadConfig(
                        new ArrayList<>(parsedSources.values()), parsedConfig, username, now);
                bulkResult.sources.forEach(name -> {
                    final String fname = sourceNameToFile.getOrDefault(name, name);
                    final Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("file", fname);
                    entry.put("source", name);
                    successList.add(entry);
                });
                bulkResult.profiles.forEach(name -> {
                    final Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("file", configFile);
                    entry.put("profile", name);
                    successList.add(entry);
                });
                bulkResult.errors.forEach(msg -> {
                    final Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("file", configFile);
                    entry.put("error", msg);
                    errorList.add(entry);
                });
            } catch (Exception e) {
                final Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("file", configFile);
                entry.put("error", "Bulk upload failed: " + e.getMessage());
                errorList.add(entry);
            }
        } else {
            // Sources-only path: existing per-file behavior, profileNames from
            // the form drives attachment. The "file" field on success/error
            // entries has historically been the parsed <datacollection-group>
            // name rather than the upload form's filename — preserved here so
            // older clients that deserialize the response shape don't regress.
            for (final Map.Entry<String, DatacollectionGroup> entry
                    : parsedSources.entrySet()) {
                final String sourceName = entry.getKey();
                final var dcg = entry.getValue();
                try {
                    dataCollectionConfPersistenceService.addDataCollectionConfig(sourceName, username, dcg, now, profileNames);
                    successList.add(buildSuccessResponse(sourceName, dcg));
                } catch (Exception e) {
                    errorList.add(buildErrorResponse(sourceName, e));
                }
            }
        }

        if (!successList.isEmpty()) {
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
        }

        return Response.ok(Map.of("success", successList, "errors", errorList)).build();
    }

    @Override
    public Response listSnmpCollectionProfiles(final SecurityContext securityContext) {
        try {
            final var profiles = dataCollectionConfPersistenceService.getAllProfiles();
            final List<SnmpCollectionProfileDto> dtos = profiles.stream()
                    .map(this::toProfileDto)
                    .toList();
            return Response.ok(dtos).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    private SnmpCollectionProfileDto toProfileDto(final SnmpCollectionProfile entity) {
        final SnmpCollectionProfileDto dto = new SnmpCollectionProfileDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setRrdStep(entity.getRrdStep());
        dto.setRrdRras(parseStringList(entity.getRrdRras()));
        dto.setStorageFlag(entity.getStorageFlag());
        dto.setSourceNames(parseStringList(entity.getSourceNames()));
        dto.setMaxVarsPerPdu(entity.getMaxVarsPerPdu());
        dto.setEnabled(entity.getEnabled());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setLastModified(entity.getLastModified());
        return dto;
    }

    private static List<String> parseStringList(final String json) {
        final List<String> parsed = DatacollectionJsonHelper.fromJson(json, new TypeReference<>() {});
        return parsed != null ? parsed : new ArrayList<>();
    }

    @Override
    public Response getSnmpCollectionProfile(final Integer profileId,
                                             final SecurityContext securityContext) {
        try {
            final var profile = dataCollectionConfPersistenceService.getProfileById(profileId);
            return Response.ok(toProfileDto(profile)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response createSnmpCollectionProfile(final SnmpCollectionProfileDto profile,
                                                final SecurityContext securityContext) {
        try {
            final Integer id = dataCollectionConfPersistenceService.createProfile(profile, new Date());
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.status(Response.Status.CREATED).entity(id).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response updateSnmpCollectionProfile(final Integer profileId,
                                                final SnmpCollectionProfileDto profile,
                                                final SecurityContext securityContext) {
        try {
            dataCollectionConfPersistenceService.updateProfile(profileId, profile, new Date());
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response deleteSnmpCollectionProfiles(final List<Integer> ids,
                                                 final SecurityContext securityContext) {
        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one id must be provided.")
                    .build();
        }
        try {
            dataCollectionConfPersistenceService.deleteProfiles(ids);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("SNMP collection profiles deleted.").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response enableDisableSnmpCollectionProfiles(final boolean enabled,
                                                        final List<Integer> ids,
                                                        final SecurityContext securityContext) {
        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one id must be provided.")
                    .build();
        }
        try {
            dataCollectionConfPersistenceService.enableDisableProfiles(enabled, ids);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("SNMP collection profiles updated.").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response addSourceToProfile(final Integer profileId,
                                       final String sourceName,
                                       final SecurityContext securityContext) {
        if (sourceName == null || sourceName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body must be a non-empty source name string.")
                    .build();
        }
        try {
            dataCollectionConfPersistenceService.addSourceToProfile(profileId, sourceName.trim());
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response removeSourceFromProfile(final Integer profileId,
                                            final String sourceName,
                                            final SecurityContext securityContext) {
        try {
            dataCollectionConfPersistenceService.removeSourceFromProfile(profileId, sourceName);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Read repeated multipart "profileNames" parts from the upload form.
     * Each part is a string (one profile name); blanks are skipped.
     */
    private List<String> readProfileNamesParts(final List<Attachment> parts) {
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        final List<String> names = new ArrayList<>();
        for (final Attachment part : parts) {
            try {
                final String value = part.getObject(String.class);
                if (value != null && !value.isBlank()) {
                    names.add(value.trim());
                }
            } catch (Exception e) {
                LOG.warn("Failed to read profileNames part: {}", e.getMessage());
            }
        }
        return names;
    }

    @Override
    public Response filterSnmpCollectionSources(String filter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if offset < 0 or limit < 1
        if (Objects.requireNonNullElse(offset, 0) < 0 || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionSource> result = dataCollectionConfPersistenceService.filterSnmpCollectionSources(filter, sortBy, order,
                totalRecords, offset, limit);

        // Check if no data found
        if (result == null
                || result.getRecords().isEmpty()
                || ((result.getTotalRecords()) == 0)) {
            return Response.noContent().build();  // 204 No Content
        }

        List<SnmpCollectionSourceDto> dtoList =
                SnmpCollectionSourceDto.fromEntity(result.getRecords());

        // Build response
        return Response.ok(Map.of("totalRecords", result.getTotalRecords(), "snmpCollectionSourceList", dtoList))
                .build();
    }

    @Override
    public Response filterDataCollectionMibGroupByCollectionSourceId(Integer collectionSourceId, String mibGroupFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(collectionSourceId, 0) <= 0 || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid collectionSourceId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionMibGroup> result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(collectionSourceId, mibGroupFilter,
                sortBy, order, totalRecords, offset, limit);

        // Check if no data found
        if (result == null
                || result.getRecords().isEmpty()
                || ((result.getTotalRecords()) == 0)) {
            return Response.noContent().build();  // 204 No Content
        }

        final var  dtoList =
                SnmpCollectionMibGroupDto.fromEntity(result.getRecords());

        // Build response
        return Response.ok(Map.of("totalRecords", result.getTotalRecords(), "dataCollectionMibGroupList", dtoList))
                .build();
    }

    @Override
    public Response filterDataCollectionResourceTypeByCollectionSourceId(Integer collectionSourceId, String resourceTypeFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(collectionSourceId, 0) <= 0 || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid collectionSourceId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionResourceType> result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(collectionSourceId, resourceTypeFilter,
                sortBy, order, totalRecords, offset, limit);

        // Check if no data found
        if (result == null
                || result.getRecords().isEmpty()
                || ((result.getTotalRecords()) == 0)) {
            return Response.noContent().build();  // 204 No Content
        }

        List<SnmpCollectionResourceTypeDto> dtoList =
                SnmpCollectionResourceTypeDto.fromEntity(result.getRecords());

        // Build response
        return Response.ok(Map.of("totalRecords", result.getTotalRecords(), "dataCollectionResourceTypeList", dtoList))
                .build();
    }

    @Override
    public Response filterDataCollectionSystemDefByCollectionSourceId(Integer collectionSourceId, String systemDefFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(collectionSourceId, 0) <= 0 || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid collectionSourceId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionSystemDef> result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(collectionSourceId, systemDefFilter,
                sortBy, order, totalRecords, offset, limit);

        // Check if no data found
        if (result == null
                || result.getRecords().isEmpty()
                || ((result.getTotalRecords()) == 0)) {
            return Response.noContent().build();  // 204 No Content
        }

        List<SnmpCollectionSystemDefDto> dtoList =
                SnmpCollectionSystemDefDto.fromEntity(result.getRecords());

        // Build response
        return Response.ok(Map.of("totalRecords", result.getTotalRecords(), "dataCollectionSystemDefsList", dtoList))
                .build();
    }

    @Override
    public Response getSnmpDataCollectionSourceById(Integer collectionSourceId, SecurityContext securityContext) {
        try {
            if (collectionSourceId == null || collectionSourceId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Invalid collectionSourceId provided"))
                        .build();
            }
            final var snmpCollectionSource = dataCollectionConfPersistenceService.getSnmpCollectionSourceById(collectionSourceId);
            if (snmpCollectionSource == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "snmpCollectionSource not found for id: " + collectionSourceId))
                        .build();
            }
            SnmpCollectionSourceDto snmpCollectionSourceDto = SnmpCollectionSourceDto.fromEntity(snmpCollectionSource);
            return Response.ok(snmpCollectionSourceDto).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unexpected error occurred: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public Response getSnmpCollectionSourceNamesAndIds(SecurityContext securityContext) throws Exception {
        try {
            final var  map = dataCollectionConfPersistenceService.getSnmpCollectionSourceNamesAndIds();
            return Response.ok(SnmpCollectionSourceNamesAndIdsResponse.fromMap(map)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch SnmpCollection source names: " + e.getMessage()).build();
        }
    }

    @Override
    public Response getDataCollectionResourceTypeNames(SecurityContext securityContext) throws Exception {
        try {
            List<String> list = dataCollectionConfPersistenceService.getAllResourceTypeNames();
            return Response.ok(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch Resource Type names: " + e.getMessage()).build();
        }
    }

    @Override
    public Response getDataCollectionMibGroupNames(SecurityContext securityContext) throws Exception {
        try {
            List<String> list = dataCollectionConfPersistenceService.getAllMibGroupNames();
            return Response.ok(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch MIB Group names: " + e.getMessage()).build();
        }
    }

    @Override
    public Response addMibGroupToSnmpCollectionSources(
            final Integer collectionSourceId,
            final SnmpCollectionMibGroupDto request,
            final SecurityContext securityContext) throws Exception {

        if (collectionSourceId == null || collectionSourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid snmpCollectionSourceId: " + collectionSourceId + ". It must be a positive integer.")
                    .build();
        }

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body (SnmpCollectionMibGroupDto) must not be null.")
                    .build();
        }

        try {
            final var snmpCollectionSource = snmpCollectionSourceDao.get(collectionSourceId);
            if (snmpCollectionSource == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("SnmpCollectionSource with ID " + collectionSourceId + " not found")
                        .build();
            }

            final var id = dataCollectionConfPersistenceService
                    .addMibGroupToSnmpCollectionSources(snmpCollectionSource, request);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();

            return Response.status(Response.Status.CREATED)
                    .entity(id)
                    .build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ex.getMessage() != null ? ex.getMessage()
                            : ("SnmpCollectionSource with ID " + collectionSourceId + " not found"))
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid Mib object payload: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response addResourceTypeToSnmpCollectionSources(
            final Integer collectionSourceId,
            final SnmpCollectionResourceTypeDto request,
            final SecurityContext securityContext) throws Exception {

        if (collectionSourceId == null || collectionSourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid snmpCollectionSourceId: " + collectionSourceId + ". It must be a positive integer.")
                    .build();
        }

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body (SnmpCollectionResourceTypeDto) must not be null.")
                    .build();
        }

        try {
            final SnmpCollectionSource source = snmpCollectionSourceDao.get(collectionSourceId);
            if (source == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("SnmpCollectionSource with ID " + collectionSourceId + " not found")
                        .build();
            }

            final var id = dataCollectionConfPersistenceService
                    .addResourceTypeToSnmpCollectionSources(source, request);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();

            return Response.status(Response.Status.CREATED)
                    .entity(id)
                    .build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ex.getMessage() != null ? ex.getMessage()
                            : ("SnmpCollectionSource with ID " + collectionSourceId + " not found"))
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid ResourceType payload: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response addSystemDefToSnmpCollectionSources(
            final Integer collectionSourceId,
            final SnmpCollectionSystemDefDto request,
            final SecurityContext securityContext) throws Exception {

        if (collectionSourceId == null || collectionSourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid snmpCollectionSourceId: " + collectionSourceId + ". It must be a positive integer.")
                    .build();
        }

        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Request body (SnmpCollectionSystemDefDto) must not be null.")
                    .build();
        }

        try {
            final SnmpCollectionSource source = snmpCollectionSourceDao.get(collectionSourceId);
            if (source == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("SnmpCollectionSource with ID " + collectionSourceId + " not found")
                        .build();
            }

            final var id = dataCollectionConfPersistenceService
                    .addSystemDefToSnmpCollectionSources(source, request);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();

            return Response.status(Response.Status.CREATED)
                    .entity(id)
                    .build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ex.getMessage() != null ? ex.getMessage()
                            : ("SnmpCollectionSource with ID " + collectionSourceId + " not found"))
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid SystemDef payload: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
       }
    }

    @Override
    public Response updateMibGroupInSnmpCollectionSources(Integer collectionSourceId, Integer mibGroupId, SnmpCollectionMibGroupDto request, SecurityContext securityContext) throws Exception {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }
        try {
            dataCollectionConfPersistenceService.updateMibGroup(mibGroupId,collectionSourceId,request);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("MibGroup updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("MibGroup was not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response updateResourceTypeInSnmpCollectionSources(Integer collectionSourceId, Integer resourceTypeId, SnmpCollectionResourceTypeDto request, SecurityContext securityContext) throws Exception {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }
        try {
            dataCollectionConfPersistenceService.updateResourceType(resourceTypeId,collectionSourceId,request);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("ResourceType updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("ResourceType was not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response updateSystemDefInSnmpCollectionSources(Integer collectionSourceId, Integer systemDefId, SnmpCollectionSystemDefDto request, SecurityContext securityContext) throws Exception {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }
        try {
            dataCollectionConfPersistenceService.updateSystemDef(systemDefId,collectionSourceId,request);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("SystemDef updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("SystemDef was not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response deleteSnmpDataCollectionSources(final List<Integer> ids,
                                                    final SecurityContext securityContext) {

        if (ids == null || ids.isEmpty()) {
            return badRequest("Snmp Data Collection IDs to delete must not be empty");
        }

        return handleDelete(
                () -> dataCollectionConfPersistenceService.deleteSnmpDataCollectionSources(ids),
                "Snmp Data Collection deleted successfully"
        );
    }

    @Override
    public Response deleteMibGroupsForSource(final Integer snmpDataCollectionSourceId,
                                             final List<Integer> ids,
                                             final SecurityContext securityContext) {

        if (ids == null || ids.isEmpty()) {
            return badRequest("MIB Group IDs to delete must not be empty");
        }

        return handleDelete(
                () -> dataCollectionConfPersistenceService
                        .deleteSnmpDataCollectionMibGroups(snmpDataCollectionSourceId, ids),
                "Snmp Data Collection Mib Groups deleted successfully"
        );
    }

    @Override
    public Response deleteResourceTypesForSource(final Integer snmpDataCollectionSourceId,
                                                 final List<Integer> ids,
                                                 final SecurityContext securityContext) {

        if (ids == null || ids.isEmpty()) {
            return badRequest("Resource Type IDs to delete must not be empty");
        }

        return handleDelete(
                () -> dataCollectionConfPersistenceService
                        .deleteSnmpDataCollectionResourceTypes(snmpDataCollectionSourceId, ids),
                "Snmp Data Collection Resource Types deleted successfully"
        );
    }

    @Override
    public Response deleteSystemDefsForSource(final Integer snmpDataCollectionSourceId,
                                              final List<Integer> ids,
                                              final SecurityContext securityContext) {

        if (ids == null || ids.isEmpty()) {
            return badRequest("System Def IDs to delete must not be empty");
        }

        return handleDelete(
                () -> dataCollectionConfPersistenceService
                        .deleteSnmpDataCollectionSystemDefs(snmpDataCollectionSourceId, ids),
                "Snmp Data Collection System Def deleted successfully"
        );
    }

    @Override
    public Response enableDisableSnmpDataCollectionSources(final boolean enabled,
                                                           final List<Integer> ids,
                                                           final SecurityContext securityContext) throws Exception {

        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one id must be provided.")
                    .build();
        }

        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("All ids must be non-null positive integers.")
                    .build();
        }

        try {

            dataCollectionConfPersistenceService.enableDisableSnmpDataCollectionSources(enabled, ids);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("SNMP data collection sources updated successfully.").build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response enableDisableSnmpMibGroups(final Integer snmpDataCollectionSourceId,
                                               final boolean enabled,
                                               final List<Integer> ids,
                                               final SecurityContext securityContext) throws Exception {

        if (snmpDataCollectionSourceId == null || snmpDataCollectionSourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("snmpDataCollectionSourceId must be provided and must be a positive integer.")
                    .build();
        }

        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one id must be provided.")
                    .build();
        }

        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("All ids must be non-null positive integers.")
                    .build();
        }

        try {
            dataCollectionConfPersistenceService.enableDisableMibGroups(snmpDataCollectionSourceId, enabled, ids);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();

            return Response.ok().entity("SNMP MIB groups updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Source or one/more ids were not found: " + ex.getMessage())
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response enableDisableSnmpResourceTypes(final Integer snmpDataCollectionSourceId,
                                                   final boolean enabled,
                                                   final List<Integer> ids,
                                                   final SecurityContext securityContext) throws Exception {

        if (snmpDataCollectionSourceId == null || snmpDataCollectionSourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("snmpDataCollectionSourceId must be provided and must be a positive integer.")
                    .build();
        }

        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one id must be provided.")
                    .build();
        }

        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("All ids must be non-null positive integers.")
                    .build();
        }

        try {
            dataCollectionConfPersistenceService.enableDisableResourceTypes(snmpDataCollectionSourceId, enabled, ids);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();

            return Response.ok().entity("SNMP resource types updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Source or one/more ids were not found: " + ex.getMessage())
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response enableDisableSnmpSystemDefs(final Integer snmpDataCollectionSourceId,
                                                final boolean enabled,
                                                final List<Integer> ids,
                                                final SecurityContext securityContext) throws Exception {

        if (snmpDataCollectionSourceId == null || snmpDataCollectionSourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("snmpDataCollectionSourceId must be provided and must be a positive integer.")
                    .build();
        }

        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one id must be provided.")
                    .build();
        }

        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("All ids must be non-null positive integers.")
                    .build();
        }

        try {
            dataCollectionConfPersistenceService.enableDisableSystemDefs(snmpDataCollectionSourceId, enabled, ids);
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();
            return Response.ok().entity("SNMP system defs updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Source or one/more ids were not found: " + ex.getMessage())
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + ex.getMessage())
                    .build();
        }
    }

    private Response handleDelete(final DeleteAction action, final String successMessage) {
        try {
            action.run();
            snmpDataCollectionConfigLoader.scheduleDataCollectionConfigReload();

            return Response.ok()
                    .entity(successMessage)
                    .build();

        } catch (IllegalArgumentException ex) {
            return badRequest(ex.getMessage());

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ex.getMessage())
                    .build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred")
                    .build();
        }
    }

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(message)
                .build();
    }

    @FunctionalInterface
    private interface DeleteAction {
        void run();
    }

    @Override
    public Response downloadSnmpDataCollectionById(Integer snmpDataCollectionId, String format, SecurityContext securityContext) throws Exception {
        if (snmpDataCollectionId == null || snmpDataCollectionId <= 0) {
            return buildXmlError(Response.Status.BAD_REQUEST, "Invalid snmp data collection ID");
        }

        SnmpCollectionSource collectionSource = snmpCollectionSourceDao.get(snmpDataCollectionId);
        if (collectionSource == null) {
            return buildXmlError(Response.Status.NOT_FOUND,
                    "No Snmp Collection Source found for ID: " + snmpDataCollectionId);
        }

        DatacollectionGroup dcg = snmpDataCollectionConfigLoader.buildDataCollectionGroupFromDb(collectionSource);

        // Default to XML if format is null/blank
        String normalizedFormat = (format == null || format.isBlank()) ? "xml" : format.trim();

        if ("json".equalsIgnoreCase(normalizedFormat)) {
            try {
                ObjectMapper mapper = new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT);

                byte[] jsonBytes = mapper.writeValueAsBytes(dcg);

                return Response.ok(jsonBytes, MediaType.APPLICATION_JSON)
                        .header("Content-Disposition",
                                "attachment; filename=\"%s.json\"".formatted(collectionSource.getName()))
                        .build();
            } catch (Exception e) {
                LOG.error("Failed to serialize datacollection-group as JSON for sourceId={}", snmpDataCollectionId, e);
                // If you have buildJsonError, prefer that when json requested.
                return buildXmlError(Response.Status.INTERNAL_SERVER_ERROR, "Failed to generate JSON: " + e.getMessage());
            }
        }

        if ("xml".equalsIgnoreCase(normalizedFormat)) {
            byte[] xmlBytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
                 BufferedWriter writer = new BufferedWriter(osw)) {

                JaxbUtils.marshal(dcg, writer);
                writer.flush();
                xmlBytes = baos.toByteArray();
            } catch (Exception e) {
                LOG.error("Failed to marshal datacollection-group for sourceId={}", snmpDataCollectionId, e);
                return buildXmlError(Response.Status.INTERNAL_SERVER_ERROR, "Failed to generate XML: " + e.getMessage());
            }

            return Response.ok(xmlBytes, MediaType.APPLICATION_XML)
                    .header("Content-Disposition",
                            "attachment; filename=\"%s.xml\"".formatted(collectionSource.getName()))
                    .build();
        }

        return buildXmlError(Response.Status.BAD_REQUEST,
                "Invalid format: " + format + ". Supported values: xml, json");
    }

    private static final String DATACOLLECTION_NAMESPACE = "http://xmlns.opennms.org/xsd/config/datacollection";

    /**
     * Parse a user-uploaded datacollection-group XML file. Tolerates XML that
     * omits the OpenNMS datacollection namespace (matching the client-side
     * validator's permissive behavior) by injecting the expected namespace on
     * elements that have none. XXE-related SAX features are disabled.
     */
    /** Package-private for testing. */
    DatacollectionGroup parseDataCollectionFile(final InputStream inputStream) throws Exception {
        final JAXBContext ctx = JAXBContext.newInstance(DatacollectionGroup.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();

        final XMLReader reader = createHardenedXmlReader();
        final DefaultNamespaceFilter filter = new DefaultNamespaceFilter(DATACOLLECTION_NAMESPACE);
        filter.setParent(reader);

        final SAXSource source = new SAXSource(filter, new InputSource(inputStream));
        return (DatacollectionGroup) unmarshaller.unmarshal(source);
    }

    /** Same SAX pipeline as {@link #parseDataCollectionFile} but for the
     *  top-level {@code <datacollection-config>} element. */
    DatacollectionConfig parseDatacollectionConfig(
            final InputStream inputStream) throws Exception {
        final JAXBContext ctx = JAXBContext.newInstance(DatacollectionConfig.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();

        final XMLReader reader = createHardenedXmlReader();
        final DefaultNamespaceFilter filter = new DefaultNamespaceFilter(DATACOLLECTION_NAMESPACE);
        filter.setParent(reader);

        final SAXSource source = new SAXSource(filter, new InputSource(inputStream));
        return (DatacollectionConfig) unmarshaller.unmarshal(source);
    }

    /**
     * Peek at the first XML element name without unmarshalling. Used to route
     * uploaded files to the right parser ({@code <datacollection-group>} vs
     * {@code <datacollection-config>}). Returns {@code null} if no element
     * could be located.
     */
    private String peekRootElement(final byte[] bytes) {
        try (final ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            final XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
            final XMLStreamReader xr = factory.createXMLStreamReader(in);
            while (xr.hasNext()) {
                if (xr.next() == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                    return xr.getLocalName();
                }
            }
        } catch (Exception ignored) {
            // Fall through to null.
        }
        return null;
    }

    private static XMLReader createHardenedXmlReader() throws SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setXIncludeAware(false);
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return spf.newSAXParser().getXMLReader();
    }

    /**
     * Injects a default namespace only on elements that have no namespace.
     * Elements that already declare a namespace pass through unchanged, so
     * an XML file that declares the wrong namespace will still fail JAXB
     * validation.
     */
    private static final class DefaultNamespaceFilter extends XMLFilterImpl {
        private final String defaultNamespace;

        DefaultNamespaceFilter(final String defaultNamespace) {
            this.defaultNamespace = defaultNamespace;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (uri == null || uri.isEmpty()) {
                uri = defaultNamespace;
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (uri == null || uri.isEmpty()) {
                uri = defaultNamespace;
            }
            super.endElement(uri, localName, qName);
        }
    }

    private String getUsername(final SecurityContext context) {
        return (context != null && context.getUserPrincipal() != null) ? context.getUserPrincipal().getName() : "unknown";
    }

    private String stripPathAndExtension(final String filename) {
        if (filename == null) return null;

        // Strip folder paths (handle both / and \ separators)
        String basename = filename;
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSlash != -1) {
            basename = filename.substring(lastSlash + 1);
        }

        // Strip extension
        int dotIndex = basename.lastIndexOf('.');
        String result = (dotIndex == -1) ? basename : basename.substring(0, dotIndex);

        // Trim trailing/leading whitespace from result
        return result.trim();
    }

    private Map<String, Object> buildSuccessResponse(String filename, DatacollectionGroup datCollectionConfig) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file", filename);
        return entry;
    }

    private Map<String, Object> buildErrorResponse(String filename, Exception ex) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file", filename);
        entry.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return entry;
    }
    private Response buildXmlError(Response.Status status, String message) {
        return Response.status(status)
                .entity("<error>%s</error>".formatted(message))
                .type(MediaType.APPLICATION_XML)
                .build();
    }

}
