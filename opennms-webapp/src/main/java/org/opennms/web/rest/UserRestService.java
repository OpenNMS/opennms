/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import java.util.Collections;
import java.util.Comparator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for OnmsUser entity
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 1.9.93
 */
@Component
@PerRequest
@Scope("prototype")
@Path("users")
@Transactional
public class UserRestService extends OnmsRestService {
    
    private UserManager m_userManager;

    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    public UserRestService() {
        try {
            UserFactory.init();
        } catch (Throwable t) {
            throw new DataRetrievalFailureException("Unable to initialize the user factory.", t);
        }
        m_userManager = UserFactory.getInstance();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsUserList getUsers() {
        final OnmsUserList list;
        try {
            list = m_userManager.getOnmsUserList();
            Collections.sort(list, new Comparator<OnmsUser>() {
                @Override
                public int compare(final OnmsUser a, final OnmsUser b) {
                    return a.getUsername().compareTo(b.getUsername());
                }
            });
            return list;
        } catch (final Throwable t) {
            throwException(Status.BAD_REQUEST, t);
        }
        return null;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{username}")
    public OnmsUser getNode(@PathParam("username") String username) {
        try {
            return m_userManager.getOnmsUser(username);
        } catch (final Throwable t) {
            throwException(Status.BAD_REQUEST, t);
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addUser(OnmsUser user) {
        log().debug("addUser: Adding user " + user);
        try {
            m_userManager.save(user);
        } catch (final Throwable t) {
            throwException(Status.BAD_REQUEST, t);
        }
        return Response.ok(user).build();
    }
    
    /*
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}")
    public Response updateNode(@PathParam("nodeCriteria") String nodeCriteria, MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "updateNode: Can't find node " + nodeCriteria);
        }
        log().debug("updateNode: updating node " + node);
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(node);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                @SuppressWarnings("unchecked")
				Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateNode: node " + node + " updated");
        m_nodeDao.saveOrUpdate(node);
        return Response.ok(node).build();
    }
    
    @DELETE
    @Path("{nodeCriteria}")
    public Response deleteNode(@PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteNode: Can't find node " + nodeCriteria);
        }
        log().debug("deleteNode: deleting node " + nodeCriteria);
        m_nodeDao.delete(node);
        try {
            sendEvent(EventConstants.NODE_DELETED_EVENT_UEI, node.getId());
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }

    @Path("{nodeCriteria}/ipinterfaces")
    public OnmsIpInterfaceResource getIpInterfaceResource() {
        return m_context.getResource(OnmsIpInterfaceResource.class);
    }

    @Path("{nodeCriteria}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource() {
        return m_context.getResource(OnmsSnmpInterfaceResource.class);
    }

    @Path("{nodeCriteria}/categories")
    public OnmsCategoryResource getCategoryResource() {
        return m_context.getResource(OnmsCategoryResource.class);
    }

    @Path("{nodeCriteria}/assetRecord")
    public AssetRecordResource getAssetRecordResource() {
        return m_context.getResource(AssetRecordResource.class);
    }
    */

    /*
    private OnmsCriteria getQueryFilters(MultivaluedMap<String,String> params) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);

        setLimitOffset(params, criteria, DEFAULT_LIMIT, false);
        addOrdering(params, criteria, false);
        // Set default ordering
        addOrdering(
            new MultivaluedMapImpl(
                new String[][] { 
                    new String[] { "orderBy", "label" }, 
                    new String[] { "order", "asc" } 
                }
            ), criteria, false
        );
        addFiltersToCriteria(params, criteria, OnmsNode.class);

        criteria.createAlias("snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);

        return getDistinctIdCriteria(OnmsNode.class, criteria);
    }
    */
    
}
