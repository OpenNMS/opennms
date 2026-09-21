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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
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
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAcknowledgmentCollection;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.web.rest.support.SecurityHelper;
import org.opennms.web.rest.v1.model.AcknowledgmentForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReST service for Acknowledgments of alarms/notifications.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component("acknowledgmentRestService")
@Path("acks")
@Tag(name = "Acknowledgments", description = """
        Acknowledging, unacknowledging, clearing or escalating an alarm or a notification appends a new row rather
        than changing an existing one. The state of the alarm or notification comes from the newest row that
        refers to it.""")
public class AcknowledgmentRestService extends OnmsRestService {
    @Autowired
    private AcknowledgmentDao m_ackDao;
    
    @Autowired
    private AlarmDao m_alarmDao;
    
    @Autowired
    private NotificationDao m_notificationDao;
    
    /**
     * <p>getAcknowledgment</p>
     *
     * @param acknowledgmentId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{id}")
    @Transactional
    @Operation(
            summary = "Get an acknowledgement",
            description = """
                    Return one acknowledgement row by id. `refId` is the id of the alarm or notification the row
                    refers to, and `ackType` says which of the two.
                    `ackTime` is epoch milliseconds in JSON and an ISO-8601 string in XML. The XML root element is
                    `ack`, while the list endpoint wraps its entries in `onmsAcknowledgment`.""",
            operationId = "getAcknowledgmentV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The acknowledgement.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsAcknowledgment.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 23632,
                      "log": null,
                      "ackType": "ALARM",
                      "ackAction": "ACKNOWLEDGE",
                      "ackTime": 1787727613204,
                      "ackUser": "admin",
                      "refId": 4547
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No acknowledgement with that id, or the path segment is not an integer.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Acknowledgement object 99999999 was not found."))),
            @ApiResponse(responseCode = "403", description = "The caller holds none of `ROLE_ADMIN`, `ROLE_REST`, `ROLE_USER` or `ROLE_MOBILE`. The container rejects the request before the resource is reached, so the body is its HTML error page rather than a REST payload.",
                    content = @Content(mediaType = MediaType.TEXT_HTML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<html>\n<head>\n<title>Error 403 Forbidden</title>\n</head>\n<body><h2>HTTP ERROR 403 Forbidden</h2>\n</body>\n</html>")))
    })
    public OnmsAcknowledgment getAcknowledgment(
            @Parameter(description = "Acknowledgement id.", example = "23632", required = true)
            @PathParam("id") Integer acknowledgmentId) {
        final OnmsAcknowledgment ack = m_ackDao.get(acknowledgmentId);
        if (ack == null) {
            throw getException(Status.NOT_FOUND, "Acknowledgement object {} was not found.", Integer.toString(acknowledgmentId));
        }
        return ack;
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    @Operation(
            summary = "Count all acknowledgements",
            description = "Return the total number of acknowledgement rows as a plain-text integer. Query "
                    + "parameters are ignored.",
            operationId = "getAcknowledgmentCountV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The acknowledgement count.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "7"))),
            @ApiResponse(responseCode = "403", description = "The caller holds none of `ROLE_ADMIN`, `ROLE_REST`, `ROLE_USER` or `ROLE_MOBILE`. The container rejects the request before the resource is reached, so the body is its HTML error page rather than a REST payload.",
                    content = @Content(mediaType = MediaType.TEXT_HTML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<html>\n<head>\n<title>Error 403 Forbidden</title>\n</head>\n<body><h2>HTTP ERROR 403 Forbidden</h2>\n</body>\n</html>")))
    })
    public String getCount() {
        return Integer.toString(m_ackDao.countAll());
    }

    /**
     * <p>getAcks</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgmentCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Search acknowledgements",
            description = """
                    Return acknowledgement rows matching the query parameters, newest `ackTime` first unless
                    `orderBy` says otherwise.
                    Filters are `OnmsAcknowledgment` property names such as `ackType`, `ackAction`, `ackUser` and
                    `refId`. `limit` (default 10), `offset`, `orderBy`, `order`, `match` and `comparator` shape the
                    result. A filter name that is not a property of the entity fails with 500.
                    `totalCount` is the unpaged match count; `count` is the size of this page.""",
            operationId = "getAcknowledgmentsV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The matching acknowledgements.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsAcknowledgmentCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 7,
                      "count": 2,
                      "offset": 0,
                      "onmsAcknowledgment": [
                        {
                          "id": 23631,
                          "log": null,
                          "ackType": "ALARM",
                          "ackAction": "CLEAR",
                          "ackTime": 1787727588461,
                          "ackUser": "admin",
                          "refId": 4547
                        },
                        {
                          "id": 23630,
                          "log": null,
                          "ackType": "ALARM",
                          "ackAction": "ESCALATE",
                          "ackTime": 1787727588337,
                          "ackUser": "admin",
                          "refId": 4547
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "A query parameter is not a property of the acknowledgement entity.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null"))),
            @ApiResponse(responseCode = "403", description = "The caller holds none of `ROLE_ADMIN`, `ROLE_REST`, `ROLE_USER` or `ROLE_MOBILE`. The container rejects the request before the resource is reached, so the body is its HTML error page rather than a REST payload.",
                    content = @Content(mediaType = MediaType.TEXT_HTML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<html>\n<head>\n<title>Error 403 Forbidden</title>\n</head>\n<body><h2>HTTP ERROR 403 Forbidden</h2>\n</body>\n</html>")))
    })
    public OnmsAcknowledgmentCollection getAcks(@Context final UriInfo uriInfo) {
        final CriteriaBuilder builder = getQueryFilters(uriInfo.getQueryParameters());
        OnmsAcknowledgmentCollection coll = new OnmsAcknowledgmentCollection(m_ackDao.findMatching(builder.toCriteria()));

        //For getting totalCount
        builder.clearOrder();
        builder.limit(null);
        builder.offset(null);
        coll.setTotalCount(m_ackDao.countMatching(builder.toCriteria()));

        return coll;
    }

    /**
     * <p>acknowledgeAlarm</p>
     *
     * @param alarmId a {@link java.lang.String} object.
     * @param action a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Acknowledge an alarm or a notification",
            description = """
                    Append an acknowledgement row for one alarm or one notification. The body is form-encoded;
                    JSON is rejected with 415.
                    Exactly one of `alarmId` and `notifId` has to be present. `action` defaults to `ack`. `ackUser`
                    defaults to the authenticated user, and a non-admin caller may only name themselves.
                    An id that parses as an integer but matches no row yields 304 rather than 404.
                    The method declares no `@Produces`, so the entity is serialized according to the request's
                    `Accept` header, XML by default. The XML root element is `ack`.""",
            operationId = "createAcknowledgmentV1"
    )
    @RequestBody(required = true, description = "The alarm or notification to act on, and the action to record.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AcknowledgmentForm.class),
                    examples = @ExampleObject(value = "alarmId=4547&action=ack")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The acknowledgement row that was written.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsAcknowledgment.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 23632,
                      "log": null,
                      "ackType": "ALARM",
                      "ackAction": "ACKNOWLEDGE",
                      "ackTime": 1787727613204,
                      "ackUser": "admin",
                      "refId": 4547
                    }"""))),
            @ApiResponse(responseCode = "304", description = "The id parsed but no alarm or notification has it."),
            @ApiResponse(responseCode = "400", description = "Neither or both ids were supplied, an id was not an integer, or `action` was not one of the four verbs.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "You must supply either an alarmId or notifId"))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'jroe', is not allowed to perform updates to alarms as user 'admin'"))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`.")
    })
    public Response acknowledge(@Context final SecurityContext securityContext, MultivaluedMap<String, String> formParams) {
        String alarmId = formParams.getFirst("alarmId");
        String notifId = formParams.getFirst("notifId");
        String action = formParams.getFirst("action");
        String ackUser = formParams.getFirst("ackUser");

        if (action == null) {
            action = "ack";
        }

        if (ackUser == null) {
            ackUser = securityContext.getUserPrincipal().getName();
        }

        SecurityHelper.assertUserEditCredentials(securityContext, ackUser);

        OnmsAcknowledgment ack = null;
        if (alarmId == null && notifId == null) {
            return getBadRequestResponse("You must supply either an alarmId or notifId");
        } else if (alarmId != null && notifId != null) {
            return getBadRequestResponse("You cannot supply both an alarmId and a notifId");
        } else if (alarmId != null) {
            final Integer numericAlarmId = getNumericValue(alarmId);
            if (numericAlarmId == null) {
                return getBadRequestResponse("The alarmId has to be an integer value");
            }
            final OnmsAlarm alarm = m_alarmDao.get(numericAlarmId);
            if (alarm == null) {
                return Response.notModified().build();
            }
            ack = new OnmsAcknowledgment(alarm, ackUser);
        } else if (notifId != null) {
            final Integer numericNotifId = getNumericValue(notifId);
            if (numericNotifId == null) {
                return getBadRequestResponse("The notifId has to be an integer value");
            }
            final OnmsNotification notification = m_notificationDao.get(numericNotifId);
            if (notification == null) {
                return Response.notModified().build();
            }
            ack = new OnmsAcknowledgment(notification, ackUser);
        }
        
        if ("ack".equals(action)) {
            ack.setAckAction(AckAction.ACKNOWLEDGE);
        } else if ("unack".equals(action)) {
            ack.setAckAction(AckAction.UNACKNOWLEDGE);
        } else if ("clear".equals(action)) {
            ack.setAckAction(AckAction.CLEAR);
        } else if ("esc".equals(action)) {
            ack.setAckAction(AckAction.ESCALATE);
        } else {
            return getBadRequestResponse("Must supply the action parameter, set to either 'ack, 'unack', 'clear', or 'esc'");
        }

        m_ackDao.processAck(ack);
        return Response.ok(ack).build();
    }

    private static CriteriaBuilder getQueryFilters(MultivaluedMap<String,String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsAcknowledgment.class);
        applyQueryFilters(params, builder);
        builder.orderBy("ackTime").desc();
        return builder;
    }
}
