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
package org.opennms.web.rest.v1.config;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapMappingGroup;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapNorthbounderConfigDao;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapSink;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.OnmsRestService;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The Class SnmpTrapNorthbounderConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class SnmpTrapNorthbounderConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The SNMP Trap Northbounder configuration DAO. */
    @Resource(name="snmpTrapNorthbounderConfigDao")
    private SnmpTrapNorthbounderConfigDao m_snmpTrapNorthbounderConfigDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class SnmpTrapSinkList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="trap-sinks")
    public static class SnmpTrapSinkList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new SNMP trap sink list.
         */
        public SnmpTrapSinkList() {}

        /**
         * Instantiates a new SNMP trap sink list.
         *
         * @param trapSinks the trap sinks
         */
        public SnmpTrapSinkList(List<SnmpTrapSink> trapSinks) {
            trapSinks.forEach(d -> add(d.getName()));
        }

        /**
         * Gets the trap sinks.
         *
         * @return the trap sinks
         */
        @XmlElement(name="trap-sink")
        public List<String> getTrapSinks() {
            return getObjects();
        }
    }

    /**
     * The Class ImportMappings.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="import-mappings")
    public static class ImportMappings extends JaxbListWrapper<String> {

        /**
         * Instantiates a new import mappings.
         */
        public ImportMappings() {}

        /**
         * Instantiates a new import mappings.
         *
         * @param mappings the mappings
         */
        public ImportMappings(List<String> mappings) {
            addAll(mappings);
        }

        /**
         * Gets the import mappings.
         *
         * @return the import mappings
         */
        @XmlElement(name="import-mapping")
        public List<String> getImportMappings() {
            return getObjects();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_snmpTrapNorthbounderConfigDao, "snmpTrapNorthbounderConfigDao must be set!");
        Assert.notNull(m_eventProxy, "eventProxy must be set!");
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the SNMP trap northbounder configuration",
            description = """
                    Returns the whole of snmptrap-northbounder-configuration.xml. Each `snmp-trap-sink` holds
                    its inline `mapping-group` entries plus an `import-mappings` list of paths to mapping groups
                    kept in separate files under the configuration directory.

                    The SNMP credentials are taken at send time from snmp-config.xml for the sink's
                    `ip-address`; only `port` and `version` are overridable on the sink, and the version has to
                    agree with what snmp-config.xml declares for that address.

                    `version` and `varbind[].type` are JAXB enums; only XML honours their `@XmlEnumValue`
                    spelling. XML spells them `v2c` and `Int32`, JSON the constant names `V2c` and
                    `TYPE_SNMP_INT32`; the wrong spelling for the media type produces a 500. The versions are `V1`, `V2c`, `V3`, `V2_INFORM` and `V3_INFORM`
                    (`v1`, `v2c`, `v3`, `v2-inform`, `v3-inform` in XML); the varbind types are
                    `TYPE_SNMP_OCTET_STRING`, `TYPE_SNMP_INT32`, `TYPE_SNMP_NULL`,
                    `TYPE_SNMP_OBJECT_IDENTIFIER`, `TYPE_SNMP_IPADDRESS`, `TYPE_SNMP_TIMETICKS`,
                    `TYPE_SNMP_COUNTER32`, `TYPE_SNMP_GAUGE32`, `TYPE_SNMP_OPAQUE` and `TYPE_SNMP_COUNTER64`
                    (`OctetString`, `Int32`, and so on in XML).""",
            operationId = "getSnmpTrapNorthbounderConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current SNMP trap northbounder configuration. The example has the mapping groups elided.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SnmpTrapNorthbounderConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "enabled": false,
                                              "nagles-delay": 1000,
                                              "batch-size": 100,
                                              "queue-size": 300000,
                                              "snmp-trap-sink": [
                                                {
                                                  "name": "localTest1",
                                                  "ip-address": "127.0.0.1",
                                                  "version": "V2c",
                                                  "mapping-group": [],
                                                  "import-mappings": []
                                                }
                                              ]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SnmpTrapNorthbounderConfig.class))
                    })
    })
    public Response getConfiguration() {
        return Response.ok(m_snmpTrapNorthbounderConfigDao.getConfig()).build();
    }

    /**
     * Sets the configuration.
     *
     * @param config the full configuration object
     * @return the response
     */
    @POST
    @Operation(
            summary = "Replace the SNMP trap northbounder configuration",
            description = """
                    Marshals the request body straight over snmptrap-northbounder-configuration.xml, then sends
                    a `reloadDaemonConfig` event for `SnmpTrapNBI`. The whole file is replaced, so anything
                    absent from the body is dropped, including comments. Mapping-group files referenced through
                    `import-mappings` are left on disk but become orphaned if the body drops the reference.

                    The handler declares no `@Consumes`, so the media type is whatever the JAXB and Jackson
                    providers accept for the body type. A body that fails to parse surfaces as a 500 rather
                    than a 400: the null check that would produce the 400 is unreachable through those
                    providers.

                    `version` and `varbind[].type` are JAXB enums; only XML honours their `@XmlEnumValue`
                    spelling. XML spells them `v2c` and `Int32`, JSON the constant names `V2c` and
                    `TYPE_SNMP_INT32`; the wrong spelling for the media type produces a 500. The versions are `V1`, `V2c`, `V3`, `V2_INFORM` and `V3_INFORM`
                    (`v1`, `v2c`, `v3`, `v2-inform`, `v3-inform` in XML); the varbind types are
                    `TYPE_SNMP_OCTET_STRING`, `TYPE_SNMP_INT32`, `TYPE_SNMP_NULL`,
                    `TYPE_SNMP_OBJECT_IDENTIFIER`, `TYPE_SNMP_IPADDRESS`, `TYPE_SNMP_TIMETICKS`,
                    `TYPE_SNMP_COUNTER32`, `TYPE_SNMP_GAUGE32`, `TYPE_SNMP_OPAQUE` and `TYPE_SNMP_COUNTER64`
                    (`OctetString`, `Int32`, and so on in XML).""",
            operationId = "setSnmpTrapNorthbounderConfiguration")
    @RequestBody(required = true, description = "The complete replacement configuration.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpTrapNorthbounderConfig.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "enabled": false,
                                              "nagles-delay": 1000,
                                              "batch-size": 100,
                                              "queue-size": 300000,
                                              "snmp-trap-sink": [
                                                {
                                                  "name": "localTest1",
                                                  "ip-address": "127.0.0.1",
                                                  "version": "V2c",
                                                  "mapping-group": [],
                                                  "import-mappings": []
                                                }
                                              ]
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SnmpTrapNorthbounderConfig.class))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The file was written and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setConfiguration(final SnmpTrapNorthbounderConfig config) {
        writeLock();
        try {
            if (config == null) {
                throw getException(Status.BAD_REQUEST, "SNMP NBI configuration object cannot be null");
            }
            try {
                File configFile = m_snmpTrapNorthbounderConfigDao.getConfigResource().getFile();
                JaxbUtils.marshal(config, new FileWriter(configFile));
                notifyDaemons();
            } catch (Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Get whether the SNMP trap northbounder is enabled",
            description = """
                    Returns the `enabled` flag from snmptrap-northbounder-configuration.xml as the literal text
                    `true` or `false`. An absent flag reads back as `false`.

                    This operation produces text/plain only, so a request sent with
                    `Accept: application/json` is rejected with a 406.""",
            operationId = "getSnmpTrapNorthbounderStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current enabled flag.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "boolean"),
                            examples = @ExampleObject(value = "false")))
    })
    public Response getStatus() {
        return Response.ok(m_snmpTrapNorthbounderConfigDao.getConfig().isEnabled()).build();
    }

    /**
     * Gets the status.
     *
     * @param enabled the enabled
     * @return the status
     * @throws WebApplicationException the web application exception
     */
    @PUT
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Enable or disable the SNMP trap northbounder",
            description = """
                    Sets the `enabled` flag, rewrites snmptrap-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `SnmpTrapNBI`. Comments and formatting in the file are lost,
                    because the whole file is re-marshalled from the in-memory model.

                    Omitting `enabled` clears the flag rather than leaving it alone, which then reads back as
                    `false`. Despite the text/plain declaration the success response has no body.""",
            operationId = "setSnmpTrapNorthbounderStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The flag was written and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response getStatus(@Parameter(description = "New value for the enabled flag. Omitting it clears the flag, which reads back as false.", example = "true") @QueryParam("enabled") final Boolean enabled) throws WebApplicationException {
        writeLock();
        try {
            m_snmpTrapNorthbounderConfigDao.getConfig().setEnabled(enabled);
            return saveConfiguration();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets all the SNMP trap sinks.
     *
     * @return the SNMP trap sinks
     */
    @GET
    @Path("trapsinks")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the SNMP trap sink names",
            description = """
                    Returns only the trap sink names, not the sinks themselves.

                    `count` and `totalCount` are always equal here: the listing is not paged and `offset` is
                    always 0.""",
            operationId = "getSnmpTrapSinks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The configured trap sink names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SnmpTrapSinkList.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 1,
                                              "count": 1,
                                              "offset": 0,
                                              "trap-sink": ["localTest1"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SnmpTrapSinkList.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <trap-sinks count="1" offset="0" totalCount="1">
                                              <trap-sink>localTest1</trap-sink>
                                            </trap-sinks>"""))
                    })
    })
    public Response getSnmpTrapSinks() {
        SnmpTrapSinkList trapSinks = new SnmpTrapSinkList(m_snmpTrapNorthbounderConfigDao.getConfig().getSnmpTrapSinks());
        return Response.ok(trapSinks).build();
    }

    /**
     * Gets the SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @return the SNMP trap sink
     */
    @GET
    @Path("trapsinks/{trapsinkName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one SNMP trap sink",
            description = """
                    Returns the sink with its inline mapping groups. `import-mappings` lists the paths of
                    mapping groups held in separate files; their contents are not inlined here.

                    `version` and `varbind[].type` are JAXB enums; only XML honours their `@XmlEnumValue`
                    spelling. XML spells them `v2c` and `Int32`, JSON the constant names `V2c` and
                    `TYPE_SNMP_INT32`; the wrong spelling for the media type produces a 500. The versions are `V1`, `V2c`, `V3`, `V2_INFORM` and `V3_INFORM`
                    (`v1`, `v2c`, `v3`, `v2-inform`, `v3-inform` in XML); the varbind types are
                    `TYPE_SNMP_OCTET_STRING`, `TYPE_SNMP_INT32`, `TYPE_SNMP_NULL`,
                    `TYPE_SNMP_OBJECT_IDENTIFIER`, `TYPE_SNMP_IPADDRESS`, `TYPE_SNMP_TIMETICKS`,
                    `TYPE_SNMP_COUNTER32`, `TYPE_SNMP_GAUGE32`, `TYPE_SNMP_OPAQUE` and `TYPE_SNMP_COUNTER64`
                    (`OctetString`, `Int32`, and so on in XML).

                    `mapping-group[].rule`, `mapping[].rule`, `varbind[].value` and `varbind[].instance` are SpEL
                    expressions evaluated against the `NorthboundAlarm` being forwarded, so `parameters['x']`
                    and `eventParametersCollection[0].value` reach the alarm's event parameters. Nothing
                    validates the expressions at write time.""",
            operationId = "getSnmpTrapSink")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The trap sink.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SnmpTrapSink.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "name": "ApiDocTrapSink",
                                              "ip-address": "127.0.0.1",
                                              "port": 1162,
                                              "version": "V2c",
                                              "community": "public",
                                              "mapping-group": [
                                                {
                                                  "name": "ApiDoc Mappings",
                                                  "rule": "foreignSource matches '^ApiDoc.*'",
                                                  "mapping": [
                                                    {
                                                      "name": "apidoctrap",
                                                      "rule": "uei == 'uei.opennms.org/trap/apiDoc'",
                                                      "enterprise-oid": ".1.3.6.1.4.1.5813.99.1",
                                                      "specific": 1,
                                                      "varbind": [
                                                        {
                                                          "oid": ".1.3.6.1.4.1.5813.99.1.1",
                                                          "type": "TYPE_SNMP_OCTET_STRING",
                                                          "value": "parameters['alarmMessage']",
                                                          "max": 48
                                                        }
                                                      ]
                                                    }
                                                  ]
                                                }
                                              ],
                                              "import-mappings": []
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SnmpTrapSink.class),
                                    examples = @ExampleObject(value = """
                                            <snmp-trap-sink>
                                              <name>ApiDocTrapSink</name>
                                              <ip-address>127.0.0.1</ip-address>
                                              <port>1162</port>
                                              <version>v2c</version>
                                              <community>public</community>
                                              <mapping-group name="ApiDoc Mappings">
                                                <rule>foreignSource matches '^ApiDoc.*'</rule>
                                                <mapping name="apidoctrap">
                                                  <rule>uei == 'uei.opennms.org/trap/apiDoc'</rule>
                                                  <enterprise-oid>.1.3.6.1.4.1.5813.99.1</enterprise-oid>
                                                  <specific>1</specific>
                                                  <varbind>
                                                    <oid>.1.3.6.1.4.1.5813.99.1.1</oid>
                                                    <type>OctetString</type>
                                                    <value>parameters['alarmMessage']</value>
                                                    <max>48</max>
                                                  </varbind>
                                                </mapping>
                                              </mapping-group>
                                            </snmp-trap-sink>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No trap sink has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Trap sink ApiDocTrapSink was not found.")))
    })
    public SnmpTrapSink getSnmpTrapSink(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName) {
        SnmpTrapSink trapSink = m_snmpTrapNorthbounderConfigDao.getConfig().getSnmpTrapSink(trapSinkName);
        if (trapSink == null) {
            throw getException(Status.NOT_FOUND, "SNMP Trap sink {} was not found.", trapSinkName);
        }
        return trapSink;
    }

    /**
     * Gets the import mappings.
     *
     * @param trapSinkName the trap sink name
     * @return the import mappings
     */
    @GET
    @Path("trapsinks/{trapsinkName}/import-mappings")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List a trap sink's imported mapping-group files",
            description = """
                    Returns the `import-mappings` entries of the sink: the relative paths, under the OpenNMS
                    configuration directory, of the files holding the sink's external mapping groups. The
                    mapping group named `X` lives in `snmptrap-northbounder-mappings.d/X.xml`.

                    `count` and `totalCount` come back as null when the list is empty.""",
            operationId = "getSnmpTrapSinkImportMappings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The imported mapping-group file paths.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = ImportMappings.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 1,
                                              "count": 1,
                                              "offset": 0,
                                              "import-mapping": ["snmptrap-northbounder-mappings.d/ApiDocMappings.xml"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = ImportMappings.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <import-mappings count="1" offset="0" totalCount="1">
                                              <import-mapping>snmptrap-northbounder-mappings.d/ApiDocMappings.xml</import-mapping>
                                            </import-mappings>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No trap sink has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Trap sink ApiDocTrapSink was not found.")))
    })
    public Response getImportMappings(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName) {
        SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
        return Response.ok(new ImportMappings(trapSink.getImportMappings())).build();

    }

    /**
     * Sets a SNMP trap sink.
     * <p>If there is a trap sunk with the same name, the existing one will be overridden.</p>
     *
     * @param snmpTrapSink the SNMP trap sink
     * @return the response
     */
    @POST
    @Path("trapsinks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace an SNMP trap sink",
            description = """
                    Adds the sink to snmptrap-northbounder-configuration.xml, rewriting the whole file and
                    sending a `reloadDaemonConfig` event for `SnmpTrapNBI`. A sink with the same `name` is
                    replaced outright, which is the only way to change its inline mapping groups: the PUT
                    reaches the scalar fields only.

                    `version` and `varbind[].type` are JAXB enums; only XML honours their `@XmlEnumValue`
                    spelling. XML spells them `v2c` and `Int32`, JSON the constant names `V2c` and
                    `TYPE_SNMP_INT32`; the wrong spelling for the media type produces a 500. The versions are `V1`, `V2c`, `V3`, `V2_INFORM` and `V3_INFORM`
                    (`v1`, `v2c`, `v3`, `v2-inform`, `v3-inform` in XML); the varbind types are
                    `TYPE_SNMP_OCTET_STRING`, `TYPE_SNMP_INT32`, `TYPE_SNMP_NULL`,
                    `TYPE_SNMP_OBJECT_IDENTIFIER`, `TYPE_SNMP_IPADDRESS`, `TYPE_SNMP_TIMETICKS`,
                    `TYPE_SNMP_COUNTER32`, `TYPE_SNMP_GAUGE32`, `TYPE_SNMP_OPAQUE` and `TYPE_SNMP_COUNTER64`
                    (`OctetString`, `Int32`, and so on in XML).

                    `mapping-group[].rule`, `mapping[].rule`, `varbind[].value` and `varbind[].instance` are SpEL
                    expressions evaluated against the `NorthboundAlarm` being forwarded, so `parameters['x']`
                    and `eventParametersCollection[0].value` reach the alarm's event parameters. Nothing
                    validates the expressions at write time.

                    An empty or unparseable body surfaces as a 500 rather than the documented 400.""",
            operationId = "setSnmpTrapSink")
    @RequestBody(required = true, description = "The trap sink to add or replace.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpTrapSink.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "name": "ApiDocTrapSink",
                                              "ip-address": "127.0.0.1",
                                              "port": 1162,
                                              "version": "V2c",
                                              "community": "public",
                                              "mapping-group": [
                                                {
                                                  "name": "ApiDoc Mappings",
                                                  "rule": "foreignSource matches '^ApiDoc.*'",
                                                  "mapping": [
                                                    {
                                                      "name": "apidoctrap",
                                                      "rule": "uei == 'uei.opennms.org/trap/apiDoc'",
                                                      "enterprise-oid": ".1.3.6.1.4.1.5813.99.1",
                                                      "specific": 1,
                                                      "varbind": [
                                                        {
                                                          "oid": ".1.3.6.1.4.1.5813.99.1.1",
                                                          "type": "TYPE_SNMP_OCTET_STRING",
                                                          "value": "parameters['alarmMessage']",
                                                          "max": 48
                                                        }
                                                      ]
                                                    }
                                                  ]
                                                }
                                              ],
                                              "import-mappings": []
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SnmpTrapSink.class),
                            examples = @ExampleObject(value = """
                                    <snmp-trap-sink>
                                              <name>ApiDocTrapSink</name>
                                              <ip-address>127.0.0.1</ip-address>
                                              <port>1162</port>
                                              <version>v2c</version>
                                              <community>public</community>
                                              <mapping-group name="ApiDoc Mappings">
                                                <rule>foreignSource matches '^ApiDoc.*'</rule>
                                                <mapping name="apidoctrap">
                                                  <rule>uei == 'uei.opennms.org/trap/apiDoc'</rule>
                                                  <enterprise-oid>.1.3.6.1.4.1.5813.99.1</enterprise-oid>
                                                  <specific>1</specific>
                                                  <varbind>
                                                    <oid>.1.3.6.1.4.1.5813.99.1.1</oid>
                                                    <type>OctetString</type>
                                                    <value>parameters['alarmMessage']</value>
                                                    <max>48</max>
                                                  </varbind>
                                                </mapping>
                                              </mapping-group>
                                            </snmp-trap-sink>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The trap sink was stored and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setSnmpTrapSink(final SnmpTrapSink snmpTrapSink) {
        writeLock();
        try {
            if (snmpTrapSink == null) {
                throw getException(Status.BAD_REQUEST, "SNMP Trap Sink object cannot be null");
            }
            m_snmpTrapNorthbounderConfigDao.getConfig().addSnmpTrapSink(snmpTrapSink);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Sets an import mapping.
     *
     * @param trapSinkName the trap sink name
     * @param mappingGroup the mapping group
     * @return the response
     */
    @POST
    @Path("trapsinks/{trapsinkName}/import-mappings")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace an imported mapping group on a trap sink",
            description = """
                    Writes the mapping group to `snmptrap-northbounder-mappings.d/<name>.xml` under the
                    configuration directory, adds that path to the sink's `import-mappings` if it is not there
                    already, then rewrites snmptrap-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `SnmpTrapNBI`. The file name is derived from the group's
                    `name`, not from anything in the URL, and an existing file of that name is overwritten.

                    `version` and `varbind[].type` are JAXB enums; only XML honours their `@XmlEnumValue`
                    spelling. XML spells them `v2c` and `Int32`, JSON the constant names `V2c` and
                    `TYPE_SNMP_INT32`; the wrong spelling for the media type produces a 500. The versions are `V1`, `V2c`, `V3`, `V2_INFORM` and `V3_INFORM`
                    (`v1`, `v2c`, `v3`, `v2-inform`, `v3-inform` in XML); the varbind types are
                    `TYPE_SNMP_OCTET_STRING`, `TYPE_SNMP_INT32`, `TYPE_SNMP_NULL`,
                    `TYPE_SNMP_OBJECT_IDENTIFIER`, `TYPE_SNMP_IPADDRESS`, `TYPE_SNMP_TIMETICKS`,
                    `TYPE_SNMP_COUNTER32`, `TYPE_SNMP_GAUGE32`, `TYPE_SNMP_OPAQUE` and `TYPE_SNMP_COUNTER64`
                    (`OctetString`, `Int32`, and so on in XML).

                    `mapping-group[].rule`, `mapping[].rule`, `varbind[].value` and `varbind[].instance` are SpEL
                    expressions evaluated against the `NorthboundAlarm` being forwarded, so `parameters['x']`
                    and `eventParametersCollection[0].value` reach the alarm's event parameters. Nothing
                    validates the expressions at write time.""",
            operationId = "setSnmpTrapSinkImportMapping")
    @RequestBody(required = true, description = "The mapping group to store. Its `name` sets the file name.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SnmpTrapMappingGroup.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "name": "ApiDocMappings",
                                              "rule": "foreignSource matches '^ApiDoc.*'",
                                              "mapping": [
                                                {
                                                  "name": "apidocimported",
                                                  "rule": "uei == 'uei.opennms.org/trap/apiDocImported'",
                                                  "enterprise-oid": ".1.3.6.1.4.1.5813.99.2",
                                                  "specific": 2,
                                                  "varbind": [
                                                    {
                                                      "oid": ".1.3.6.1.4.1.5813.99.2.1",
                                                      "type": "TYPE_SNMP_INT32",
                                                      "value": "count"
                                                    }
                                                  ]
                                                }
                                              ]
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SnmpTrapMappingGroup.class),
                            examples = @ExampleObject(value = """
                                    <mapping-group name="ApiDocMappings">
                                              <rule>foreignSource matches '^ApiDoc.*'</rule>
                                              <mapping name="apidocimported">
                                                <rule>uei == 'uei.opennms.org/trap/apiDocImported'</rule>
                                                <enterprise-oid>.1.3.6.1.4.1.5813.99.2</enterprise-oid>
                                                <specific>2</specific>
                                                <varbind>
                                                  <oid>.1.3.6.1.4.1.5813.99.2.1</oid>
                                                  <type>Int32</type>
                                                  <value>count</value>
                                                </varbind>
                                              </mapping>
                                            </mapping-group>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The mapping group file was written and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No trap sink has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Trap sink ApiDocTrapSink was not found."))),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, the mapping file could not be written, or the configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setImportMapping(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName, final SnmpTrapMappingGroup mappingGroup) {
        writeLock();
        try {
            SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
            try {
                trapSink.addImportMapping(mappingGroup);
            } catch (Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update a specific SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trapsinks/{trapsinkName}")
    @Operation(
            summary = "Update fields on an SNMP trap sink",
            description = """
                    Takes a form-encoded body and applies each key to the matching writable bean property of
                    the sink, then rewrites the file and sends a `reloadDaemonConfig` event for `SnmpTrapNBI`.
                    Keys are bean property names, not XML element names, and unrecognised keys are ignored
                    rather than rejected.

                    The writable properties are `name`, `ipAddress`, `port`, `v1AgentIpAddress`, `version`,
                    `community`, `mappings` and `importMappings`. `firstOccurrenceOnly` has a getter but no
                    setter, so a key of that name is accepted and ignored. `version` takes the constant name,
                    for example `V2c`. The collection properties cannot be expressed as a form value.""",
            operationId = "updateSnmpTrapSink")
    @RequestBody(required = true, description = "Form-encoded property assignments, keyed by bean property name.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "port=1163&community=public&version=V2c")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the configuration was saved."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "No trap sink has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Trap sink ApiDocTrapSink was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateSnmpTrapSink(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            boolean modified = false;
            SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(trapSink);
            for (final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update import mapping.
     *
     * @param trapSinkName the trap sink name
     * @param mappingName the mapping name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("trapsinks/{trapsinkName}/import-mappings/{mappingName}")
    @Operation(
            summary = "Update fields on an imported mapping group",
            description = """
                    Reads `snmptrap-northbounder-mappings.d/{mappingName}.xml`, applies each form key to the
                    matching writable bean property of the mapping group, writes the file back and sends a
                    `reloadDaemonConfig` event for `SnmpTrapNBI`.

                    The writable properties are `name` and `rule`, plus `mappings`, which a form value cannot
                    express. Renaming through `name` writes a new file rather than moving the old one.""",
            operationId = "updateSnmpTrapSinkImportMapping")
    @RequestBody(required = true, description = "Form-encoded property assignments, keyed by bean property name.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "rule=foreignSource matches '^ApiDocUpdated.*'")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the mapping file was rewritten."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "The sink has no imported mapping group of that name. Bodiless. A 404 with a plain-text body instead means the trap sink itself was not found.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Trap sink ApiDocTrapSink was not found."))),
            @ApiResponse(responseCode = "500", description = "The mapping file could not be read or written, or the configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateImportMapping(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName, @Parameter(description = "Name of the mapping group, without the .xml extension. The file is snmptrap-northbounder-mappings.d/<name>.xml.", required = true, example = "ApiDocMappings") @PathParam("mappingName") final String mappingName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
            SnmpTrapMappingGroup mappingGroup = null;
            try {
                mappingGroup = trapSink.getImportMapping(mappingName);
            } catch (Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            if (mappingGroup == null) {
                return Response.status(404).build();
            }
            boolean modified = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(mappingGroup);
            for (final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                try {
                    trapSink.addImportMapping(mappingGroup);
                } catch (Throwable t) {
                    throw getException(Status.INTERNAL_SERVER_ERROR, t);
                }
                saveConfiguration();
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Removes the import mapping.
     *
     * @param trapSinkName the trap sink name
     * @param mappingName the mapping name
     * @return the response
     */
    @DELETE
    @Path("trapsinks/{trapsinkName}/import-mappings/{mappingName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete an imported mapping group from a trap sink",
            description = """
                    Drops the path from the sink's `import-mappings`, deletes
                    `snmptrap-northbounder-mappings.d/{mappingName}.xml`, then rewrites the configuration and
                    sends a `reloadDaemonConfig` event for `SnmpTrapNBI`.

                    A mapping name that is not in the list yields 304, not 404. So does a name that is in the
                    list but whose file is already gone, in which case the list entry is still removed from
                    the in-memory model but the configuration is not written.""",
            operationId = "removeSnmpTrapSinkImportMapping")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The mapping file was deleted and the reload event sent."),
            @ApiResponse(responseCode = "304", description = "The sink has no imported mapping group of that name, so nothing was deleted."),
            @ApiResponse(responseCode = "404", description = "No trap sink has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Trap sink ApiDocTrapSink was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeImportMapping(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName, @Parameter(description = "Name of the mapping group, without the .xml extension. The file is snmptrap-northbounder-mappings.d/<name>.xml.", required = true, example = "ApiDocMappings") @PathParam("mappingName") final String mappingName) {
        SnmpTrapSink trapSink = getSnmpTrapSink(trapSinkName);
        if (trapSink.removeImportMapping(mappingName)) {
            return saveConfiguration();
        }
        return Response.notModified().build();
    }

    /**
     * Removes a specific SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @return the response
     */
    @DELETE
    @Path("trapsinks/{trapsinkName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete an SNMP trap sink",
            description = """
                    Removes the sink, rewrites snmptrap-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `SnmpTrapNBI`. Any mapping-group files the sink imported are
                    left on disk.""",
            operationId = "removeSnmpTrapSink")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The trap sink was removed and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No trap sink has that name. Bodiless."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeSnmpTrapSink(@Parameter(description = "Name of the trap sink.", required = true, example = "localTest1") @PathParam("trapsinkName") final String trapSinkName) {
        if (m_snmpTrapNorthbounderConfigDao.getConfig().removeSnmpTrapSink(trapSinkName)) {
            return saveConfiguration();
        }
        return Response.status(404).build();
    }

    /**
     * Saves the configuration.
     *
     * @return the response
     */
    private Response saveConfiguration() {
        try {
            m_snmpTrapNorthbounderConfigDao.save();
            notifyDaemons();
            return Response.noContent().build();
        } catch (Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
        }
    }

    /**
     * Notify daemons.
     *
     * @throws Exception the exception
     */
    private void notifyDaemons() throws Exception {
        EventBuilder eb = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "ReST");
        eb.addParam(EventConstants.PARM_DAEMON_NAME, "SnmpTrapNBI");
        m_eventProxy.send(eb.getEvent());
    }

}
