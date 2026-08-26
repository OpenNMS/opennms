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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.model.SnmpCollectionMibGroupDto;
import org.opennms.netmgt.model.SnmpCollectionProfileDto;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.netmgt.model.SnmpCollectionSourceDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;
import org.opennms.web.rest.v2.model.DataCollectionConfErrorResponse;
import org.opennms.web.rest.v2.model.DataCollectionConfUploadResponse;
import org.opennms.web.rest.v2.model.DataCollectionMibGroupPageResponse;
import org.opennms.web.rest.v2.model.DataCollectionResourceTypePageResponse;
import org.opennms.web.rest.v2.model.DataCollectionSystemDefPageResponse;
import org.opennms.web.rest.v2.model.SnmpCollectionCreateSourceDto;
import org.opennms.web.rest.v2.model.SnmpCollectionSourceNamesAndIdsResponse;
import org.opennms.web.rest.v2.model.SnmpCollectionSourcePageResponse;

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
@Tag(name = "DataCollectionConf", description = """
        DataCollectionConf API.

        SNMP data collection lives in the database rather than in `datacollection-config.xml` and the
        `datacollection/*.xml` group files. A **source** stands for one `<datacollection-group>` and owns
        its MIB groups, resource types and system definitions. A **profile** stands for one
        `<snmp-collection>`: it carries the rrd step, RRAs, storage flag and `max-vars-per-pdu`, and its
        `sourceNames` list is the set of `<include-collection>` entries. Only enabled profiles reach
        collectd, so a source that no enabled profile references is never scheduled.

        Every write schedules a reload of the in-memory data collection config, so changes take effect
        without a restart.

        Two wire-format details apply across the family. `createdTime` and `lastModified` serialize as
        epoch milliseconds, not as the date-time strings the generated schema shows. Ids that do not
        exist are usually ignored rather than reported: the bulk delete and the enable/disable endpoints
        answer 200 for an unknown id.

        `GET /datacollectionconf/config/download` plus one
        `GET /datacollectionconf/collectsources/{id}/download` per source reproduce the on-disk file set,
        and that set is accepted back as a single multipart batch by `POST /datacollectionconf/upload`.""")
public interface DataCollectionConfRestApi {

    String PROFILE_JSON_EXAMPLE = """
            {
              "name": "default",
              "rrdStep": 300,
              "rrdRras": [
                "RRA:AVERAGE:0.5:1:2016",
                "RRA:AVERAGE:0.5:12:1488",
                "RRA:AVERAGE:0.5:288:366",
                "RRA:MAX:0.5:288:366",
                "RRA:MIN:0.5:288:366"
              ],
              "storageFlag": "select",
              "sourceNames": [ "MIB2", "Cisco" ],
              "maxVarsPerPdu": 10,
              "enabled": true
            }""";

    String PROFILE_JSON_RESPONSE_EXAMPLE = """
            {
              "id": 1,
              "name": "default",
              "rrdStep": 300,
              "rrdRras": [
                "RRA:AVERAGE:0.5:1:2016",
                "RRA:AVERAGE:0.5:12:1488",
                "RRA:AVERAGE:0.5:288:366",
                "RRA:MAX:0.5:288:366",
                "RRA:MIN:0.5:288:366"
              ],
              "storageFlag": "select",
              "sourceNames": [ "MIB2", "Cisco" ],
              "maxVarsPerPdu": 10,
              "enabled": true,
              "createdTime": 1787727852928,
              "lastModified": 1787727871283
            }""";

    String MIB_GROUP_JSON_EXAMPLE = """
            {
              "name": "cisco-router",
              "ifType": "ignore",
              "mibObjects": "[{\\"alias\\":\\"cpu5min\\",\\"oid\\":\\".1.3.6.1.4.1.9.2.1.58\\",\\"instance\\":\\"0\\",\\"type\\":\\"gauge\\"}]",
              "enabled": true
            }""";

    String RESOURCE_TYPE_JSON_EXAMPLE = """
            {
              "name": "cpqDaPhyDrv",
              "label": "Compaq Physical Drive",
              "resourceLabel": "${index}",
              "persistenceSelectorStrategy": "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy",
              "storageStrategy": "org.opennms.netmgt.dao.support.IndexStorageStrategy",
              "enabled": true
            }""";

