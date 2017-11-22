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

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventRestService")
@Path("events")
public class EventRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(EventRestService.class);
    private static final DateTimeFormatter ISO8601_FORMATTER_MILLIS = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private EventIpcManager m_eventForwarder;

    /**
     * <p>
     * getEvent
     * </p>
     * 
     * @param eventId
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{eventId}")
    @Transactional
    public OnmsEvent getEvent(@PathParam("eventId") final Integer eventId) {
        final OnmsEvent e = m_eventDao.get(eventId);
        if (e == null) {
            throw getException(Status.NOT_FOUND, "Event object {} was not found.", Integer.toString(eventId));
        }
        return e;
    }

    /**
     * returns a plaintext string being the number of events
     * 
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_eventDao.countAll());
    }

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     * 
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws java.text.ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsEventCollection getEvents(@Context final UriInfo uriInfo) throws ParseException {
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters());
        builder.orderBy("eventTime").asc();

        final OnmsEventCollection coll = new OnmsEventCollection(m_eventDao.findMatching(builder.toCriteria()));
        coll.setTotalCount(m_eventDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     * 
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws java.text.ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("between")
    @Transactional
    public OnmsEventCollection getEventsBetween(@Context final UriInfo uriInfo) throws ParseException {
        final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        final String column;
        if (params.containsKey("column")) {
            column = params.getFirst("column");
            params.remove("column");
        } else {
            column = "eventTime";
        }
        Date begin;
        if (params.containsKey("begin")) {
            try {
                begin = ISO8601_FORMATTER.parseLocalDateTime(params.getFirst("begin")).toDate();
            } catch (final Throwable t1) {
                try {
                    begin = ISO8601_FORMATTER_MILLIS.parseDateTime(params.getFirst("begin")).toDate();
                } catch (final Throwable t2) {
                    throw getException(Status.BAD_REQUEST, "Can't parse start date");
                }
            }
            params.remove("begin");
        } else {
            begin = new Date(0);
        }
        Date end;
        if (params.containsKey("end")) {
            try {
                end = ISO8601_FORMATTER.parseLocalDateTime(params.getFirst("end")).toDate();
            } catch (final Throwable t1) {
                try {
                    end = ISO8601_FORMATTER_MILLIS.parseLocalDateTime(params.getFirst("end")).toDate();
                } catch (final Throwable t2) {
                    throw getException(Status.BAD_REQUEST, "Can't parse end date");
                }
            }
            params.remove("end");
        } else {
            end = new Date();
        }

        final CriteriaBuilder builder = getCriteriaBuilder(params);
        builder.match("all");
        try {
            builder.between(column, begin, end);
        } catch (final Throwable t) {
            throw getException(Status.BAD_REQUEST, "Unable to parse " + begin + " and " + end + " as dates!");
        }

        final OnmsEventCollection coll = new OnmsEventCollection(m_eventDao.findMatching(builder.toCriteria()));
        coll.setTotalCount(m_eventDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * Updates the event with id "eventid" If the "ack" parameter is "true",
     * then acks the events as the current logged in user, otherwise unacks
     * the events
     * 
     * @param eventId
     *            a {@link java.lang.Integer} object.
     * @param ack
     *            a {@link java.lang.Boolean} object.
     */
    @PUT
    @Path("{eventId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateEvent(@Context final SecurityContext securityContext, @PathParam("eventId") final Integer eventId, @FormParam("ack") final Boolean ack) {
        writeLock();

        try {
            final OnmsEvent event = getEvent(eventId);
            if (ack == null) {
                throw getException(Status.BAD_REQUEST, "Must supply the 'ack' parameter, set to either 'true' or 'false'");
            }
            processEventAck(securityContext, event, ack);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Updates all the events that match any filter/query supplied in the
     * form. If the "ack" parameter is "true", then acks the events as the
     * current logged in user, otherwise unacks the events
     * 
     * @param formProperties
     *            Map of the parameters passed in by form encoding
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateEvents(@Context final SecurityContext securityContext, final MultivaluedMapImpl formProperties) {
        writeLock();

        try {
            Boolean ack = false;
            if (formProperties.containsKey("ack")) {
                ack = "true".equals(formProperties.getFirst("ack"));
                formProperties.remove("ack");
            }

            final CriteriaBuilder builder = getCriteriaBuilder(formProperties);
            builder.orderBy("eventTime").desc();

            for (final OnmsEvent event : m_eventDao.findMatching(builder.toCriteria())) {
                processEventAck(securityContext, event, ack);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    private void processEventAck(final SecurityContext securityContext, final OnmsEvent event, final Boolean ack) {
        if (ack) {
            event.setEventAckTime(new Date());
            event.setEventAckUser(securityContext.getUserPrincipal().getName());
        } else {
            event.setEventAckTime(null);
            event.setEventAckUser(null);
        }
        m_eventDao.save(event);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public Response publishEvent(final org.opennms.netmgt.xml.event.Event event) {
        if (event.getSource() == null) {
            event.setSource("ReST");
        }
        if (event.getTime() == null) {
            event.setTime(new Date());
        }
        try {
            final Validator validator = factory.getValidator();
            final Set<ConstraintViolation<org.opennms.netmgt.xml.event.Event>> errors = validator.validate(event);
            LOG.debug("got errors: {}", errors);
            if (errors.size() > 0) {
                final StringBuilder sb = new StringBuilder("Error validating event:\n");
                for (final ConstraintViolation<?> error : errors) {
                    sb.append(error.toString()).append("\n");
                }
                LOG.debug(sb.toString());
                throw getException(Status.BAD_REQUEST, errors.size() + " errors found while validating event.");
            }
            m_eventForwarder.sendNow(event);
            return Response.accepted().build();
        } catch (final Exception e) {
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    private static CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsEvent.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("node.location", "location", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);

        applyQueryFilters(params, builder);
        return builder;
    }

}

