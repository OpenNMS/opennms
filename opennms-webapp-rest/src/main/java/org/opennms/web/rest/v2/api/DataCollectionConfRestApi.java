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
package org.opennms.web.rest.v2.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.model.SnmpCollectionMibGroupDto;
import org.opennms.netmgt.model.SnmpCollectionProfileDto;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.PATCH;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("datacollectionconf")
@Tag(name = "DataCollectionConf", description = "DataCollectionConf API")
public interface DataCollectionConfRestApi {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Operation(
            summary = "Upload datacollectionconf files",
            description = "Upload one or more data collection config files. Each `upload` part must "
                    + "be an XML file whose root element is either `<datacollection-group>` (a source "
                    + "definition) or `<datacollection-config>` (a profile-driver file with "
                    + "`<snmp-collection>` entries). At most one `<datacollection-config>` is allowed "
                    + "per request.\n\n"
                    + "How `profileNames` is applied depends on the batch composition:\n"
                    + "- **Pure-new batch** (every source is new): `profileNames` is required; new "
                    + "sources are attached to those profiles.\n"
                    + "- **Pure-update batch** (every source already exists in the DB): `profileNames` "
                    + "is optional. If non-empty, it is applied additively to every source — treat as "
                    + "an explicit \"also associate these updates with these profiles\" intent.\n"
                    + "- **Mixed batch** (≥1 new and ≥1 update): `profileNames` is required (for the "
                    + "new sources) and is applied **only to the new sources**. Updates keep their "
                    + "existing profile memberships untouched. To change an existing source's "
                    + "memberships in this case, use the dedicated `/profiles/{profileId}/sources` "
                    + "endpoints.\n"
                    + "- **`<datacollection-config>` present**: `profileNames` is ignored; the "
                    + "`<include-collection>` entries in the config drive attachment.",
            operationId = "uploadSnmpDataCollectionConfFiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload processed (per-file results in success/errors arrays)"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid XML, missing required profileNames for source-only uploads, or other request error")
    })
    Response uploadSnmpDataCollectionConfFiles(@Multipart("upload") List<Attachment> attachments,
                                  @Multipart(value = "profileNames", required = false) List<Attachment> profileNames,
                                  @Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List all SNMP collection profiles",
            description = "Returns id, name, enabled, sourceNames, and other config for every profile.",
            operationId = "listSnmpCollectionProfiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles returned", content = @Content)
    })
    Response listSnmpCollectionProfiles(@Context SecurityContext securityContext);

    @GET
    @Path("/profiles/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a single SNMP collection profile",
            operationId = "getSnmpCollectionProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    Response getSnmpCollectionProfile(@PathParam("profileId") Integer profileId,
                                      @Context SecurityContext securityContext);

    @POST
    @Path("/profiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a new SNMP collection profile",
            operationId = "createSnmpCollectionProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created; body contains the new id"),
            @ApiResponse(responseCode = "400", description = "Invalid profile body or name already in use")
    })
    Response createSnmpCollectionProfile(SnmpCollectionProfileDto profile,
                                         @Context SecurityContext securityContext);

    @PUT
    @Path("/profiles/{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update an SNMP collection profile",
            operationId = "updateSnmpCollectionProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid profile body or name conflict"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    Response updateSnmpCollectionProfile(@PathParam("profileId") Integer profileId,
                                         SnmpCollectionProfileDto profile,
                                         @Context SecurityContext securityContext);

    @DELETE
    @Path("/profiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Bulk-delete SNMP collection profiles",
            operationId = "deleteSnmpCollectionProfiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deletion complete"),
            @ApiResponse(responseCode = "400", description = "Empty id list")
    })
    Response deleteSnmpCollectionProfiles(List<Integer> ids,
                                          @Context SecurityContext securityContext);

    @PUT
    @Path("/profiles/enable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Bulk enable/disable SNMP collection profiles",
            operationId = "enableDisableSnmpCollectionProfiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles updated"),
            @ApiResponse(responseCode = "400", description = "Empty id list")
    })
    Response enableDisableSnmpCollectionProfiles(@QueryParam("enabled") boolean enabled,
                                                 List<Integer> ids,
                                                 @Context SecurityContext securityContext);

    @POST
    @Path("/profiles/{profileId}/sources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a source to a profile",
            description = "Appends the given source name to the profile's source_names (idempotent).",
            operationId = "addSourceToProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source added"),
            @ApiResponse(responseCode = "400", description = "Invalid profileId or empty sourceName"),
            @ApiResponse(responseCode = "404", description = "Profile or source not found")
    })
    Response addSourceToProfile(@PathParam("profileId") Integer profileId,
                                String sourceName,
                                @Context SecurityContext securityContext);

    @DELETE
    @Path("/profiles/{profileId}/sources/{sourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Remove a source from a profile",
            description = "Removes the given source name from the profile's source_names (no-op if absent).",
            operationId = "removeSourceFromProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source removed (or already absent)"),
            @ApiResponse(responseCode = "400", description = "Invalid profileId"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    Response removeSourceFromProfile(@PathParam("profileId") Integer profileId,
                                     @PathParam("sourceName") String sourceName,
                                     @Context SecurityContext securityContext);

    @GET
    @Path("filter/collectsources")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Filter SnmpCollectionSource Records",
            description = "Fetch SnmpCollectionSource records based on provided filters such as name, vendor, description.",
            operationId = "filterSnmpCollectionSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SnmpCollectionSource records retrieved successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No matching SnmpCollectionSource records found for the given criteria",
                    content = @Content)
    })
    Response filterSnmpCollectionSources(
            @QueryParam("filter") String filter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("totalRecords") Integer totalRecords,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );

    @GET
    @Path("/filter/{collectionSourceId}/mibgroups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get DataCollectionMibGroup by Collection Source ID with filtering and sorting",
            description = """
        Retrieves DataCollectionMibGroup records for the given Collection source ID with optional filtering, sorting, and pagination.
        - `eventFilter`: case-insensitive match on Name, IfType.
        - `eventSortBy`: sort field `name`, `ifType` defaults to `createdTime` if invalid.
        - `eventOrder`: `asc` or `desc` (default: `desc`).
        - `offset` and `limit`: for pagination.""",
            operationId = "filterDataCollectionMibGroupByCollectionSourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DataCollectionMibGroup records retrieved successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No matching DataCollectionMibGroup record found for the given criteria",
                    content = @Content)
    })
    Response filterDataCollectionMibGroupByCollectionSourceId(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @QueryParam("mibGroupFilter") String mibGroupFilter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("totalRecords") Integer totalRecords,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );

    @GET
    @Path("/filter/{collectionSourceId}/resourcetypes")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get DataCollectionResourceType by Collection Source ID with filtering and sorting",
            description = """
    Retrieves DataCollectionResourceType records for the given Collection source ID with optional filtering, sorting, and pagination.
    - `resourceTypeFilter`: case-insensitive match on Name, Label.
    - `sortBy`: sort field `name`, `label` (defaults to `createdTime` if invalid).
    - `order`: `asc` or `desc` (default: `desc`).
    - `offset` and `limit`: for pagination.
    """,
            operationId = "filterDataCollectionResourceTypeByCollectionSourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "DataCollectionResourceType records retrieved successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request – invalid or missing input parameters",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No matching DataCollectionResourceType record found for the given criteria",
                    content = @Content
            )
    })
    Response filterDataCollectionResourceTypeByCollectionSourceId(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @QueryParam("resourceTypeFilter") String resourceTypeFilter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("totalRecords") Integer totalRecords,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/filter/{collectionSourceId}/systemdefs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get DataCollectionSystemDef by Collection Source ID with filtering and sorting",
            description = """
    Retrieves DataCollectionSystemDef records for the given Collection source ID with optional filtering, sorting, and pagination.
    - `systemDefFilter`: case-insensitive match on Name
    - `sortBy`: sort field `name` (defaults to `createdTime` if invalid).
    - `order`: `asc` or `desc` (default: `desc`).
    - `offset` and `limit`: for pagination.
    """,
            operationId = "filterDataCollectionSystemDefByCollectionSourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "DataCollectionSystemDef records retrieved successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request – invalid or missing input parameters",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No matching DataCollectionSystemDef record found for the given criteria",
                    content = @Content
            )
    })
    Response filterDataCollectionSystemDefByCollectionSourceId(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @QueryParam("systemDefsFilter") String systemDefFilter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("totalRecords") Integer totalRecords,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/collectsources/{collectionSourceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get SnmpCollectionSource by ID",
            description = "Retrieve an SnmpCollectionSource by its unique identifier.",
            operationId = "getSnmpDataCollectionSourceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SnmpCollectionSource retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSource not found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")

    })
    Response getSnmpDataCollectionSourceById(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/collectsources/names-and-ids")
    @Produces("application/json")
    @Operation(
            summary = "Get SnmpCollection Source Names",
            description = "Retrieve the names and Ids of all SnmpCollection sources stored in the database.",
            operationId = "getSnmpCollectionSourceNamesAndIds"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved source names"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response getSnmpCollectionSourceNamesAndIds(@Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/resourcetypes/names")
    @Produces("application/json")
    @Operation(
            summary = "Get DataCollection Resource Type Names",
            description = "Retrieve the names of all DataCollection Resource Types stored in the database.",
            operationId = "getDataCollectionResourceTypeNames"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved resource type names"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response getDataCollectionResourceTypeNames(@Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/mibgroups/names")
    @Produces("application/json")
    @Operation(
            summary = "Get DataCollection MIB Group Names",
            description = "Retrieve the names of all DataCollection MIB Groups stored in the database.",
            operationId = "getDataCollectionMibGroupNames"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved MIB group names"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response getDataCollectionMibGroupNames(@Context SecurityContext securityContext) throws Exception;
    @POST
    @Path("/collectsources/{collectionSourceId}/mibgroups")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new Mib Group to an SnmpCollectionSources",
            description = "Creates and adds a new Mib Group under the given SnmpCollectionSources by its ID.",
            operationId = "addMibGroupToSnmpCollectionSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "MibGroup created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources not found")})
    Response addMibGroupToSnmpCollectionSources(@PathParam("collectionSourceId") final Integer collectionSourceId,
             final  SnmpCollectionMibGroupDto request, @Context SecurityContext securityContext) throws Exception;


    @POST
    @Path("/collectsources/{collectionSourceId}/resourcetypes")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new Resource Type to an SnmpCollectionSources",
            description = "Creates and adds a new Resource Type under the given SnmpCollectionSources by its ID.",
            operationId = "addResourceTypeToSnmpCollectionSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "ResourceType created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources not found")})
    Response addResourceTypeToSnmpCollectionSources(
            @PathParam("collectionSourceId") final Integer collectionSourceId,
            final  SnmpCollectionResourceTypeDto request,
            @Context SecurityContext securityContext) throws Exception;

    @POST
    @Path("/collectsources/{collectionSourceId}/systemdefs")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new System Definition to an SnmpCollectionSources",
            description = "Creates and adds a new System Definition under the given SnmpCollectionSources by its ID.",
            operationId = "addSystemDefToSnmpCollectionSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SystemDef created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources not found")})
    Response addSystemDefToSnmpCollectionSources(
            @PathParam("collectionSourceId") final Integer collectionSourceId,
            final SnmpCollectionSystemDefDto request,
            @Context SecurityContext securityContext) throws Exception;

    @PUT
    @Path("/collectsources/{collectionSourceId}/mibgroups/{mibGroupId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update a Mib Group in an SnmpCollectionSources",
            description = "Updates an existing Mib Group under the given SnmpCollectionSources by its ID.",
            operationId = "updateMibGroupInSnmpCollectionSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MibGroup updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources or MibGroup not found")
    })
    Response updateMibGroupInSnmpCollectionSources(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @PathParam("mibGroupId") Integer mibGroupId,
            SnmpCollectionMibGroupDto request,
            @Context SecurityContext securityContext
    ) throws Exception;

    @PUT
    @Path("/collectsources/{collectionSourceId}/resourcetypes/{resourceTypeId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update a Resource Type in an SnmpCollectionSources",
            description = "Updates an existing Resource Type under the given SnmpCollectionSources by its ID.",
            operationId = "updateResourceTypeInSnmpCollectionSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ResourceType updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources or ResourceType not found")
    })
    Response updateResourceTypeInSnmpCollectionSources(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @PathParam("resourceTypeId") Integer resourceTypeId,
            SnmpCollectionResourceTypeDto request,
            @Context SecurityContext securityContext
    ) throws Exception;

    @PUT
    @Path("/collectsources/{collectionSourceId}/systemdefs/{systemDefId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update a System Definition in an SnmpCollectionSources",
            description = "Updates an existing System Definition under the given SnmpCollectionSources by its ID.",
            operationId = "updateSystemDefInSnmpCollectionSources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SystemDef updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)"),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources or SystemDef not found")
    })
    Response updateSystemDefInSnmpCollectionSources(
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @PathParam("systemDefId") Integer systemDefId,
            SnmpCollectionSystemDefDto request,
            @Context SecurityContext securityContext
    ) throws Exception;

    @DELETE
    @Path("/collectsources")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete SNMP Data Collection Sources",
            description = "Delete one or more SNMP data collection sources by their IDs.",
            operationId = "deleteSnmpDataCollectionSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sources deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid IDs)"),
            @ApiResponse(responseCode = "404", description = "One or more sources not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response deleteSnmpDataCollectionSources(
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources/{snmpDataCollectionSourceId}/mib-groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete MIB Groups for a Source",
            description = "Delete one or more MIB groups belonging to the specified SNMP data collection source.",
            operationId = "deleteMibGroupsForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MIB groups deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid MIB group IDs or invalid source id)"),
            @ApiResponse(responseCode = "404", description = "Source and/or MIB groups not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response deleteMibGroupsForSource(
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources/{snmpDataCollectionSourceId}/resource-types")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete Resource Types for a Source",
            description = "Delete one or more resource types belonging to the specified SNMP data collection source.",
            operationId = "deleteResourceTypesForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource types deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid resource type IDs or invalid source id)"),
            @ApiResponse(responseCode = "404", description = "Source and/or resource types not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response deleteResourceTypesForSource(
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources/{snmpDataCollectionSourceId}/system-defs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete System Definitions for a Source",
            description = "Delete one or more system definitions belonging to the specified SNMP data collection source.",
            operationId = "deleteSystemDefsForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System definitions deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid system definition IDs or invalid source id)"),
            @ApiResponse(responseCode = "404", description = "Source and/or system definitions not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    Response deleteSystemDefsForSource(
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @PATCH
    @Path("/collectsources/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP Data Collection Sources",
            description = "Enable or disable one or more SNMP data collection sources",
            operationId = "enableDisableSnmpDataCollectionSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    Response enableDisableSnmpDataCollectionSources(
            @PathParam("enabled") boolean enabled,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;

    @PATCH
    @Path("/collectsources/{snmpDataCollectionSourceId}/mib-groups/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP MIB Groups",
            description = "Enable or disable one or more SNMP MIB groups",
            operationId = "enableDisableSnmpMibGroups"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    Response enableDisableSnmpMibGroups(
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @PathParam("enabled") boolean enabled,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;

    @PATCH
    @Path("/collectsources/{snmpDataCollectionSourceId}/resource-types/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP Resource Types",
            description = "Enable or disable one or more SNMP resource types",
            operationId = "enableDisableSnmpResourceTypes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    Response enableDisableSnmpResourceTypes(
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @PathParam("enabled") boolean enabled,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;


    @PATCH
    @Path("/collectsources/{snmpDataCollectionSourceId}/system-defs/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP System Definitions",
            description = "Enable or disable one or more SNMP system definitions",
            operationId = "enableDisableSnmpSystemDefs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    Response enableDisableSnmpSystemDefs(
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @PathParam("enabled") boolean enabled,
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;


    @GET
    @Path("/collectsources/{collectionSourceId}/download")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Download Snmp Data Collection XML for a Source",
            description = """
            Downloads all Resource types, Mib groups and System defs associated with the specified snmpDataCollection ID.
        """,
            operationId = "downloadSnmpDataCollectionById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Snmp Data Collection XML downloaded successfully",
                    content = @Content(mediaType = "application/xml")),
            @ApiResponse(responseCode = "400", description = "Invalid or missing source ID"),
            @ApiResponse(responseCode = "404", description = "No snmpDataCollection found for the specified snmpDataCollection ID")
    })
    Response downloadSnmpDataCollectionById(
            @PathParam("collectionSourceId") Integer snmpDataCollectionId, @QueryParam("format") String format,
            @Context SecurityContext securityContext
    ) throws Exception;

    @GET
    @Path("/config/download")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Download the top-level datacollection-config",
            description = """
            Returns the top-level <datacollection-config> assembled from the
            current profile rows: one <snmp-collection> per enabled profile,
            with rrd metadata, storage flag, max-vars-per-pdu, and one
            <include-collection dataCollectionGroup="..."> per source name in
            the profile's source_names. Pair with /collectsources/{id}/download
            to obtain the matching <datacollection-group> files. Re-uploadable
            as a multipart batch via /upload.
            """,
            operationId = "downloadDatacollectionConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "datacollection-config downloaded",
                    content = @Content(mediaType = "application/xml")),
            @ApiResponse(responseCode = "400", description = "Invalid format"),
            @ApiResponse(responseCode = "500", description = "Marshal error")
    })
    Response downloadDatacollectionConfig(
            @QueryParam("format") String format,
            @Context SecurityContext securityContext
    ) throws Exception;

}
