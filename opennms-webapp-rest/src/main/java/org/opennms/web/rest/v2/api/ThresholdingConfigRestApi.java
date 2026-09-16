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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.web.rest.v2.model.ThresholdGroupDto;
import org.opennms.web.rest.v2.model.ThresholdGroupSummaryDto;
import org.opennms.web.rest.v2.model.ThresholdingMetadataDto;

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

@Path("thresholding")
@Tag(name = "Thresholding", description = """
        Thresholding configuration API V2 (the contents of the former `thresholds.xml`).

        A threshold group is the unit of transfer: `GET`, `POST` and `PUT` all carry the whole group,
        including its thresholds, expressions and resource filters. There is no per-threshold endpoint,
        because a threshold has no identity of its own — it is addressed only by its position in the group,
        and that position is not stable across concurrent edits. A definition left out of a `PUT` body is
        therefore deleted.

        Reads list the merged configuration, which includes groups contributed by OSGi extensions; those are
        flagged `readOnly` and cannot be written. Writes apply to the stored configuration only.

        `GET` on a group returns an `ETag`. Sending it back as `If-Match` on a write makes the write fail
        with a 412 rather than overwrite a change someone else made in the meantime. Omitting `If-Match`
        keeps the last writer.

        Saving a threshold whose `triggeredUEI` or `rearmedUEI` is not yet known to eventconf also creates a
        matching event definition, cloned from the built-in threshold event for that threshold type.""")
public interface ThresholdingConfigRestApi {

    String GROUP_JSON_EXAMPLE = """
            {
              "name": "mib2",
              "rrdRepository": "/opt/opennms/share/rrd/snmp/",
              "thresholds": [
                {
                  "type": "high",
                  "dsName": "ifInOctets",
                  "dsType": "if",
                  "value": "90",
                  "rearm": "75",
                  "trigger": "2",
                  "description": "Inbound utilization",
                  "filterOperator": "or",
                  "resourceFilters": [
                    { "field": "ifDescr", "content": "^eth.*" }
                  ]
                }
              ],
              "expressions": []
            }""";

