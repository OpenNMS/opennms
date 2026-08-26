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
package org.opennms.web.rest.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.dao.outages.api.WriteablePollOutagesDao;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * <p>ScheduledOutagesRestService class.</p>
 * 
 * <ul>
 * <li><b>GET /sched-outages</b><br>to get a list of configured scheduled outages.</li>
 * <li><b>POST /sched-outages</b><br>to add a new outage (or update an existing one).</li>
 * <li><b>GET /sched-outages/{outageName}</b><br>to get the details of a specific outage.</li>
 * <li><b>DELETE /sched-outages/{outageName}</b><br>to delete a specific outage.</li>
 * <li><b>PUT /sched-outages/{outageName}/collectd/{package}</b><br>to add a specific outage to a collectd's package.</li>
 * <li><b>PUT /sched-outages/{outageName}/pollerd/{package}</b><br>to add a specific outage to a pollerd's package.</li>
 * <li><b>PUT /sched-outages/{outageName}/threshd/{package}</b><br>to add a specific outage to a threshd's package.</li>
 * <li><b>PUT /sched-outages/{outageName}/notifd</b><br>to add a specific outage to the notifications.</li>
 * <li><b>DELETE /sched-outages/{outageName}/collectd/{package}</b><br>to remove a specific outage from a collectd's package.</li>
 * <li><b>DELETE /sched-outages/{outageName}/pollerd/{package}</b><br>to remove a specific outage from a pollerd's package.</li>
 * <li><b>DELETE /sched-outages/{outageName}/threshd/{package}</b><br>to remove a specific outage from a threshd's package.</li>
 * <li><b>DELETE /sched-outages/{outageName}/notifd</b><br>to remove a specific outage from the notifications.</li>
 * </ul>
 * 
 * <p>Node and Interface status (the requests return true or false):</p>
 * <ul>
 * <li><b>GET /sched-outages/{outageName}/nodeInOutage/{nodeId}</b><br>to check if a node (with a specific nodeId) is currently on outage for a specific scheduled outage calendar.</li>
 * <li><b>GET /sched-outages/{outageName}/interfaceInOutage/{ipAddr}</b><br>to check if an interface (with a specific IP address) is currently on outage for a specific scheduled outage calendar.</li>
 * <li><b>GET /sched-outages/nodeInOutage/{nodeId}</b><br>to check if a node (with a specific nodeId) is currently in outage.</li>
 * <li><b>GET /sched-outages/interfaceInOutage/{ipAddr}</b><br>to check if an interface (with a specific IP address) is currently on outage.</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@Component("scheduledOutagesRestService")
@Path("sched-outages")
@Tag(name = "Sched-outages", description = """
        A scheduled outage is a named calendar in `poll-outages.xml` listing time windows plus the nodes and
        interfaces they cover. The calendar has no effect on its own: it has to be attached to a collectd, pollerd
        or threshd package, or to notifd.

        Every write rewrites the affected configuration file through the JAXB marshaller, which reformats the file
        and drops its XML comments, and then sends `uei.opennms.org/internal/schedOutagesChanged`.

        A calendar whose window covers the current time suppresses polling, collection, thresholding or
        notification for whatever it names.""")
