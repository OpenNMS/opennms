/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.svclayer.api.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for OnmsGroup entity
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 1.9.93
 */
@Component("groupRestService")
@Path("groups")
@Transactional
public class GroupRestService extends OnmsRestService {
	
    private static final Logger LOG = LoggerFactory.getLogger(GroupRestService.class);

    @Autowired
    private GroupService m_groupService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public OnmsGroupList getGroups() {
        readLock();
        
        try {
            final OnmsGroupList list = m_groupService.getOnmsGroupList();
            final List<OnmsGroup> groups = new ArrayList<OnmsGroup>(list.getGroups());
            Collections.sort(groups, new Comparator<OnmsGroup>() {
                @Override
                public int compare(final OnmsGroup a, final OnmsGroup b) {
                    return a.getName().compareTo(b.getName());
                }
            });
            list.setGroups(groups);
            return list;
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{groupName}")
    public OnmsGroup getGroup(@PathParam("groupName") final String groupName) {
        readLock();
        
        try {
            return getOnmsGroup(groupName);
        } finally {
            readUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addGroup(@Context final UriInfo uriInfo, final OnmsGroup group) {
        writeLock();
        
        try {
            LOG.debug("addGroup: Adding group {}", group);
            m_groupService.saveGroup(group);
            return Response.created(getRedirectUri(uriInfo, group.getName())).build();
        } finally {
            writeUnlock();
        }
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{groupName}")
    public Response updateGroup(@PathParam("groupName") final String groupName, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            OnmsGroup group = getOnmsGroup(groupName);
            LOG.debug("updateGroup: updating group {}", group);
            boolean modified = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(group);
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                LOG.debug("updateGroup: group {} updated", group);
                m_groupService.saveGroup(group);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }
    
    @DELETE
    @Path("{groupName}")
    public Response deleteGroup(@PathParam("groupName") final String groupName) {
        writeLock();
        try {
            final OnmsGroup group = getOnmsGroup(groupName);
            LOG.debug("deleteGroup: deleting group {}", group);
            m_groupService.deleteGroup(groupName);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{groupName}/users/{userName}")
    public Response addUser(@PathParam("groupName") final String groupName, @PathParam("userName") final String userName) {
        writeLock();
        try {
            getOnmsGroup(groupName); // just ensure that group exists
            boolean success = m_groupService.addUser(groupName, userName);
            if (success) {
                return Response.noContent().build();
            }
        } finally {
            writeUnlock();
        }
        throw getException(Status.BAD_REQUEST, "User with name '{}' already added or does not exist.", userName);
    }

    @DELETE
    @Path("{groupName}/users/{userName}")
    public Response removeUser(@PathParam("groupName") final String groupName, @PathParam("userName") final String userName) {
        writeLock();
        try {
            final OnmsGroup group = getOnmsGroup(groupName);
            if (group.getUsers().contains(userName)) {
                group.removeUser(userName);
                m_groupService.saveGroup(group);
                return Response.noContent().build();
            } else {
                throw getException(Status.BAD_REQUEST, "User with name '{}' does not exist in group '{}'", userName, groupName);
            }
        } finally {
            writeUnlock();
        }
    }
    
    @GET
    @Path("{groupName}/users/")
    public OnmsUserList listUsersOfGroup(@PathParam("groupName") final String groupName) {
        getOnmsGroup(groupName); // check if group exists.
        return m_groupService.getUsersOfGroup(groupName);
    }
    
    @GET
    @Path("{groupName}/users/{userName}")
    public OnmsUser getUser(@PathParam("groupName") final String groupName, @PathParam("userName") final String userName) {
        getOnmsGroup(groupName); // check if group exists.
        OnmsUser user = m_groupService.getUserForGroup(groupName,  userName);
        if (user != null) return user;
        throw getException(Status.NOT_FOUND, "User with name '{}' does not exist in group '{}'", userName, groupName);
    }

    @PUT
    @Path("{groupName}/categories/{categoryName}")
    public Response addCategory(@PathParam("groupName") final String groupName, @PathParam("categoryName") final String categoryName) {
        writeLock();
        try {
            getOnmsGroup(groupName); // check if group exists.
            boolean success = m_groupService.addCategory(groupName, categoryName);
            if (success) {
                return Response.noContent().build();
            }
            throw getException(Status.BAD_REQUEST, "Category with name '{}' already added or does not exist.", categoryName);
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{groupName}/categories/{categoryName}")
    public Response removeCategory(@PathParam("groupName") final String groupName, @PathParam("categoryName") final String categoryName) {
        writeLock();
        try {
            getOnmsGroup(groupName); // check if group exists.
            boolean success = m_groupService.removeCategory(groupName, categoryName);
            if (success) {
                return Response.noContent().build();
            }
            throw getException(Status.BAD_REQUEST, "Category with name '{}' does not exist. Remove failed.", categoryName);
        } finally {
            writeUnlock();
        }
    }

    @GET
    @Path("{groupName}/categories/{categoryName}")
    public OnmsCategory getCategoryForGroup(@PathParam("groupName") final String groupName, @PathParam("categoryName") final String categoryName) {
        getOnmsGroup(groupName); // check if group exists.
        List<OnmsCategory> categories = m_groupService.getAuthorizedCategories(groupName);
        for (OnmsCategory eachCategory : categories) {
            if (eachCategory.getName().equals(categoryName)) return eachCategory;
        }
        throw getException(Status.NOT_FOUND, "Category with name '{}' does not exist for group '{}'.", categoryName, groupName);
    }
    
    @GET
    @Path("{groupName}/categories")
    public OnmsCategoryCollection listCategories(@PathParam("groupName") final String groupName) {
        writeLock();
        try {
            getOnmsGroup(groupName); // check if group exists.
            return new OnmsCategoryCollection(m_groupService.getAuthorizedCategories(groupName));
        } finally {
            writeUnlock();
        }
    }
    
    protected OnmsGroup getOnmsGroup(final String groupName) {
        OnmsGroup group = m_groupService.getOnmsGroup(groupName);
        if (group == null) throw getException(Status.NOT_FOUND, "Group with name '{}' does not exist.", groupName);
        return group;
    }

}
