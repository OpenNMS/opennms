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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.web.rest.v2.model.TrapdConfigDto;

@Path("trapd")
@Tag(name = "Trapd", description = """
        Trapd API V2.

        Reads and replaces the whole trapd configuration. There is no partial update: `PUT /trapd/config`
        and both upload endpoints replace the stored configuration with the body they are given, so send a
        complete document rather than the fields you want to change. `snmpTrapPort` and `newSuspectOnTrap`
        must be present; the remaining numeric fields fall back to the defaults in
        `trapd-configuration.xsd` when omitted.

        SNMPv3 passphrases are masked as `******` by `GET /trapd/config`. Sending a masked value back on a
        user that already has an `id` keeps the stored passphrase, which makes read-modify-write safe
        without handling cleartext. Sending a masked value for a user id that does not already exist is
        rejected with a 400. The two download endpoints return passphrases in cleartext, not masked.""")
public interface TrapdRestApi {

    String TRAPD_CONFIG_JSON_EXAMPLE = """
            {
              "snmpTrapAddress": "*",
              "snmpTrapPort": 10162,
              "newSuspectOnTrap": false,
              "includeRawMessage": false,
              "threads": 0,
              "queueSize": 10000,
              "batchSize": 1000,
              "batchInterval": 500,
              "useAddressFromVarbind": false,
              "snmpv3User": [
                {
                  "id": "b0019905-75f8-4856-8c0b-84381e9485a3",
                  "engineId": "0x0102030405",
                  "securityName": "trapUser",
                  "securityLevel": 3,
                  "authProtocol": "SHA-256",
                  "authPassphrase": "******",
                  "privacyProtocol": "AES256",
                  "privacyPassphrase": "******"
                }
              ]
            }""";

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload Trap configuration in JSON format.",
            description = """
        Upload Trap configuration in JSON format and apply the changes. Requires ROLE_ADMIN.
        The single part must be named `upload` and hold a JSON document with the same shape as
        `GET /trapd/config`. The uploaded document replaces the stored configuration in full, and a
        successful response carries no body.""",
            operationId = "uploadTrapdConfiguration"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with one `upload` part holding the JSON trapd configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trap configuration uploaded and applied successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable JSON, a failed validation rule, or a masked passphrase on a user id that does not exist. A request with no `upload` part is rejected before the handler runs and has no body.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(type = "string"),
                        examples = @ExampleObject(value = "snmpTrapPort is required and must be between 1 and 65535."))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(type = "string"),
                        examples = @ExampleObject(value = "Admin role required to upload Trapd configuration."))),
            @ApiResponse(responseCode = "500", description = "Failed to persist Trap configuration.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(type = "string"),
                        examples = @ExampleObject(value = "Failed to persist Trapd configuration.")))
    })
    Response uploadTrapdConfiguration(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);

    @POST
    @Path("upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Upload Trap configuration in XML format.",
            description = """
        Upload Trap configuration in XML format and apply the changes. Requires ROLE_ADMIN.
        The single part must be named `upload` and hold a `<trapd-configuration>` document, the same form
        that `GET /trapd/download?format=xml` produces. The uploaded document replaces the stored
        configuration in full, and a successful response carries no body.""",
            operationId = "uploadTrapdConfigurationXml"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with one `upload` part holding the XML trapd configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trap configuration uploaded and applied successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "Unparseable or schema-invalid XML, or a failed validation rule. A request with no `upload` part is rejected before the handler runs and has no body.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(type = "string"),
                        examples = @ExampleObject(value = "Invalid Trapd XML configuration."))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(type = "string"),
                        examples = @ExampleObject(value = "Admin role required to upload Trapd configuration."))),
            @ApiResponse(responseCode = "500", description = "Failed to persist Trap configuration.",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(type = "string"),
                        examples = @ExampleObject(value = "Failed to persist Trapd configuration.")))
    })
    Response uploadTrapdConfigurationXml(@Multipart("upload") Attachment attachment, @Context SecurityContext securityContext);

    @GET
    @Path("download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download trapd configuration",
            description = """
        Download trapd configuration in JSON or XML format. Requires ROLE_ADMIN.
        The response is served as an attachment (`trapd-config.json` or `trapd-config.xml`) and is
        accepted back by the matching upload endpoint. SNMPv3 passphrases appear in cleartext here.""",
            operationId = "downloadTrapdConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trapd configuration in download format retrieved successfully",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = TrapdConfigDto.class),
                                examples = @ExampleObject(value = """
                    {
                      "snmpTrapAddress" : "*",
                      "snmpTrapPort" : 10162,
                      "newSuspectOnTrap" : false,
                      "includeRawMessage" : false,
                      "threads" : 0,
                      "queueSize" : 10000,
                      "batchSize" : 1000,
                      "batchInterval" : 500,
                      "useAddressFromVarbind" : false,
                      "snmpv3User" : [ ]
                    }""")),
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(value = """
                    <trapd-configuration xmlns="http://xmlns.opennms.org/xsd/config/trapd" snmp-trap-address="*" snmp-trap-port="10162" new-suspect-on-trap="false" include-raw-message="false" threads="0" queue-size="10000" batch-size="1000" batch-interval="500" use-address-from-varbind="false"/>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "`format` was neither `json` nor `xml`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid format parameter. Supported values are 'json' and 'xml'."))),
            @ApiResponse(responseCode = "403", description = "Admin role required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Admin role required to download Trapd configuration."))),
            @ApiResponse(responseCode = "404", description = "No trapd configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Trapd configuration not found."))),
            @ApiResponse(responseCode = "500", description = "The stored configuration could not be serialized.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error retrieving Trapd config.")))
    })
    Response downloadTrapdConfig(
            @Parameter(description = "Serialization format. Anything other than `json` or `xml` is rejected. Defaults to `json` when omitted.",
                    example = "xml",
                    schema = @Schema(allowableValues = {"json", "xml"}))
            @QueryParam("format") String format,
            @Context SecurityContext securityContext);

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get trapd configuration",
            description = """
        Retrieve the current trapd configuration. SNMPv3 passphrases are returned masked as `******`;
        send those masked values straight back to `PUT /trapd/config` to keep the stored credentials.""",
            operationId = "getTrapdConfiguration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TrapdConfigDto.class),
                            examples = @ExampleObject(value = TRAPD_CONFIG_JSON_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "No trapd configuration is stored.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Trapd configuration not found."))),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve trapd configuration",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to retrieve Trapd configuration.")))
    })
    Response getTrapdConfiguration(@Context SecurityContext securityContext);
    
    @PUT
    @Path("config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update trapd configuration",
            description = """
        Replace the trapd configuration with the supplied JSON document. The body is a whole
        configuration, not a patch: omitted fields fall back to the XSD defaults rather than keeping
        their current values. `snmpTrapPort` and `newSuspectOnTrap` are required.
        A field the DTO does not declare fails with a 500 from the JSON parser rather than a 400.
        A successful response carries no body.""",
            operationId = "updateTrapdConfiguration"
    )
    @RequestBody(
            required = true,
            description = "Complete trapd configuration.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TrapdConfigDto.class),
                    examples = @ExampleObject(value = TRAPD_CONFIG_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing body, a failed validation rule, a duplicate SNMPv3 user id, or a masked passphrase on a user id that does not exist.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid SNMPv3 user at index 0: authProtocol and authPassphrase must be provided together."))),
            @ApiResponse(responseCode = "500", description = "Failed to update trapd configuration, including a body carrying a field the DTO does not declare.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to persist Trapd configuration.")))
    })
    Response updateTrapdConfiguration(TrapdConfigDto payload, @Context SecurityContext securityContext);
}