public class ScheduledOutagesRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledOutagesRestService.class);


    private enum ConfigAction { ADD, REMOVE, REMOVE_FROM_ALL };

    @Autowired
    protected CollectdConfigFactory m_collectdConfigFactory;

    @Autowired
    @Qualifier("eventProxy")
    protected EventProxy m_eventProxy;
    
    @Autowired
    private WriteableThreshdDao m_threshdDao;
    
    @Autowired
    private WriteablePollOutagesDao m_pollOutagesDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List scheduled outages",
            description = "Return every scheduled outage calendar in `poll-outages.xml`.",
            operationId = "getScheduledOutagesV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The configured calendars.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Outages.class),
                            examples = @ExampleObject(value = """
                    {
                      "outage": [ {
                          "name": "Weekend maintenance",
                          "type": "weekly",
                          "time": [
                            { "id": null, "day": "saturday", "begins": "00:00:00", "ends": "23:59:59" }
                          ],
                          "interface": [ { "address": "192.0.2.99" } ],
                          "node": [ { "id": 2 } ]
                        } ]
                    }""")))
    })
    public Outages getOutages() {
        Outages outages = new Outages();
        outages.setOutages(m_pollOutagesDao.getReadOnlyConfig().getOutages());
        return outages;
    }

    @GET
    @Path("{outageName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get a scheduled outage",
            description = """
                    Return one calendar by name. The name is matched exactly and may contain spaces, so it has to
                    be URL-encoded.
                    In JSON the `time` entries carry `id` and `day` as explicit nulls when they are unset.""",
            operationId = "getScheduledOutageV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The calendar.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Outage.class),
                            examples = @ExampleObject(value = """
                    {
                      "name": "Weekend maintenance",
                      "type": "weekly",
                      "time": [
                        { "id": null, "day": "saturday", "begins": "00:00:00", "ends": "23:59:59" }
                      ],
                      "interface": [ { "address": "192.0.2.99" } ],
                      "node": [ { "id": 2 } ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No scheduled outage with that name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Scheduled outage Weekend maintenance was not found.")))
    })
    public Outage getOutage(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName) throws IllegalArgumentException {
        Outage outage = m_pollOutagesDao.getReadOnlyConfig().getOutage(outageName);
        if (outage == null) throw getException(Status.NOT_FOUND, "Scheduled outage {} was not found.", outageName);
        return outage;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Add or replace a scheduled outage",
            description = """
                    Create a calendar, or replace the existing one with the same `name`. A new calendar answers
                    201 with a `Location` header; a replacement answers 204.
                    `type` has to be one of `specific`, `daily`, `weekly` or `monthly`, and the `begins`/`ends`
                    format depends on it: `dd-MMM-yyyy HH:mm:ss` for `specific`, `HH:mm:ss` for `daily`, `HH:mm:ss`
                    with a `day` name for `weekly`, and `HH:mm:ss` with a numeric `day` for `monthly`. `type` is
                    not validated on the way in; an unknown value is accepted into the in-memory configuration and
                    then fails when the file is marshalled, leaving the invalid calendar in memory until it is
                    deleted again.
                    The XML form needs the `http://xmlns.opennms.org/xsd/config/poller/outages` namespace on the
                    document element.""",
            operationId = "saveOrUpdateScheduledOutageV1"
    )
    @RequestBody(required = true, description = "The calendar to store.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Outage.class),
                            examples = @ExampleObject(value = """
                    {
                      "name": "Weekend maintenance",
                      "type": "weekly",
                      "time": [ { "day": "saturday", "begins": "00:00:00", "ends": "23:59:59" } ],
                      "interface": [ { "address": "192.0.2.99" } ],
                      "node": [ { "id": 2 } ]
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = Outage.class),
                            examples = @ExampleObject(value = """
                    <outage xmlns="http://xmlns.opennms.org/xsd/config/poller/outages"
                            name="Weekend maintenance" type="weekly">
                      <time day="saturday" begins="00:00:00" ends="23:59:59"/>
                      <interface address="192.0.2.99"/>
                      <node id="2"/>
                    </outage>"""))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "A new calendar was added. `Location` points at it."),
            @ApiResponse(responseCode = "204", description = "An existing calendar with the same name was replaced."),
            @ApiResponse(responseCode = "400", description = "The body was absent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Outage object can't be null"))),
            @ApiResponse(responseCode = "500", description = "The body could not be unmarshalled, or the configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save or update the scheduled outage Weekend maintenance because, Failed to marshal/unmarshal XML file while marshalling Outages: javax.xml.bind.MarshalException")))
    })
    public Response saveOrUpdateOutage(@Context final UriInfo uriInfo, final Outage newOutage) {
        writeLock();
        try {
            if (newOutage == null) throw getException(Status.BAD_REQUEST, "Outage object can't be null");
            Outage oldOutage = m_pollOutagesDao.getWriteableConfig().getOutage(newOutage.getName());
            if (oldOutage == null) {
                LOG.debug("saveOrUpdateOutage: adding outage {}", newOutage.getName());
                m_pollOutagesDao.withWriteLock(outages -> outages.addOutage(newOutage));
            } else {
                LOG.debug("saveOrUpdateOutage: updating outage {}", newOutage.getName());
                m_pollOutagesDao.withWriteLock(outages -> outages.replaceOutage(oldOutage, newOutage));
            }
            try {
                m_pollOutagesDao.saveConfig();
            } catch (Exception e) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Can't save or update the scheduled outage {} because, {}", newOutage.getName(), e.getMessage());
            }
            sendConfigChangedEvent();
            return oldOutage == null ? Response.created(getRedirectUri(uriInfo, newOutage.getName())).build() : Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}")
    @Operation(
            summary = "Delete a scheduled outage",
            description = """
                    Detach the calendar from every collectd, pollerd and threshd package and from notifd, then
                    remove it from `poll-outages.xml`. All five configuration files are rewritten.
                    The detach step validates the name first, so deleting a calendar that is not there is a 404
                    rather than a no-op.""",
            operationId = "deleteScheduledOutageV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The calendar was deleted."),
            @ApiResponse(responseCode = "404", description = "No scheduled outage with that name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Scheduled outage Weekend maintenance was not found."))),
            @ApiResponse(responseCode = "500", description = "One of the configuration files could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't delete the scheduled outage Weekend maintenance because, java.io.IOException")))
    })
    public Response deleteOutage(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName) {
        writeLock();
        try {
            LOG.debug("deleteOutage: deleting outage {}", outageName);
            updateCollectd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updatePollerd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updateThreshd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updateNotifd(ConfigAction.REMOVE, outageName);
            try {
                m_pollOutagesDao.withWriteLock(outages -> outages.removeOutage(outageName));
                m_pollOutagesDao.saveConfig();
            } catch (Exception e) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Can't delete the scheduled outage {} because, {}", outageName, e.getMessage());
            }
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/collectd/{packageName}")
    @Operation(
            summary = "Attach a scheduled outage to a collectd package",
            description = """
                    Add the calendar to a collectd package's outage-calendar list. The request takes no body and
                    is idempotent: attaching an already-attached calendar is a 204 and does not duplicate the
                    entry.
                    `collectd-configuration.xml` is rewritten and a configuration-changed event is sent.""",
            operationId = "addScheduledOutageToCollectdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No such calendar, or no such package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Collector package example1 does not exist."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save collector's configuration: java.io.IOException")))
    })
    public Response addOutageToCollector(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName,
            @Parameter(description = "Package name as it appears in the daemon's configuration file.",
                    example = "example1", required = true)
            @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateCollectd(ConfigAction.ADD, outageName, packageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/collectd/{packageName}")
    @Operation(
            summary = "Detach a scheduled outage from a collectd package",
            description = """
                    Remove the calendar from a collectd package's outage-calendar list. The request takes no body
                    and is idempotent: detaching a calendar that is not attached is a 204.
                    `collectd-configuration.xml` is rewritten and a configuration-changed event is sent.""",
            operationId = "removeScheduledOutageFromCollectdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No such calendar, or no such package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Collector package example1 does not exist."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save collector's configuration: java.io.IOException")))
    })
    public Response removeOutageFromCollector(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName,
            @Parameter(description = "Package name as it appears in the daemon's configuration file.",
                    example = "example1", required = true)
            @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateCollectd(ConfigAction.REMOVE, outageName, packageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/pollerd/{packageName}")
    @Operation(
            summary = "Attach a scheduled outage to a pollerd package",
            description = """
                    Add the calendar to a pollerd package's outage-calendar list. The request takes no body and is
                    idempotent.
                    While the calendar's window is open, pollerd stops polling the nodes and interfaces the
                    calendar names in that package. `poller-configuration.xml` is rewritten and a
                    configuration-changed event is sent.""",
            operationId = "addScheduledOutageToPollerdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No such calendar, or no such package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Poller package example1 does not exist."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save poller's configuration: java.io.IOException")))
    })
    public Response addOutageToPoller(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") final String outageName,
            @Parameter(description = "Package name as it appears in the daemon's configuration file.",
                    example = "example1", required = true)
            @PathParam("packageName") final String packageName) {
        writeLock();
        try {
            updatePollerd(ConfigAction.ADD, outageName, packageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/pollerd/{packageName}")
    @Operation(
            summary = "Detach a scheduled outage from a pollerd package",
            description = """
                    Remove the calendar from a pollerd package's outage-calendar list. The request takes no body
                    and is idempotent.
                    `poller-configuration.xml` is rewritten and a configuration-changed event is sent.""",
            operationId = "removeScheduledOutageFromPollerdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No such calendar, or no such package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Poller package example1 does not exist."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save poller's configuration: java.io.IOException")))
    })
    public Response removeOutageFromPoller(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") final String outageName,
            @Parameter(description = "Package name as it appears in the daemon's configuration file.",
                    example = "example1", required = true)
            @PathParam("packageName") final String packageName) {
        writeLock();
        try {
            updatePollerd(ConfigAction.REMOVE, outageName, packageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/threshd/{packageName}")
    @Operation(
            summary = "Attach a scheduled outage to a threshd package",
            description = """
                    Add the calendar to a threshd package's outage-calendar list. The request takes no body and is
                    idempotent.
                    `threshd-configuration.xml` is rewritten and a configuration-changed event is sent.""",
            operationId = "addScheduledOutageToThreshdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No such calendar, or no such package.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Threshold package mib2 does not exist."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save thresholds configuration: java.io.IOException")))
    })
    public Response addOutageToThresholder(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName,
            @Parameter(description = "Package name as it appears in the daemon's configuration file.",
                    example = "example1", required = true)
            @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateThreshd(ConfigAction.ADD, outageName, packageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/threshd/{packageName}")
    @Operation(
            summary = "Detach a scheduled outage from a threshd package",
            description = """
                    Remove the calendar from a threshd package's outage-calendar list. The request takes no body
                    and is idempotent.
                    `threshd-configuration.xml` is rewritten and a configuration-changed event is sent.
                    This handler wraps every failure, including the not-found cases, into a 500, so an unknown
                    calendar or package is a 500 here rather than the 404 the other detach operations return.""",
            operationId = "removeScheduledOutageFromThreshdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "500", description = "No such calendar, no such package, or the configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't delete the scheduled outage Weekend maintenance because, HTTP 404 Not Found")))
    })
    public Response removeOutageFromThresholder(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") final String outageName,
            @Parameter(description = "Package name as it appears in the daemon's configuration file.",
                    example = "example1", required = true)
            @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateThreshd(ConfigAction.REMOVE, outageName, packageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't delete the scheduled outage {} because, {}", outageName, e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/notifd")
    @Operation(
            summary = "Attach a scheduled outage to notifd",
            description = """
                    Add the calendar to notifd's outage-calendar list, suppressing notifications while its window
                    is open. The request takes no body.
                    This one is not idempotent: repeating it appends the name again, and each detach removes only
                    one occurrence.
                    `notifd-configuration.xml` is rewritten and a configuration-changed event is sent.""",
            operationId = "addScheduledOutageToNotifdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No scheduled outage with that name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Scheduled outage Weekend maintenance was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save notifications configuration: java.io.IOException")))
    })
    public Response addOutageToNotifications(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName) {
        writeLock();
        try {
            updateNotifd(ConfigAction.ADD, outageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/notifd")
    @Operation(
            summary = "Detach a scheduled outage from notifd",
            description = """
                    Remove one occurrence of the calendar from notifd's outage-calendar list. The request takes no
                    body and detaching a calendar that is not attached is a 204.
                    `notifd-configuration.xml` is rewritten and a configuration-changed event is sent.""",
            operationId = "removeScheduledOutageFromNotifdV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The configuration was written."),
            @ApiResponse(responseCode = "404", description = "No scheduled outage with that name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Scheduled outage Weekend maintenance was not found."))),
            @ApiResponse(responseCode = "500", description = "The configuration could not be written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't save notifications configuration: java.io.IOException")))
    })
    public Response removeOutageFromNotifications(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName) {
        writeLock();
        try {
            updateNotifd(ConfigAction.REMOVE, outageName);
            sendConfigChangedEvent();
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @GET
    @Path("{outageName}/nodeInOutage/{nodeId}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Check a node against one scheduled outage",
            description = """
                    Return `true` when the calendar names the node and the current time falls inside one of its
                    windows, and `false` otherwise. A calendar whose window is in the past answers `false`.""",
            operationId = "isNodeInScheduledOutageV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Whether the node is currently in this outage.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string", allowableValues = {"true", "false"}),
                            examples = @ExampleObject(value = "false"))),
            @ApiResponse(responseCode = "404", description = "No such calendar, or the node id is not an integer.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Scheduled outage Weekend maintenance was not found.")))
    })
    public String isNodeInOutage(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName,
            @Parameter(description = "Node id.", example = "2", required = true)
            @PathParam("nodeId") Integer nodeId) {
        Outage outage = getOutage(outageName);
        Boolean inOutage = m_pollOutagesDao.isNodeIdInOutage(nodeId, outage) && m_pollOutagesDao.isCurTimeInOutage(outage);
        return inOutage.toString();
    }

    @GET
    @Path("nodeInOutage/{nodeId}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Check a node against every scheduled outage",
            description = "Return `true` when any calendar names the node and is currently inside one of its "
                    + "windows. An unknown node id is not an error and answers `false`.",
            operationId = "isNodeInAnyScheduledOutageV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Whether the node is currently in any scheduled outage.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string", allowableValues = {"true", "false"}),
                            examples = @ExampleObject(value = "false"))),
            @ApiResponse(responseCode = "404", description = "The node id is not an integer.")
    })
    public String isNodeInOutage(
            @Parameter(description = "Node id.", example = "2", required = true)
            @PathParam("nodeId") int nodeId) {
        for (Outage outage : m_pollOutagesDao.getReadOnlyConfig().getOutages()) {
            if (m_pollOutagesDao.isNodeIdInOutage(nodeId, outage) && m_pollOutagesDao.isCurTimeInOutage(outage)) {
                return Boolean.TRUE.toString();
            }
        }
        return Boolean.FALSE.toString();
    }

    @GET
    @Path("{outageName}/interfaceInOutage/{ipAddr}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Check an interface against one scheduled outage",
            description = """
                    Return `true` when the calendar covers the address and the current time falls inside one of its
                    windows, and `false` otherwise. The address is checked against the calendar's `interface`
                    entries, which may be exact addresses or ranges.
                    The address is validated here, so a malformed one is a 400.""",
            operationId = "isInterfaceInScheduledOutageV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Whether the interface is currently in this outage.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string", allowableValues = {"true", "false"}),
                            examples = @ExampleObject(value = "false"))),
            @ApiResponse(responseCode = "400", description = "The address could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Malformed IP Address notanip"))),
            @ApiResponse(responseCode = "404", description = "No scheduled outage with that name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Scheduled outage Weekend maintenance was not found.")))
    })
    public String isInterfaceInOutage(
            @Parameter(description = "Scheduled outage name as it appears in `poll-outages.xml`.",
                    example = "Weekend maintenance", required = true)
            @PathParam("outageName") String outageName,
            @Parameter(description = "IPv4 or IPv6 address to test.", example = "192.0.2.99", required = true)
            @PathParam("ipAddr") String ipAddr) {
        validateAddress(ipAddr);
        Outage outage = getOutage(outageName);
        Boolean inOutage = m_pollOutagesDao.isInterfaceInOutage(ipAddr, outage) && m_pollOutagesDao.isCurTimeInOutage(outage);
        return inOutage.toString();
    }

    @GET
    @Path("interfaceInOutage/{ipAddr}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Check an interface against every scheduled outage",
            description = """
                    Return `true` when any calendar covers the address and is currently inside one of its windows.
                    Unlike the calendar-scoped form, this one does not validate the address: a value that is not an
                    address answers `false` rather than 400.""",
            operationId = "isInterfaceInAnyScheduledOutageV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Whether the interface is currently in any scheduled outage.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string", allowableValues = {"true", "false"}),
                            examples = @ExampleObject(value = "false")))
    })
    public String isInterfaceInOutage(
            @Parameter(description = "IPv4 or IPv6 address to test.", example = "192.0.2.99", required = true)
            @PathParam("ipAddr") String ipAddr) {
        for (Outage outage : m_pollOutagesDao.getReadOnlyConfig().getOutages()) {
            if (m_pollOutagesDao.isInterfaceInOutage(ipAddr, outage) && m_pollOutagesDao.isCurTimeInOutage(outage)) {
                return Boolean.TRUE.toString();
            }
        }
        return Boolean.FALSE.toString();
    }

    private static void validateAddress(String ipAddress) {
        boolean valid = false;
        try {
            valid = InetAddressUtils.addr(ipAddress) != null;
        } catch (Exception e) {
            valid = false;
        }
        if (!valid) {
            throw getException(Status.BAD_REQUEST, "Malformed IP Address {}", ipAddress);
        }
    }

    private void updateCollectd(ConfigAction action, String outageName, String packageName) {
        getOutage(outageName); // Validate if outageName exists.
        if (action.equals(ConfigAction.ADD)) {
            Package pkg = getCollectdPackage(packageName);
            if (!pkg.getOutageCalendars().contains(outageName))
                pkg.addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE)) {
            Package pkg = getCollectdPackage(packageName);
            pkg.removeOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            for (Package pkg : m_collectdConfigFactory.getPackages()) {
                pkg.removeOutageCalendar(outageName);
            }
        }
        try {
            m_collectdConfigFactory.saveCurrent();
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't save collector's configuration: {}", e.getMessage());
        }
    }

    private Package getCollectdPackage(String packageName) {
        Package pkg = m_collectdConfigFactory.getPackage(packageName);
        if (pkg == null) throw getException(Status.NOT_FOUND, "Collector package {} does not exist.", packageName);
        return pkg;
    }

    private void updatePollerd(ConfigAction action, String outageName, String packageName) {
        getOutage(outageName); // Validate if outageName exists.
        if (action.equals(ConfigAction.ADD)) {
            org.opennms.netmgt.config.poller.Package pkg = getPollerdPackage(packageName);
            if (!pkg.getOutageCalendars().contains(outageName))
                pkg.addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE)) {
            org.opennms.netmgt.config.poller.Package pkg = getPollerdPackage(packageName);
            pkg.removeOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            for (org.opennms.netmgt.config.poller.Package pkg : PollerConfigFactory.getInstance().getExtendedConfiguration().getPackages()) {
                pkg.removeOutageCalendar(outageName);
            }
        }
        try {
            PollerConfigFactory.getInstance().save();
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't save poller's configuration: {}", e.getMessage());
        }
    }

    private static org.opennms.netmgt.config.poller.Package getPollerdPackage(String packageName) {
        org.opennms.netmgt.config.poller.Package pkg = PollerConfigFactory.getInstance().getPackage(packageName);
        if (pkg == null) throw getException(Status.NOT_FOUND, "Poller package {} does not exist.", packageName);
        return pkg;
    }

    private void updateThreshd(ConfigAction action, String outageName, String packageName) {
        getOutage(outageName); // Validate if outageName exists.
        if (action.equals(ConfigAction.ADD)) {
            org.opennms.netmgt.config.threshd.Package pkg = getThreshdPackage(packageName);
            if (!pkg.getOutageCalendars().contains(outageName))
                pkg.addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE)) {
            org.opennms.netmgt.config.threshd.Package pkg = getThreshdPackage(packageName);
            pkg.removeOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            for (org.opennms.netmgt.config.threshd.Package pkg : m_threshdDao.getWriteableConfig().getPackages()) {
                pkg.removeOutageCalendar(outageName);
            }
        }
        try {
            m_threshdDao.saveConfig();
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't save thresholds configuration: {}", e.getMessage());
        }
    }

    private org.opennms.netmgt.config.threshd.Package getThreshdPackage(String packageName) {
        return m_threshdDao.getWriteableConfig().getPackage(packageName)
                .orElseThrow(() -> getException(Status.NOT_FOUND, "Threshold package {} does not exist.", packageName));
    }

    private void updateNotifd(ConfigAction action, String outageName) {
        getOutage(outageName); // Validate if outageName exists.
        try {
            NotifdConfigFactory factory = NotifdConfigFactory.getInstance();
            if (action.equals(ConfigAction.ADD)) {
                factory.getConfiguration().addOutageCalendar(outageName);
            }
            if (action.equals(ConfigAction.REMOVE) || action.equals(ConfigAction.REMOVE_FROM_ALL)) {
                factory.getConfiguration().removeOutageCalendar(outageName);
            }
            factory.saveCurrent();
        } catch (Exception e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't save notifications configuration: {}", e.getMessage());
        }
    }

    private void sendConfigChangedEvent() {
        EventBuilder builder = new EventBuilder(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI, "ReST");
        try {
            m_eventProxy.send(builder.getEvent());
        } catch (Throwable e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Can't send event {} : {}", builder.getEvent().getUei(), e.getMessage());
        }
    }

}
