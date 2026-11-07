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
import java.util.Collection;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.config.UserDataChangedException;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.dao.api.OnCallRoleDao;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.ReservedNames;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.web.rest.v2.UserMgmtRestSupport;
import org.opennms.web.rest.v2.UserMgmtValidation;
import org.opennms.web.rest.v2.UserMgmtValidationException;
import org.opennms.web.rest.v1.model.UserUpdateForm;
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

    @Autowired
    private UserMgmtRestSupport m_support;

    @Autowired
    private UserAccountDao m_userAccountDao;

    @Autowired
    private SecurityRoleDao m_securityRoleDao;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List users",
            description = """
                    Return every user defined in `users.xml`, sorted by user name. The list is not paged and
                    ignores query parameters.
                    A caller without `ROLE_ADMIN` sees the literal `xxxxxxxx` in place of every password hash
                    except their own.""",
            operationId = "getUsersV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The configured users.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsUserList.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "user": [
                        {
                              "user-id": "admin",
                              "full-name": "Administrator",
                              "user-comments": "Default administrator, do not delete",
                              "email": "",
                              "password": "gU2wmSW7k9v1xg4/MrAsaI+VyddBAhJJt4zPX5SGG0BK+qiASGnJsqM8JOug/aEL",
                              "passwordSalt": true,
                              "duty-schedule": [],
                              "role": [ "ROLE_ADMIN" ]
                            },
                        {
                          "user-id": "rtc",
                          "full-name": "RTC",
                          "user-comments": "RTC user, do not delete",
                          "email": "",
                          "password": "sHMy+HycWKGJC/uUMF0IGlXUXP1KhcqD0GEchFlvYTw40jT9r+zMxOb3F+phWNzX",
                          "passwordSalt": true,
                          "duty-schedule": [],
                          "role": [ "ROLE_RTC" ]
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "The user configuration could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.io.FileNotFoundException: users.xml")))
    })
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
    @Operation(
            summary = "Get the authenticated user",
            description = """
                    Return the `users.xml` entry for the authenticated principal. `password` and `passwordSalt`
                    are stripped from the response, so they are absent rather than masked.
                    An authenticated principal that has no `users.xml` entry is answered with 404
                    `User <name> does not exist.` before the handler's own null check runs.""",
            operationId = "getAuthenticatedUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The authenticated user, without password fields.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsUser.class),
                            examples = @ExampleObject(value = """
                    {
                      "user-id": "admin",
                      "full-name": "Administrator",
                      "user-comments": "Default administrator, do not delete",
                      "email": "",
                      "duty-schedule": [],
                      "role": [ "ROLE_ADMIN" ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "The authenticated principal has no `users.xml` entry.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User admin does not exist.")))
    })
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
    @Operation(
            summary = "Get a user",
            description = """
                    Return one user by name. A caller without `ROLE_ADMIN` asking for somebody else sees the
                    literal `xxxxxxxx` in place of the password hash.""",
            operationId = "getUserByNameV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsUser.class),
                            examples = @ExampleObject(value = """
                    {
                      "user-id": "admin",
                      "full-name": "Administrator",
                      "user-comments": "Default administrator, do not delete",
                      "email": "",
                      "password": "gU2wmSW7k9v1xg4/MrAsaI+VyddBAhJJt4zPX5SGG0BK+qiASGnJsqM8JOug/aEL",
                      "passwordSalt": true,
                      "duty-schedule": [],
                      "role": [ "ROLE_ADMIN" ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist.")))
    })
    public OnmsUser getUser(@Context final SecurityContext securityContext,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "admin", required = true)
            @PathParam("username") final String username) {
        final OnmsUser user = getOnmsUser(username);
        return filterUserPassword(securityContext, user);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Operation(
            summary = "Add or replace a user",
            description = """
                    Create a user from an XML `<user>` document. Only `application/xml` is consumed; a JSON body is
                    rejected with 415.
                    Posting a `user-id` that already exists overwrites that entry rather than failing. `password`
                    has to be present: a body without one fails with 500.
                    The response carries no entity; the new user's URI is in the `Location` header.""",
            operationId = "addUserV1"
    )
    @RequestBody(required = true, description = "The user to store.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = OnmsUser.class),
                    examples = @ExampleObject(value = """
                    <user>
                      <user-id>jroe</user-id>
                      <full-name>Jane Roe</full-name>
                      <user-comments>On call for the NOC</user-comments>
                      <email>jane.roe@example.org</email>
                      <password>s3cret</password>
                      <duty-schedule>MoTuWeThFr800-1700</duty-schedule>
                      <role>ROLE_USER</role>
                    </user>""")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User stored. `Location` points at the new user."),
            @ApiResponse(responseCode = "400", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not have write access to users!"))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/xml`."),
            @ApiResponse(responseCode = "500", description = "The body could not be unmarshalled, or the user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "'password' cannot be null!")))
    })
    public Response addUser(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsUser user,
            @Parameter(description = "Hash and salt the supplied password before storing it.", example = "true")
            @QueryParam("hashPassword") final boolean hashPassword) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            LOG.debug("addUser: Adding user {}", user);
            try {
                // The checks run before the manager call and without m_support.lockForWrite(): the manager saves in its
                // own REQUIRES_NEW transaction and takes the version locks itself, so holding them here would
                // deadlock. So the last-administrator check is not race-free in this deprecated API (v2 is).
                checkNewUser(user);
                if (hashPassword) hashPassword(user);
                checkSalted(user);
                m_userManager.save(user);
            } catch (final Throwable t) {
                throw translate(t);
            }
            return Response.created(getRedirectUri(uriInfo, user.getUsername())).build();
        } finally {
            writeUnlock();
        }
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{userCriteria}")
    @Operation(
            summary = "Update a user",
            description = """
                    Apply form-encoded fields to an existing user. Keys are matched against the writable
                    `OnmsUser` bean properties, so they are `fullName` and `comments` rather than the `full-name`
                    and `user-comments` spellings the read endpoints emit. A key that matches no writable property
                    is skipped, and a request that wrote nothing comes back as 304.
                    `password` is stored verbatim unless `hashPassword=true` is sent in the same request.""",
            operationId = "updateUserV1"
    )
    @RequestBody(required = true, description = "Fields to apply.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = UserUpdateForm.class),
                    examples = @ExampleObject(value = "fullName=Jane+Roe&comments=On+call+for+the+NOC&email=jane.roe@example.org")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was updated."),
            @ApiResponse(responseCode = "304", description = "No supplied key matched a writable property, so nothing was written."),
            @ApiResponse(responseCode = "400", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not have write access to users!"))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "500", description = "The user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.io.IOException: users.xml is not writable")))
    })
    public Response updateUser(@Context final SecurityContext securityContext,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria, final MultivaluedMapImpl params) {
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
                if ("username".equals(key)) {
                    throw getException(Status.BAD_REQUEST, "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/users/{userId}/rename.");
                }
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
                    // not under m_support.lockForWrite(), see addUser: the last-administrator check is not race-free here
                    checkUpdatedUser(user, params.containsKey("roles"), params.containsKey("dutySchedule"));
                    if (passwordModified && hashPassword) hashPassword(user);
                    checkSalted(user);
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw translate(t);
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
    @Operation(
            summary = "Grant a role to a user",
            description = """
                    Add one security role to a user. The role has to be a known role name, either one of the
                    built-in `ROLE_*` constants or one declared in `etc/security-roles.properties`.
                    The request takes no body.""",
            operationId = "addUserRoleV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The role was added."),
            @ApiResponse(responseCode = "304", description = "The user already held the role."),
            @ApiResponse(responseCode = "400", description = "Unknown role name, or the caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid role ROLE_BOGUS!"))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "500", description = "The user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.io.IOException: users.xml is not writable")))
    })
    public Response addRole(@Context final SecurityContext securityContext,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria,
            @Parameter(description = "Security role to grant.", example = "ROLE_PROVISION", required = true,
                    schema = @Schema(type = "string"))
            @PathParam("roleName") final String roleName) {
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
                    throw translate(t);
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
    @Operation(
            summary = "Delete a user",
            description = "Remove a user from `users.xml`. Group memberships and acknowledgements that name the "
                    + "user are left alone.",
            operationId = "deleteUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was deleted."),
            @ApiResponse(responseCode = "400", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not have write access to users!"))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "500", description = "The user could not be removed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.io.IOException: users.xml is not writable")))
    })
    public Response deleteUser(@Context final SecurityContext securityContext,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria) {
        writeLock();
        try {
            if (!hasEditRights(securityContext)) {
                throw getException(Status.BAD_REQUEST, "User {} does not have write access to users!", securityContext.getUserPrincipal().getName());
            }
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("deleteUser: deleting user {}", user);
            try {
                // not under m_support.lockForWrite(), see addUser: the last-administrator check is not race-free here
                checkDeletable(user.getUsername());
                m_userManager.deleteUser(user.getUsername());
            } catch (final Throwable t) {
                throw translate(t);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{userCriteria}/roles/{roleName}")
    @Operation(
            summary = "Revoke a role from a user",
            description = "Remove one security role from a user. The role has to be a known role name even when "
                    + "the user does not hold it.",
            operationId = "deleteUserRoleV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The role was removed."),
            @ApiResponse(responseCode = "304", description = "The user did not hold the role."),
            @ApiResponse(responseCode = "400", description = "Unknown role name, or the caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid role ROLE_BOGUS!"))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "500", description = "The user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.io.IOException: users.xml is not writable")))
    })
    public Response deleteRole(@Context final SecurityContext securityContext,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria,
            @Parameter(description = "Security role to revoke.", example = "ROLE_PROVISION", required = true,
                    schema = @Schema(type = "string"))
            @PathParam("roleName") final String roleName) {
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
                    // not under m_support.lockForWrite(), see addUser: the last-administrator check is not race-free here
                    checkRolesKeepAdministrator(user.getUsername(), user.getRoles());
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw translate(t);
                }
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /** Checks a user that a POST creates or overwrites. */
    private void checkNewUser(final OnmsUser user) throws Exception {
        final String id = user.getUsername();
        final UserAccount stored = id == null ? null : m_userAccountDao.findByUsername(id);
        if (stored == null && id != null) {
            final String problem = UserMgmtValidation.validateName(id, "user-id", ReservedNames.USER_IDS);
            if (problem != null) {
                throw m_support.badRequest(problem);
            }
            m_support.checkNotLookAlike(id, UserMgmtRestSupport.PROTECTED_USERS, "user-id");
        }
        checkDutySchedules(user, stored);
        checkRolesKnown(user.getRoles());
        checkRolesKeepAdministrator(id, user.getRoles());
    }

    /** Checks the user of a form PUT after the form's fields were applied to it. */
    private void checkUpdatedUser(final OnmsUser user, final boolean rolesGiven, final boolean dutyGiven) {
        if (dutyGiven) {
            checkDutySchedules(user, m_userAccountDao.findByUsername(user.getUsername()));
        }
        if (rolesGiven) {
            checkRolesKnown(user.getRoles());
            checkRolesKeepAdministrator(user.getUsername(), user.getRoles());
        }
    }

    private void checkDutySchedules(final OnmsUser user, final UserAccount stored) {
        if (user.getDutySchedule() != null) {
            UserMgmtValidation.validateDutySchedules("User " + user.getUsername(), user.getDutySchedule(),
                    stored == null ? null : stored.getDutySchedules());
        }
    }

    private void checkRolesKnown(final Collection<String> roles) {
        if (roles == null) {
            return;
        }
        for (final String role : roles) {
            if (role == null || m_securityRoleDao.findByName(role) == null) {
                throw m_support.badRequest("Unknown security role " + role + ".");
            }
        }
    }

    /** The admin user keeps ROLE_ADMIN (400), and so does the last holder of it (409). */
    private void checkRolesKeepAdministrator(final String id, final Collection<String> roles) {
        if (roles != null && roles.contains(Authentication.ROLE_ADMIN)) {
            return;
        }
        if ("admin".equals(id)) {
            throw m_support.badRequest("The admin user must keep the " + Authentication.ROLE_ADMIN + " role.");
        }
        final UserAccount stored = id == null ? null : m_userAccountDao.findByUsername(id);
        if (stored != null) {
            m_support.checkNotLastAdministrator(stored, "lose the " + Authentication.ROLE_ADMIN + " role");
        }
    }

    /** The manager refuses an unsalted password when org.opennms.users.allowUnsalted is false; that is a bad request. */
    private static void checkSalted(final OnmsUser user) {
        if (!Boolean.parseBoolean(System.getProperty(UserManager.ALLOW_UNSALTED_PROPERTY, "true")) && !user.getPasswordSalted()) {
            throw getException(Status.BAD_REQUEST, UserManager.ALLOW_UNSALTED_PROPERTY + "=false, but " + user.getUsername()
                    + " contains an unsalted password.");
        }
    }

    private void checkDeletable(final String id) throws Exception {
        if (UserMgmtRestSupport.PROTECTED_USERS.contains(id)) {
            throw m_support.badRequest("The system user " + id + " cannot be deleted.");
        }
        final UserAccount stored = m_userAccountDao.findByUsername(id);
        if (stored != null) {
            m_support.checkNotLastAdministrator(stored, "be deleted");
        }
        // the manager refuses this too, with this message, but as an IllegalStateException that is not told apart from a failure
        final List<OnCallRole> supervised = m_onCallRoleDao.findBySupervisor(id);
        if (!supervised.isEmpty()) {
            throw m_support.conflict("User \"" + id + "\" is the supervisor of the on-call role \""
                    + supervised.get(0).getName() + "\". Change the supervisor first.");
        }
        m_support.checkNotDestinationPathTarget("User " + id, id);
    }

    /** Answers what the checks and the manager refuse with 400 or 409; only a real failure is a 500. */
    private static WebApplicationException translate(final Throwable t) {
        if (t instanceof WebApplicationException) {
            return (WebApplicationException) t;
        }
        if (t instanceof IllegalArgumentException || t instanceof UserMgmtValidationException) {
            return getException(Status.BAD_REQUEST, t);
        }
        if (t instanceof UserDataChangedException) {
            return getException(Status.CONFLICT, t);
        }
        return getException(Status.INTERNAL_SERVER_ERROR, t);
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

    /** Masks the password hash of every user but the caller for a caller without edit rights; shared with {@link GroupRestService}. */
    static OnmsUser filterUserPassword(final SecurityContext securityContext, final OnmsUser user) {
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
