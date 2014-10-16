/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.core.ResourceContext;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

import java.util.ArrayList;

@Component
/**
 * <p>CategoryRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Path("categories")
@Transactional
public class CategoryRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CategoryRestService.class);

    @Autowired
    private CategoryDao m_categoryDao;

    @Context
    UriInfo m_uriInfo;

    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/nodes/{nodeCriteria}")
    public OnmsCategoryCollection getCategoriesForNode(@PathParam("nodeCriteria") String nodeCriteria) {
        return m_context.getResource(NodeRestService.class).getCategoriesForNode(nodeCriteria);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{categoryName}/nodes/{nodeCriteria}")
    public OnmsCategory getCategoryForNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName) {
        return m_context.getResource(NodeRestService.class).getCategoryForNode(nodeCriteria, categoryName);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{categoryName}/nodes/{nodeCriteria}/")
    public Response addCategoryToNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") final String categoryName) {
        return m_context.getResource(NodeRestService.class).addCategoryToNode(nodeCriteria, categoryName);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{categoryName}")
    public Response updateCategory(@PathParam("categoryName") String categoryName, MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsCategory category = m_categoryDao.findByName(categoryName);
            if (category == null) {
                throw getException(Status.BAD_REQUEST, "updateCategory: Category " + categoryName + " not found.");
            }
            LOG.debug("updateCategory: updating category {}", category);
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(category);
            for(String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            LOG.debug("updateCategory: category {} updated", category);
            m_categoryDao.saveOrUpdate(category);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("/{categoryName}/nodes/{nodeCriteria}/")
    public Response removeCategoryFromNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName) {
        return m_context.getResource(NodeRestService.class).removeCategoryFromNode(nodeCriteria, categoryName);
    }

    @GET
    @Path("/{categoryName}")
    public OnmsCategory getCategory(@PathParam("categoryName") final String categoryName) {
        OnmsCategory category = m_categoryDao.findByName(categoryName);
        if (category == null) throw getException(Response.Status.NOT_FOUND, "Category with name {} was not found.", categoryName);
        return category;
    }
    
    @POST
    @Path("/")
    public Response createCategory(final OnmsCategory category) {
        if (category == null) throw getException(Response.Status.BAD_REQUEST, "Category must not be null.");
        boolean exists = m_categoryDao.findByName(category.getName()) != null;
        if (!exists) {
            m_categoryDao.save(category);
            return Response.seeOther(getRedirectUri(m_uriInfo, category.getName())).build();
        }
        throw getException(Response.Status.BAD_REQUEST, "A category with name '{}' already exists.", category.getName());
    }

    @GET
    @Path("/")
    public OnmsCategoryCollection listCategories() {
        return new OnmsCategoryCollection(new ArrayList<OnmsCategory>(m_categoryDao.findAll()));
    }

    @DELETE
    @Path("/{categoryName}")
    public Response deleteCategory(@PathParam("categoryName") final String categoryName) {
        OnmsCategory category = m_categoryDao.findByName(categoryName);
        if (category != null) {
            m_categoryDao.delete(category);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        }
        throw getException(Response.Status.BAD_REQUEST, "A category with name '{}' does not exist.", categoryName);
    }


    @PUT
    @Path("/{categoryName}/groups/{groupName}")
    public OnmsCategory addCategoryToGroup(@PathParam("groupName") final String groupName, @PathParam("categoryName") final String categoryName) {
        return m_context.getResource(GroupRestService.class).addCategory(groupName, categoryName);
    }

    @DELETE
    @Path("/{categoryName}/groups/{groupName}")
    public Response removeCategoryFromGroup(@PathParam("groupName") final String groupName, @PathParam("categoryName") final String categoryName) {
        return m_context.getResource(GroupRestService.class).removeCategory(groupName, categoryName);
    }

    @GET
    @Path("/groups/{groupName}")
    public OnmsCategoryCollection listCategoriesForGroup(@PathParam("groupName") final String groupName) {
        return m_context.getResource(GroupRestService.class).listCategories(groupName);
    }
}