    @GET
    @Path("groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List threshold groups",
            description = """
        List every threshold group, without its definitions. The list is the merged view: groups contributed
        by an OSGi extension appear with `readOnly` set to true. Sorted by name.""",
            operationId = "getThresholdGroups"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully. An empty array when no configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = ThresholdGroupSummaryDto.class)))),
            @ApiResponse(responseCode = "500", description = "Failed to read the thresholding configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to retrieve thresholding configuration.")))
    })
    Response getThresholdGroups(@Context SecurityContext securityContext);

    @GET
    @Path("groups/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a threshold group",
            description = """
        Retrieve one threshold group with all of its thresholds and expressions, in the order the daemon
        evaluates them. The response carries an `ETag` to be used as `If-Match` on a later write.""",
            operationId = "getThresholdGroup"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ThresholdGroupDto.class),
                            examples = @ExampleObject(value = GROUP_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No group of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Threshold group 'mib2' does not exist.")))
    })
    Response getThresholdGroup(
            @Parameter(description = "Name of the group.", example = "mib2")
            @PathParam("groupName") String groupName,
            @Context SecurityContext securityContext);

    @POST
    @Path("groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a threshold group",
            description = "Create a new threshold group. Requires ROLE_ADMIN. The group name must not already be in use.",
            operationId = "createThresholdGroup"
    )
    @RequestBody(required = true, description = "The group to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThresholdGroupDto.class),
                    examples = @ExampleObject(value = GROUP_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created. The `Location` header points at the new group."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid threshold at index 0: trigger '0' must be a positive integer or a metadata reference."))),
            @ApiResponse(responseCode = "404", description = "No thresholding configuration is stored yet.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "A group of that name already exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Threshold group 'mib2' already exists.")))
    })
    Response createThresholdGroup(ThresholdGroupDto group, @Context SecurityContext securityContext);

    @PUT
    @Path("groups/{groupName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Replace a threshold group",
            description = """
        Replace a threshold group in full. Requires ROLE_ADMIN. The body is not a patch: thresholds and
        expressions missing from it are removed. Giving the body a different `name` renames the group,
        provided the new name is free. A group contributed by an OSGi extension cannot be written.""",
            operationId = "updateThresholdGroup"
    )
    @RequestBody(required = true, description = "The complete group.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThresholdGroupDto.class),
                    examples = @ExampleObject(value = GROUP_JSON_EXAMPLE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group replaced. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "The group is provided by an extension and is read only.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No group of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "409", description = "The new name is already taken by another group.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored group; it changed since it was read.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Threshold group 'mib2' has changed since it was read.")))
    })
    Response updateThresholdGroup(
            @Parameter(description = "Name of the group to replace.", example = "mib2")
            @PathParam("groupName") String groupName,
            ThresholdGroupDto group,
            @Parameter(description = "Version of the group as returned by the `ETag` of a previous GET. Omit to force the write.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @DELETE
    @Path("groups/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete a threshold group",
            description = """
        Delete a threshold group. Requires ROLE_ADMIN. Note that threshd packages referencing the group
        through a `thresholding-group` service parameter are not updated and will reference a group that no
        longer exists.""",
            operationId = "deleteThresholdGroup"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted. No response body."),
            @ApiResponse(responseCode = "403", description = "The group is provided by an extension and is read only.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No group of that name exists.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "412", description = "`If-Match` does not match the stored group.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response deleteThresholdGroup(
            @Parameter(description = "Name of the group to delete.", example = "mib2")
            @PathParam("groupName") String groupName,
            @Parameter(description = "Version of the group as returned by the `ETag` of a previous GET.")
            @HeaderParam(HttpHeaders.IF_MATCH) String ifMatch,
            @Context SecurityContext securityContext);

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get the values a threshold editor can choose from",
            description = """
        The selectable datasource types, threshold types and filter operators. The datasource types depend on
        the generic resource types configured in `datacollection-config.xml` on this system, so a client
        cannot hardcode them: `node` and `if` are always present, followed by every generic index resource
        type sorted by label.""",
            operationId = "getThresholdingMetadata"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ThresholdingMetadataDto.class),
                            examples = @ExampleObject(value = """
                    {
                      "dsTypes" : [ { "name" : "node", "label" : "Node" }, { "name" : "if", "label" : "Interface" } ],
                      "thresholdTypes" : [ "high", "low", "relativeChange", "absoluteChange", "rearmingAbsoluteChange" ],
                      "filterOperators" : [ "and", "or" ]
                    }""")))
    })
    Response getThresholdingMetadata(@Context SecurityContext securityContext);

    @POST
    @Path("reload")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Ask threshd to reload the thresholding configuration",
            description = """
        Send a `reloadDaemonConfig` event for `thresholds.xml`. Requires ROLE_ADMIN. Every write through
        this API already sends one, so this is only needed after the stored configuration was changed by
        some other means. The event is asynchronous: a 202 means it was sent, not that threshd has finished
        reloading.""",
            operationId = "reloadThresholdingConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Reload event sent."),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The event could not be sent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response reloadThresholdingConfiguration(@Context SecurityContext securityContext);

    @GET
    @Path("download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download the thresholding configuration",
            description = """
        Download the stored thresholding configuration as JSON or XML. Requires ROLE_ADMIN. The response is
        served as an attachment (`thresholds.json` or `thresholds.xml`) and is accepted back by the matching
        upload endpoint. Groups contributed by extensions are not included.""",
            operationId = "downloadThresholdingConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully."),
            @ApiResponse(responseCode = "400", description = "`format` was neither `json` nor `xml`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "No thresholding configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The stored configuration could not be serialized.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response downloadThresholdingConfiguration(
            @Parameter(description = "Serialization format. Defaults to `json`.",
                    example = "xml", schema = @Schema(allowableValues = {"json", "xml"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext);

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload the thresholding configuration in JSON format",
            description = """
        Replace the whole stored thresholding configuration. Requires ROLE_ADMIN. The single part must be
        named `upload` and hold a JSON document of the same shape as `GET /thresholding/download`.""",
            operationId = "uploadThresholdingConfiguration"
    )
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the JSON thresholding configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable JSON or a failed validation rule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Failed to persist the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadThresholdingConfiguration(@Multipart("upload") Attachment attachment,
                                             @Context SecurityContext securityContext);

    @POST
    @Path("upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload the thresholding configuration in XML format",
            description = """
        Replace the whole stored thresholding configuration. Requires ROLE_ADMIN. The single part must be
        named `upload` and hold a `<thresholding-config>` document, the form that
        `GET /thresholding/download?format=xml` produces. The document is validated against
        `thresholding.xsd` before it is stored.""",
            operationId = "uploadThresholdingConfigurationXml"
    )
    @RequestBody(required = true,
            description = "Multipart form with one `upload` part holding the XML thresholding configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration uploaded and applied. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable or schema-invalid XML.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Failed to persist the configuration.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    })
    Response uploadThresholdingConfigurationXml(@Multipart("upload") Attachment attachment,
                                                @Context SecurityContext securityContext);
}
