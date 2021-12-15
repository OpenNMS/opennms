/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.rest.internal;


import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.WebApplicationException;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.alarmd.rest.AlarmRestService;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.rest.mapper.v2.AlarmMapper;
import org.opennms.web.rest.model.v2.AlarmCollectionDTO;
import org.opennms.web.rest.model.v2.AlarmDTO;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;

import org.slf4j.helpers.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Web Service using REST for {@link OnmsAlarm} entity, but from Karaf container.
 * ...based of of v2 of the currently existing rest webservice for alarmD
 * @author Mark Bordelon
 */
@Path("/alarms")
public class AlarmRestServiceImpl implements AlarmRestService {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmRestServiceImpl.class);


    private AlarmDao alarmDao;

    private AlarmMapper m_alarmMapper;

    private AlarmRepository alarmRepository;

    //private TroubleTicketProxy troubleTicketProxy;

    private SessionUtils sessionUtils;

//========================================
// Getters and Setters
//========================================


    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    public void setAlarmRepository(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
    }

//    public void setTroubleTicketProxy(TroubleTicketProxy troubleTicketProxy) {
//        this.troubleTicketProxy = troubleTicketProxy;
//    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    public void setAlarmMapper(AlarmMapper m_alarmMapper) {
        this.m_alarmMapper = m_alarmMapper;
    }

//========================================
//
//========================================

    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass(), Aliases.alarm.toString());

        builder.fetch("lastEvent", FetchType.EAGER);

        // 1st level JOINs
        builder.alias("lastEvent", "lastEvent", JoinType.LEFT_JOIN);
        builder.alias("distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("ipInterfaces"), Aliases.ipInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.ipInterface.prop("ipAddress"), Aliases.alarm.prop("ipAddr")), Restrictions.isNull(Aliases.ipInterface.prop("ipAddress"))));
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("snmpInterfaces"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.snmpInterface.prop("ifIndex"), Aliases.alarm.prop("ifIndex")), Restrictions.isNull(Aliases.snmpInterface.prop("ifIndex"))));

        builder.orderBy("lastEventTime").desc(); // order by last event time by default

        return builder;
    }

    protected Class<OnmsAlarm> getDaoClass() {
        return OnmsAlarm.class;
    }
    protected WebApplicationException getException(final Status status, String msg, String... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    private boolean isTicketerPluginEnabled() {
        return "true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"));
    }

    private Response runIfTicketerPluginIsEnabled(Callable<Response> callable) throws Exception {
        if (!isTicketerPluginEnabled()) {
            return Response.status(Status.NOT_IMPLEMENTED).entity("AlarmTroubleTicketer is not enabled. Cannot perform operation").build();
        }
        Objects.requireNonNull(callable);
        final Response response = callable.call();
        return response;
    }


