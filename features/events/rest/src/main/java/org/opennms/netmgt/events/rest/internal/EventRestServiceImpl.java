/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgt.events.rest.internal;

//import org.hibernate.validator.HibernateValidatorFactory;
//import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.rest.EventRestService;
import org.opennms.netmgt.events.rest.ISO8601DateEditor;
import org.opennms.netmgt.events.rest.StringXmlCalendarPropertyEditor;
import org.opennms.netmgt.model.InetAddressTypeEditor;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSeverityEditor;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.PrimaryTypeEditor;
import org.opennms.web.rest.mapper.v2.EventMapper;
import org.opennms.web.rest.model.v2.EventCollectionDTO;
import org.opennms.web.rest.model.v2.EventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

//import javax.validation.Configuration;
//import javax.validation.ConstraintViolation;
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.XMLGregorianCalendar;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// TODO: SECURITY

@Path("events")
public class EventRestServiceImpl implements EventRestService {
    private static final Logger LOG = LoggerFactory.getLogger(EventRestServiceImpl.class);
    private static final DateTimeFormatter ISO8601_FORMATTER_MILLIS = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
//    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private EventDao m_eventDao;
    private EventForwarder m_eventForwarder;

    private EventMapper m_eventMapper;

    private SessionUtils sessionUtils;

// https://docs.jboss.org/hibernate/validator/4.3/reference/en-US/html_single/#section-resource-bundle-locator
//    static {
//        Configuration<?> config = Validation.byDefaultProvider()
//                .providerResolver( new OSGiServiceDiscoverer() )
//                .configure();
//    }

//========================================
// Getters and Setters
//========================================

    public EventDao getEventDao() {
        return m_eventDao;
    }

    public void setEventDao(EventDao eventDao) {
        this.m_eventDao = eventDao;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.m_eventForwarder = eventForwarder;
    }

    public EventMapper getEventMapper() {
        return m_eventMapper;
    }

    public void setEventMapper(EventMapper m_eventMapper) {
        this.m_eventMapper = m_eventMapper;
    }

