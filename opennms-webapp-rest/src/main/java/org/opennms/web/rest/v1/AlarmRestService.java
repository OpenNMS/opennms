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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.AlarmSummaryCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("alarmRestService")
@Path("alarms")
@Tag(name = "Alarms", description = "Alarms API V1")
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
    public Response getAlarm(@Context SecurityContext securityContext, @PathParam("alarmId") final String alarmId) {
        SecurityHelper.assertUserReadCredentials(securityContext);
        if ("summaries".equals(alarmId)) {
            final List<AlarmSummary> collection = m_alarmDao.getNodeAlarmSummaries();
            return collection == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(new AlarmSummaryCollection(collection)).build();
        } else {
            final OnmsAlarm alarm = m_alarmDao.get(Integer.valueOf(alarmId));
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
        SecurityHelper.assertUserReadCredentials(securityContext);
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
        SecurityHelper.assertUserReadCredentials(securityContext);
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
            final String ticketIdValue = formProperties.getFirst("ticketId");
            formProperties.remove("ticketId");
            final String ticketStateValue = formProperties.getFirst("ticketState");
            formProperties.remove("ticketState");

            final OnmsAlarm alarm = m_alarmDao.get(alarmId);
            if (alarm == null) {
                return getBadRequestResponse("Unable to locate alarm with ID '" + alarmId + "'");
            }

            boolean alarmUpdated = false;
            if (StringUtils.isNotBlank(ticketIdValue)) {
                alarmUpdated = true;
                alarm.setTTicketId(ticketIdValue);
            }
            if (EnumUtils.isValidEnum(TroubleTicketState.class, ticketStateValue)) {
                alarmUpdated = true;
                alarm.setTTicketState(TroubleTicketState.valueOf(ticketStateValue));
            }
            if (alarmUpdated) {
                m_alarmDao.saveOrUpdate(alarm);
            }

            final String ackUser = ackUserValue == null ? securityContext.getUserPrincipal().getName() : ackUserValue;

            if (ackUser != null && StringUtils.isNotBlank(ackUser)) {
                SecurityHelper.assertUserEditCredentials(securityContext, ackUser);
            }

            final OnmsAcknowledgment acknowledgement = new OnmsAcknowledgment(alarm, ackUser);
            acknowledgement.setAckAction(AckAction.UNSPECIFIED);

            boolean isProcessAck = false;
            if (ackValue != null) {
                isProcessAck = true;
                if (Boolean.parseBoolean(ackValue)) {
                    acknowledgement.setAckAction(AckAction.ACKNOWLEDGE);
                } else {
                    acknowledgement.setAckAction(AckAction.UNACKNOWLEDGE);
                }
            } else if (escalateValue != null) {
                isProcessAck = true;
                if (Boolean.parseBoolean(escalateValue)) {
                    acknowledgement.setAckAction(AckAction.ESCALATE);
                }
            } else if (clearValue != null) {
                isProcessAck = true;
                if (Boolean.parseBoolean(clearValue)) {
                    acknowledgement.setAckAction(AckAction.CLEAR);
                }
            }
            if (isProcessAck) {
                m_ackDao.processAck(acknowledgement);
                m_ackDao.flush();
            }

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
            SecurityHelper.assertUserEditCredentials(securityContext, ackUser);

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

}
