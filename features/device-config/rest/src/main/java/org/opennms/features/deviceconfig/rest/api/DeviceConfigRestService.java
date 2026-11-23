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
package org.opennms.features.deviceconfig.rest.api;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.rest.BackupRequestDTO;
import org.opennms.features.deviceconfig.rest.BackupResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Device Config Backup (DCB) ReST API, served under {@code /rest/device-config}.
 *
 * Registered as an OSGi service rather than through the webapp's CXF servlet, so
 * {@code opennms-openapi-docs} lists this interface explicitly for the v1 document.
 */
@Path("/device-config")
@Tag(name = "DeviceConfig", description = """
        Device Config Backup: list, download and delete the device configurations retrieved by the
        `DeviceConfig` monitored services, and trigger a backup on demand. Each record belongs to one
        IP interface and one configuration type (typically `default` or `running`). Dates are epoch
        milliseconds.""")
public interface DeviceConfigRestService {

    String DEVICE_CONFIG_EXAMPLE = """
            {
              "id": 42,
              "serviceName": "DeviceConfig-running",
              "configType": "running",
              "configName": "Running Configuration",
              "ipInterfaceId": 17,
              "ipAddress": "192.168.1.1",
              "nodeId": 5,
              "nodeLabel": "core-router-01",
              "deviceName": "core-router-01",
              "location": "Default",
              "operatingSystem": "IOS-XE",
              "lastBackupDate": 1787727308166,
              "lastUpdatedDate": 1787813708166,
              "lastSucceededDate": 1787813708166,
              "lastFailedDate": null,
              "nextScheduledBackupDate": 1787875200000,
              "scheduledInterval": {"DeviceConfig-running": "At 12:00 AM"},
              "backupStatus": "success",
              "isSuccessfulBackup": true,
              "successfulBackup": true,
              "failureReason": null,
              "encoding": "UTF-8",
              "fileName": "core-router-01-running.cfg",
              "config": "hostname core-router-01\\ninterface GigabitEthernet0/0\\n ip address 192.168.1.1 255.255.255.0\\n"
            }""";

    String DEVICE_CONFIG_LIST_EXAMPLE = "[" + DEVICE_CONFIG_EXAMPLE + "]";

    String CONTENT_RANGE_DESCRIPTION = "Position of this page in the full result, as `items <first>-<last>/<total>`.";

    String ORDER_BY_DESCRIPTION = """
            Sort property, case-insensitive: `lastUpdated`, `deviceName`, `lastBackup`, `ipAddress`, `location` \
            or `status`.""";

    String ORDER_DESCRIPTION = "`desc` sorts descending; any other value sorts ascending.";

    String STATUS_DESCRIPTION = """
            Only return records whose latest backup has one of these statuses. Repeat the parameter for more than \
            one (`status=SUCCESS&status=FAILED`); values are case-sensitive. Omit it to return every status.""";

