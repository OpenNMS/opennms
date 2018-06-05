/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.PropertyNotFoundException;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchConditionVisitor;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.ISO8601DateEditor;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBuilderSearchVisitor;
import org.opennms.web.rest.support.DateCollection;
import org.opennms.web.rest.support.FloatCollection;
import org.opennms.web.rest.support.IntegerCollection;
import org.opennms.web.rest.support.LongCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;
import org.opennms.web.utils.CriteriaBuilderUtils;
import org.opennms.web.utils.QueryParameters;
import org.opennms.web.utils.QueryParametersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;

/**
 * Abstract class for easily implemented V2 endpoints.
 *
 * @param <T> Entity object (eg. OnmsEvent)
 * @param <D> DTO object (eg. EventDTO). This is the type of object that will be transfered
 *   to and from the client. Mapping to and from the entity objects is delegated to the
 *   implementation.
 * @param <Q> Query bean. This can be the same as the entity object if the object is a simple
 *   bean but for types with more than one level of bean properties, it makes sense to use a
 *   custom query bean or Apache CXF's {@link SearchBean}.
 * @param <K> Type of the primary key of the entity in the database (eg. Integer).
 * @param <I> Object Index (typically the same as the primary key, but can be different in some cases).
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Transactional
public abstract class AbstractDaoRestServiceWithDTO<T,D,Q,K extends Serializable,I extends Serializable> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoRestServiceWithDTO.class);

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Autowired
    private SessionFactory m_sessionFactory;

    private final ReadWriteUpdateLock m_globalLock = new ReentrantReadWriteUpdateLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    protected static final int DEFAULT_LIMIT = 10;

    protected abstract OnmsDao<T,K> getDao();
    protected abstract Class<T> getDaoClass();
    protected abstract Class<Q> getQueryBeanClass();
    protected abstract CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo);
    protected abstract JaxbListWrapper<D> createListWrapper(Collection<D> list);
    protected abstract T doGet(UriInfo uriInfo, I id); // Abstracted to be able to retrieve the object on different ways

    protected final void writeLock() {
        m_writeLock.lock();
    }

    protected final void writeUnlock() {
        m_writeLock.unlock();
    }

    // Do not allow create by default
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, T object) {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    // Do not allow update by default
    protected Response doUpdate(SecurityContext securityContext, UriInfo uriInfo, K key, T targetObject) {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    // Do not allow updating properties by default
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, T targetObject, final MultivaluedMapImpl params) {
        return Response.status(Status.NOT_IMPLEMENTED).build();
    }

    // Do not allow delete by default
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, T object) {
        throw new WebApplicationException(Response.status(Status.NOT_IMPLEMENTED).build());
    }

    /**
     * <p>Get a list of query properties that this endpoint supports
     * for FIQL expressions and {@code orderBy} expressions.</p>
     * 
     * @return
     */
    protected Set<SearchProperty> getQueryProperties() {
        return Collections.emptySortedSet();
    }

    /**
     * <p>Map properties in the search expression to bean properties
     * in the query capture bean. This is identical to using the
     * {@code search.bean.property.map} context property but allows us
     * to specify a different set of mappings for each service endpoint.</p>
     * <ul>
     * <li>Key: Query property name</li>
     * <li>Value: Bean property path</li>
     * </ul>
     * 
     * @see http://cxf.apache.org/docs/jax-rs-search.html#JAX-RSSearch-Mappingofquerypropertiestobeanproperties
     * 
     * @return
     */
    protected Map<String,String> getSearchBeanPropertyMap() {
        return null;
    }

    /**
     * <p>Map CXF query bean properties to Criteria property names, conversions,
     * and actions. In the absence of a mapping, the query bean property will be
     * specified directly as a Criteria property with the same name.</p>
     * <ul>
     * <li>Key: CXF query property name</li>
     * <li>Value: {@link CriteriaBehavior} to execute when this search term is specified</li>
     * </ul>
     * @return
     */
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        return null;
    }

    protected Criteria getCriteria(UriInfo uriInfo, SearchContext searchContext) {
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo);
        if (searchContext != null) {
            try {
                SearchCondition<Q> condition = searchContext.getCondition(getQueryBeanClass(), getSearchBeanPropertyMap());
                if (condition != null) {
                    SearchConditionVisitor<Q,CriteriaBuilder> visitor = new CriteriaBuilderSearchVisitor<T,Q>(builder, getDaoClass(), getCriteriaBehaviors());
                    condition.accept(visitor);
                }
            } catch (PropertyNotFoundException | ArrayIndexOutOfBoundsException e) {
                LOG.warn(e.getClass().getSimpleName() + " while parsing FIQL search, ignoring: " + e.getMessage(), e);
            }
        }

        // Apply limit, offset, orderBy, order parameters
        final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        applyLimitOffsetOrderBy(params, builder);
        Criteria crit = builder.toCriteria();

        /*
         * TODO: Figure out how to do stuff like this
         * 
         * // Don't include deleted nodes by default
         * final String type = params.getFirst("type");
         * if (type == null) {
         *     final List<Restriction> restrictions = new ArrayList<Restriction>(crit.getRestrictions());
         *     restrictions.add(Restrictions.ne("type", "D"));
         *     crit.setRestrictions(restrictions);
         * }
         */
        return crit;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        Criteria crit = getCriteria(uriInfo, searchContext);
        final List<T> coll = getDao().findMatching(crit);
        if (coll == null || coll.size() < 1) {
            return Response.status(Status.NO_CONTENT).build();
        } else {
            Integer offset = crit.getOffset();

            // Remove limit, offset and ordering when fetching count
            crit.setLimit(null);
            crit.setOffset(null);
            crit.setOrders(new ArrayList<Order>());
            int totalCount = getDao().countMatching(crit);

            // Map the entities to the corresponding DTOs
            final List<D> collOfDtos = coll.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
            final JaxbListWrapper<D> list = createListWrapper(collOfDtos);
            list.setTotalCount(totalCount);
            list.setOffset(offset);

            // Make sure that offset is set to a numeric value when setting the Content-Range header
            offset = (offset == null ? 0 : offset);
            return Response.ok(list).header("Content-Range", String.format("items %d-%d/%d", offset, offset + coll.size() - 1, totalCount)).build();
        }
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return Response.ok(String.valueOf(getDao().countMatching(getCriteria(uriInfo, searchContext)))).build();
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProperties(@QueryParam("q") final String query) {
        Set<SearchProperty> props = getQueryProperties();
        if (props != null && props.size() > 0) {
            return Response.ok(new SearchPropertyCollection(props.stream().filter(s -> {
                // If there is a query string...
                if (query != null && query.length() > 0) {
                    if (s.name.toLowerCase().indexOf(query.toLowerCase()) >= 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // If there is no query string, don't filter
                    return true;
                }
            }).collect(Collectors.toList()))).build();
        } else {
            return Response.noContent().build();
        }
    }

    private static class HibernateListCallback<T> implements HibernateCallback<List<T>> {
        private final SearchProperty property;
        private final String query;
        private final Integer limit;

        public HibernateListCallback(final SearchProperty property, final String query, final Integer limit) {
            this.property = property;
            this.query = query;
            this.limit = limit;
        }

        @Override
        public List<T> doInHibernate(Session session) throws HibernateException, SQLException {
            final Query hql;

            // TODO: Sort by count?

            // If there is a query string...
            if (query != null && query.length() > 0) {
                hql = session.createQuery(
                        String.format(
                                "select distinct %s from %s where lower(%s) like :query order by %s",
                                property.id,
                                property.entityClass.getSimpleName(),
                                property.id,
                                property.id
                        )
                );
                hql.setParameter("query", "%" + query.toLowerCase() + "%");
            } else {
                hql = session.createQuery(
                        String.format(
                                "select distinct %s from %s order by %s",
                                property.id,
                                property.entityClass.getSimpleName(),
                                property.id
                        )
                );
            }

            if (limit != null && limit > 0) {
                hql.setMaxResults(limit);
            }

            @SuppressWarnings("unchecked")
            List<T> list = (List<T>)hql.list();
            return list;
        }
    };

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) {
        Set<SearchProperty> props = getQueryProperties();
        // Find the property with the matching ID
        Optional<SearchProperty> prop = props.stream().filter(p -> p.getId().equals(propertyId)).findAny();
        if (prop.isPresent()) {
            SearchProperty property = prop.get();
            if (property.values != null && property.values.size() > 0) {
                final Set<String> validValues;
                if (query != null && query.length() > 0) {
                    validValues = property.values.keySet().stream().filter(v -> v.contains(query)).collect(Collectors.toSet());
                } else {
                    validValues = property.values.keySet();
                }

                switch(property.type) {
                    case FLOAT:
                        return Response.ok(new FloatCollection(validValues.stream().map(Float::parseFloat).collect(Collectors.toList()))).build();
                    case INTEGER:
                        return Response.ok(new IntegerCollection(validValues.stream().map(Integer::parseInt).collect(Collectors.toList()))).build();
                    case LONG:
                        return Response.ok(new LongCollection(validValues.stream().map(Long::parseLong).collect(Collectors.toList()))).build();
                    case IP_ADDRESS:
                    case STRING:
                        return Response.ok(new StringCollection(validValues)).build();
                    case TIMESTAMP:
                        return Response.ok(new DateCollection(validValues.stream().map(v -> {
                            try {
                                return ISO8601DateEditor.stringToDate(v);
                            } catch (IllegalArgumentException|UnsupportedOperationException e) {
                                LOG.error("Invalid date in value list: " + v, e);
                                return null;
                            }
                        })
                                // Filter out invalid null values
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))).build();
                    default:
                        return Response.noContent().build();
                }
            }

            switch(property.type) {
                case FLOAT:
                    List<Float> floats = new HibernateTemplate(m_sessionFactory).execute(new HibernateListCallback<Float>(property, query, limit));
                    return Response.ok(new FloatCollection(floats)).build();
                case INTEGER:
                    List<Integer> ints = new HibernateTemplate(m_sessionFactory).execute(new HibernateListCallback<Integer>(property, query, limit));
                    return Response.ok(new IntegerCollection(ints)).build();
                case LONG:
                    List<Long> longs = new HibernateTemplate(m_sessionFactory).execute(new HibernateListCallback<Long>(property, query, limit));
                    return Response.ok(new LongCollection(longs)).build();
                case IP_ADDRESS:
                    List<InetAddress> addresses = new HibernateTemplate(m_sessionFactory).execute(new HibernateListCallback<InetAddress>(property, query, limit));
                    return Response.ok(new StringCollection(addresses.stream().map(InetAddressUtils::str).collect(Collectors.toList()))).build();
                case STRING:
                    List<String> strings = new HibernateTemplate(m_sessionFactory).execute(new HibernateListCallback<String>(property, query, limit));
                    return Response.ok(new StringCollection(strings)).build();
                case TIMESTAMP:
                    List<Date> dates = new HibernateTemplate(m_sessionFactory).execute(new HibernateListCallback<Date>(property, query, limit));
                    return Response.ok(new DateCollection(dates)).build();
                default:
                    return Response.noContent().build();
            }
        } else {
            // 404
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    public Response get(@Context final UriInfo uriInfo, @PathParam("id") final I id) {
        T retval = doGet(uriInfo, id);
        if (retval == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.ok(mapEntityToDTO(retval)).build();
        }
    }

    @POST
    @Path("{id}")
    public Response createSpecific() {
        // Return a 404 if somebody tries to create with a specific ID
        return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, D object) {
        writeLock();
        try {
            return doCreate(securityContext, uriInfo, mapDTOToEntity(object));
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        writeLock();
        try {
            Criteria crit = getCriteria(uriInfo, searchContext);
            final List<T> objects = getDao().findMatching(crit);
            if (objects == null || objects.size() == 0) {
                return Response.status(Status.NOT_FOUND).build();
            }
            for (T object : objects) {
                RestUtils.setBeanProperties(object, params);
                doUpdateProperties(securityContext, uriInfo, object, params);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    public Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final K id, final T object) {
        writeLock();
        try {
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return doUpdate(securityContext, uriInfo, id, object);
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    public Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final I id, final MultivaluedMapImpl params) {
        writeLock();
        try {
            final T object = doGet(uriInfo, id);
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return doUpdateProperties(securityContext, uriInfo, object, params);
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        writeLock();
        try {
            Criteria crit = getCriteria(uriInfo, searchContext);
            final List<T> objects = getDao().findMatching(crit);
            if (objects == null || objects.size() == 0) {
                return Response.status(Status.NOT_FOUND).build();
            }
            for (T object : objects) {
                doDelete(securityContext, uriInfo, object);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final I id) {
        writeLock();
        try {
            final T object = doGet(uriInfo, id);
            if (object == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            doDelete(securityContext, uriInfo, object);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    public static void applyLimitOffsetOrderBy(final MultivaluedMap<String,String> p, final CriteriaBuilder builder) {
        applyLimitOffsetOrderBy(p, builder, DEFAULT_LIMIT);
    }

    private static void applyLimitOffsetOrderBy(final MultivaluedMap<String,String> p, final CriteriaBuilder builder, final Integer defaultLimit) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(p);
        if (queryParameters.getLimit() == null) {
            queryParameters.setLimit(defaultLimit);
        }
        CriteriaBuilderUtils.applyQueryParameters(builder, queryParameters);
    }

    protected void sendEvent(final Event event) {
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", event.getUei(), e.getMessage());
        }
    }

    protected WebApplicationException getException(final Status status, String msg, String... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    /**
     * Map the given instance of the entity to the corresponding DTO.
     */
    public abstract D mapEntityToDTO(T entity);

    /**
     * Map the given instance of the DTO to the corresponding entity.
     */
    public abstract T mapDTOToEntity(D dto);
}
