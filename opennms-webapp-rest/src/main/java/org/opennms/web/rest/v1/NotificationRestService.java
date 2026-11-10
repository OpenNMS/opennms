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

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.logging.Logging;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsNotificationCollection;
import org.opennms.netmgt.notifd.api.NotificationConfigProvider;
import org.opennms.netmgt.notifd.api.NotificationTester;
import org.opennms.netmgt.provision.service.MonitorHolder;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.model.AckOnlyForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>NotificationRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Component("notificationRestService")
@Path("notifications")
@Tag(name = "Notifications", description = "Notifications API")
public class NotificationRestService extends OnmsRestService {
    @Autowired
    private NotificationDao m_notifDao;

    /**
     * <p>getNotification</p>
     *
     * @param notifId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNotification} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{notifId}")
    @Transactional
    @Operation(
            summary = "Get a notification",
            description = """
                    Return one notification by id, including the per-user delivery records in `destinations`.
                    `ackUser` and `ackTime` carry the `answeredBy` and `respondTime` columns, so they are null
                    while the notification is outstanding.
                    The derived schema shows `date-time`; the wire carries epoch milliseconds in JSON and ISO-8601
                    strings in XML.""",
            operationId = "getNotificationV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The notification.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsNotification.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 2615,
                      "uei": "uei.opennms.org/nodes/nodeUp",
                      "nodeId": 2,
                      "nodeLabel": "loopback-001",
                      "subject": "Notice #2615: Node loopback-001 has been cleared.",
                      "textMessage": "The node loopback-001 which was previously down is now up.",
                      "numericMessage": "111-2615",
                      "severity": "NORMAL",
                      "serviceType": null,
                      "ackUser": null,
                      "ackTime": null,
                      "ackId": 2615,
                      "pageTime": 1787074522047,
                      "queueId": "default",
                      "eventId": 9650,
                      "type": "NOTIFICATION",
                      "destinations": [
                        {
                          "id": 4801,
                          "userId": "admin",
                          "contactInfo": "",
                          "notifyTime": 1787074527360,
                          "media": "browser",
                          "autoNotify": "C"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No notification with that id, or the path segment is not an integer.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Notification 99999999 was not found.")))
    })
    public OnmsNotification getNotification(
            @Parameter(description = "Notification id.", example = "2615", required = true)
            @PathParam("notifId") Integer notifId) {
        if (notifId == null) {
            throw getException(Status.BAD_REQUEST, "Notification ID is required");
        }
        final OnmsNotification notif = m_notifDao.get(notifId);
        if (notif == null) {
            throw getException(Status.NOT_FOUND, "Notification {} was not found.", Integer.toString(notifId));
        }
        return notif;
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
            summary = "Count all notifications",
            description = "Return the total number of notification rows as a plain-text integer. Query parameters "
                    + "are ignored.",
            operationId = "getNotificationCountV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The notification count.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "2613")))
    })
    public String getCount() {
        return Integer.toString(m_notifDao.countAll());
    }

    @GET
    @Path("summary")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get a notification summary for the caller",
            description = """
                    Return outstanding notification counts for the authenticated user, plus up to the ten newest
                    notifications addressed to them.
                    This operation produces JSON only. A request that asks for XML does not get a 406: the
                    `{notifId}` route matches the literal `summary` instead, and the integer conversion fails with
                    404.
                    `userUnacknowledgedCount` and `teamUnacknowledgedCount` are counted over a join against the
                    per-user delivery rows, so a notification delivered to a user by several media is counted once
                    per delivery and these figures can exceed `totalCount`.""",
            operationId = "getNotificationSummaryV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The summary.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = NotificationSummary.class),
                            examples = @ExampleObject(value = """
                    {
                      "user": "admin",
                      "totalCount": 2613,
                      "totalUnacknowledgedCount": 2293,
                      "userUnacknowledgedCount": 0,
                      "teamUnacknowledgedCount": 1,
                      "userUnacknowledgedNotifications": {
                        "totalCount": null,
                        "count": null,
                        "offset": 0,
                        "notification": []
                      }
                    }"""))),
            @ApiResponse(responseCode = "404", description = "The request asked for a media type other than JSON, so the `{notifId}` route was matched instead.")
    })
    public NotificationSummary getInfo(@Context final SecurityContext securityContext) {
        final String user = securityContext.getUserPrincipal().getName();
        final NotificationSummary info = new NotificationSummary();
        info.setUser(user);

        // All notifications (ack + unack)
        info.setTotalCount(m_notifDao.countAll());

        // All unack notifications
        info.setTotalUnacknowledgedCount(m_notifDao.countMatching(new CriteriaBuilder(OnmsNotification.class).isNull("answeredBy").toCriteria()));

        // All unacknowledged notifications for current user. A notification holds one
        // usersNotified row per notification method, so the join must be counted over
        // distinct notifications rather than over rows.
        info.setUserUnacknowledgedCount(m_notifDao.countMatching(new CriteriaBuilder(OnmsNotification.class).isNull("answeredBy")
                .alias("usersNotified", "usersNotified").eq("usersNotified.userId", user)
                .distinct()
                .toCriteria()));

        // Determine number of notices not acknowledged and not "assigned to" current user
        info.setTeamUnacknowledgedCount(m_notifDao.countMatching(new CriteriaBuilder(OnmsNotification.class)
                .isNull("answeredBy")
                .alias("usersNotified", "usersNotified", JoinType.LEFT_JOIN)
                .or(Restrictions.ne("usersNotified.userId", user), Restrictions.isNull("usersNotified.userId"))
                .distinct()
                .toCriteria()));

        // Load newest unacknowledged notifications for user, but only N
        if (info.getUserUnacknowledgedCount() != 0) {
            final List<OnmsNotification> newestNotifications = m_notifDao.findMatching(new CriteriaBuilder(OnmsNotification.class).isNull("answeredBy")
                    .alias("usersNotified", "usersNotified").eq("usersNotified.userId", user)
                    .distinct()
                    .orderBy("pageTime", false)
                    .limit(10)
                    .toCriteria());
            info.setUserUnacknowledgedNotifications(new OnmsNotificationCollection(newestNotifications));
        }
        return info;
    }

    /**
     * <p>getNotifications</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNotificationCollection} object.
     */
    @GET
    // We have to have a blank path here so that the UriBuilder calls work
    @Path("")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Operation(
            summary = "Search notifications",
            description = """
                    Return notifications matching the query parameters, highest `notifyId` first unless `orderBy`
                    says otherwise.
                    Filters are `OnmsNotification` property names, with `node.*`, `event.*`, `ipInterface.*`,
                    `snmpInterface.*` and `usersNotified.*` reachable through their aliases. Outstanding
                    notifications are the ones matching `answeredBy=null`. `limit` (default 10), `offset`,
                    `orderBy`, `order`, `match` and `comparator` shape the result. A filter name that is not a
                    property of the entity fails with 500.""",
            operationId = "getNotificationsV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The matching notifications.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsNotificationCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 2613,
                      "count": 1,
                      "offset": 0,
                      "notification": [ {
                          "id": 2615,
                          "uei": "uei.opennms.org/nodes/nodeUp",
                          "nodeId": 2,
                          "nodeLabel": "loopback-001",
                          "subject": "Notice #2615: Node loopback-001 has been cleared.",
                          "textMessage": "The node loopback-001 which was previously down is now up.",
                          "numericMessage": "111-2615",
                          "severity": "NORMAL",
                          "serviceType": null,
                          "ackUser": null,
                          "ackTime": null,
                          "ackId": 2615,
                          "pageTime": 1787074522047,
                          "queueId": "default",
                          "eventId": 9650,
                          "type": "NOTIFICATION",
                          "destinations": [
                            {
                              "id": 4801,
                              "userId": "admin",
                              "contactInfo": "",
                              "notifyTime": 1787074527360,
                              "media": "browser",
                              "autoNotify": "C"
                            }
                          ]
                        } ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "A query parameter is not a property of the notification entity.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public OnmsNotificationCollection getNotifications(@Context final UriInfo uriInfo) {
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters());
        builder.orderBy("notifyId").desc();

        OnmsNotificationCollection coll = new OnmsNotificationCollection(m_notifDao.findMatching(builder.toCriteria()));

        coll.setTotalCount(m_notifDao.countMatching(builder.count().toCriteria()));

        return coll;
    }
    
