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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.PropertyNotFoundException;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchConditionVisitor;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.CriteriaBuilderSearchVisitor;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;

@Transactional
public abstract class AbstractDaoRestService<T,K extends Serializable> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoRestService.class);

	private final ReadWriteUpdateLock m_globalLock = new ReentrantReadWriteUpdateLock();
	private final Lock m_writeLock = m_globalLock.writeLock();

	protected static final int DEFAULT_LIMIT = 10;

	protected abstract OnmsDao<T,K> getDao();
	protected abstract Class<T> getDaoClass();
	protected abstract CriteriaBuilder getCriteriaBuilder();
	protected abstract JaxbListWrapper<T> createListWrapper(Collection<T> list);

	protected final void writeLock() {
		m_writeLock.lock();
	}

	protected final void writeUnlock() {
		m_writeLock.unlock();
	}

	protected Criteria getCriteria(UriInfo uriInfo, SearchContext searchContext) {
		final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

		final CriteriaBuilder builder = getCriteriaBuilder();

		if (searchContext != null) {
			try {
				SearchCondition<T> condition = searchContext.getCondition(getDaoClass());
				if (condition != null) {
					SearchConditionVisitor<T,CriteriaBuilder> visitor = new CriteriaBuilderSearchVisitor<T>(builder, getDaoClass());
					condition.accept(visitor);
				}
			} catch (PropertyNotFoundException | ArrayIndexOutOfBoundsException e) {
				LOG.warn(e.getClass().getSimpleName() + " while parsing FIQL search, ignoring: " + e.getMessage());
			}
		}

		// Apply limit, offset, orderBy, order params
		applyLimitOffsetOrderBy(params, builder);

		Criteria crit = builder.toCriteria();

		/*
		TODO: Figure out how to do stuff like this

		// Don't include deleted nodes by default
		final String type = params.getFirst("type");
		if (type == null) {
			final List<Restriction> restrictions = new ArrayList<Restriction>(crit.getRestrictions());
			restrictions.add(Restrictions.ne("type", "D"));
			crit.setRestrictions(restrictions);
		}
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

			JaxbListWrapper<T> list = createListWrapper(coll);
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
	@Path("{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
	public Response get(@PathParam("id") final K id) {
		T retval = getDao().get(id);
		if (retval == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.ok(retval).build();
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
	public Response create(@Context final UriInfo uriInfo, T object) {
		K id = getDao().save(object);
		return Response.created(getRedirectUri(uriInfo, id)).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updateMany(@Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
		// TODO: Implement me
		return Response.status(Status.NOT_IMPLEMENTED).build();
	}

	@PUT
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Path("{id}")
	public Response update(@Context final UriInfo uriInfo, @PathParam("id") final K id, final T object) {
		writeLock();

		try {
			// TODO: Assert that the ID of the path equals the ID of the object

			if (object == null) {
				return Response.status(Status.NOT_FOUND).build();
			}

			LOG.debug("update: updating object {}", object);

			getDao().saveOrUpdate(object);
			return Response.noContent().build();
		} finally {
			writeUnlock();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("{id}")
	public Response updateProperties(@Context final UriInfo uriInfo, @PathParam("id") final K id, final MultivaluedMapImpl params) {
		writeLock();

		try {
			final T object = getDao().get(id);

			if (object == null) {
				return Response.status(Status.NOT_FOUND).build();
			}

			LOG.debug("update: updating object {}", object);

			RestUtils.setBeanProperties(object, params);

			LOG.debug("update: object {} updated", object);
			getDao().saveOrUpdate(object);
			return Response.noContent().build();
		} finally {
			writeUnlock();
		}
	}

	@DELETE
	public Response deleteMany(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
		writeLock();

		try {
			Criteria crit = getCriteria(uriInfo, searchContext);

			final List<T> objects = getDao().findMatching(crit);

			if (objects == null || objects.size() == 0) {
				return Response.status(Status.NOT_FOUND).build();
			}

			for (T object : objects) {
				LOG.debug("delete: deleting object {}", object);
				getDao().delete(object);
			}
			return Response.ok().build();
		} finally {
			writeUnlock();
		}
	}

	@DELETE
	@Path("{id}")
	public Response delete(@PathParam("id") final K criteria) {
		writeLock();

		try {
			final T object = getDao().get(criteria);

			if (object == null) {
				return Response.status(Status.NOT_FOUND).build();
			}

			LOG.debug("delete: deleting object {}", criteria);
			getDao().delete(object);
			return Response.ok().build();
		} finally {
			writeUnlock();
		}
	}

	private static void applyLimitOffsetOrderBy(final MultivaluedMap<String,String> p, final CriteriaBuilder builder) {
		applyLimitOffsetOrderBy(p, builder, DEFAULT_LIMIT);
	}

	private static void applyLimitOffsetOrderBy(final MultivaluedMap<String,String> p, final CriteriaBuilder builder, final Integer defaultLimit) {

		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.putAll(p);

		builder.limit(defaultLimit);

		if (params.containsKey("limit") && params.getFirst("limit") != null && !"".equals(params.getFirst("limit").trim())) {
			builder.limit(Integer.valueOf(params.getFirst("limit").trim()));
			params.remove("limit");
		}

		if (params.containsKey("offset") && params.getFirst("offset") != null && !"".equals(params.getFirst("offset").trim())) {
			builder.offset(Integer.valueOf(params.getFirst("offset").trim()));
			params.remove("offset");
		}

		if (params.containsKey("orderBy") && params.getFirst("orderBy") != null && !"".equals(params.getFirst("orderBy").trim())) {
			builder.clearOrder();

			builder.orderBy(params.getFirst("orderBy").trim());
			params.remove("orderBy");

			if (params.containsKey("order") && params.getFirst("order") != null && !"".equals(params.getFirst("order").trim())) {
				if("desc".equalsIgnoreCase(params.getFirst("order").trim())) {
					builder.desc();
				} else {
					builder.asc();
				}
				params.remove("order");
			}
		}
	}

	private static URI getRedirectUri(final UriInfo uriInfo, final Object... pathComponents) {
		return RedirectHelper.getRedirectUri(uriInfo, pathComponents);
	}
}
