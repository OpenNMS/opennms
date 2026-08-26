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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.ws.rs.core.Response.Status;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.northbounder.email.EmailDestination;
import org.opennms.netmgt.alarmd.northbounder.email.EmailNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.email.EmailNorthbounderConfigDao;
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
 * The Class EmailNorthbounderConfigurationResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component
public class EmailNorthbounderConfigurationResource extends OnmsRestService implements InitializingBean {

    /** The Email Northbounder configuration DAO. */
    @Resource(name="emailNorthbounderConfigDao")
    private EmailNorthbounderConfigDao m_emailNorthbounderConfigDao;

    /** The event proxy. */
    @Resource(name="eventProxy")
    private EventProxy m_eventProxy;

    /**
     * The Class EmailDestinationList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="email-destinations")
    public static class EmailDestinationList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new email destination list.
         */
        public EmailDestinationList() {}

        /**
         * Instantiates a new email destination list.
         *
         * @param destinations the destinations
         */
        public EmailDestinationList(List<EmailDestination> destinations) {
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
        Assert.notNull(m_emailNorthbounderConfigDao, "emailNorthbounderConfigDao must be set!");
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
            summary = "Get the Email northbounder configuration",
            description = """
                    Returns the whole of email-northbounder-configuration.xml. `destination[].name` has to match
                    a `sendmail-config` entry in javamail-configuration.xml, which is what supplies the SMTP
                    host and credentials; the northbounder destination only carries the filters and the
                    per-filter message overrides.

                    A `filter` with no `from`, `to`, `subject` or `body` inherits the message from the
                    sendmail-config. `filter[].enabled` comes back as null when the attribute is absent, which
                    the northbounder treats as enabled.""",
            operationId = "getEmailNorthbounderConfiguration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current Email northbounder configuration.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = EmailNorthbounderConfig.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "enabled": false,
                                              "nagles-delay": 1000,
                                              "batch-size": 100,
                                              "queue-size": 300000,
                                              "destination": [
                                                {
                                                  "name": "google",
                                                  "filter": [
                                                    {
                                                      "enabled": null,
                                                      "name": "Only Servers",
                                                      "rule": "foreignSource matches '^Servers.*'"
                                                    }
                                                  ]
                                                }
                                              ]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = EmailNorthbounderConfig.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                        <email-northbounder-config>
                                              <enabled>false</enabled>
                                              <nagles-delay>1000</nagles-delay>
                                              <batch-size>100</batch-size>
                                              <queue-size>300000</queue-size>
                                              <destination>
                                                <name>google</name>
                                                <filter name="Only Servers">
                                                  <rule>foreignSource matches '^Servers.*'</rule>
                                                </filter>
                                              </destination>
                                            </email-northbounder-config>"""))
                    })
    })
    public Response getConfiguration() {
        return Response.ok(m_emailNorthbounderConfigDao.getConfig()).build();
    }

    /**
     * Sets the configuration.
     *
     * @param config the full configuration object
     * @return the response
     */
    @POST
    @Operation(
            summary = "Replace the Email northbounder configuration",
            description = """
                    Marshals the request body straight over email-northbounder-configuration.xml, then sends a
                    `reloadDaemonConfig` event for `EmailNBI`. The whole file is replaced, so anything absent
                    from the body is dropped, including comments. There is no merge and no dry run.

                    The handler declares no `@Consumes`, so the media type is whatever the JAXB and Jackson
                    providers accept for the body type. A body that fails to parse surfaces as a 500 rather
                    than a 400: the null check that would produce the 400 is unreachable through those
                    providers.""",
            operationId = "setEmailNorthbounderConfiguration")
    @RequestBody(required = true, description = "The complete replacement configuration.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmailNorthbounderConfig.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "enabled": false,
                                              "nagles-delay": 1000,
                                              "batch-size": 100,
                                              "queue-size": 300000,
                                              "destination": [
                                                {
                                                  "name": "google",
                                                  "filter": [
                                                    {
                                                      "enabled": null,
                                                      "name": "Only Servers",
                                                      "rule": "foreignSource matches '^Servers.*'"
                                                    }
                                                  ]
                                                }
                                              ]
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = EmailNorthbounderConfig.class),
                            examples = @ExampleObject(value = """
                                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                        <email-northbounder-config>
                                              <enabled>false</enabled>
                                              <nagles-delay>1000</nagles-delay>
                                              <batch-size>100</batch-size>
                                              <queue-size>300000</queue-size>
                                              <destination>
                                                <name>google</name>
                                                <filter name="Only Servers">
                                                  <rule>foreignSource matches '^Servers.*'</rule>
                                                </filter>
                                              </destination>
                                            </email-northbounder-config>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The file was written and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the file could not be written.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setConfiguration(final EmailNorthbounderConfig config) {
        writeLock();
        if (config == null) {
            throw getException(Status.BAD_REQUEST, "Email NBI configuration object cannot be null");
        }
        try {
            File configFile = m_emailNorthbounderConfigDao.getConfigResource().getFile();
            JaxbUtils.marshal(config, new FileWriter(configFile));
            notifyDaemons();
            return Response.noContent().build();
        } catch (Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
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
            summary = "Get whether the Email northbounder is enabled",
            description = """
                    Returns the `enabled` flag from email-northbounder-configuration.xml as the literal text
                    `true` or `false`. An absent flag reads back as `false`.

                    This operation produces text/plain only, so a request sent with
                    `Accept: application/json` is rejected with a 406.""",
            operationId = "getEmailNorthbounderStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current enabled flag.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "boolean"),
                            examples = @ExampleObject(value = "false")))
    })
    public Response getStatus() {
        return Response.ok(m_emailNorthbounderConfigDao.getConfig().isEnabled()).build();
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
            summary = "Enable or disable the Email northbounder",
            description = """
                    Sets the `enabled` flag, rewrites email-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `EmailNBI`. Comments and formatting in the file are lost,
                    because the whole file is re-marshalled from the in-memory model.

                    Omitting `enabled` clears the flag rather than leaving it alone, which then reads back as
                    `false`. Despite the text/plain declaration the success response has no body.""",
            operationId = "setEmailNorthbounderStatus")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The flag was written and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response getStatus(@Parameter(description = "New value for the enabled flag. Omitting it clears the flag, which reads back as false.", example = "true") @QueryParam("enabled") final Boolean enabled) throws WebApplicationException {
        writeLock();
        try {
            m_emailNorthbounderConfigDao.getConfig().setEnabled(enabled);
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
            summary = "List the Email northbounder destination names",
            description = """
                    Returns only the destination names, not the destinations themselves. Fetch a single
                    destination to see its filters.

                    `count` and `totalCount` are always equal here: the listing is not paged and `offset` is
                    always 0.""",
            operationId = "getEmailDestinations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The configured destination names.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = EmailDestinationList.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "totalCount": 1,
                                              "count": 1,
                                              "offset": 0,
                                              "destination": ["google"]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = EmailDestinationList.class),
                                    examples = @ExampleObject(value = """
                                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                            <email-destinations count="1" offset="0" totalCount="1">
                                              <destination>google</destination>
                                            </email-destinations>"""))
                    })
    })
    public Response getEmailDestinations() {
        final EmailDestinationList destinations = new EmailDestinationList(m_emailNorthbounderConfigDao.getConfig().getEmailDestinations());
        return Response.ok(destinations).build();
    }

    /**
     * Gets an email destination.
     *
     * @param destinationName the destination name
     * @return the email destination
     */
    @GET
    @Path("destinations/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one Email northbounder destination",
            description = """
                    Returns the destination and its filters. `filter[].enabled` is null when the attribute is
                    absent from the file.""",
            operationId = "getEmailDestination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The destination.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = EmailDestination.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "name": "ApiDocDest",
                                              "filter": [
                                                {
                                                  "name": "Only Routers",
                                                  "rule": "foreignSource matches '^Routers.*'",
                                                  "from": "donotreply@example.org",
                                                  "to": "noc@example.org",
                                                  "subject": "${nodeLabel} : Something is wrong!",
                                                  "body": "${logMsg}"
                                                }
                                              ]
                                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = EmailDestination.class),
                                    examples = @ExampleObject(value = """
                                            <email-destination>
                                              <name>ApiDocDest</name>
                                              <filter name="Only Routers">
                                                <rule>foreignSource matches '^Routers.*'</rule>
                                                <from>donotreply@example.org</from>
                                                <to>noc@example.org</to>
                                                <subject>${nodeLabel} : Something is wrong!</subject>
                                                <body>${logMsg}</body>
                                              </filter>
                                            </email-destination>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No destination has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Email destination ApiDocDest was not found.")))
    })
    public EmailDestination getEmailDestination(@Parameter(description = "Name of the destination, matching a sendmail-config entry in javamail-configuration.xml.", required = true, example = "google") @PathParam("destinationName") final String destinationName) {
       final EmailDestination destination = m_emailNorthbounderConfigDao.getConfig().getEmailDestination(destinationName);
        if (destination == null) {
            throw getException(Status.NOT_FOUND, "Email destination {} was not found.", destinationName);
        }
        return destination;
    }

    /**
     * Sets an email destination.
     * <p>If there is a destination with the same name, the existing one will be overridden.</p>
     *
     * @param destination the destination
     * @return the response
     */
    @POST
    @Path("destinations")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace an Email northbounder destination",
            description = """
                    Adds the destination to email-northbounder-configuration.xml, rewriting the whole file and
                    sending a `reloadDaemonConfig` event for `EmailNBI`. A destination with the same `name` is
                    replaced outright, so this is the only way to change a destination's filters: the PUT can
                    reach the name but not the filter list.

                    `name` has to match a `sendmail-config` entry in javamail-configuration.xml. Nothing
                    validates that at write time; a destination naming a config that does not exist is
                    accepted and fails later in the northbounder.

                    An empty or unparseable body surfaces as a 500 rather than the documented 400.""",
            operationId = "setEmailDestination")
    @RequestBody(required = true, description = "The destination to add or replace.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmailDestination.class),
                            examples = @ExampleObject(value = """
                                    {
                                              "name": "ApiDocDest",
                                              "filter": [
                                                {
                                                  "name": "Only Routers",
                                                  "rule": "foreignSource matches '^Routers.*'",
                                                  "from": "donotreply@example.org",
                                                  "to": "noc@example.org",
                                                  "subject": "${nodeLabel} : Something is wrong!",
                                                  "body": "${logMsg}"
                                                }
                                              ]
                                            }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = EmailDestination.class),
                            examples = @ExampleObject(value = """
                                    <email-destination>
                                              <name>ApiDocDest</name>
                                              <filter name="Only Routers">
                                                <rule>foreignSource matches '^Routers.*'</rule>
                                                <from>donotreply@example.org</from>
                                                <to>noc@example.org</to>
                                                <subject>${nodeLabel} : Something is wrong!</subject>
                                                <body>${logMsg}</body>
                                              </filter>
                                            </email-destination>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The destination was stored and the reload event sent."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed, or the configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response setEmailDestination(final EmailDestination destination) {
        writeLock();
        try {
            if (destination == null) {
                throw getException(Status.BAD_REQUEST, "Email destination object cannot be null");
            }
            m_emailNorthbounderConfigDao.getConfig().addEmailDestination(destination);
            saveConfiguration();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Updates a specific email destination.
     *
     * @param destinationName the destination name
     * @param params the parameters map
     * @return the response
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("destinations/{destinationName}")
    @Operation(
            summary = "Update fields on an Email northbounder destination",
            description = """
                    Takes a form-encoded body and applies each key to the matching writable bean property of
                    the destination, then rewrites the file and sends a `reloadDaemonConfig` event for
                    `EmailNBI`. Keys are bean property names, not XML element names, and unrecognised keys are
                    ignored rather than rejected.

                    `EmailDestination` exposes only `name` and `filters` as writable properties, and a form
                    value cannot express a filter list, so in practice this operation can only rename a
                    destination. POST the destination again to change its filters.""",
            operationId = "updateEmailDestination")
    @RequestBody(required = true, description = "Form-encoded property assignments. `name` is the only property a form value can usefully set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "name=ApiDocDestRenamed")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "At least one property was applied and the configuration was saved."),
            @ApiResponse(responseCode = "304", description = "No key in the body matched a writable property, so nothing was changed."),
            @ApiResponse(responseCode = "404", description = "No destination has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Email destination ApiDocDest was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response updateEmailDestination(@Parameter(description = "Name of the destination, matching a sendmail-config entry in javamail-configuration.xml.", required = true, example = "google") @PathParam("destinationName") final String destinationName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            boolean modified = false;
            final EmailDestination destination = getEmailDestination(destinationName);
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
     * Removes a specific email destination.
     *
     * @param destinationName the destination name
     * @return the response
     */
    @DELETE
    @Path("destinations/{destinationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete an Email northbounder destination",
            description = """
                    Removes the destination, rewrites email-northbounder-configuration.xml and sends a
                    `reloadDaemonConfig` event for `EmailNBI`. The matching `sendmail-config` in
                    javamail-configuration.xml is left alone.""",
            operationId = "removeEmailDestination")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The destination was removed and the reload event sent."),
            @ApiResponse(responseCode = "404", description = "No destination has that name. Bodiless."),
            @ApiResponse(responseCode = "500", description = "The configuration could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string")))
    })
    public Response removeEmailDestination(@Parameter(description = "Name of the destination, matching a sendmail-config entry in javamail-configuration.xml.", required = true, example = "google") @PathParam("destinationName") final String destinationName) {
        if (m_emailNorthbounderConfigDao.getConfig().removeEmailDestination(destinationName)) {
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
            m_emailNorthbounderConfigDao.save();
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
        eb.addParam(EventConstants.PARM_DAEMON_NAME, "EmailNBI");
        m_eventProxy.send(eb.getEvent());
    }

}