    /**
     * <p>updateNotification</p>
     *
     * @param notifId a {@link java.lang.String} object.
     * @param ack a {@link java.lang.Boolean} object.
     */
    @PUT
    @Path("{notifId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Acknowledge or unacknowledge one notification",
            description = """
                    Set or clear `answeredBy` and `respondTime` on one notification. The answering user is taken
                    from the authenticated principal and cannot be overridden.
                    `ack` has to be present. Any value other than `true` parses as `false` and clears the
                    acknowledgement.""",
            operationId = "updateNotificationV1"
    )
    @RequestBody(required = true, description = "The acknowledge flag.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AckOnlyForm.class),
                    examples = @ExampleObject(value = "ack=true")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The notification was updated."),
            @ApiResponse(responseCode = "400", description = "`ack` was absent.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Must supply the 'ack' parameter, set to either 'true' or 'false'"))),
            @ApiResponse(responseCode = "404", description = "No notification with that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Notification 99999999 was not found."))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`.")
    })
    public Response updateNotification(@Context final SecurityContext securityContext,
            @Parameter(description = "Notification id.", example = "2615", required = true)
            @PathParam("notifId") final Integer notifId, @FormParam("ack") final Boolean ack) {
        writeLock();
        
        try {
            if(ack==null) {
                throw getException(Status.BAD_REQUEST, "Must supply the 'ack' parameter, set to either 'true' or 'false'");
            }
            OnmsNotification notif= getNotification(notifId);
            processNotifAck(securityContext, notif,ack);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateNotifications</p>
     *
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Acknowledge or unacknowledge matching notifications",
            description = """
                    Set or clear the acknowledgement on every notification matching the filters in the form body.
                    Fields other than `ack` are read as filters on `OnmsNotification` property names such as
                    `notifyId` and `nodeId`.
                    `ack` is optional here and defaults to `false`, and only the exact string `true`
                    acknowledges. The default limit of 10 applies, so a large match set is processed one page at a
                    time. A body that matches nothing still returns 204.""",
            operationId = "updateNotificationsV1"
    )
    @RequestBody(required = true, description = "The acknowledge flag, plus the filters selecting the notifications.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AckOnlyForm.class),
                    examples = @ExampleObject(value = "ack=true&notifyId=2615")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The request was processed, whether or not anything matched."),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`."),
            @ApiResponse(responseCode = "500", description = "A form parameter is not a property of the notification entity.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public Response updateNotifications(@Context final SecurityContext securityContext, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            Boolean ack=false;
            if(params.containsKey("ack")) {
                ack="true".equals(params.getFirst("ack"));
                params.remove("ack");
            }

            final CriteriaBuilder builder = getCriteriaBuilder(params);
            
            for (final OnmsNotification notif : m_notifDao.findMatching(builder.toCriteria())) {
                processNotifAck(securityContext, notif, ack);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("destination-paths/{destinationPathName}/trigger")
    @Transactional
    @Operation(
            summary = "Trigger a destination path",
            description = """
                    Run every notification command of every target on a destination path. The request takes no
                    body and requires `ROLE_ADMIN`.
                    A path name with no targets, including one that does not exist in `destinationPaths.xml`,
                    returns 204 without doing anything. Otherwise the commands are executed synchronously and the
                    response is 202 once they have all been attempted.
                    Only the path's own targets are used; escalation levels are not walked.""",
            operationId = "triggerDestinationPathV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "The path had targets and its commands were run."),
            @ApiResponse(responseCode = "204", description = "The path has no targets, or does not exist."),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`. The body is "
                    + "the handler's message when the request reaches the resource, and the container's error page "
                    + "when a security filter rejects it first.")
    })
    public Response triggerDestinationPath(@Context final SecurityContext securityContext,
            @Parameter(description = "Destination path name as it appears in `destinationPaths.xml`.",
                    example = "Email-Admin", required = true)
            @PathParam("destinationPathName") final String destinationPathName) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw getException(Status.FORBIDDEN, "User {} does not have access to trigger notifications.", securityContext.getUserPrincipal().getName());
        }

        NotificationConfigProvider notificationConfigProvider = BeanUtils.getBean("notifdContext",
                "notificationConfigProvider", NotificationConfigProvider.class);
        NotificationTester notificationTester = BeanUtils.getBean("notifdContext",
                "notificationTester", NotificationTester.class);

        List<String> targetNames = notificationConfigProvider.getTargetNames(destinationPathName, false);
        if (targetNames.isEmpty()) {
            return Response.noContent().build();
        }

        for (String targetName : targetNames) {
            for (String command : notificationConfigProvider.getCommands(destinationPathName, targetName, false)) {
                try(Logging.MDCCloseable ignored = Logging.withPrefixCloseable("notifd")) {
                    notificationTester.triggerNotificationsForTarget(targetName, command);
                }
            }
        }

        return Response.accepted().build();
    }

    private void processNotifAck(final SecurityContext securityContext, final OnmsNotification notif, final Boolean ack) {
        if(ack) {
            notif.setRespondTime(new Date());
            notif.setAnsweredBy(securityContext.getUserPrincipal().getName());
        } else {
            notif.setRespondTime(null);
            notif.setAnsweredBy(null);
        }
        m_notifDao.save(notif);
    }

    private static CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNotification.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("event", "event", JoinType.LEFT_JOIN);
        builder.alias("usersNotified", "usersNotified", JoinType.LEFT_JOIN);

        applyQueryFilters(params, builder);
        return builder;
    }

}
