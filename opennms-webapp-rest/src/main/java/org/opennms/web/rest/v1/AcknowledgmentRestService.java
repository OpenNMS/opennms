/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAcknowledgmentCollection;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNotification;
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
     * @param alarmId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{id}")
    @Transactional
    public OnmsAcknowledgment getAcknowledgment(@PathParam("id") Integer alarmId) {
        final OnmsAcknowledgment ack = m_ackDao.get(alarmId);
        if (ack == null) {
            throw getException(Status.NOT_FOUND, "Acknowledgement object {} was not found.", Integer.toString(alarmId));
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

        AlarmRestService.assertUserEditCredentials(securityContext, ackUser);

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
