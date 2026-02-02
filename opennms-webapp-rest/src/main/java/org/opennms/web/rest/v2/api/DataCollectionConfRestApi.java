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

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
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
            description = "Upload one or more  data collection config files.",
            operationId = "uploadSnmpDataCollectionConfFiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload successful"),
            @ApiResponse(responseCode = "400", description = "Invalid xml or request")
    })
    Response uploadSnmpDataCollectionConfFiles(@Multipart("upload") List<Attachment> attachments,
                                  @Context SecurityContext securityContext) throws Exception;

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
    @Path("/filter/{dataCollectionGroupId}/mibgroups")
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
            @PathParam("dataCollectionGroupId") Integer dataCollectionGroupId,
            @QueryParam("mibGroupFilter") String mibGroupFilter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("totalRecords") Integer totalRecords,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );

    @GET
    @Path("/filter/{dataCollectionGroupId}/resourcetypes")
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
            @PathParam("dataCollectionGroupId") Integer dataCollectionGroupId,
            @QueryParam("resourceTypeFilter") String resourceTypeFilter,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("totalRecords") Integer totalRecords,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/filter/{dataCollectionGroupId}/systemdefs")
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
            @PathParam("dataCollectionGroupId") Integer dataCollectionGroupId,
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
}