    public SessionUtils getSessionUtils() {
        return sessionUtils;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

//========================================
// Interface
//========================================

    /**
     * <p>
     * getEvent
     * </p>
     *
     * @param eventId
     *            a {@link String} object.
     * @return a {@link OnmsEvent} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{eventId}")
    public EventDTO getEvent(@PathParam("eventId") Integer eventId) {
        Supplier<EventDTO> op = () -> {
            OnmsEvent onmsEvent = this.lookupRequiredEvent(eventId);

            return this.m_eventMapper.eventToEventDTO(onmsEvent);
        };

        return this.sessionUtils.withReadOnlyTransaction(op);
    }

    /**
     * returns a plaintext string being the number of events
     *
     * @return a {@link String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @RolesAllowed({"user", "admin"})
    public String getCount() {
        return Integer.toString(m_eventDao.countAll());
    }

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     *
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @RolesAllowed({"user", "admin"})
    public EventCollectionDTO getEvents(@Context final UriInfo uriInfo) throws ParseException {
        CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters());
        builder.orderBy("eventTime").asc();

        return this.sessionUtils.withReadOnlyTransaction(
                () -> this.dbLookupEventListWithCriteria(builder));
    }

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     *
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("between")
    @RolesAllowed({"user", "admin"})
    public EventCollectionDTO getEventsBetween(@Context UriInfo uriInfo) throws ParseException {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        return this.sessionUtils.withReadOnlyTransaction(
                () -> this.dbLookupEventsBetween(params)
        );
    }

    /**
     * Updates the event with id "eventid" If the "ack" parameter is "true",
     * then acks the events as the current logged in user, otherwise unacks
     * the events
     *
     * @param eventId
     *            a {@link Integer} object.
     * @param ack
     *            a {@link Boolean} object.
     */
    @PUT
    @Path("{eventId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed({"admin"})
    public Response updateEvent(@Context SecurityContext securityContext, @PathParam("eventId") Integer eventId, @FormParam("ack") Boolean ack) {
        if (ack == null) {
            throw new WebApplicationException(
                    Response
                        .status(Status.BAD_REQUEST)
                        .entity("Must supply the 'ack' parameter, set to either 'true' or 'false'")
                        .build()
            );
        }

        String username = securityContext.getUserPrincipal().getName();
        this.sessionUtils.withReadOnlyTransaction(() -> this.dbUpdateEvent(username, eventId, ack));

        return Response.noContent().build();
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
    @RolesAllowed({"admin"})
    public Response updateEvents(@Context SecurityContext securityContext, MultivaluedHashMap<String, String> formProperties) {
        Boolean ack = false;
        if (formProperties.containsKey("ack")) {
            ack = "true".equals(formProperties.getFirst("ack"));
            formProperties.remove("ack");
        }

        CriteriaBuilder builder = getCriteriaBuilder(formProperties);
        builder.orderBy("eventTime").desc();

        String username = securityContext.getUserPrincipal().getName();

        final boolean finalAck = ack;
        this.sessionUtils.withTransaction(() -> this.dbUpdateMatchingEvents(username, builder, finalAck));

        return Response.noContent().build();
    }


    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @RolesAllowed({"admin"})
    public Response publishEvent(final org.opennms.netmgt.xml.event.Event event) {
        if (event.getSource() == null) {
            event.setSource("ReST");
        }
        if (event.getTime() == null) {
            event.setTime(new Date());
        }
        try {
// TODO: validation
//            final Validator validator = factory.getValidator();
//            final Set<ConstraintViolation<org.opennms.netmgt.xml.event.Event>> errors = validator.validate(event);
//            LOG.debug("got errors: {}", errors);
//            if (errors.size() > 0) {
//                final StringBuilder sb = new StringBuilder("Error validating event:\n");
//                for (final ConstraintViolation<?> error : errors) {
//                    sb.append(error.toString()).append("\n");
//                }
//                LOG.debug(sb.toString());
//
//                throw new WebApplicationException(
//                        Response
//                                .status(Status.BAD_REQUEST)
//                                .entity(errors.size() + " errors found while validating event.")
//                                .build()
//                );
//            }
            m_eventForwarder.sendNowSync(event);
            return Response.accepted().build();
        } catch (Exception exc) {
            // TODO: any reason _not_ to use the built-in exception mapping and handling?
            throw new WebApplicationException(
                    Response
                            .status(Status.BAD_REQUEST)
                            .entity(exc.getMessage())
                            .build()
            );
        }
    }

//========================================
// Database Operations
//========================================

    private EventCollectionDTO dbLookupEventListWithCriteria(CriteriaBuilder builder) {
        OnmsEventCollection coll = new OnmsEventCollection(m_eventDao.findMatching(builder.toCriteria()));

        int totalCount = m_eventDao.countMatching(builder.count().toCriteria());
        EventCollectionDTO result = this.mapEventCollection(coll, totalCount);

        return result;
    }

    private EventCollectionDTO dbLookupEventsBetween(MultivaluedMap<String, String> params) {
        String column;
        if (params.containsKey("column")) {
            column = params.getFirst("column");
            params.remove("column");
        } else {
            column = "eventTime";
        }

        Date begin = this.parseParamDate(params, "begin");
        Date end = this.parseParamDate(params, "end");

        params.remove("begin");
        params.remove("end");

        CriteriaBuilder builder = getCriteriaBuilder(params);
        builder.match("all");

        try {
            builder.between(column, begin, end);
        } catch (final Exception exc) {
            throw new WebApplicationException(
                    Response
                            .status(Status.BAD_REQUEST)
                            .entity("Unable to parse " + begin + " and " + end + " as dates!")
                            .build()
            );
        }

        OnmsEventCollection coll = new OnmsEventCollection(m_eventDao.findMatching(builder.toCriteria()));
        int count = m_eventDao.countMatching(builder.count().toCriteria());

        EventCollectionDTO result = this.mapEventCollection(coll, count);

        return result;
    }

    private void dbUpdateEvent(String username, Integer eventId, boolean ack) {
        OnmsEvent event = this.lookupRequiredEvent(eventId);

        saveEventAndAck(username, event, ack);
    }

    private void dbUpdateMatchingEvents(String username, CriteriaBuilder builder, boolean ack) {
        for (OnmsEvent event : m_eventDao.findMatching(builder.toCriteria())) {
            saveEventAndAck(username, event, ack);
        }
    }

//========================================
// Internals
//========================================

    private OnmsEvent lookupRequiredEvent(Integer eventId) {
        OnmsEvent event = this.m_eventDao.get(eventId);
        if (event == null) {
            throw new WebApplicationException(
                    Response
                            .status(Status.NOT_FOUND)
                            .entity("Event object " + eventId + " was not found")
                            .build()
            );
        }

        return event;
    }

    private Date parseParamDate(MultivaluedMap<String, String> params, String paramName) {
        if (params.containsKey(paramName)) {
            String dateString = params.getFirst(paramName);

            try {
                return ISO8601_FORMATTER.parseLocalDateTime(dateString).toDate();
            } catch (Exception exc1) {
                try {
                    LOG.debug("failed to parse date using ISO-8601 format without milliseconds", exc1);

                    return ISO8601_FORMATTER_MILLIS.parseLocalDateTime(dateString).toDate();
                } catch (Exception exc2) {
                    LOG.debug("failed to parse date using ISO-8601 format with milliseconds", exc2);

                    throw new WebApplicationException(
                            Response
                                    .status(Status.BAD_REQUEST)
                                    .entity("Can't parse " + paramName + " date")
                                    .build()
                    );
                }
            }
        } else {
            return new Date(0);
        }
    }

    /**
     * Save the given event, with the needed ACK settings.
     *
     * @param username
     * @param event event to store
     * @param ack true => include ACK details in the stored event; false => exclude ACK details in the stored event.
     */
    private void saveEventAndAck(String username, OnmsEvent event, Boolean ack) {
        if (ack) {
            event.setEventAckTime(new Date());
            event.setEventAckUser(username);
        } else {
            event.setEventAckTime(null);
            event.setEventAckUser(null);
        }

        m_eventDao.save(event);
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

//=============================================
// Copied, and modified, from OnmsRestService
//=============================================

    private static final int DEFAULT_LIMIT = 10;


    private static BeanWrapper getBeanWrapperForClass(final Class<?> criteriaClass) {
        final BeanWrapper wrapper = new BeanWrapperImpl(criteriaClass);
        wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
        wrapper.registerCustomEditor(Date.class, new ISO8601DateEditor());
        wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
        wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
        wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
        return wrapper;
    }


    private static void applyQueryFilters(MultivaluedMap<String,String> p, CriteriaBuilder builder) {
        applyQueryFilters(p, builder, DEFAULT_LIMIT);
    }

    private static void applyQueryFilters(MultivaluedMap<String,String> params, CriteriaBuilder builder, Integer defaultLimit) {

        MultivaluedMap<String, String> paramsCopy = new MultivaluedHashMap();
        paramsCopy.putAll(params);

        builder.distinct();
        builder.limit(defaultLimit);

        if (paramsCopy.containsKey("limit")) {
            builder.limit(Integer.valueOf(paramsCopy.getFirst("limit")));
            paramsCopy.remove("limit");
        }
        if (paramsCopy.containsKey("offset")) {
            builder.offset(Integer.valueOf(paramsCopy.getFirst("offset")));
            paramsCopy.remove("offset");
        }

        if(paramsCopy.containsKey("orderBy")) {
            builder.clearOrder();
            builder.orderBy(paramsCopy.getFirst("orderBy"));
            paramsCopy.remove("orderBy");

            if(paramsCopy.containsKey("order")) {
                if("desc".equalsIgnoreCase(paramsCopy.getFirst("order"))) {
                    builder.desc();
                } else {
                    builder.asc();
                }
                paramsCopy.remove("order");
            }
        }

        if (Boolean.getBoolean("org.opennms.web.rest.enableQuery")) {
            String query = paramsCopy.getFirst("query");
            paramsCopy.remove("query");

            if (query != null) {
                builder.sql(query);
            }
        }

        String matchType;
        String match = paramsCopy.getFirst("match");
        paramsCopy.remove("match");

        if (match == null) {
            matchType = "all";
        } else {
            matchType = match;
        }
        builder.match(matchType);

        Class<?> criteriaClass = builder.toCriteria().getCriteriaClass();
        BeanWrapper wrapper = getBeanWrapperForClass(criteriaClass);

        String comparatorParam = paramsCopy.getFirst("comparitor");
        if (comparatorParam != null) {
            comparatorParam = comparatorParam.toLowerCase();
        } else {
            // Default to "eq"
            comparatorParam = "eq";
        }
        paramsCopy.remove("comparitor");

        Criteria currentCriteria = builder.toCriteria();

        for (String key : paramsCopy.keySet()) {
            for (String paramValue : paramsCopy.get(key)) { // NOSONAR
                // NOSONAR the interface of MultivaluedMap.class declares List<String> as return value,
                // the actual implementation com.sun.jersey.core.util.MultivaluedMapImpl returns a String, so this is fine in some way ...
                if ("null".equalsIgnoreCase(paramValue)) {
                    builder.isNull(key);
                } else if ("notnull".equalsIgnoreCase(paramValue)) {
                    builder.isNotNull(key);
                } else {
                    Object value;
                    Class<?> type = Object.class;
                    try {
                        type = currentCriteria.getType(key);
                    } catch (IntrospectionException e) {
                        LOG.debug("Unable to determine type for key {}", key);
                    }
                    if (type == null) {
                        type = Object.class;
                    }
                    LOG.debug("comparator = {}, key = {}, propertyType = {}", comparatorParam, key, type);

                    if (comparatorParam.equals("contains") || comparatorParam.equals("iplike") || comparatorParam.equals("ilike") || comparatorParam.equals("like")) {
                        value = paramValue;
                    } else {
                        LOG.debug("convertIfNecessary({}, {})", key, paramValue);
                        try {
                            value = wrapper.convertIfNecessary(paramValue, type);
                        } catch (final Throwable t) {
                            LOG.debug("failed to introspect (key = {}, value = {})", key, paramValue, t);
                            value = paramValue;
                        }
                    }

                    try {
                        Method m = builder.getClass().getMethod(comparatorParam, String.class, Object.class);
                        m.invoke(builder, new Object[] { key, value });
                    } catch (final Throwable t) {
                        LOG.warn("Unable to find method for comparator: {}, key: {}, value: {}", comparatorParam, key, value, t);
                    }
                }
            }
        }
    }

    private EventCollectionDTO mapEventCollection(OnmsEventCollection onmsEventCollection, int totalCount) {
        EventCollectionDTO result = new EventCollectionDTO();

        List<EventDTO> dtoList =
                onmsEventCollection.getObjects().stream()
                        .map(this.m_eventMapper::eventToEventDTO)
                        .collect(Collectors.toList())
                ;

        result.setObjects(dtoList);
        result.setCount(totalCount);

        return result;
    }

}

