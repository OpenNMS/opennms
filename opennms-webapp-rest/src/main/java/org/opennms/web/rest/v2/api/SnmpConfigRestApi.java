/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements. See the LICENSE.md file
 * distributed with this work for additional information.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License"); you may not use
 * this file except in compliance with the License.
 * https://www.gnu.org/licenses/agpl-3.0.txt
 */
package org.opennms.web.rest.v2.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("snmp-config")
@Tag(name = "SnmpConfig", description = """
        SNMP Configuration API.

        Reads and edits `snmp-config.xml`: the top-level defaults, the `<definition>` blocks that override
        them per address, range or IP-match expression, and the labelled `<profile>` entries provisioning
        can try against an unknown agent.

        Bodies here are the JAXB config beans, and they serialize through the codehaus Jackson provider,
        so JSON uses the `@JsonProperty` names (`readCommunity`, `maxVarsPerPdu`) rather than the
        hyphenated XML attribute names. On `SnmpProfile` the XML `<filter>` element is `filter` in JSON,
        not `filterExpression`; an unrecognised field fails with a 500 from the parser rather than a 400.

        Validation failures return a `text/plain` message.""")
public interface SnmpConfigRestApi {

    String DEFINITION_JSON_EXAMPLE = """
            {
              "specific": [ "192.0.2.10" ],
              "range": [ { "begin": "192.0.2.32", "end": "192.0.2.63" } ],
              "ipMatch": [],
              "location": "Default",
              "version": "v2c",
              "readCommunity": "public",
              "port": 161,
              "timeout": 1800,
              "retry": 1,
              "maxVarsPerPdu": 10,
              "maxRepetitions": 2,
              "maxRequestSize": 65535
            }""";

    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get SNMP configuration",
            description = """
        Return the whole SNMP configuration: the top-level defaults, every `<definition>`, and the
        profiles. Fields the config does not set are present and null rather than absent.""",
            operationId = "getSnmpConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpConfig.class),
                            examples = @ExampleObject(value = """
                    {
                      "maxVarsPerPdu": 10,
                      "maxRepetitions": 2,
                      "maxRequestSize": 65535,
                      "version": "v2c",
                      "readCommunity": "public",
                      "writeCommunity": null,
                      "timeout": 1800,
                      "retry": 1,
                      "port": 161,
                      "ttl": null,
                      "encrypted": false,
                      "definition": [
                        {
                          "maxVarsPerPdu": 10,
                          "maxRepetitions": 2,
                          "maxRequestSize": 65535,
                          "timeout": 0,
                          "retry": 0,
                          "port": 1161,
                          "encrypted": false,
                          "range": [],
                          "specific": [ "127.0.0.1" ],
                          "ipMatch": [],
                          "location": null,
                          "profileLabel": null
                        }
                      ],
                      "profiles": { "profile": [] }
                    }"""))),
            @ApiResponse(responseCode = "500", description = "The stored configuration could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error retrieving SNMP config.")))
    })
    Response getSnmpConfig();

    @GET
    @Path("/lookup")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Lookup SNMP configuration",
            description = """
        Resolve the effective agent configuration for one address, which is what the poller and collector
        would use: the top-level defaults with the first matching `<definition>` applied on top.
        A definition that carries an explicit `timeout` or `retry` of 0 supplies that 0 rather than
        falling back to the top-level value, so a 0 here usually means a definition set it.""",
            operationId = "getConfigForIp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration for the given item retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpAgentConfig.class),
                            examples = @ExampleObject(value = """
                    {
                      "address": "127.0.0.1",
                      "proxyFor": null,
                      "version3": false,
                      "port": 1161,
                      "version": 2,
                      "contextName": null,
                      "timeout": 1800,
                      "retries": 1,
                      "authPassPhrase": null,
                      "privPassPhrase": null,
                      "securityLevel": 1,
                      "authProtocol": null,
                      "privProtocol": null,
                      "engineId": null,
                      "contextEngineId": null,
                      "enterpriseId": null,
                      "maxRequestSize": 65535,
                      "maxVarsPerPdu": 10,
                      "maxRepetitions": 2,
                      "ttl": null,
                      "securityName": "opennmsUser",
                      "readCommunity": "public",
                      "writeCommunity": "private",
                      "versionAsString": "v2c"
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`ipAddress` was missing or unparseable, or `location` named a monitoring location that does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Missing or invalid 'ipAddress'."))),
            @ApiResponse(responseCode = "500", description = "The lookup failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error looking up SNMP config.")))
    })
    Response getConfigForIp(
            @Parameter(description = "IPv4 or IPv6 address to resolve. Required.", required = true, example = "127.0.0.1")
            @QueryParam("ipAddress") String ipAddress,
            @Parameter(description = "Monitoring location name. Omitted, empty or `Default` all mean the default location.", example = "Default")
            @QueryParam("location") String location);

    @POST
    @Path("/defaults")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Save SNMP default override configuration",
            description = """
        Replace the top-level defaults in `snmp-config.xml`. The body is a whole `Configuration`, not a
        patch, and the saved document is normalised: attributes that were previously absent may be
        written out explicitly with their default values. Existing definitions and profiles are left
        alone. The body is validated against `snmp-config.xsd` before it is saved.""",
            operationId = "saveDefaultOverrides"
    )
    @RequestBody(
            required = true,
            description = "Replacement top-level defaults.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Configuration.class),
                    examples = @ExampleObject(value = """
                    {
                      "version": "v2c",
                      "readCommunity": "public",
                      "timeout": 1800,
                      "retry": 1,
                      "maxVarsPerPdu": 10,
                      "maxRepetitions": 2,
                      "maxRequestSize": 65535
                    }"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration default overrides saved successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "Missing request body.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Missing or invalid request body."))),
            @ApiResponse(responseCode = "500", description = "Schema validation or the save itself failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error saving SNMP default overrides, failed schema validation.")))
    })
    Response saveDefaultOverrides(Configuration config);

    @PUT
    @Path("/definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add an SNMP configuration definition",
            description = """
        Add a `<definition>` to `snmp-config.xml`. At least one of `specific`, `range` or `ipMatch` has
        to be present, and `ipMatch` cannot be combined with either of the other two. Every address and
        range is parsed before the save, and `location` has to name an existing monitoring location
        (omitted, empty or `Default` all mean the default location).

        The factory merges the new entry into an existing definition when the two carry identical
        attributes, and drops attributes that repeat the top-level defaults, so the saved XML may not
        look field-for-field like the body that was sent. The `Location` header on the 201 points at
        `/snmp-config` rather than at the new definition, which has no address of its own.""",
            operationId = "addDefinition"
    )
    @RequestBody(
            required = true,
            description = "Definition to add.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Definition.class),
                    examples = @ExampleObject(value = DEFINITION_JSON_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SNMP configuration definition added successfully. No response body; `Location` is `/snmp-config`."),
            @ApiResponse(responseCode = "400", description = "Missing body, no specific/range/ipMatch, `ipMatch` mixed with ranges or specifics, an unparseable address, or an unknown location.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Definition must have at least one specific IP, IP range or IP match specified."))),
            @ApiResponse(responseCode = "500", description = "Schema validation or the save itself failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error saving SNMP definition, failed schema validation.")))
    })
    Response addDefinition(Definition definition);

    @DELETE
    @Path("/definition")
    @Operation(
            summary = "Delete an SNMP configuration definition",
            description = """
        Remove specific addresses, ranges or IP-match expressions from the definitions at one location.
        Each parameter takes a comma-separated list; ranges use `begin-end`. A request that matches
        nothing is reported as a 400 rather than as a no-op success.""",
            operationId = "removeDefinition"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration definition for the given item removed successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "No items supplied, an unparseable address or range, an unknown location, or nothing matched.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No configuration items removed, mostly likely no matching definitions found."))),
            @ApiResponse(responseCode = "500", description = "The removal failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error removing SNMP definition.")))
    })
    Response removeDefinition(
            @Parameter(description = "Comma-separated specific IP addresses to remove.", example = "192.0.2.10,192.0.2.11")
            @QueryParam("specifics") String specifics,
            @Parameter(description = "Comma-separated ranges to remove, each written `begin-end`.", example = "192.0.2.32-192.0.2.63")
            @QueryParam("ranges") String ranges,
            @Parameter(description = "Comma-separated IP-match expressions to remove.", example = "192.0.2.*")
            @QueryParam("ipmatches") String ipMatches,
            @Parameter(description = "Monitoring location the definitions belong to. Omitted, empty or `Default` all mean the default location.", example = "Default")
            @QueryParam("location") String location);

    @POST
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add or update an SNMP configuration profile",
            description = """
        Add a profile, or replace the existing profile that carries the same `label`. `label` is
        required. The XML `<filter>` element is `filter` in JSON: sending `filterExpression` fails with a
        500 from the parser, as does any other field the bean does not declare. A body of `null` also
        fails with a 500.""",
            operationId = "saveProfile"
    )
    @RequestBody(
            required = true,
            description = "Profile to add or replace.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SnmpProfile.class),
                    examples = @ExampleObject(value = """
                    {
                      "label": "edge-switches",
                      "filter": "iphostname LIKE '%edge%'",
                      "version": "v2c",
                      "readCommunity": "public",
                      "port": 161,
                      "timeout": 1800,
                      "retry": 1
                    }"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration profile added or updated successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "`label` was missing or empty.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Missing or invalid 'label'."))),
            @ApiResponse(responseCode = "500", description = "A missing body, or a body carrying a field the bean does not declare.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unrecognized field \"filterExpression\" (Class org.opennms.netmgt.config.snmp.SnmpProfile), not marked as ignorable")))
    })
    Response saveProfile(SnmpProfile profile);

    @DELETE
    @Path("/profile")
    @Operation(
            summary = "Delete an SNMP configuration profile",
            description = "Delete the SNMP configuration profile carrying the given label.",
            operationId = "removeProfile"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "SNMP configuration profile with the given label removed successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "`label` was missing or empty.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Missing or invalid 'label'."))),
            @ApiResponse(responseCode = "404", description = "No profile carries that label.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Profile with label edge-switches not found.")))
    })
    Response removeProfile(
            @Parameter(description = "Label of the profile to delete. Required.", required = true, example = "edge-switches")
            @QueryParam("label") final String label);

    @GET
    @Path("/download")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Download SNMP configuration",
            description = """
        Download the SNMP configuration as an attachment (`snmp-config.json` or `snmp-config.xml`),
        for re-upload through `/snmp-config/upload` or `/snmp-config/upload/xml`.
        `format=xml` selects XML; every other value, including an unrecognised one, yields JSON.

        Only the XML form round-trips faithfully. The JSON form materialises inherited numeric fields as
        literal `0`, so re-uploading it through `/snmp-config/upload` pins each definition's `timeout`
        and `retry` to 0 instead of leaving them to inherit the top-level defaults. Prefer XML when the
        download is meant to be restored.""",
            operationId = "downloadConfig"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration in download format retrieved successfully",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = SnmpConfig.class)),
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(type = "string"),
                                examples = @ExampleObject(value = """
                    <snmp-config xmlns="http://xmlns.opennms.org/xsd/config/snmp" max-vars-per-pdu="10" max-repetitions="2" max-request-size="65535" version="v2c" read-community="public" timeout="1800" retry="1">
                       <definition max-vars-per-pdu="10" max-repetitions="2" max-request-size="65535" port="1161">
                          <specific>127.0.0.1</specific>
                       </definition>
                    </snmp-config>"""))
                    }),
            @ApiResponse(responseCode = "500", description = "The stored configuration could not be serialized.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error retrieving SNMP config.")))
    })
    Response downloadConfig(
            @Parameter(description = "`xml` for XML. Any other value, including an unrecognised one, yields JSON.",
                    example = "xml",
                    schema = @Schema(allowableValues = {"json", "xml"}))
            @QueryParam("format") String format);

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload SNMP configuration in Json format.",
            description = """
        Replace the whole SNMP configuration from a JSON document and apply the changes. The single part
        must be named `upload`. The document is converted to XML and validated against
        `snmp-config.xsd` before it is applied.

        A document produced by `GET /snmp-config/download` is not a faithful restore: its inherited
        numeric fields are serialized as literal `0`, and uploading them pins each definition's
        `timeout` and `retry` to 0. Use `/snmp-config/upload/xml` with the XML download to restore a
        configuration unchanged.""",
            operationId = "uploadConfig"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with one `upload` part holding the JSON SNMP configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration uploaded and applied successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "Unreadable, unparseable or schema-invalid document. A request with no `upload` part is rejected before the handler runs and has no body.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid configuration file."))),
            @ApiResponse(responseCode = "500", description = "The document parsed but could not be applied.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Could not save updated config")))
    })
    Response uploadConfig(@Multipart("upload") Attachment attachment);

    @POST
    @Path("/upload/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload SNMP configuration in XML format",
            description = """
        Replace the whole SNMP configuration from a `<snmp-config>` document and apply the changes. The
        single part must be named `upload`. The document is validated against `snmp-config.xsd` before
        it is applied. This is the path that restores a `GET /snmp-config/download?format=xml` body
        byte-for-byte.""",
            operationId = "uploadConfigXml"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with one `upload` part holding the XML SNMP configuration.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP configuration uploaded and applied successfully. No response body."),
            @ApiResponse(responseCode = "400", description = "Unreadable, unparseable or schema-invalid document. A request with no `upload` part is rejected before the handler runs and has no body.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid configuration file."))),
            @ApiResponse(responseCode = "500", description = "The document parsed but could not be applied.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Could not save updated config")))
    })
    Response uploadConfigXml(@Multipart("upload") Attachment attachment);
}
