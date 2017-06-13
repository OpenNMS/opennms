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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsAlarm} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("alarms")
@Transactional
public class AlarmRestService extends AbstractDaoRestService<OnmsAlarm,Integer,Integer> {

    @Autowired
    private AlarmDao m_dao;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    @Autowired
    private AlarmRepository m_repository;

//    @Autowired
    private TroubleTicketProxy m_troubleTicketProxy;

    @Override
    protected AlarmDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsAlarm> getDaoClass() {
        return OnmsAlarm.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass(), "alarm");
        builder.fetch("firstEvent", FetchType.EAGER);
        builder.fetch("lastEvent", FetchType.EAGER);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty("snmpInterface.ifIndex", "alarm.ifIndex"), Restrictions.isNull("snmpInterface.ifIndex")));
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty("ipInterface.ipAddress", "alarm.ipAddr"), Restrictions.isNull("ipInterface.ipAddress")));
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        builder.alias("node.categories", "categories", JoinType.LEFT_JOIN);
        builder.alias("node.location", "location", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "service", JoinType.LEFT_JOIN);
        builder.orderBy("lastEventTime").desc(); // order by last event time by default
        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsAlarm> createListWrapper(Collection<OnmsAlarm> list) {
        return new OnmsAlarmCollection(list);
    }

    @Override
    protected Map<String, String> getBeanPropertiesMapping() {
        final Map<String, String> map = new HashMap<>();
        map.put("categoryName", "node.categories.name");
        map.put("service", "serviceType.name");
        return map;
    }

    @Override
    protected Map<String, String> getCriteriaPropertiesMapping() {
        final Map<String, String> map = new HashMap<>();
        map.put("node.categories.name", "categories.name");
        map.put("ipaddress", "ipAddr");
        map.put("ipAddress", "ipAddr");
        map.put("serviceType", "service");
        map.put("serviceType.name", "service.name");
        return map;
    }

    @Override
    protected OnmsAlarm doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    protected Response doUpdate(SecurityContext securityContext, UriInfo uriInfo, OnmsAlarm alarm, MultivaluedMapImpl params) {
        boolean isProcessAck = true;

        final String ackValue = params.getFirst("ack");
        final String escalateValue = params.getFirst("escalate");
        final String clearValue = params.getFirst("clear");
        final String ackUserValue = params.getFirst("ackUser");
        final String ticketIdValue = params.getFirst("ticketId");
        final String ticketStateValue = params.getFirst("ticketState");

        final String ackUser = ackUserValue == null ? securityContext.getUserPrincipal().getName() : ackUserValue;
        SecurityHelper.assertUserEditCredentials(securityContext, ackUser);

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
        } else if (StringUtils.isNotBlank(ticketIdValue)) {
            isProcessAck = false;
            alarm.setTTicketId(ticketIdValue);
        } else if (EnumUtils.isValidEnum(TroubleTicketState.class, ticketStateValue)) {
            isProcessAck = false;
            alarm.setTTicketState(TroubleTicketState.valueOf(ticketStateValue));
        } else {
            throw getException(Status.BAD_REQUEST, "Must supply one of the 'ack', 'escalate', or 'clear' parameters, set to either 'true' or 'false'.");
        }
        if (isProcessAck) {
            m_ackDao.processAck(acknowledgement);
        } else {
            getDao().saveOrUpdate(alarm);
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
        SecurityHelper.assertUserEditCredentials(securityContext, user);
        final String body = params.getFirst("body");
        if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
        m_repository.updateStickyMemo(alarmId, body, user);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
        SecurityHelper.assertUserEditCredentials(securityContext, user);
        final String body = params.getFirst("body");
        if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
        m_repository.updateReductionKeyMemo(alarmId, body, user);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        m_repository.removeStickyMemo(alarmId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        m_repository.removeReductionKeyMemo(alarmId);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/ticket/create")
    public Response createTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());

        return runIfTicketerPluginIsEnabled(() -> {
            final Map<String, String> parameters = new HashMap<>();
            parameters.put(EventConstants.PARM_USER, securityContext.getUserPrincipal().getName());
            m_troubleTicketProxy.createTicket(alarmId, parameters);
            return Response.status(Status.ACCEPTED).build();
        });
    }

    @POST
    @Path("{id}/ticket/update")
    public Response updateTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());

        return runIfTicketerPluginIsEnabled(() -> {
            m_troubleTicketProxy.updateTicket(alarmId);
            return Response.status(Status.ACCEPTED).build();
        });
    }

    @POST
    @Path("{id}/ticket/close")
    public Response closeTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());

        return runIfTicketerPluginIsEnabled(() -> {
            m_troubleTicketProxy.closeTicket(alarmId);
            return Response.status(Status.ACCEPTED).build();
        });
    }

    private Response runIfTicketerPluginIsEnabled(Callable<Response> callable) throws Exception {
        if (!isTicketerPluginEnabled()) {
            return Response.status(Status.NOT_IMPLEMENTED).entity("AlarmTroubleTicketer is not enabled. Cannot perform operation").build();
        }
        Objects.requireNonNull(callable);
        final Response response = callable.call();
        return response;
    }

    private boolean isTicketerPluginEnabled() {
        return "true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"));
    }
}
