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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.UserMgmtJaxbMapper;
import org.opennms.netmgt.dao.api.SecurityRoleDao;
import org.opennms.netmgt.dao.api.UserAccountDao;
import org.opennms.netmgt.model.usermgmt.ReservedNames;
import org.opennms.netmgt.model.usermgmt.UserAccount;
import org.opennms.web.rest.v2.UserMgmtRestSupport;
import org.opennms.web.rest.v2.UserMgmtValidation;
import org.opennms.web.rest.v1.model.UserUpdateForm;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for OnmsUser entity
 *
 * @deprecated Deprecated in Horizon 37 and removed in Horizon 38. Use the v2 users API ({@code /api/v2/users}).
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 1.9.93
 */
@Component("userRestService")
@Path("users")
@Tag(name = "Users", description = """
        **Deprecated** in Horizon 37 and removed in Horizon 38. Use `/api/v2/users`.

        Users are stored in the database. The write operations need `ROLE_ADMIN`, like the v2 API (403 without
        it), and apply the same checks as the v2 API. A new user ID is validated and must not be reserved. Comments,
        lengths and duty schedules are validated (values already stored are accepted unchanged). Security roles have
        to be known roles. A password is only accepted in plain text with `hashPassword=true`, and is stored hashed and
        salted; a password hash is not accepted. These checks answer 400, as does renaming a user
        (use `PUT /api/v2/users/{userId}/rename`). The `admin` user always keeps `ROLE_ADMIN`.
        A change that would leave the system without a `ROLE_ADMIN` holder, or that removes a user that an
        on-call role or a notification destination path depends on, answers 409, as does a stale save.""")