//========================================
// Interface
//========================================

    /**
     * <p>
     * getAlarms
     * </p>
     *
     * @return a Response containing {@link org.opennms.netmgt.model.OnmsAlarmCollection} object.
     */
    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response getAlarms(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo) {
        // replace the next line with @RolesAllowed("")
        SecurityHelper.assertUserReadCredentials(securityContext);

        return this.sessionUtils.withReadOnlyTransaction(() -> {
            CriteriaBuilder builder = getCriteriaBuilder(uriInfo);
            builder.distinct();

            List<OnmsAlarm> matchingAlarms = this.alarmDao.findMatching(builder.toCriteria());

            List<AlarmDTO> dtoAlarmList =
                matchingAlarms
                        .stream()
                        .map(this.m_alarmMapper::alarmToAlarmDTO)
                        .collect(Collectors.toList())
                        ;

            AlarmCollectionDTO alarmsCollection = new AlarmCollectionDTO(dtoAlarmList);
            alarmsCollection.setTotalCount(dtoAlarmList.size());

            return Response.status(Status.ACCEPTED).entity(alarmsCollection).build();
        });

    }

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        // replace the next two lines with @RolesAllowed("")
        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
        SecurityHelper.assertUserEditCredentials(securityContext, user);

        return this.sessionUtils.withTransaction(() -> {
            final String body = params.getFirst("body");
            if (body == null) { throw getException(Status.BAD_REQUEST, "Body cannot be null."); }
            alarmRepository.updateStickyMemo(alarmId, body, user); // TODO doing anything??
            return Response.noContent().build();
        });
    }

    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateJournal(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        return this.sessionUtils.withTransaction(() -> {
            final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
            // SecurityHelper.assertUserEditCredentials(securityContext, user);
            final String body = params.getFirst("body");
            if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
            alarmRepository.updateReductionKeyMemo(alarmId, body, user); // TODO doing anything??
            return Response.noContent().build();
        });
    }

    @DELETE
    @Path("{id}/memo")
    public Response removeMemo(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId)  {
        //SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        try {
            return runIfTicketerPluginIsEnabled(() -> {
                return this.sessionUtils.withTransaction(() -> {
                    alarmRepository.removeStickyMemo(alarmId); // TODO doing anything??
                    return Response.noContent().build();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

/*
    @POST
    @Path("{id}/ticket/update")
    public Response updateTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception {
        // SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        return runIfTicketerPluginIsEnabled(() -> {
            return this.sessionUtils.withTransaction(() -> {
                troubleTicketProxy.updateTicket(alarmId);
                return Response.status(Status.ACCEPTED).build();
            });
        });
    }
*/

/*
    @POST
    @Path("{id}/ticket/close")
    public Response closeTicket(@Context final SecurityContext securityContext, @PathParam("id") final Integer alarmId) throws Exception {
        // SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        return runIfTicketerPluginIsEnabled(() -> {
            return this.sessionUtils.withTransaction(() -> {
                troubleTicketProxy.closeTicket(alarmId);
                return Response.status(Status.ACCEPTED).build();
            });
        });
    }
*/


    @GET
    @Path("/testjaxrs")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response echoJaxrsSecCtxAnyone(@Context SecurityContext context) {
        final String[] GROUPS = {"admingroup", "group", "capybaras"};
        final String[] ROLES = {"admin", "manager", "viewer", "ssh", "read-only", "user", "ROLE_USER"};

        Token token = new Token("jaxrs test: check logs for user's role membership");

        // get access to user principle via SecurityContext, check role membership
        Principal userPrincipal = context.getUserPrincipal();

        if (userPrincipal != null) {
            token.setUserPrincipal(userPrincipal.getName());
            for (String role : ROLES) {
                LOG.info("User {} {} member of Role {}.", userPrincipal.getName(), (context.isUserInRole(role)? "is" : "is not"), role);
            }
        } else {
            token.setError("no user principal in context");
        }
        return Response.ok(token).build();

    }


    @GET
    @Path("/testjaas/user")
    @RolesAllowed("user")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response echoJaasUserPrincipalUser() {

        Token token = new Token("jaas test");

        // get access to subject via container, and get user principal from subject principles
        // (cannot check role membership without a JaxRs SecurityContext)
        AccessControlContext acc = AccessController.getContext();
        if (acc == null) {
            token.setError("access control context is null");
        }
        Subject subject = Subject.getSubject(acc);
        if (subject == null) {
            token.setError("subject is null");
        } else {
            Optional<Principal> anyPrincipal = subject.getPrincipals().stream().filter(p-> (p instanceof UserPrincipal)).findAny();
            if (anyPrincipal.isPresent()) {
                Principal userPrincipal = anyPrincipal.get();
                token.setUserPrincipal(userPrincipal.getName());
            }
        }

        return Response.ok(token).build();
    }




    @GET
    @Path("/testjaas/admin")
    @RolesAllowed("admin") // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response echoJaasUserPrincipalAdmin() {

        Token token = new Token("jaas test");

        // get access @RolesAllowed to subject via container, and get user principal from subject principles
        // (cannot check role membership without a JaxRs SecurityContext)
        AccessControlContext acc = AccessController.getContext();
        if (acc == null) {
            token.setError("access control context is null");
        }
        Subject subject = Subject.getSubject(acc);
        if (subject == null) {
            token.setError("subject is null");
        } else {
            Set<Principal> allPrincipals = subject.getPrincipals();
            Optional<Principal> anyUserPrincipal = allPrincipals.stream().filter(p-> (p instanceof UserPrincipal)).findAny();
            if (anyUserPrincipal.isPresent()) {
                Principal userPrincipal = anyUserPrincipal.get();
                token.setUserPrincipal(userPrincipal.getName());
            }
        }

        return Response.ok(token).build();
    }






}