    String NO_BODY_DESCRIPTION = "No body is returned.";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id : \\d+}")
    @Operation(
            summary = "Get a device config",
            description = """
        Return one device config record by database id, including the configuration text and the
        backup schedule of the services that retrieve it. A binary configuration is returned as a
        hexadecimal string with `encoding` set to `binary`.""",
            operationId = "DeviceConfigRestServiceGETDeviceConfig")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The device config.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DeviceConfigDTO.class),
                            examples = @ExampleObject(value = DEVICE_CONFIG_EXAMPLE))),
            @ApiResponse(responseCode = "204", description = "No device config has that id. " + NO_BODY_DESCRIPTION)
    })
    Response getDeviceConfig(
            @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Database id of the device config.", example = "42")
            @PathParam("id") long id);

    /**
     * Gets a list of device configs along with backup schedule information.
     * Unlike {@link #getLatestDeviceConfigsForDeviceAndConfigType}, every stored record matches,
     * not only the newest per interface and config type.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get device configs",
            description = """
        Return every stored device config record matching the filters, including older versions of a
        configuration. To get only the newest record for each interface and configuration type, use
        `GET /device-config/latest`.

        `deviceName`, `ipAddress` and `configType` are case-insensitive SQL `LIKE` patterns: without a
        `%` they must match the whole value.""",
            operationId = "DeviceConfigRestServiceGETDeviceConfigs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching device configs.",
                    headers = @Header(name = "Content-Range", description = CONTENT_RANGE_DESCRIPTION,
                            schema = @Schema(type = "string", example = "items 0-9/37")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = DeviceConfigDTO.class)),
                            examples = @ExampleObject(value = DEVICE_CONFIG_LIST_EXAMPLE))),
            @ApiResponse(responseCode = "204", description = "No device config matches, or `offset` is past the last match. " + NO_BODY_DESCRIPTION)
    })
    Response getDeviceConfigs(
        @Parameter(in = ParameterIn.QUERY, name = "limit", description = "Maximum number of records to return.", example = "10")
        @QueryParam("limit") @DefaultValue("10") Integer limit,
        @Parameter(in = ParameterIn.QUERY, name = "offset", description = "Number of records to skip.", example = "0")
        @QueryParam("offset") @DefaultValue("0") Integer offset,
        @Parameter(in = ParameterIn.QUERY, name = "orderBy",
                description = ORDER_BY_DESCRIPTION + " An unrecognized value sorts by `lastUpdated` descending, ignoring `order`.",
                example = "lastUpdated")
        @QueryParam("orderBy") @DefaultValue("lastUpdated") String orderBy,
        @Parameter(in = ParameterIn.QUERY, name = "order", description = ORDER_DESCRIPTION, example = "desc")
        @QueryParam("order") @DefaultValue("desc") String order,
        @Parameter(in = ParameterIn.QUERY, name = "deviceName", description = "Node label pattern.", example = "core-router%")
        @QueryParam("deviceName") String deviceName,
        @Parameter(in = ParameterIn.QUERY, name = "ipAddress", description = "IP address pattern.", example = "192.168.1.%")
        @QueryParam("ipAddress") String ipAddress,
        @Parameter(in = ParameterIn.QUERY, name = "ipInterfaceId", description = "Database id of the IP interface.", example = "17")
        @QueryParam("ipInterfaceId") Integer ipInterfaceId,
        @Parameter(in = ParameterIn.QUERY, name = "configType", description = "Configuration type pattern, typically `default` or `running`.", example = "running")
        @QueryParam("configType") String configType,
        @Parameter(in = ParameterIn.QUERY, name = "status", description = STATUS_DESCRIPTION)
        @QueryParam("status") Set<DeviceConfigStatus> statuses,
        @Parameter(in = ParameterIn.QUERY, name = "createdAfter",
                description = "Only return records whose `lastBackupDate` is at or after this time, in epoch milliseconds.",
                example = "1787000000000")
        @QueryParam("createdAfter") Long createdAfter,
        @Parameter(in = ParameterIn.QUERY, name = "createdBefore",
                description = "Only return records whose `lastBackupDate` is at or before this time, in epoch milliseconds.",
                example = "1788000000000")
        @QueryParam("createdBefore") Long createdBefore
    );

    /**
     * Gets the most recent device config for each device / config type combination.
     * Typically called by UI to get latest status.
     */
    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get the latest device config of each interface",
            description = """
        Return the most recently updated record for each IP interface and configuration type: the
        current backup state of every device, as the Device Config Backup page shows it.

        The total in `Content-Range` counts distinct IP interfaces, so it is lower than the number of
        records when an interface has more than one configuration type.""",
            operationId = "DeviceConfigRestServiceGETLatestDeviceConfigs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The latest device config of each interface and configuration type.",
                    headers = @Header(name = "Content-Range", description = CONTENT_RANGE_DESCRIPTION,
                            schema = @Schema(type = "string", example = "items 0-9/24")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = DeviceConfigDTO.class)),
                            examples = @ExampleObject(value = DEVICE_CONFIG_LIST_EXAMPLE))),
            @ApiResponse(responseCode = "204", description = "No device config matches, or `offset` is past the last match. " + NO_BODY_DESCRIPTION)
    })
    Response getLatestDeviceConfigsForDeviceAndConfigType(
        @Parameter(in = ParameterIn.QUERY, name = "limit",
                description = "Maximum number of records to return. A value of 0 or less returns up to 20.", example = "10")
        @QueryParam("limit") @DefaultValue("10") Integer limit,
        @Parameter(in = ParameterIn.QUERY, name = "offset", description = "Number of records to skip.", example = "0")
        @QueryParam("offset") @DefaultValue("0") Integer offset,
        @Parameter(in = ParameterIn.QUERY, name = "orderBy",
                description = ORDER_BY_DESCRIPTION + " An unrecognized value leaves the order unspecified.",
                example = "lastUpdated")
        @QueryParam("orderBy") @DefaultValue("lastUpdated") String orderBy,
        @Parameter(in = ParameterIn.QUERY, name = "order", description = ORDER_DESCRIPTION, example = "desc")
        @QueryParam("order") @DefaultValue("desc") String order,
        @Parameter(in = ParameterIn.QUERY, name = "search",
                description = "Case-sensitive substring of the node label or IP address.", example = "core")
        @QueryParam("search") String searchTerm,
        @Parameter(in = ParameterIn.QUERY, name = "status", description = STATUS_DESCRIPTION)
        @QueryParam("status") Set<DeviceConfigStatus> statuses,
        @Parameter(in = ParameterIn.QUERY, name = "pageEnter",
                description = "Set by the web UI when the Device Config Backup page opens, to count page visits in usage statistics. Does not change the result.",
                example = "false")
        @QueryParam("pageEnter") @DefaultValue("false") boolean pageEnter
    );

    /**
     * Get a list of device configs for a given IP interface id.
     * This is a history of configs for a particular device.
     * Returns all config types by default, use 'configType' to filter.
     */
    @GET
    @Path("/interface/{id : \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get the device config history of an interface",
            description = """
        Return every stored device config record of one IP interface, most recently updated first.
        Not paged: all records are returned.""",
            operationId = "DeviceConfigRestServiceGETDeviceConfigsByInterface")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device configs of the interface.",
                    headers = @Header(name = "Content-Range", description = CONTENT_RANGE_DESCRIPTION,
                            schema = @Schema(type = "string", example = "items 0-2/3")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = DeviceConfigDTO.class)),
                            examples = @ExampleObject(value = DEVICE_CONFIG_LIST_EXAMPLE))),
            @ApiResponse(responseCode = "204", description = "The interface has no device configs, or does not exist. " + NO_BODY_DESCRIPTION)
    })
    Response getDeviceConfigsByInterface(
        @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                description = "Database id of the IP interface.", example = "17")
        @PathParam("id") Integer ipInterfaceId,
        @Parameter(in = ParameterIn.QUERY, name = "configType",
                description = "Case-insensitive SQL `LIKE` pattern for the configuration type. Empty returns every type.",
                example = "running")
        @QueryParam("configType") @DefaultValue("") String configType);

    /**
     * Delete multiple device configs.
     * @param ids ids to delete, one 'id' query parameter each
     */
    @DELETE
    @Operation(
            summary = "Delete device configs",
            description = """
        Delete several device config records, given as repeated `id` parameters
        (`?id=42&id=43`); a comma-separated list is not accepted. If any id does not exist, nothing is
        deleted.""",
            operationId = "DeviceConfigRestServiceDELETEDeviceConfigs")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The device configs were deleted. " + NO_BODY_DESCRIPTION),
            @ApiResponse(responseCode = "400", description = "No `id` was given.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid 'id' parameter"))),
            @ApiResponse(responseCode = "404", description = "At least one id does not exist. Nothing was deleted.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid 'id' parameter")))
    })
    Response deleteDeviceConfigs(
            @Parameter(in = ParameterIn.QUERY, name = "id", required = true,
                    description = "Database id of a device config to delete. Repeat for each record.",
                    array = @ArraySchema(schema = @Schema(type = "integer", format = "int64", example = "42")))
            @QueryParam("id") List<Long> ids);

    /**
     * Delete a single device config.
     * @param id database id of the device config
     */
    @DELETE
    @Path("{id : \\d+}")
    @Operation(
            summary = "Delete a device config",
            description = "Delete one device config record by database id.",
            operationId = "DeviceConfigRestServiceDELETEDeviceConfig")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The device config was deleted. " + NO_BODY_DESCRIPTION),
            @ApiResponse(responseCode = "404", description = "No device config has that id.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid 'id' parameter")))
    })
    Response deleteDeviceConfig(
            @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Database id of the device config.", example = "42")
            @PathParam("id") long id);

    /**
     * Download configurations for the given id or comma-separated list of ids.
     * Single configurations will be returned as a single file.
     * Multiple configurations will be returned inside a .tgz file.
     */
    @GET
    @Path("/download")
    @Operation(
            summary = "Download device configs",
            description = """
        Download the stored configuration of one or more records as a file attachment.

        A single id returns the configuration as `text/plain`, with the record's encoding as the
        charset, in a file named `<device>-<ip>-<configType>-<timestamp>.cfg` (dots in the IP address
        become underscores). Several ids return a gzipped tar archive named
        `device-configs-<timestamp>.tar.gz` holding one such file per record; ids that do not exist or
        hold no configuration are skipped.""",
            operationId = "DeviceConfigRestServiceGETDownloadDeviceConfigs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The configuration file, or an archive of several.",
                    headers = @Header(name = "Content-Disposition", description = "`attachment` with the file name.",
                            schema = @Schema(type = "string", example = "attachment; filename=core-router-01-192_168_1_1-running-20260826-025508.cfg")),
                    content = {
                            @Content(mediaType = MediaType.TEXT_PLAIN,
                                    schema = @Schema(type = "string", format = "binary"),
                                    examples = @ExampleObject(value = "hostname core-router-01\ninterface GigabitEthernet0/0\n")),
                            @Content(mediaType = "application/gzip",
                                    schema = @Schema(type = "string", format = "binary"))
                    }),
            @ApiResponse(responseCode = "204", description = "No `id` was given, or none of the records exists or holds a configuration. " + NO_BODY_DESCRIPTION),
            @ApiResponse(responseCode = "400", description = "`id` is not a comma-separated list of numbers.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid 'id' parameter"))),
            @ApiResponse(responseCode = "500", description = """
                    The archive could not be built. This also happens when a comma in `id` is followed by a \
                    space, and when two records give the same file name, for example when an id is given twice.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \" 43\"")))
    })
    Response downloadDeviceConfig(
            @Parameter(in = ParameterIn.QUERY, name = "id", required = true,
                    description = "Database id of a device config, or a comma-separated list of ids with no spaces. Give each id once.",
                    example = "42,43")
            @QueryParam("id") @DefaultValue("") String id);

    @POST
    @Path("/backup")
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Trigger device config backups",
            description = """
        Retrieve the configuration of one or more devices now, outside their schedule. Each request
        names the IP address, monitoring location and `DeviceConfig` service bound to that interface.

        With `blocking` false (the default) the call returns once each backup has been dispatched, and
        reports only requests that could not be dispatched; the outcome of the retrieval appears later
        in the device config records. With `blocking` true it waits for each retrieval in turn and
        also reports retrievals that failed.

        A request that fails on its own is answered with that request's status and message. When
        several requests were sent and any failed, the answer is 207 listing the failed requests only.""",
            operationId = "DeviceConfigRestServicePOSTBackup")
    @RequestBody(required = true, description = "The backups to trigger.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(implementation = BackupRequestDTO.class)),
                    examples = @ExampleObject(value = """
                    [
                      {"ipAddress": "192.168.1.1", "location": "Default", "serviceName": "DeviceConfig-running", "blocking": false},
                      {"ipAddress": "10.0.0.5", "location": "Branch-Office", "serviceName": "DeviceConfig"}
                    ]""")))
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Every backup was dispatched, or with `blocking`, completed. " + NO_BODY_DESCRIPTION),
            @ApiResponse(responseCode = "207", description = "Several requests were sent and at least one failed. Lists the failed requests only.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = BackupResponseDTO.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "status": 400,
                        "failureMessage": "No interface found with ipAddress 10.0.0.5 at location Branch-Office",
                        "request": {"ipAddress": "10.0.0.5", "location": "Branch-Office", "serviceName": "DeviceConfig", "blocking": false}
                      }
                    ]"""))),
            @ApiResponse(responseCode = "400", description = """
                    The body was empty, or a single request named an invalid IP address, an interface that \
                    does not exist at the location, or a service not bound to the interface.""",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service DeviceConfig-running not bound to interface with ipAddress 192.168.1.1 at location Default"))),
            @ApiResponse(responseCode = "500", description = "A single request could not be dispatched, or with `blocking`, its retrieval failed.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Connection refused")))
    })
    Response triggerDeviceConfigBackup(List<BackupRequestDTO> backupRequestDtoList);
}