@Transactional
@Deprecated(since = "37.0.0", forRemoval = true)
public class UserRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(UserRestService.class);
    /** For the message of a write without the admin role. */
    private static final String WHAT = "User management";
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

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            deprecated = true,
            summary = "List users",
            description = """
                    Return every user, sorted by user name. The list is not paged and
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
            @ApiResponse(responseCode = "500", description = "The users could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.lang.IllegalStateException: The user data could not be read.")))
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
            deprecated = true,
            summary = "Get the authenticated user",
            description = """
                    Return the user record of the authenticated principal. `password` and `passwordSalt`
                    are stripped from the response, so they are absent rather than masked.
                    An authenticated principal that has no user record is answered with 404
                    `User <name> does not exist.`.
                    Use `GET /rest/whoami` instead. It is not deprecated and returns a different shape.""",
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
            @ApiResponse(responseCode = "404", description = "The authenticated principal has no user record.",
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
            deprecated = true,
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
            @Parameter(description = "User name.", example = "admin", required = true)
            @PathParam("username") final String username) {
        final OnmsUser user = getOnmsUser(username);
        return V1UserMgmtForms.filterUserPassword(securityContext, user);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Operation(
            deprecated = true,
            summary = "Add or replace a user",
            description = """
                    Create a user from an XML `<user>` document. Only `application/xml` is consumed; a JSON body is
                    rejected with 415.
                    Posting a `user-id` that already exists overwrites that user rather than failing. A body without
                    `user-id` answers 400.
                    `password` is the password in plain text and needs `hashPassword=true`: it is stored hashed and
                    salted. A password hash is not accepted, and `passwordSalt` is ignored. The one exception is an
                    overwrite that sends the stored `password` and `passwordSalt` back unchanged, as `GET` returns
                    them: it keeps the stored password. A missing password, or one without `hashPassword=true`,
                    answers 400.
                    A new `user-id` is validated: it must not be reserved, look like `admin` or `rtc`, or contain
                    markup, whitespace, control characters or `: / \\ % ? #`. Comments must not contain markup, values
                    must fit their columns, duty schedules are validated (stored values are grandfathered) and every
                    role must be a known security role. Any of these answers 400.
                    Overwriting a user so that it loses `ROLE_ADMIN` answers 400 for `admin` and 409 for the last
                    holder of the role. A stale save answers 409.
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
            @ApiResponse(responseCode = "400", description = "The request is invalid (see the operation description).",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "user-id is required."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User management requires the admin role."))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/xml`."),
            @ApiResponse(responseCode = "409", description = "The change would leave no `ROLE_ADMIN` holder, or the user data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The body could not be unmarshalled, or the user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.lang.IllegalStateException: The user could not be saved.")))
    })
    public Response addUser(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsUser user,
            @Parameter(description = "Must be `true` to set a password: the plain-text password is hashed and salted before it is stored.", example = "true")
            @QueryParam("hashPassword") final boolean hashPassword) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            LOG.debug("addUser: Adding user {}", user == null ? null : user.getUsername());
            try {
                // The checks run before the manager call and without m_support.lockForWrite(): the manager saves in its
                // own REQUIRES_NEW transaction and takes the version locks itself, so holding them here would
                // deadlock. So the last-administrator check is not race-free in this deprecated API (v2 is).
                checkNewUser(user, hashPassword);
                if (hashPassword) hashPassword(user);
                m_userManager.save(user);
            } catch (final Throwable t) {
                throw V1UserMgmtForms.translate(t);
            }
            // not getRedirectUri: it keeps the query, and a password is always sent with ?hashPassword=true
            return Response.created(uriInfo.getAbsolutePathBuilder().path(user.getUsername()).build()).build();
        } finally {
            writeUnlock();
        }
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{userCriteria}")
    @Operation(
            deprecated = true,
            summary = "Update a user",
            description = """
                    Apply form-encoded fields to an existing user. The accepted keys are `fullName`, `comments`,
                    `email`, `password`, `hashPassword`, `dutySchedule` and `roles`, spelled exactly; they are the
                    `OnmsUser` bean properties, not the `full-name` and `user-comments` spellings the read endpoints
                    emit. Any other key answers 400, before anything is applied. Sending `username` or `name`
                    answers 400, because renaming is not supported here; use `PUT /api/v2/users/{userId}/rename`.
                    A request with only `hashPassword` comes back as 304.
                    `password` is the new password in plain text and needs `hashPassword=true`: it is stored hashed
                    and salted. A blank password, or one without `hashPassword=true`, answers 400.
                    Comments must not contain markup and values must fit their columns. Duty schedules are validated
                    (stored values are grandfathered) and roles have to be known. All of these answer 400.
                    A user without `ROLE_ADMIN` after the change answers 400 for `admin` and 409 for the last holder
                    of the role. A stale save answers 409.""",
            operationId = "updateUserV1"
    )
    @RequestBody(required = true, description = "Fields to apply.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = UserUpdateForm.class),
                    examples = @ExampleObject(value = "fullName=Jane+Roe&comments=On+call+for+the+NOC&email=jane.roe@example.org")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was updated."),
            @ApiResponse(responseCode = "304", description = "The form carried no field to write, so nothing was written."),
            @ApiResponse(responseCode = "400", description = "The request is invalid (see the operation description).",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/users/{userId}/rename."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "409", description = "The change would leave no `ROLE_ADMIN` holder, or the user data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.lang.IllegalStateException: The user could not be saved.")))
    })
    public Response updateUser(@Context final SecurityContext securityContext,
            @Parameter(description = "User name.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria, final MultivaluedMapImpl params) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("updateUser: updating user {}", user.getUsername());
            V1UserMgmtForms.checkFormKeys(params, V1UserMgmtForms.USER_FORM_KEYS, V1UserMgmtForms.RENAME_USER);
            final boolean hashPassword = Boolean.parseBoolean(params.getFirst("hashPassword"));
            final boolean passwordModified = params.containsKey("password");
            if (passwordModified) {
                checkPlainPassword(params.getFirst("password"), hashPassword);
            }
            if (V1UserMgmtForms.applyForm(user, params, Set.of("hashPassword"))) {
                LOG.debug("updateUser: user {} updated", user.getUsername());
                try {
                    // not under m_support.lockForWrite(), see addUser: the last-administrator check is not race-free here
                    checkUpdatedUser(user);
                    if (passwordModified) hashPassword(user);
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw V1UserMgmtForms.translate(t);
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
            deprecated = true,
            summary = "Grant a role to a user",
            description = """
                    Add one security role to a user. The role has to be a known security role, as listed by
                    `/api/v2/security-roles`.
                    The request takes no body. A stale save answers 409.""",
            operationId = "addUserRoleV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The role was added."),
            @ApiResponse(responseCode = "304", description = "The user already held the role."),
            @ApiResponse(responseCode = "400", description = "Unknown role name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown security role ROLE_BOGUS."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "409", description = "The user data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.lang.IllegalStateException: The user could not be saved.")))
    })
    public Response addRole(@Context final SecurityContext securityContext,
            @Parameter(description = "User name.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria,
            @Parameter(description = "Security role to grant.", example = "ROLE_PROVISION", required = true,
                    schema = @Schema(type = "string"))
            @PathParam("roleName") final String roleName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            checkRolesKnown(List.of(roleName));
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("addRole: updating user {}", user.getUsername());
            boolean modified = false;
            if (!user.getRoles().contains(roleName)) {
                user.getRoles().add(roleName);
                modified = true;
            }
            if (modified) {
                LOG.debug("addRole: user {} updated", user.getUsername());
                try {
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw V1UserMgmtForms.translate(t);
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
            deprecated = true,
            summary = "Delete a user",
            description = "Delete a user. Group memberships and acknowledgements that name the user are left alone. "
                    + "Deleting `admin` or `rtc` answers 400. Deleting the last `ROLE_ADMIN` holder, the supervisor of an "
                    + "on-call role, or the target of a notification destination path answers 409, as in v2. A path "
                    + "target is no obstacle when a group or an on-call role has the same name, because the path "
                    + "notifies that group or role.",
            operationId = "deleteUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was deleted."),
            @ApiResponse(responseCode = "400", description = "The request is invalid (see the operation description).",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The system user admin cannot be deleted."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "409", description = "The user is the last `ROLE_ADMIN` holder, an on-call supervisor or a notification destination path target, or the user data was changed by another process.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The user could not be removed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.lang.IllegalStateException: The user could not be saved.")))
    })
    public Response deleteUser(@Context final SecurityContext securityContext,
            @Parameter(description = "User name.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            final OnmsUser user = getOnmsUser(userCriteria);
            LOG.debug("deleteUser: deleting user {}", user.getUsername());
            try {
                // not under m_support.lockForWrite(), see addUser: the last-administrator check is not race-free here
                m_support.checkUserDeletable(user.getUsername(), m_userAccountDao.findByUsername(user.getUsername()));
                m_userManager.deleteUser(user.getUsername());
            } catch (final Throwable t) {
                throw V1UserMgmtForms.translate(t);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{userCriteria}/roles/{roleName}")
    @Operation(
            deprecated = true,
            summary = "Revoke a role from a user",
            description = "Remove one security role from a user. The role has to be a known role name even when "
                    + "the user does not hold it. Removing `ROLE_ADMIN` answers 400 for `admin` and 409 for the last holder "
                    + "of the role.",
            operationId = "deleteUserRoleV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The role was removed."),
            @ApiResponse(responseCode = "304", description = "The user did not hold the role."),
            @ApiResponse(responseCode = "400", description = "Unknown role name, or `admin` would lose `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown security role ROLE_BOGUS."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User jroe does not exist."))),
            @ApiResponse(responseCode = "409", description = "The change would leave no `ROLE_ADMIN` holder, or the user data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "The user could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "java.lang.IllegalStateException: The user could not be saved.")))
    })
    public Response deleteRole(@Context final SecurityContext securityContext,
            @Parameter(description = "User name.", example = "jroe", required = true)
            @PathParam("userCriteria") final String userCriteria,
            @Parameter(description = "Security role to revoke.", example = "ROLE_PROVISION", required = true,
                    schema = @Schema(type = "string"))
            @PathParam("roleName") final String roleName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            checkRolesKnown(List.of(roleName));
            final OnmsUser user = getOnmsUser(userCriteria);
            boolean modified = false;
            if (user.getRoles().contains(roleName)) {
                user.getRoles().remove(roleName);
                modified = true;
            }
            if (modified) {
                LOG.debug("deleteRole: user {} updated", user.getUsername());
                try {
                    // not under m_support.lockForWrite(), see addUser: the last-administrator check is not race-free here
                    checkRolesKeepAdministrator(user.getUsername(), user.getRoles());
                    m_userManager.save(user);
                } catch (final Throwable t) {
                    throw V1UserMgmtForms.translate(t);
                }
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Checks a user that a POST creates or overwrites. A password is only accepted in plain text, with hashPassword,
     * which stores it hashed and salted. An overwrite may echo the stored hash and salted flag back unchanged, so
     * that a user read with GET can be posted again.
     */
    private void checkNewUser(final OnmsUser user, final boolean hashPassword) throws Exception {
        final String id = user == null ? null : user.getUsername();
        if (id == null || id.isEmpty()) {
            throw m_support.badRequest("user-id is required.");
        }
        final UserAccount stored = m_userAccountDao.findByUsername(id);
        if (stored == null) {
            final String problem = UserMgmtValidation.validateName(id, "user-id", ReservedNames.USER_IDS);
            if (problem != null) {
                throw m_support.badRequest(problem);
            }
            m_support.checkNotLookAlike(id, UserMgmtRestSupport.PROTECTED_USERS, "user-id");
            UserMgmtValidation.checkLength(owner(id), "user name", id, UserMgmtJaxbMapper.USERNAME_LENGTH);
        }
        final boolean unchanged = stored != null && user.getPassword() != null
                && user.getPassword().equals(stored.getPasswordHash()) && user.getPasswordSalted() == stored.isPasswordSalted();
        if (!unchanged || hashPassword) {
            checkPlainPassword(user.getPassword(), hashPassword);
        }
        checkFields(user, stored);
    }

    /**
     * Checks the user of a form PUT after the form's fields were applied to it, whatever the fields were: values that
     * are stored already are grandfathered as in v2, and the stored roles are known.
     */
    private void checkUpdatedUser(final OnmsUser user) {
        checkFields(user, m_userAccountDao.findByUsername(user.getUsername()));
    }

    /** The v2 checks of the fields an OnmsUser carries; stored values that today's rules would reject pass. */
    private void checkFields(final OnmsUser user, final UserAccount stored) {
        final String owner = owner(user.getUsername());
        UserMgmtValidation.checkComments(owner, user.getComments(), stored == null ? null : stored.getComments());
        UserMgmtValidation.checkLength(owner, "full name", StringUtils.trimToNull(user.getFullName()), UserMgmtJaxbMapper.FULL_NAME_LENGTH);
        UserMgmtValidation.checkLength(owner, "email", StringUtils.trimToNull(user.getEmail()), UserMgmtJaxbMapper.CONTACT_LENGTH);
        if (user.getDutySchedule() != null) {
            UserMgmtValidation.validateDutySchedules("User " + user.getUsername(), user.getDutySchedule(),
                    stored == null ? null : stored.getDutySchedules());
        }
        checkRolesKnown(user.getRoles());
        checkRolesKeepAdministrator(user.getUsername(), user.getRoles());
    }

    /** A password is given in plain text and stored hashed and salted, as in v2: a hash supplied by the caller is refused. */
    private void checkPlainPassword(final String password, final boolean hashPassword) {
        if (StringUtils.isBlank(password)) {
            throw m_support.badRequest("A password is required.");
        }
        if (!hashPassword) {
            throw m_support.badRequest("Send the password in plain text with hashPassword=true; a password hash is not accepted.");
        }
    }

    /** The security roles of the database, as in v2. */
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

    private static String owner(final String userId) {
        return "User \"" + userId + "\"";
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

    private static OnmsUserList filterUserPasswords(final SecurityContext securityContext, final OnmsUserList users) {
        Collections.sort(users.getUsers(), USER_COMPARATOR);
        for (final OnmsUser user : users) {
            V1UserMgmtForms.filterUserPassword(securityContext, user);
        }
        return users;
    }
}
