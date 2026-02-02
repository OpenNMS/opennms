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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.*;

import org.opennms.web.rest.v2.api.DataCollectionConfRestApi;
import org.opennms.web.rest.v2.model.SnmpCollectionSourceNamesAndIdsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

    @Override
    public Response uploadSnmpDataCollectionConfFiles(List<Attachment> attachments, SecurityContext securityContext) throws Exception {

        final String username = getUsername(securityContext);
        final Date now = new Date();

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

        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();
        for (final String fileName : orderedFiles) {
            final Attachment attachment = fileMap.get(fileName);
            if (attachment == null) {
                continue;
            }

            DatacollectionGroup dataCollection;
            try (InputStream stream = attachment.getObject(InputStream.class)) {
                dataCollection = parseDataCollectionFile(new ByteArrayInputStream(stream.readAllBytes()));
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
                continue;
            }

            try {
                dataCollectionConfPersistenceService.addDataCollectionConfig(fileName,username,dataCollection,now);
                successList.add(buildSuccessResponse(fileName, dataCollection));
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
            }
        }


        return Response.ok(Map.of("success", successList, "errors", errorList)).build();
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
    public Response filterDataCollectionMibGroupByCollectionSourceId(Integer dataCollectionGroupId, String mibGroupFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(dataCollectionGroupId, 0) <= 0 || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid dataCollectionGroupId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionMibGroup> result = dataCollectionConfPersistenceService.filterMibGroupByDataCollectionGroupId(dataCollectionGroupId, mibGroupFilter,
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
    public Response filterDataCollectionResourceTypeByCollectionSourceId(Integer dataCollectionGroupId, String resourceTypeFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(dataCollectionGroupId, 0) <= 0 || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid dataCollectionGroupId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionResourceType> result = dataCollectionConfPersistenceService.filterResourceTypeByDataCollectionGroupId(dataCollectionGroupId, resourceTypeFilter,
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
    public Response filterDataCollectionSystemDefByCollectionSourceId(Integer dataCollectionGroupId, String systemDefFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit, SecurityContext securityContext) {
        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(dataCollectionGroupId, 0) <= 0 || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid dataCollectionGroupId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        PageResponse<SnmpCollectionSystemDef> result = dataCollectionConfPersistenceService.filterSystemDefByDataCollectionGroupId(dataCollectionGroupId, systemDefFilter,
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

    private DatacollectionGroup parseDataCollectionFile(final InputStream inputStream) throws Exception {
        return JaxbUtils.unmarshal(DatacollectionGroup.class, inputStream);
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
}