    String SYSTEM_DEF_JSON_EXAMPLE = """
            {
              "name": "Cisco Routers",
              "sysoidMask": ".1.3.6.1.4.1.9.1.",
              "mibGroupNames": "[\\"cisco-router\\"]",
              "enabled": true
            }""";

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Operation(
            summary = "Upload datacollectionconf files",
            description = """
        Upload one or more data collection config files. Each `upload` part must be an XML file whose root
        element is either `<datacollection-group>` (a source definition) or `<datacollection-config>`
        (a profile-driver file with `<snmp-collection>` entries). At most one `<datacollection-config>`
        is allowed per request.

        Parts are keyed by filename with the directory and extension stripped, and the first part wins
        when two filenames reduce to the same basename, so uploading the same file twice is a no-op
        rather than a duplicate error.

        How `profileNames` is applied depends on the batch composition:
        - **Pure-new batch** (every source is new): `profileNames` is required; new sources are attached
          to those profiles.
        - **Pure-update batch** (every source already exists in the DB): `profileNames` is optional.
          If non-empty, it is applied additively to every source, taken as an explicit "also associate
          these updates with these profiles" intent.
        - **Mixed batch** (at least one new and at least one update): `profileNames` is required (for the
          new sources) and is applied **only to the new sources**. Updates keep their existing profile
          memberships untouched. To change an existing source's memberships in this case, use the
          dedicated `/profiles/{profileId}/sources` endpoints.
        - **`<datacollection-config>` present**: `profileNames` is ignored; the `<include-collection>`
          entries in the config drive attachment, and profiles named by `<snmp-collection>` are created
          or updated.

        A part is capped at 16 MiB and a batch at 64 MiB. Files that fail to parse are reported in
        `errors` while the rest are still stored, so a 200 does not by itself mean every file was
        accepted.""",
            operationId = "uploadSnmpDataCollectionConfFiles"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with one `upload` part per XML file, plus zero or more "
                    + "`profileNames` parts each holding a single profile name.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload processed. Per-file outcomes are split between `success` and `errors`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfUploadResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "success": [
                        { "file": "Cisco", "source": "Cisco" },
                        { "file": "datacollection-config", "profile": "default" }
                      ],
                      "errors": [
                        { "file": "Broken", "error": "UnmarshalException: null" }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400",
                    description = "A new source was uploaded without any `profileNames` part.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = """
                    {"error":"At least one profileNames value is required when uploading new source files."}""")))
    })
    Response uploadSnmpDataCollectionConfFiles(@Multipart("upload") List<Attachment> attachments,
                                  @Multipart(value = "profileNames", required = false) List<Attachment> profileNames,
                                  @Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List all SNMP collection profiles",
            description = """
        Returns id, name, enabled, sourceNames, and the rrd metadata for every profile.
        `createdTime` and `lastModified` come back as epoch milliseconds; `lastModified` is null on a
        profile that has never been updated.""",
            operationId = "listSnmpCollectionProfiles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = SnmpCollectionProfileDto.class)),
                            examples = @ExampleObject(value = "[" + PROFILE_JSON_RESPONSE_EXAMPLE + "]"))),
            @ApiResponse(responseCode = "500", description = "The profiles could not be read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response listSnmpCollectionProfiles(@Context SecurityContext securityContext);

    @GET
    @Path("/profiles/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a single SNMP collection profile",
            description = """
        Fetch one profile by id. `createdTime` and `lastModified` come back as epoch milliseconds.
        The 400 and 404 bodies are bare strings served under `application/json`.""",
            operationId = "getSnmpCollectionProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpCollectionProfileDto.class),
                            examples = @ExampleObject(value = PROFILE_JSON_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "`profileId` was not a positive integer.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "profileId must be a positive integer"))),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Profile not found for id: 999999"))),
            @ApiResponse(responseCode = "500", description = "The profile could not be read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response getSnmpCollectionProfile(
            @Parameter(description = "Profile id. Must be a positive integer.", example = "1", required = true)
            @PathParam("profileId") Integer profileId,
            @Context SecurityContext securityContext);

    @POST
    @Path("/profiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a new SNMP collection profile",
            description = """
        Create a profile. `name` must be non-empty and unique, and at least one RRA is required.
        `id`, `createdTime` and `lastModified` in the body are ignored. The response body is the new
        profile id as a bare integer.""",
            operationId = "createSnmpCollectionProfile"
    )
    @RequestBody(
            required = true,
            description = "Profile to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionProfileDto.class),
                    examples = @ExampleObject(value = PROFILE_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profile created; body is the new id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "integer", format = "int32"),
                            examples = @ExampleObject(value = "3"))),
            @ApiResponse(responseCode = "400", description = "Missing body, empty name, no RRAs, or a name already in use",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "A profile with name 'default' already exists"))),
            @ApiResponse(responseCode = "500", description = "The profile could not be created.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response createSnmpCollectionProfile(SnmpCollectionProfileDto profile,
                                         @Context SecurityContext securityContext);

    @PUT
    @Path("/profiles/{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update an SNMP collection profile",
            description = """
        Replace a profile's fields. The body is a whole profile, not a patch. A successful response
        carries no body.""",
            operationId = "updateSnmpCollectionProfile"
    )
    @RequestBody(
            required = true,
            description = "Replacement profile.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionProfileDto.class),
                    examples = @ExampleObject(value = PROFILE_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body, invalid field, or a name conflict",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Profile body must not be null"))),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Profile not found for id: 999999"))),
            @ApiResponse(responseCode = "500", description = "The update failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response updateSnmpCollectionProfile(
            @Parameter(description = "Profile id. Must be a positive integer.", example = "3", required = true)
            @PathParam("profileId") Integer profileId,
                                         SnmpCollectionProfileDto profile,
                                         @Context SecurityContext securityContext);

    @DELETE
    @Path("/profiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Bulk-delete SNMP collection profiles",
            description = """
        Delete the profiles named by the id list in the request body. Ids that do not exist are ignored
        rather than reported. Sources the deleted profiles referenced are not themselves deleted, so a
        source can be left unreferenced and therefore unscheduled.""",
            operationId = "deleteSnmpCollectionProfiles"
    )
    @RequestBody(
            required = true,
            description = "Profile ids to delete.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(type = "integer", format = "int32")),
                    examples = @ExampleObject(value = "[3, 4]"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deletion complete",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP collection profiles deleted."))),
            @ApiResponse(responseCode = "400", description = "Missing or empty id list",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "At least one id must be provided."))),
            @ApiResponse(responseCode = "500", description = "The deletion failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response deleteSnmpCollectionProfiles(List<Integer> ids,
                                          @Context SecurityContext securityContext);

    @PUT
    @Path("/profiles/enable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Bulk enable/disable SNMP collection profiles",
            description = """
        Set the enabled flag on the profiles named by the id list in the request body. A disabled profile
        is left out of the config collectd consumes and out of
        `GET /datacollectionconf/config/download`. Ids that do not exist are ignored rather than
        reported.""",
            operationId = "enableDisableSnmpCollectionProfiles"
    )
    @RequestBody(
            required = true,
            description = "Profile ids to update.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(type = "integer", format = "int32")),
                    examples = @ExampleObject(value = "[3, 4]"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP collection profiles updated."))),
            @ApiResponse(responseCode = "400", description = "Missing or empty id list",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "At least one id must be provided."))),
            @ApiResponse(responseCode = "500", description = "The update failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response enableDisableSnmpCollectionProfiles(
            @Parameter(description = "Target state for every listed profile.", example = "false", required = true)
            @QueryParam("enabled") boolean enabled,
                                                 List<Integer> ids,
                                                 @Context SecurityContext securityContext);

    @POST
    @Path("/profiles/{profileId}/sources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a source to a profile",
            description = """
        Append the given source name to the profile's `sourceNames`. The request body is the bare source
        name, not a JSON object. The call is idempotent: a name already present is left alone and still
        answers 200. The source has to exist; an unknown name is a 400, not a 404. A successful response
        carries no body.""",
            operationId = "addSourceToProfile"
    )
    @RequestBody(
            required = true,
            description = "Source name, as a bare string.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Cisco"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source added, or already present. No response body."),
            @ApiResponse(responseCode = "400", description = "Empty body, `profileId` not a positive integer, or an unknown source name",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown source name: Ciscoo"))),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Profile not found for id: 999999"))),
            @ApiResponse(responseCode = "500", description = "The update failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response addSourceToProfile(
            @Parameter(description = "Profile id. Must be a positive integer.", example = "1", required = true)
            @PathParam("profileId") Integer profileId,
                                String sourceName,
                                @Context SecurityContext securityContext);

    @DELETE
    @Path("/profiles/{profileId}/sources/{sourceName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Remove a source from a profile",
            description = """
        Remove the given source name from the profile's `sourceNames`. A name that is not present is a
        no-op and still answers 200. The source row itself is not deleted. A successful response carries
        no body.""",
            operationId = "removeSourceFromProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Source removed, or already absent. No response body."),
            @ApiResponse(responseCode = "400", description = "`profileId` was not a positive integer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "profileId must be a positive integer"))),
            @ApiResponse(responseCode = "404", description = "Profile not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Profile not found for id: 999999"))),
            @ApiResponse(responseCode = "500", description = "The update failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response removeSourceFromProfile(
            @Parameter(description = "Profile id. Must be a positive integer.", example = "1", required = true)
            @PathParam("profileId") Integer profileId,
            @Parameter(description = "Source name to remove.", example = "Cisco", required = true)
            @PathParam("sourceName") String sourceName,
                                     @Context SecurityContext securityContext);

    @GET
    @Path("filter/collectsources")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Filter SnmpCollectionSource Records",
            description = """
        Page through the SNMP data collection sources.
        - `filter`: case-insensitive substring match on name, vendor and description.
        - `sortBy`: `name`, `vendor`, `description`; anything else falls back to `createdTime`.
        - `order`: `asc` or `desc` (default `desc`).
        - `limit` is **required** and has to be 1..1000; `offset` defaults to 0 when omitted.

        `createdTime` and `lastModified` come back as epoch milliseconds.""",
            operationId = "filterSnmpCollectionSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SnmpCollectionSource records retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpCollectionSourcePageResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalRecords": 1,
                      "snmpCollectionSourceList": [
                        {
                          "id": 24,
                          "name": "Cisco",
                          "vendor": "Cisco",
                          "description": null,
                          "enabled": true,
                          "createdTime": 1786129480772,
                          "lastModified": 1786129480772,
                          "uploadedBy": "admin"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`limit` was missing, below 1, or above 1000, or `offset` was negative",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Invalid offset/limit values (limit must be 1..1000)\"}"))),
            @ApiResponse(responseCode = "204", description = "No matching SnmpCollectionSource records found for the given criteria")
    })
    Response filterSnmpCollectionSources(
            @Parameter(description = "Case-insensitive substring matched against name, vendor and description.", example = "Cisco")
            @QueryParam("filter") String filter,
            @Parameter(description = "Sort column. An unrecognised value falls back to `createdTime`.",
                    example = "name", schema = @Schema(allowableValues = {"name", "vendor", "description", "createdTime"}))
            @QueryParam("sortBy") String sortBy,
            @Parameter(description = "Sort direction.", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @QueryParam("order") String order,
            @Parameter(description = "Total row count already known to the caller, used to skip the count query.", example = "120")
            @QueryParam("totalRecords") Integer totalRecords,
            @Parameter(description = "Zero-based index of the first row to return. Defaults to 0.", example = "0")
            @QueryParam("offset") Integer offset,
            @Parameter(description = "Rows to return. Required, 1..1000.", example = "20", required = true)
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );

    @GET
    @Path("/filter/{collectionSourceId}/mibgroups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get DataCollectionMibGroup by Collection Source ID with filtering and sorting",
            description = """
        Page through the MIB groups belonging to one source.
        - `mibGroupFilter`: case-insensitive match on name and ifType.
        - `sortBy`: `name` or `ifType`; anything else falls back to `createdTime`.
        - `order`: `asc` or `desc` (default `desc`).
        - `offset` and `limit` are **both required**: omitting `offset` fails with a 500 rather than
          defaulting to 0. `limit` has to be 1..1000.

        A `collectionSourceId` that does not exist answers 204 rather than 404.""",
            operationId = "filterDataCollectionMibGroupByCollectionSourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DataCollectionMibGroup records retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionMibGroupPageResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalRecords": 1,
                      "dataCollectionMibGroupList": [
                        {
                          "id": 343,
                          "name": "cisco-router",
                          "ifType": "ignore",
                          "mibGroupNames": null,
                          "mibObjects": "[{\\"alias\\":\\"cpu5min\\",\\"oid\\":\\".1.3.6.1.4.1.9.2.1.58\\",\\"instance\\":\\"0\\",\\"type\\":\\"gauge\\"}]",
                          "mibObjProperties": null,
                          "enabled": true,
                          "collectionSourceId": 24,
                          "collectionSourceName": "Cisco"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` not a positive integer, or `limit` missing or outside 1..1000",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Invalid collectionSourceId/offset/limit values (limit must be 1..1000)\"}"))),
            @ApiResponse(responseCode = "204", description = "No matching DataCollectionMibGroup record found for the given criteria"),
            @ApiResponse(responseCode = "500", description = "Raised when `offset` is omitted.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"java.lang.Integer.intValue()\" because \"offset\" is null")))
    })
    Response filterDataCollectionMibGroupByCollectionSourceId(
            @Parameter(description = "Source id owning the MIB groups. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Parameter(description = "Case-insensitive substring matched against name and ifType.", example = "cisco")
            @QueryParam("mibGroupFilter") String mibGroupFilter,
            @Parameter(description = "Sort column. An unrecognised value falls back to `createdTime`.",
                    example = "name", schema = @Schema(allowableValues = {"name", "ifType", "createdTime"}))
            @QueryParam("sortBy") String sortBy,
            @Parameter(description = "Sort direction.", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @QueryParam("order") String order,
            @Parameter(description = "Total row count already known to the caller, used to skip the count query.", example = "40")
            @QueryParam("totalRecords") Integer totalRecords,
            @Parameter(description = "Zero-based index of the first row to return. Required.", example = "0", required = true)
            @QueryParam("offset") Integer offset,
            @Parameter(description = "Rows to return. Required, 1..1000.", example = "20", required = true)
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );

    @GET
    @Path("/filter/{collectionSourceId}/resourcetypes")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get DataCollectionResourceType by Collection Source ID with filtering and sorting",
            description = """
        Page through the resource types belonging to one source.
        - `resourceTypeFilter`: case-insensitive match on name and label.
        - `sortBy`: `name` or `label`; anything else falls back to `createdTime`.
        - `order`: `asc` or `desc` (default `desc`).
        - `offset` and `limit` are **both required**: omitting `offset` fails with a 500 rather than
          defaulting to 0. `limit` has to be 1..1000.

        A `collectionSourceId` that does not exist answers 204 rather than 404.""",
            operationId = "filterDataCollectionResourceTypeByCollectionSourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DataCollectionResourceType records retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionResourceTypePageResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalRecords": 1,
                      "dataCollectionResourceTypeList": [
                        {
                          "id": 211,
                          "name": "cpqDaPhyDrv",
                          "label": "Compaq Physical Drive",
                          "resourceLabel": "${index}",
                          "persistenceSelectorStrategy": "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy",
                          "persistenceSelectorParams": null,
                          "storageStrategy": "org.opennms.netmgt.dao.support.IndexStorageStrategy",
                          "storageStrategyParams": null,
                          "enabled": true,
                          "collectionSourceId": 24,
                          "collectionSourceName": "Cisco"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` not a positive integer, or `limit` missing or outside 1..1000",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Invalid collectionSourceId/offset/limit values (limit must be 1..1000)\"}"))),
            @ApiResponse(responseCode = "204", description = "No matching DataCollectionResourceType record found for the given criteria"),
            @ApiResponse(responseCode = "500", description = "Raised when `offset` is omitted.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"java.lang.Integer.intValue()\" because \"offset\" is null")))
    })
    Response filterDataCollectionResourceTypeByCollectionSourceId(
            @Parameter(description = "Source id owning the resource types. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Parameter(description = "Case-insensitive substring matched against name and label.", example = "cpq")
            @QueryParam("resourceTypeFilter") String resourceTypeFilter,
            @Parameter(description = "Sort column. An unrecognised value falls back to `createdTime`.",
                    example = "name", schema = @Schema(allowableValues = {"name", "label", "createdTime"}))
            @QueryParam("sortBy") String sortBy,
            @Parameter(description = "Sort direction.", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @QueryParam("order") String order,
            @Parameter(description = "Total row count already known to the caller, used to skip the count query.", example = "12")
            @QueryParam("totalRecords") Integer totalRecords,
            @Parameter(description = "Zero-based index of the first row to return. Required.", example = "0", required = true)
            @QueryParam("offset") Integer offset,
            @Parameter(description = "Rows to return. Required, 1..1000.", example = "20", required = true)
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/filter/{collectionSourceId}/systemdefs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get DataCollectionSystemDef by Collection Source ID with filtering and sorting",
            description = """
        Page through the system definitions belonging to one source.
        - `systemDefsFilter`: case-insensitive match on name.
        - `sortBy`: `name`; anything else falls back to `createdTime`.
        - `order`: `asc` or `desc` (default `desc`).
        - `offset` and `limit` are **both required**: omitting `offset` fails with a 500 rather than
          defaulting to 0. `limit` has to be 1..1000.

        A `collectionSourceId` that does not exist answers 204 rather than 404.""",
            operationId = "filterDataCollectionSystemDefByCollectionSourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DataCollectionSystemDef records retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionSystemDefPageResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalRecords": 1,
                      "dataCollectionSystemDefsList": [
                        {
                          "id": 219,
                          "name": "Cisco Routers",
                          "sysoid": null,
                          "sysoidMask": ".1.3.6.1.4.1.9.1.",
                          "ipAddresses": [],
                          "ipAddressMasks": [],
                          "mibGroupNames": "[\\"cisco-router\\"]",
                          "enabled": true,
                          "collectionSourceId": 24,
                          "collectionSourceName": "Cisco"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` not a positive integer, or `limit` missing or outside 1..1000",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Invalid collectionSourceId/offset/limit values (limit must be 1..1000)\"}"))),
            @ApiResponse(responseCode = "204", description = "No matching DataCollectionSystemDef record found for the given criteria"),
            @ApiResponse(responseCode = "500", description = "Raised when `offset` is omitted.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"java.lang.Integer.intValue()\" because \"offset\" is null")))
    })
    Response filterDataCollectionSystemDefByCollectionSourceId(
            @Parameter(description = "Source id owning the system definitions. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Parameter(description = "Case-insensitive substring matched against name.", example = "Cisco")
            @QueryParam("systemDefsFilter") String systemDefFilter,
            @Parameter(description = "Sort column. An unrecognised value falls back to `createdTime`.",
                    example = "name", schema = @Schema(allowableValues = {"name", "createdTime"}))
            @QueryParam("sortBy") String sortBy,
            @Parameter(description = "Sort direction.", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @QueryParam("order") String order,
            @Parameter(description = "Total row count already known to the caller, used to skip the count query.", example = "8")
            @QueryParam("totalRecords") Integer totalRecords,
            @Parameter(description = "Zero-based index of the first row to return. Required.", example = "0", required = true)
            @QueryParam("offset") Integer offset,
            @Parameter(description = "Rows to return. Required, 1..1000.", example = "20", required = true)
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/collectsources/{collectionSourceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get SnmpCollectionSource by ID",
            description = """
        Retrieve one SNMP data collection source by id. `createdTime` and `lastModified` come back as
        epoch milliseconds. `vendor` defaults to the source name for a source created through the API.""",
            operationId = "getSnmpDataCollectionSourceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SnmpCollectionSource retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpCollectionSourceDto.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 24,
                      "name": "Cisco",
                      "vendor": "Cisco",
                      "description": null,
                      "enabled": true,
                      "createdTime": 1786129480772,
                      "lastModified": 1786129480772,
                      "uploadedBy": "admin"
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` was not a positive integer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Invalid collectionSourceId provided\"}"))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSource not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"snmpCollectionSource not found for id: 999999\"}"))),
            @ApiResponse(responseCode = "500", description = "The source could not be read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response getSnmpDataCollectionSourceById(
            @Parameter(description = "Source id. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Context SecurityContext securityContext
    );

    @GET
    @Path("/collectsources/names-and-ids")
    @Produces("application/json")
    @Operation(
            summary = "Get SnmpCollection Source Names",
            description = "Retrieve the id and name of every SNMP data collection source, in no defined order.",
            operationId = "getSnmpCollectionSourceNamesAndIds"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved source names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = SnmpCollectionSourceNamesAndIdsResponse.class)),
                            examples = @ExampleObject(value = """
                    [
                      { "id": 1, "name": "Makelsan" },
                      { "id": 24, "name": "Cisco" }
                    ]"""))),
            @ApiResponse(responseCode = "500", description = "The source names could not be read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to fetch SnmpCollection source names: ...")))
    })
    Response getSnmpCollectionSourceNamesAndIds(@Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/resourcetypes/names")
    @Produces("application/json")
    @Operation(
            summary = "Get DataCollection Resource Type Names",
            description = "Retrieve the names of every resource type across all sources, as a flat string array.",
            operationId = "getDataCollectionResourceTypeNames"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved resource type names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(value = "[\"ifIndex\", \"cpqDaPhyDrv\", \"mtxrWlStatIndex\"]"))),
            @ApiResponse(responseCode = "500", description = "The names could not be read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to fetch Resource Type names: ...")))
    })
    Response getDataCollectionResourceTypeNames(@Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/mibgroups/names")
    @Produces("application/json")
    @Operation(
            summary = "Get DataCollection MIB Group Names",
            description = "Retrieve the names of every MIB group across all sources, as a flat string array.",
            operationId = "getDataCollectionMibGroupNames"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved MIB group names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(value = "[\"cisco-router\", \"mib2-interfaces\", \"domino-stats\"]"))),
            @ApiResponse(responseCode = "500", description = "The names could not be read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to fetch MIB Group names: ...")))
    })
    Response getDataCollectionMibGroupNames(@Context SecurityContext securityContext) throws Exception;

    @POST
    @Path("/collectsources/{collectionSourceId}/mibgroups")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new Mib Group to an SnmpCollectionSources",
            description = """
        Create a MIB group under the given source, the equivalent of one `<group>` in a
        `<datacollection-group>`. `mibObjects` and `mibObjProperties` are JSON documents carried as
        strings, so their quotes are escaped inside the request body. The response body is the new MIB
        group id as a bare integer.""",
            operationId = "addMibGroupToSnmpCollectionSources")
    @RequestBody(
            required = true,
            description = "MIB group to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionMibGroupDto.class),
                    examples = @ExampleObject(value = MIB_GROUP_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "MibGroup created successfully; body is the new id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "integer", format = "int32"),
                            examples = @ExampleObject(value = "343"))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` not a positive integer, missing body, or an unusable payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body (SnmpCollectionMibGroupDto) must not be null."))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found")))})
    Response addMibGroupToSnmpCollectionSources(
            @Parameter(description = "Source id the MIB group is created under. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") final Integer collectionSourceId,
             final  SnmpCollectionMibGroupDto request, @Context SecurityContext securityContext) throws Exception;


    @POST
    @Path("/collectsources/{collectionSourceId}/resourcetypes")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new Resource Type to an SnmpCollectionSources",
            description = """
        Create a resource type under the given source, the equivalent of one `<resourceType>` in a
        `<datacollection-group>`. `persistenceSelectorStrategy` and `storageStrategy` are fully
        qualified class names; the `*Params` fields carry their parameters as a JSON string. The
        response body is the new resource type id as a bare integer.""",
            operationId = "addResourceTypeToSnmpCollectionSources")
    @RequestBody(
            required = true,
            description = "Resource type to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionResourceTypeDto.class),
                    examples = @ExampleObject(value = RESOURCE_TYPE_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "ResourceType created successfully; body is the new id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "integer", format = "int32"),
                            examples = @ExampleObject(value = "211"))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` not a positive integer, missing body, or an unusable payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body (SnmpCollectionResourceTypeDto) must not be null."))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found")))})
    Response addResourceTypeToSnmpCollectionSources(
            @Parameter(description = "Source id the resource type is created under. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") final Integer collectionSourceId,
            final  SnmpCollectionResourceTypeDto request,
            @Context SecurityContext securityContext) throws Exception;

    @POST
    @Path("/collectsources/{collectionSourceId}/systemdefs")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new System Definition to an SnmpCollectionSources",
            description = """
        Create a system definition under the given source, the equivalent of one `<systemDef>` in a
        `<datacollection-group>`. Supply either `sysoid` for an exact match or `sysoidMask` for a prefix
        match. `mibGroupNames` is a JSON array carried as a string and names the MIB groups the
        definition collects. The response body is the new system definition id as a bare integer.""",
            operationId = "addSystemDefToSnmpCollectionSources")
    @RequestBody(
            required = true,
            description = "System definition to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionSystemDefDto.class),
                    examples = @ExampleObject(value = SYSTEM_DEF_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SystemDef created successfully; body is the new id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "integer", format = "int32"),
                            examples = @ExampleObject(value = "219"))),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` not a positive integer, missing body, or an unusable payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body (SnmpCollectionSystemDefDto) must not be null."))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found")))})
    Response addSystemDefToSnmpCollectionSources(
            @Parameter(description = "Source id the system definition is created under. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") final Integer collectionSourceId,
            final SnmpCollectionSystemDefDto request,
            @Context SecurityContext securityContext) throws Exception;

    @PUT
    @Path("/collectsources/{collectionSourceId}/mibgroups/{mibGroupId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update a Mib Group in an SnmpCollectionSources",
            description = """
        Replace the fields of an existing MIB group. The body is a whole MIB group, not a patch: a field
        left out is written as null. The MIB group has to belong to the given source.""",
            operationId = "updateMibGroupInSnmpCollectionSources")
    @RequestBody(
            required = true,
            description = "Replacement MIB group.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionMibGroupDto.class),
                    examples = @ExampleObject(value = MIB_GROUP_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MibGroup updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "MibGroup updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Missing body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body cannot be null"))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources or MibGroup not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "MibGroup was not found: No MibGroup found for collectionSourceId=24, mibGroupId=999999")))
    })
    Response updateMibGroupInSnmpCollectionSources(
            @Parameter(description = "Source id owning the MIB group.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Parameter(description = "MIB group id to update.", example = "343", required = true)
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
            description = """
        Replace the fields of an existing resource type. The body is a whole resource type, not a patch:
        a field left out is written as null. The resource type has to belong to the given source.""",
            operationId = "updateResourceTypeInSnmpCollectionSources")
    @RequestBody(
            required = true,
            description = "Replacement resource type.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionResourceTypeDto.class),
                    examples = @ExampleObject(value = RESOURCE_TYPE_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ResourceType updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "ResourceType updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Missing body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body cannot be null"))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources or ResourceType not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "ResourceType was not found: No ResourceType found for collectionSourceId=24, resourceTypeId=999999")))
    })
    Response updateResourceTypeInSnmpCollectionSources(
            @Parameter(description = "Source id owning the resource type.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Parameter(description = "Resource type id to update.", example = "211", required = true)
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
            description = """
        Replace the fields of an existing system definition. The body is a whole system definition, not a
        patch: a field left out is written as null, so switching from `sysoid` to `sysoidMask` means
        sending only the one that should survive. The system definition has to belong to the given
        source.""",
            operationId = "updateSystemDefInSnmpCollectionSources")
    @RequestBody(
            required = true,
            description = "Replacement system definition.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionSystemDefDto.class),
                    examples = @ExampleObject(value = SYSTEM_DEF_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SystemDef updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SystemDef updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Missing body",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body cannot be null"))),
            @ApiResponse(responseCode = "404", description = "SnmpCollectionSources or SystemDef not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SystemDef was not found: No SystemDef found for collectionSourceId=24, systemDefId=999999")))
    })
    Response updateSystemDefInSnmpCollectionSources(
            @Parameter(description = "Source id owning the system definition.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer collectionSourceId,
            @Parameter(description = "System definition id to update.", example = "219", required = true)
            @PathParam("systemDefId") Integer systemDefId,
            SnmpCollectionSystemDefDto request,
            @Context SecurityContext securityContext
    ) throws Exception;

    @POST
    @Path("/collectsources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create SNMP Data Collection Source",
            description = """
        Create an empty SNMP data collection source and attach it to one or more existing profiles.
        `name` and at least one entry in `profiles` are both required, every profile name has to already
        exist, and the source name has to be unused. The source starts with no MIB groups, resource types
        or system definitions: add those through the `/collectsources/{id}/mibgroups`,
        `/resourcetypes` and `/systemdefs` endpoints, or upload a `<datacollection-group>` file.
        `vendor` is set to the source name. The response body is the new source id as a bare integer.""",
            operationId = "createSnmpDataCollectionSource"
    )
    @RequestBody(
            required = true,
            description = "Source name and the profiles to attach it to.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpCollectionCreateSourceDto.class),
                    examples = @ExampleObject(value = """
                    {
                      "name": "Acme Packet",
                      "profiles": [ "default" ]
                    }"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Source created successfully; body is the new id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "integer", format = "int32"),
                            examples = @ExampleObject(value = "84"))),
            @ApiResponse(responseCode = "400", description = "Missing body, blank name, empty `profiles`, a name already in use, or a profile that does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The following profiles do not exist: [staging]"))),
            @ApiResponse(responseCode = "500", description = "The source could not be created.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response createSnmpDataCollectionSource(
            SnmpCollectionCreateSourceDto request,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete SNMP Data Collection Sources",
            description = """
        Delete one or more sources and everything under them. Ids that do not exist are ignored, so a
        request naming only unknown ids still answers 200. The source name is left behind in any
        profile's `sourceNames`, so remove it from the profiles as well if the reference should go.""",
            operationId = "deleteSnmpDataCollectionSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sources deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Snmp Data Collection deleted successfully"))),
            @ApiResponse(responseCode = "400", description = "No `id` parameter supplied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Snmp Data Collection IDs to delete must not be empty"))),
            @ApiResponse(responseCode = "404", description = "Reported when the delete itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found"))),
            @ApiResponse(responseCode = "500", description = "The deletion failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response deleteSnmpDataCollectionSources(
            @Parameter(description = "Source id to delete. Repeat the parameter to delete several.", example = "84", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources/{snmpDataCollectionSourceId}/mib-groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete MIB Groups for a Source",
            description = """
        Delete one or more MIB groups belonging to the given source. Ids that do not exist are ignored,
        so a request naming only unknown ids still answers 200. A system definition that still names a
        deleted MIB group in `mibGroupNames` is not updated.""",
            operationId = "deleteMibGroupsForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MIB groups deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Snmp Data Collection Mib Groups deleted successfully"))),
            @ApiResponse(responseCode = "400", description = "No `id` parameter supplied, or an invalid source id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "MIB Group IDs to delete must not be empty"))),
            @ApiResponse(responseCode = "404", description = "Reported when the delete itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found"))),
            @ApiResponse(responseCode = "500", description = "The deletion failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response deleteMibGroupsForSource(
            @Parameter(description = "Source id owning the MIB groups.", example = "24", required = true)
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @Parameter(description = "MIB group id to delete. Repeat the parameter to delete several.", example = "343", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources/{snmpDataCollectionSourceId}/resource-types")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete Resource Types for a Source",
            description = """
        Delete one or more resource types belonging to the given source. Ids that do not exist are
        ignored, so a request naming only unknown ids still answers 200.""",
            operationId = "deleteResourceTypesForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource types deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Snmp Data Collection Resource Types deleted successfully"))),
            @ApiResponse(responseCode = "400", description = "No `id` parameter supplied, or an invalid source id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Resource Type IDs to delete must not be empty"))),
            @ApiResponse(responseCode = "404", description = "Reported when the delete itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found"))),
            @ApiResponse(responseCode = "500", description = "The deletion failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response deleteResourceTypesForSource(
            @Parameter(description = "Source id owning the resource types.", example = "24", required = true)
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @Parameter(description = "Resource type id to delete. Repeat the parameter to delete several.", example = "211", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @DELETE
    @Path("/collectsources/{snmpDataCollectionSourceId}/system-defs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete System Definitions for a Source",
            description = """
        Delete one or more system definitions belonging to the given source. Ids that do not exist are
        ignored, so a request naming only unknown ids still answers 200.""",
            operationId = "deleteSystemDefsForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System definitions deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Snmp Data Collection System Def deleted successfully"))),
            @ApiResponse(responseCode = "400", description = "No `id` parameter supplied, or an invalid source id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "System Def IDs to delete must not be empty"))),
            @ApiResponse(responseCode = "404", description = "Reported when the delete itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SnmpCollectionSource with ID 999999 not found"))),
            @ApiResponse(responseCode = "500", description = "The deletion failed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DataCollectionConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\":\"Unexpected error occurred\"}")))
    })
    Response deleteSystemDefsForSource(
            @Parameter(description = "Source id owning the system definitions.", example = "24", required = true)
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @Parameter(description = "System definition id to delete. Repeat the parameter to delete several.", example = "219", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    );

    @PATCH
    @Path("/collectsources/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP Data Collection Sources",
            description = """
        Set the enabled flag on one or more sources. Ids are supplied as repeated `id` query parameters,
        not as a request body. Ids that do not exist are ignored, so a request naming only unknown ids
        still answers 200.""",
            operationId = "enableDisableSnmpDataCollectionSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP data collection sources updated successfully."))),
            @ApiResponse(responseCode = "400", description = "No `id` parameter supplied, or an id that is not a positive integer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "All ids must be non-null positive integers.")))
    })
    Response enableDisableSnmpDataCollectionSources(
            @Parameter(description = "Target state for every listed source.", example = "false", required = true,
                    schema = @Schema(type = "boolean", allowableValues = {"true", "false"}))
            @PathParam("enabled") boolean enabled,
            @Parameter(description = "Source id to update. Repeat the parameter to update several.", example = "24", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;

    @PATCH
    @Path("/collectsources/{snmpDataCollectionSourceId}/mib-groups/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP MIB Groups",
            description = """
        Set the enabled flag on one or more MIB groups belonging to the given source. Ids are supplied as
        repeated `id` query parameters, not as a request body. Ids that do not exist are ignored, so a
        request naming only unknown ids still answers 200.""",
            operationId = "enableDisableSnmpMibGroups"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP MIB groups updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid source id, no `id` parameter supplied, or an id that is not a positive integer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "snmpDataCollectionSourceId must be provided and must be a positive integer."))),
            @ApiResponse(responseCode = "404", description = "Reported when the update itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Source or one/more ids were not found: ...")))
    })
    Response enableDisableSnmpMibGroups(
            @Parameter(description = "Source id owning the MIB groups.", example = "24", required = true)
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @Parameter(description = "Target state for every listed MIB group.", example = "false", required = true,
                    schema = @Schema(type = "boolean", allowableValues = {"true", "false"}))
            @PathParam("enabled") boolean enabled,
            @Parameter(description = "MIB group id to update. Repeat the parameter to update several.", example = "343", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;

    @PATCH
    @Path("/collectsources/{snmpDataCollectionSourceId}/resource-types/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP Resource Types",
            description = """
        Set the enabled flag on one or more resource types belonging to the given source. Ids are
        supplied as repeated `id` query parameters, not as a request body. Ids that do not exist are
        ignored, so a request naming only unknown ids still answers 200.""",
            operationId = "enableDisableSnmpResourceTypes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP resource types updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid source id, no `id` parameter supplied, or an id that is not a positive integer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "snmpDataCollectionSourceId must be provided and must be a positive integer."))),
            @ApiResponse(responseCode = "404", description = "Reported when the update itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Source or one/more ids were not found: ...")))
    })
    Response enableDisableSnmpResourceTypes(
            @Parameter(description = "Source id owning the resource types.", example = "24", required = true)
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @Parameter(description = "Target state for every listed resource type.", example = "false", required = true,
                    schema = @Schema(type = "boolean", allowableValues = {"true", "false"}))
            @PathParam("enabled") boolean enabled,
            @Parameter(description = "Resource type id to update. Repeat the parameter to update several.", example = "211", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;


    @PATCH
    @Path("/collectsources/{snmpDataCollectionSourceId}/system-defs/status/{enabled}")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable SNMP System Definitions",
            description = """
        Set the enabled flag on one or more system definitions belonging to the given source. Ids are
        supplied as repeated `id` query parameters, not as a request body. Ids that do not exist are
        ignored, so a request naming only unknown ids still answers 200.""",
            operationId = "enableDisableSnmpSystemDefs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP system defs updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid source id, no `id` parameter supplied, or an id that is not a positive integer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "snmpDataCollectionSourceId must be provided and must be a positive integer."))),
            @ApiResponse(responseCode = "404", description = "Reported when the update itself raises an entity-not-found; an unknown id on its own answers 200.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Source or one/more ids were not found: ...")))
    })
    Response enableDisableSnmpSystemDefs(
            @Parameter(description = "Source id owning the system definitions.", example = "24", required = true)
            @PathParam("snmpDataCollectionSourceId") Integer snmpDataCollectionSourceId,
            @Parameter(description = "Target state for every listed system definition.", example = "false", required = true,
                    schema = @Schema(type = "boolean", allowableValues = {"true", "false"}))
            @PathParam("enabled") boolean enabled,
            @Parameter(description = "System definition id to update. Repeat the parameter to update several.", example = "219", required = true)
            @QueryParam("id") List<Integer> ids,
            @Context SecurityContext securityContext
    ) throws Exception;


    @GET
    @Path("/collectsources/{collectionSourceId}/download")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Download Snmp Data Collection XML for a Source",
            description = """
        Reassemble one source as a `<datacollection-group>` document carrying its resource types, MIB
        groups and system definitions, served as an attachment named after the source. Defaults to XML;
        `format=json` returns the same group as JSON, using the JAXB bean field names
        (`groups`, `mibObjs`, `clazz`) rather than the XML element names.

        The XML form is what `POST /datacollectionconf/upload` accepts back. Disabled child rows are
        included, so the download is the stored definition rather than only what collectd would use.
        Error responses on this endpoint are `<error>` XML regardless of `format`.""",
            operationId = "downloadSnmpDataCollectionById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Snmp Data Collection XML downloaded successfully",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(value = """
                    <datacollection-group xmlns="http://xmlns.opennms.org/xsd/config/datacollection" name="Cisco">
                       <resourceType name="cpqDaPhyDrv" label="Compaq Physical Drive" resourceLabel="${index}">
                          <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"/>
                          <storageStrategy class="org.opennms.netmgt.dao.support.IndexStorageStrategy"/>
                       </resourceType>
                       <group name="cisco-router" ifType="ignore">
                          <mibObj oid=".1.3.6.1.4.1.9.2.1.58" instance="0" alias="cpu5min" type="gauge"/>
                       </group>
                       <systemDef name="Cisco Routers">
                          <sysoidMask>.1.3.6.1.4.1.9.1.</sysoidMask>
                          <collect>
                             <includeGroup>cisco-router</includeGroup>
                          </collect>
                       </systemDef>
                    </datacollection-group>""")),
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(type = "object"),
                                examples = @ExampleObject(value = """
                    {
                      "groups" : [ {
                        "includeGroups" : [ ],
                        "ifType" : "ignore",
                        "mibObjs" : [ {
                          "maxval" : null,
                          "minval" : null,
                          "alias" : "cpu5min",
                          "instance" : "0",
                          "oid" : ".1.3.6.1.4.1.9.2.1.58",
                          "type" : "gauge"
                        } ],
                        "name" : "cisco-router",
                        "properties" : [ ]
                      } ],
                      "resourceTypes" : [ ],
                      "systemDefs" : [ ],
                      "name" : "Cisco"
                    }"""))
                    }),
            @ApiResponse(responseCode = "400", description = "`collectionSourceId` was not a positive integer, or `format` was neither `xml` nor `json`",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>Invalid format: yaml. Supported values: xml, json</error>"))),
            @ApiResponse(responseCode = "404", description = "No snmpDataCollection found for the specified snmpDataCollection ID",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>No Snmp Collection Source found for ID: 999999</error>"))),
            @ApiResponse(responseCode = "500", description = "The group could not be serialized.",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>Failed to generate XML: ...</error>")))
    })
    Response downloadSnmpDataCollectionById(
            @Parameter(description = "Source id to export. Must be a positive integer.", example = "24", required = true)
            @PathParam("collectionSourceId") Integer snmpDataCollectionId,
            @Parameter(description = "Serialization format. Defaults to `xml` when omitted or blank.",
                    example = "xml", schema = @Schema(allowableValues = {"xml", "json"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext
    ) throws Exception;

    @GET
    @Path("/config/download")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Download the top-level datacollection-config",
            description = """
        Return the top-level `<datacollection-config>` assembled from the current profile rows: one
        `<snmp-collection>` per **enabled** profile, with rrd metadata, storage flag, max-vars-per-pdu,
        and one `<include-collection dataCollectionGroup="..."/>` per source name in the profile's
        `sourceNames`. Disabled profiles are omitted, matching what collectd loads. The `rrdRepository`
        attribute is copied from the live in-memory config, and the upload path does not read it back.

        Pair it with `/collectsources/{id}/download` to obtain the matching `<datacollection-group>`
        files; the whole set is re-uploadable as one multipart batch via `/upload`. Defaults to XML;
        `format=json` returns the same document using the JAXB bean field names. Error responses are
        `<error>` XML regardless of `format`.""",
            operationId = "downloadDatacollectionConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "datacollection-config downloaded",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(value = """
                    <datacollection-config xmlns="http://xmlns.opennms.org/xsd/config/datacollection" rrdRepository="/opt/opennms/share/rrd/snmp">
                       <snmp-collection name="default" snmpStorageFlag="select">
                          <rrd step="300">
                             <rra>RRA:AVERAGE:0.5:1:2016</rra>
                             <rra>RRA:AVERAGE:0.5:12:1488</rra>
                             <rra>RRA:AVERAGE:0.5:288:366</rra>
                             <rra>RRA:MAX:0.5:288:366</rra>
                             <rra>RRA:MIN:0.5:288:366</rra>
                          </rrd>
                          <include-collection dataCollectionGroup="MIB2"/>
                          <include-collection dataCollectionGroup="Cisco"/>
                       </snmp-collection>
                    </datacollection-config>""")),
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(type = "object"),
                                examples = @ExampleObject(value = """
                    {
                      "snmpCollections" : [ {
                        "groups" : null,
                        "rrd" : {
                          "step" : 300,
                          "rras" : [ "RRA:AVERAGE:0.5:1:2016", "RRA:AVERAGE:0.5:12:1488" ]
                        },
                        "resourceTypes" : [ ],
                        "includeCollections" : [ {
                          "excludeFilters" : [ ],
                          "systemDef" : null,
                          "dataCollectionGroup" : "MIB2"
                        } ],
                        "name" : "default",
                        "snmpStorageFlag" : "select"
                      } ],
                      "rrdRepository" : "/opt/opennms/share/rrd/snmp"
                    }"""))
                    }),
            @ApiResponse(responseCode = "400", description = "`format` was neither `xml` nor `json`",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>Invalid format: yaml. Supported values: xml, json</error>"))),
            @ApiResponse(responseCode = "500", description = "The document could not be serialized.",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>Failed to generate XML: ...</error>")))
    })
    Response downloadDatacollectionConfig(
            @Parameter(description = "Serialization format. Defaults to `xml` when omitted or blank.",
                    example = "xml", schema = @Schema(allowableValues = {"xml", "json"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext
    ) throws Exception;

}
