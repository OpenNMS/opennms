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
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogDestination;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfigDao;
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
 * The Class SyslogNorthbounderConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class SyslogNorthbounderConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The Syslog Northbounder configuration DAO. */
    @Resource(name="syslogNorthbounderConfigDao")
    private SyslogNorthbounderConfigDao m_syslogNorthbounderConfigDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class SyslogDestinationList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="syslog-destinations")
    public static class SyslogDestinationList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new syslog destination list.
         */
        public SyslogDestinationList() {}

        /**
         * Instantiates a new syslog destination list.
         *
         * @param destinations the destinations
         */
        public SyslogDestinationList(List<SyslogDestination> destinations) {
            destinations.forEach(d -> add(d.getName()));
        }

        /**
         * Gets the destinations.
         *
         * @return the destinations
         */
        @XmlElement(name="destination")
        public List<String> getDestinations() {
            return getObjects();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_syslogNorthbounderConfigDao, "syslogNorthbounderConfigDao must be set!");
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
            summary = "Get the Syslog northbounder configuration",
            description = """
                    Returns the whole of syslog-northbounder-configuration.xml: the queue settings, the
                    top-level `message-format` template, the optional `uei` allow list and one entry per
                    destination.

                    `ip-protocol` and `facility` are JAXB enums declared without `@XmlEnumValue`, so they carry the
                    same constant spelling in JSON and XML: `UDP`/`TCP` and the usual syslog facility names in
                    upper case.""",
            operationId = "getSyslogNorthbounderConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current Syslog northbounder configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SyslogNorthbounderConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "enabled": false,
                                              "nagles-delay": 1000,
                                              "batch-size": 100,
                                              "queue-size": 300000,
                                              "message-format": "ALARM ID:${alarmId} NODE:${nodeLabel}; ${logMsg}",
                                              "destination": [
                                                {
                                                  "destination-name": "localTest",
                                                  "host": "127.0.0.1",
                                                  "port": 514,
                                                  "ip-protocol": "UDP",
                                                  "facility": "LOCAL0",
                                                  "max-message-length": 1024,
                                                  "send-local-name": true,
                                                  "send-local-time": true,
                                                  "truncate-message": false,
                                                  "filter": []
                                                }
                                              ]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SyslogNorthbounderConfig.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <syslog-northbounder-config>
                                              <enabled>false</enabled>
                                              <nagles-delay>1000</nagles-delay>
                                              <batch-size>100</batch-size>
                                              <queue-size>300000</queue-size>
                                              <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}; ${logMsg}</message-format>
                                              <destination>
                                                <destination-name>localTest</destination-name>
                                                <host>127.0.0.1</host>
                                                <port>514</port>
                                                <ip-protocol>UDP</ip-protocol>
                                                <facility>LOCAL0</facility>
                                                <max-message-length>1024</max-message-length>
                                                <send-local-name>true</send-local-name>
                                                <send-local-time>true</send-local-time>
                                                <truncate-message>false</truncate-message>
                                              </destination>
                                            </syslog-northbounder-config>"""))
                    })
    })
    public Response getConfiguration() {
        return Response.ok(m_syslogNorthbounderConfigDao.getConfig()).build();
    }

    /**
     * Sets the configuration.
     *
     * @param config the full configuration object
     * @return the response
     */
    @POST
    @Operation(
            summary = "Replace the Syslog northbounder configuration",
            description = """
                    Marshals the request body straight over syslog-northbounder-configuration.xml, then sends a
                    `reloadDaemonConfig` event for `SyslogNBI`. The whole file is replaced, so anything absent
                    from the body is dropped, including comments. There is no merge and no dry run.

                    The handler declares no `@Consumes`, so the media type is whatever the JAXB and Jackson
                    providers accept for the body type. A body that fails to parse surfaces as a 500 rather
                    than a 400: the null check that would produce the 400 is unreachable through those
                    providers.""",
            operationId = "setSyslogNorthbounderConfiguration")
    @RequestBody(required = true, description = "The complete replacement configuration.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SyslogNorthbounderConfig.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "enabled": false,
                                              "nagles-delay": 1000,
                                              "batch-size": 100,
                                              "queue-size": 300000,
                                              "message-format": "ALARM ID:${alarmId} NODE:${nodeLabel}; ${logMsg}",
                                              "destination": [
                                                {
                                                  "destination-name": "localTest",
                                                  "host": "127.0.0.1",
                                                  "port": 514,
                                                  "ip-protocol": "UDP",
                                                  "facility": "LOCAL0",
                                                  "max-message-length": 1024,
                                                  "send-local-name": true,
                                                  "send-local-time": true,
                                                  "truncate-message": false,
                                                  "filter": []
                                                }
                                              ]
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SyslogNorthbounderConfig.class),
                            examples = @ExampleObject(value = """
                                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <syslog-northbounder-config>
                                              <enabled>false</enabled>
                                              <nagles-delay>1000</nagles-delay>
                                              <batch-size>100</batch-size>
                                              <queue-size>300000</queue-size>
                                              <message-format>ALARM ID:${alarmId} NODE:${nodeLabel}; ${logMsg}</message-format>
                                              <destination>
                                                <destination-name>localTest</destination-name>
                                                <host>127.0.0.1</host>
                                                <port>514</port>
                                                <ip-protocol>UDP</ip-protocol>
                                                <facility>LOCAL0</facility>
                                                <max-message-length>1024</max-message-length>
                                                <send-local-name>true</send-local-name>
                                                <send-local-time>true</send-local-time>
                                                <truncate-message>false</truncate-message>
                                              </destination>
                                            </syslog-northbounder-config>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The file was written and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setConfiguration(final SyslogNorthbounderConfig config) {
        writeLock();
        try {
            if (config == null) {
                throw getException(Status.BAD_REQUEST, "Syslog NBI configuration object cannot be null");
            }
            try {
                File configFile = m_syslogNorthbounderConfigDao.getConfigResource().getFile();
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
            summary = "Get whether the Syslog northbounder is enabled",
            description = """
                    Returns the `enabled` flag from syslog-northbounder-configuration.xml as the literal text
                    `true` or `false`. An absent flag reads back as `false`.

                    This operation produces text/plain only, so a request sent with
                    `Accept: application/json` is rejected with a 406.""",
            operationId = "getSyslogNorthbounderStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current enabled flag.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "boolean"),
                            examples = @ExampleObject(value = "false")))
    })
    public Response getStatus() {
        return Response.ok(m_syslogNorthbounderConfigDao.getConfig().isEnabled()).build();
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
            summary = "Enable or disable the Syslog northbounder",
            description = """
                    Sets the `enabled` flag, rewrites syslog-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `SyslogNBI`. Comments and formatting in the file are lost,
                    because the whole file is re-marshalled from the in-memory model.

                    Omitting `enabled` clears the flag rather than leaving it alone, which then reads back as
                    `false`. Despite the text/plain declaration the success response has no body.""",
            operationId = "setSyslogNorthbounderStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The flag was written and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response getStatus(@Parameter(description = "New value for the enabled flag. Omitting it clears the flag, which reads back as false.", example = "true") @QueryParam("enabled") final Boolean enabled) throws WebApplicationException {
        writeLock();
        try {
            m_syslogNorthbounderConfigDao.getConfig().setEnabled(enabled);
            return saveConfiguration();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets all the email destinations.
     *
     * @return the email destinations
     */
    @GET
    @Path("destinations")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the Syslog northbounder destination names",
            description = """
                    Returns only the destination names, not the destinations themselves. Fetch a single
                    destination to see its settings.

                    `count` and `totalCount` are always equal here: the listing is not paged and `offset` is
                    always 0.""",
            operationId = "getSyslogDestinations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The configured destination names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SyslogDestinationList.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 1,
                                              "count": 1,
                                              "offset": 0,
                                              "destination": ["localTest"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SyslogDestinationList.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <syslog-destinations count="1" offset="0" totalCount="1">
                                              <destination>localTest</destination>
                                            </syslog-destinations>"""))
                    })
    })
    public Response getEmailDestinations() {
        SyslogDestinationList destinations = new SyslogDestinationList(m_syslogNorthbounderConfigDao.getConfig().getDestinations());
        return Response.ok(destinations).build();
    }

    /**
     * Gets a syslog destination.
     *
     * @param destinationName the destination name
     * @return the syslog destination
     */
    @GET
    @Path("destinations/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one Syslog northbounder destination",
            description = """
                    Returns the destination, including any per-destination `filter` entries.

                    `ip-protocol` and `facility` are JAXB enums declared without `@XmlEnumValue`, so they carry the
                    same constant spelling in JSON and XML: `UDP`/`TCP` and the usual syslog facility names in
                    upper case.""",
            operationId = "getSyslogDestination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The destination.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SyslogDestination.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "destination-name": "ApiDocSyslogDest",
                                              "host": "127.0.0.1",
                                              "port": 5140,
                                              "ip-protocol": "UDP",
                                              "facility": "LOCAL1",
                                              "max-message-length": 1024,
                                              "send-local-name": true,
                                              "send-local-time": true,
                                              "truncate-message": false,
                                              "first-occurrence-only": false
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = SyslogDestination.class),
                                    examples = @ExampleObject(value = """
                                            <syslog-destination>
                                              <destination-name>ApiDocSyslogDest</destination-name>
                                              <host>127.0.0.1</host>
                                              <port>5140</port>
                                              <ip-protocol>UDP</ip-protocol>
                                              <facility>LOCAL1</facility>
                                              <max-message-length>1024</max-message-length>
                                              <send-local-name>true</send-local-name>
                                              <send-local-time>true</send-local-time>
                                              <truncate-message>false</truncate-message>
                                              <first-occurrence-only>false</first-occurrence-only>
                                            </syslog-destination>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No destination has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Syslog destination ApiDocSyslogDest was not found.")))
    })
    public SyslogDestination getSyslogDestination(@Parameter(description = "Value of the destination-name element for the destination.", required = true, example = "localTest") @PathParam("destinationName") final String destinationName) {
        SyslogDestination destination = m_syslogNorthbounderConfigDao.getConfig().getSyslogDestination(destinationName);
        if (destination == null) {
            throw getException(Status.NOT_FOUND, "Syslog destination {} was not found.", destinationName);
        }
        return destination;
    }

    /**
     * Sets a syslog destination.
     * <p>If there is a destination with the same name, the existing one will be overridden.</p>
     *
     * @param destination the destination
     * @return the response
     */
    @POST
    @Path("destinations")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace a Syslog northbounder destination",
            description = """
                    Adds the destination to syslog-northbounder-configuration.xml, rewriting the whole file and
                    sending a `reloadDaemonConfig` event for `SyslogNBI`. A destination with the same
                    `destination-name` is replaced outright, which is the only way to change `facility` or the
                    filter list: neither is reachable through the PUT.

                    `ip-protocol` and `facility` are JAXB enums declared without `@XmlEnumValue`, so they carry the
                    same constant spelling in JSON and XML: `UDP`/`TCP` and the usual syslog facility names in
                    upper case.

                    An empty or unparseable body surfaces as a 500 rather than the documented 400.""",
            operationId = "setSyslogDestination")
    @RequestBody(required = true, description = "The destination to add or replace.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SyslogDestination.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "destination-name": "ApiDocSyslogDest",
                                              "host": "127.0.0.1",
                                              "port": 5140,
                                              "ip-protocol": "UDP",
                                              "facility": "LOCAL1",
                                              "max-message-length": 1024,
                                              "send-local-name": true,
                                              "send-local-time": true,
                                              "truncate-message": false,
                                              "first-occurrence-only": false
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = SyslogDestination.class),
                            examples = @ExampleObject(value = """
                                    <syslog-destination>
                                              <destination-name>ApiDocSyslogDest</destination-name>
                                              <host>127.0.0.1</host>
                                              <port>5140</port>
                                              <ip-protocol>UDP</ip-protocol>
                                              <facility>LOCAL1</facility>
                                              <max-message-length>1024</max-message-length>
                                              <send-local-name>true</send-local-name>
                                              <send-local-time>true</send-local-time>
                                              <truncate-message>false</truncate-message>
                                              <first-occurrence-only>false</first-occurrence-only>
                                            </syslog-destination>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The destination was stored and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setSyslogDestination(final SyslogDestination destination) {
        writeLock();
        try {
            if (destination == null) {
                throw getException(Status.BAD_REQUEST, "Syslog destination object cannot be null");
            }
            m_syslogNorthbounderConfigDao.getConfig().addSyslogDestination(destination);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update a specific Syslog Destination.
     *
     * @param destinationName the destination name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("destinations/{destinationName}")
    @Operation(
            summary = "Update fields on a Syslog northbounder destination",
            description = """
                    Takes a form-encoded body and applies each key to the matching writable bean property of
                    the destination, then rewrites the file and sends a `reloadDaemonConfig` event for
                    `SyslogNBI`. Keys are bean property names, not XML element names, and unrecognised keys
                    are ignored rather than rejected.

                    The writable properties are `name` (which is the `destination-name` element), `host`,
                    `port`, `protocol` (the `ip-protocol` element), `charSet`, `maxMessageLength`,
                    `sendLocalName`, `sendLocalTime`, `truncateMessage` and `firstOccurrenceOnly`. `facility`
                    has a getter but no setter, so a `facility` key is accepted and silently ignored; POST the
                    destination again to change it.""",
            operationId = "updateSyslogDestination")
    @RequestBody(required = true, description = "Form-encoded property assignments, keyed by bean property name.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "port=5141&protocol=TCP&maxMessageLength=2048")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the configuration was saved."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "No destination has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Syslog destination ApiDocSyslogDest was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateSyslogDestination(@Parameter(description = "Value of the destination-name element for the destination.", required = true, example = "localTest") @PathParam("destinationName") final String destinationName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            boolean modified = false;
            SyslogDestination destination = getSyslogDestination(destinationName);
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(destination);
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
     * Removes a specific syslog destination.
     *
     * @param destinationName the destination name
     * @return the response
     */
    @DELETE
    @Path("destinations/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a Syslog northbounder destination",
            description = """
                    Removes the destination, rewrites syslog-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `SyslogNBI`.""",
            operationId = "removeSyslogDestination")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The destination was removed and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No destination has that name. Bodiless."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeSyslogDestination(@Parameter(description = "Value of the destination-name element for the destination.", required = true, example = "localTest") @PathParam("destinationName") final String destinationName) {
        if (m_syslogNorthbounderConfigDao.getConfig().removeSyslogDestination(destinationName)) {
            return saveConfiguration();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * Saves the configuration.
     *
     * @return the response
     */
    private Response saveConfiguration() {
        try {
            m_syslogNorthbounderConfigDao.save();
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
        eb.addParam(EventConstants.PARM_DAEMON_NAME, "SyslogNBI");
        m_eventProxy.send(eb.getEvent());
    }

}
