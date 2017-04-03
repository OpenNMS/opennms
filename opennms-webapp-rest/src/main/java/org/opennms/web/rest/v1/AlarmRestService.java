/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v1;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.AlarmSummaryCollection;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("alarmRestService")
@Path("alarms")
public class AlarmRestService extends AlarmRestServiceBase {

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    /**
     * <p>
     * getAlarm
     * </p>
     * 
     * @param alarmId
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAlarm} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{alarmId}")
    @Transactional
    public Response getAlarm(@Context SecurityContext securityContext, @PathParam("alarmId") final Integer alarmId) {
        assertUserReadCredentials(securityContext);
        if ("summaries".equals(alarmId)) {
            final List<AlarmSummary> collection = m_alarmDao.getNodeAlarmSummaries();
            return collection == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(new AlarmSummaryCollection(collection)).build();
        } else {
            final OnmsAlarm alarm = m_alarmDao.get(alarmId);
            return alarm == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(alarm).build();
        }
    }

    /**
     * <p>
     * getCount
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount(@Context SecurityContext securityContext) {
        assertUserReadCredentials(securityContext);
        return Integer.toString(m_alarmDao.countAll());
    }

    /**
     * <p>
     * getAlarms
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.model.OnmsAlarmCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsAlarmCollection getAlarms(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo) {
        assertUserReadCredentials(securityContext);
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters(), false);
        builder.distinct();
        final OnmsAlarmCollection coll = new OnmsAlarmCollection(m_alarmDao.findMatching(builder.toCriteria()));

        // For getting totalCount
        coll.setTotalCount(m_alarmDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * <p>
     * updateAlarm
     * </p>
     * 
     * @param alarmId
     *            a {@link java.lang.String} object.
     * @param ack
     *            a {@link java.lang.Boolean} object.
     */
    @PUT
    @Path("{alarmId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateAlarm(@Context final SecurityContext securityContext, @PathParam("alarmId") final Integer alarmId, final MultivaluedMapImpl formProperties) {
        writeLock();

        try {
            if (alarmId == null) {
                return getBadRequestResponse("Unable to determine alarm ID to update based on query path.");
            }

            final String ackValue = formProperties.getFirst("ack");
            formProperties.remove("ack");
            final String escalateValue = formProperties.getFirst("escalate");
            formProperties.remove("escalate");
            final String clearValue = formProperties.getFirst("clear");
            formProperties.remove("clear");
            final String ackUserValue = formProperties.getFirst("ackUser");
            formProperties.remove("ackUser");

            final OnmsAlarm alarm = m_alarmDao.get(alarmId);
            if (alarm == null) {
                return getBadRequestResponse("Unable to locate alarm with ID '" + alarmId + "'");
            }

            final String ackUser = ackUserValue == null ? securityContext.getUserPrincipal().getName() : ackUserValue;
            assertUserEditCredentials(securityContext, ackUser);

            final OnmsAcknowledgment acknowledgement = new OnmsAcknowledgment(alarm, ackUser);
            acknowledgement.setAckAction(AckAction.UNSPECIFIED);
            if (ackValue != null) {
                if (Boolean.parseBoolean(ackValue)) {
                    acknowledgement.setAckAction(AckAction.ACKNOWLEDGE);
                } else {
                    acknowledgement.setAckAction(AckAction.UNACKNOWLEDGE);
                }
            } else if (escalateValue != null) {
                if (Boolean.parseBoolean(escalateValue)) {
                    acknowledgement.setAckAction(AckAction.ESCALATE);
                }
            } else if (clearValue != null) {
                if (Boolean.parseBoolean(clearValue)) {
                    acknowledgement.setAckAction(AckAction.CLEAR);
                }
            } else {
                return getBadRequestResponse("Must supply one of the 'ack', 'escalate', or 'clear' parameters, set to either 'true' or 'false'.");
            }
            m_ackDao.processAck(acknowledgement);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>
     * updateAlarms
     * </p>
     * 
     * @param formProperties
     *            a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateAlarms(@Context final SecurityContext securityContext, final MultivaluedMapImpl formProperties) {
        writeLock();

        try {
            final String ackValue = formProperties.getFirst("ack");
            formProperties.remove("ack");
            final String escalateValue = formProperties.getFirst("escalate");
            formProperties.remove("escalate");
            final String clearValue = formProperties.getFirst("clear");
            formProperties.remove("clear");

            final CriteriaBuilder builder = getCriteriaBuilder(formProperties, false);
            builder.distinct();
            builder.limit(0);
            builder.offset(0);

            final String ackUser = formProperties.containsKey("ackUser") ? formProperties.getFirst("ackUser") : securityContext.getUserPrincipal().getName();
            formProperties.remove("ackUser");
            assertUserEditCredentials(securityContext, ackUser);

            final List<OnmsAlarm> alarms = m_alarmDao.findMatching(builder.toCriteria());
            for (final OnmsAlarm alarm : alarms) {
                final OnmsAcknowledgment acknowledgement = new OnmsAcknowledgment(alarm, ackUser);
                acknowledgement.setAckAction(AckAction.UNSPECIFIED);
                if (ackValue != null) {
                    if (Boolean.parseBoolean(ackValue)) {
                        acknowledgement.setAckAction(AckAction.ACKNOWLEDGE);
                    } else {
                        acknowledgement.setAckAction(AckAction.UNACKNOWLEDGE);
                    }
                } else if (escalateValue != null) {
                    if (Boolean.parseBoolean(escalateValue)) {
                        acknowledgement.setAckAction(AckAction.ESCALATE);
                    }
                } else if (clearValue != null) {
                    if (Boolean.parseBoolean(clearValue)) {
                        acknowledgement.setAckAction(AckAction.CLEAR);
                    }
                } else {
                    throw getException(Status.BAD_REQUEST, "Must supply one of the 'ack', 'escalate', or 'clear' parameters, set to either 'true' or 'false'.");
                }
                m_ackDao.processAck(acknowledgement);
            }

            return alarms == null || alarms.isEmpty() ? Response.notModified().build() : Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    private static void assertUserReadCredentials(SecurityContext securityContext) {
        final String currentUser = securityContext.getUserPrincipal().getName();

        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            // admin can do anything
            return;
        }
        if (securityContext.isUserInRole(Authentication.ROLE_REST) ||
                securityContext.isUserInRole(Authentication.ROLE_USER) ||
                securityContext.isUserInRole(Authentication.ROLE_MOBILE)) {
            return;
        }
        // otherwise
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is not allowed to read alarms.").type(MediaType.TEXT_PLAIN).build());
    }

    public static void assertUserEditCredentials(final SecurityContext securityContext, final String ackUser) {
        final String currentUser = securityContext.getUserPrincipal().getName();

        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            // admin can do anything
            return;
        }
        if (securityContext.isUserInRole(Authentication.ROLE_READONLY)) {
            // read only is not allowed to edit
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is a read-only user!").type(MediaType.TEXT_PLAIN).build());
        }
        if (securityContext.isUserInRole(Authentication.ROLE_REST) ||
                securityContext.isUserInRole(Authentication.ROLE_USER) ||
                securityContext.isUserInRole(Authentication.ROLE_MOBILE)) {
            if (ackUser.equals(currentUser)) {
                // ROLE_REST and ROLE_MOBILE are allowed to modify things as long as it's as the
                // same user as they're logging in with.
                return;
            }
        }
        // otherwise
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("User '" + currentUser + "', is not allowed to perform updates to alarms as user '" + ackUser + "'").type(MediaType.TEXT_PLAIN).build());
    }

}
