/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
    @Autowired
    private UserManager m_userManager;

    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public OnmsUserList getUsers() {
        final OnmsUserList list;
        readLock();
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
            throw getException(Status.BAD_REQUEST, t);
        } finally {
            readUnlock();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{username}")
    public OnmsUser getUser(@PathParam("username") final String username) {
        readLock();
        try {
            final OnmsUser user = m_userManager.getOnmsUser(username);
            if (user != null) return user;
            throw getException(Status.NOT_FOUND, username + " does not exist");
        } catch (final Throwable t) {
            if (t instanceof WebApplicationException) throw (WebApplicationException)t;
            throw getException(Status.BAD_REQUEST, t);
        } finally {
            readUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addUser(final OnmsUser user) {
        writeLock();
        try {
            log().debug("addUser: Adding user " + user);
            m_userManager.save(user);
            return Response.seeOther(getRedirectUri(m_uriInfo, user.getUsername())).build();
        } catch (final Throwable t) {
            throw getException(Status.BAD_REQUEST, t);
        } finally {
            writeUnlock();
        }
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{userCriteria}")
    public Response updateUser(@PathParam("userCriteria") final String userCriteria, final MultivaluedMapImpl params) {
        OnmsUser user = null;
        writeLock();
        try {
            try {
                user = m_userManager.getOnmsUser(userCriteria);
            } catch (final Throwable t) {
                throw getException(Status.BAD_REQUEST, t);
            }
            if (user == null) throw getException(Status.BAD_REQUEST, "updateUser: User does not exist: " + userCriteria);
            log().debug("updateUser: updating user " + user);
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(user);
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    @SuppressWarnings("unchecked")
                    final Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            log().debug("updateUser: user " + user + " updated");
            try {
                m_userManager.save(user);
            } catch (final Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }
    
    @DELETE
    @Path("{userCriteria}")
    public Response deleteUser(@PathParam("userCriteria") final String userCriteria) {
        writeLock();
        try {
            OnmsUser user = null;
            try {
                user = m_userManager.getOnmsUser(userCriteria);
            } catch (final Throwable t) {
                throw getException(Status.BAD_REQUEST, t);
            }
            if (user == null) throw getException(Status.BAD_REQUEST, "deleteUser: User does not exist: " + userCriteria);
            log().debug("deleteUser: deleting user " + user);
            try {
                m_userManager.deleteUser(user.getUsername());
            } catch (final Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

}
