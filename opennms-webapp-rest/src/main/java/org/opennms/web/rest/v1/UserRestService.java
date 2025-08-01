/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v1;

import java.security.Principal;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for OnmsUser entity
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 1.9.93
 */
@Component("userRestService")
@Path("users")
@Tag(name = "Users", description = "Users API")
@Transactional
public class UserRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(UserRestService.class);
    private static final Comparator<OnmsUser> USER_COMPARATOR = new Comparator<OnmsUser>() {
        @Override public int compare(final OnmsUser a, final OnmsUser b) {
            return a.getUsername().compareTo(b.getUsername());
        }
    };

    @Autowired
    private UserManager m_userManager;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public OnmsUserList getUsers(@Context final SecurityContext securityContext) {
        try {
            return filterUserPasswords(securityContext, m_userManager.getOnmsUserList());
        } catch (final Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("whoami")
    public OnmsUser whoami(@Context final SecurityContext securityContext) {
        final String userName = securityContext.getUserPrincipal().getName();
        final OnmsUser user = getOnmsUser(userName);
        // Don't expose the user's password
        if (user != null) {
            user.setPassword(null);
            user.setPasswordSalted(null);
        }
        return user;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{username}")
    public OnmsUser getUser(@Context final SecurityContext securityContext, @PathParam("username") final String username) {
        final OnmsUser user = getOnmsUser(username);
        return filterUserPassword(securityContext, user);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addUser(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsUser user, @QueryParam("hashPassword") final boolean hashPassword) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            LOG.debug("addUser: Adding user {}", user);
            try {
                if (hashPassword) hashPassword(user);
                m_userManager.save(user);
            } catch (final Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            return Response.created(getRedirectUri(uriInfo, user.getUsername())).build();
        } finally {
            writeUnlock();
        }
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{userCriteria}")
    public Response updateUser(@Context final SecurityContext securityContext, @PathParam("userCriteria") final String userCriteria, final MultivaluedMapImpl params) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("updateUser: updating user {}", user);
            boolean modified = false;
            boolean passwordModified = false;
            boolean hashPassword = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(user);
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
                if (key.equals("password")) {
                    passwordModified = true;
                } else if (key.equals("hashPassword")) {
                    hashPassword = Boolean.valueOf(params.getFirst("hashPassword"));
                }
            }
            if (modified) {
                LOG.debug("updateUser: user {} updated", user);
                try {
                    if (passwordModified && hashPassword) hashPassword(user);
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw getException(Status.INTERNAL_SERVER_ERROR, t);
                }
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{userCriteria}/roles/{roleName}")
    public Response addRole(@Context final SecurityContext securityContext, @PathParam("userCriteria") final String userCriteria, @PathParam("roleName") final String roleName) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            if (! Authentication.isValidRole(roleName)) {
                throw getException(Status.BAD_REQUEST, "Invalid role {}!", roleName);
            }
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("addRole: updating user {}", user);
            boolean modified = false;
            if (!user.getRoles().contains(roleName)) {
                user.getRoles().add(roleName);
                modified = true;
            }
            if (modified) {
                LOG.debug("addRole: user {} updated", user);
                try {
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw getException(Status.INTERNAL_SERVER_ERROR, t);
                }
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{userCriteria}")
    public Response deleteUser(@Context final SecurityContext securityContext, @PathParam("userCriteria") final String userCriteria) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("deleteUser: deleting user {}", user);
            try {
                m_userManager.deleteUser(user.getUsername());
            } catch (final Throwable t) {
                throw getException(Status.INTERNAL_SERVER_ERROR, t);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{userCriteria}/roles/{roleName}")
    public Response deleteRole(@Context final SecurityContext securityContext, @PathParam("userCriteria") final String userCriteria, @PathParam("roleName") final String roleName) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            if (! Authentication.isValidRole(roleName)) {
                throw getException(Status.BAD_REQUEST, "Invalid role {}!", roleName);
            }
            final OnmsUser user = getOnmsUser(userCriteria);
            boolean modified = false;
            if (user.getRoles().contains(roleName)) {
                user.getRoles().remove(roleName);
                modified = true;
            }
            if (modified) {
                LOG.debug("deleteRole: user {} updated", user);
                try {
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw getException(Status.INTERNAL_SERVER_ERROR, t);
                }
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    private OnmsUser getOnmsUser(String username) {
        OnmsUser user = null;
        try {
            user = m_userManager.getOnmsUser(username);
        } catch (final Throwable t) {
            throw getException(Status.INTERNAL_SERVER_ERROR, t);
        }
        if (user == null) throw getException(Status.NOT_FOUND, "User {} does not exist.", username);
        return user;
    }

    private OnmsUser hashPassword(final OnmsUser user) {
        final String password = m_userManager.encryptedPassword(user.getPassword(), true);
        user.setPassword(password);
        user.setPasswordSalted(true);
        return user;
    }

    private static boolean hasEditRights(SecurityContext securityContext) {
        if (securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return true;
        } else {
            return false;
        }
    }

    private static OnmsUserList filterUserPasswords(final SecurityContext securityContext, final OnmsUserList users) {
        Collections.sort(users.getUsers(), USER_COMPARATOR);
        for (final OnmsUser user : users) {
            filterUserPassword(securityContext, user);
        }
        return users;
    }

    private static OnmsUser filterUserPassword(final SecurityContext securityContext, final OnmsUser user) {
        if (!hasEditRights(securityContext)) {
            final Principal principal = securityContext.getUserPrincipal();
            // users may see their own password hash  :)
            if (!user.getUsername().equals(principal.getName())) {
                user.setPassword("xxxxxxxx");
                user.setPasswordSalted(false);
            }
        }
        return user;
    }
}
