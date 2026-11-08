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

import java.io.IOException;
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
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.netmgt.model.usermgmt.OnCallRole;
import org.opennms.netmgt.model.usermgmt.ReservedNames;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.model.GroupUpdateForm;
import org.opennms.web.rest.v2.GroupsRestService;
import org.opennms.web.rest.v2.UserMgmtRestSupport;
import org.opennms.web.rest.v2.UserMgmtValidation;
import org.opennms.web.rest.v2.UserMgmtValidationException;
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
 * @deprecated Deprecated in Horizon 37 and removed in Horizon 38. Use the v2 groups API ({@code /api/v2/groups}).
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 1.9.93
 */
@Component("groupRestService")
@Path("groups")
@Tag(name = "Groups", description = """
        **Deprecated** in Horizon 37 and removed in Horizon 38. Use `/api/v2/groups`.

        Groups are stored in the database. A new group name is validated and must not be reserved, and every
        member must be an existing user. Both answer 400, as does renaming a group
        (use `PUT /api/v2/groups/{name}/rename`). A save through this API keeps the duty schedules and the
        default map of the stored group. Deleting a group that is the membership group of an on-call role or
        a target of a notification destination path answers 409, as does a stale save.
        The write operations need `ROLE_ADMIN`, like the v2 API (403 without it).
        Password hashes of other users are shown as `xxxxxxxx` to callers without `ROLE_ADMIN`.""")
@Transactional
@Deprecated(since = "37.0.0", forRemoval = true)
public class GroupRestService extends OnmsRestService {
	
    private static final Logger LOG = LoggerFactory.getLogger(GroupRestService.class);
    /** For the message of a write without the admin role. */
    private static final String WHAT = "Group management";

    @Autowired
    private GroupService m_groupService;

    @Autowired
    private UserManager m_userManager;

    @Autowired
    private OnCallRoleDao m_onCallRoleDao;

    @Autowired
    private UserMgmtRestSupport m_support;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            deprecated = true,
            summary = "List groups",
            description = "Return every group, sorted by name. The list is not paged and "
                    + "ignores query parameters.",
            operationId = "getGroupsV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The configured groups.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsGroupList.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "group": [
                        {
                          "name": "Admin",
                          "comments": "The administrators",
                          "user": [ "admin" ]
                        }
                      ]
                    }""")))
    })
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
    @Operation(
            deprecated = true,
            summary = "Get a group",
            description = "Return one group by name, including its member user names.",
            operationId = "getGroupByNameV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The group.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsGroup.class),
                            examples = @ExampleObject(value = """
                    {
                      "name": "Admin",
                      "comments": "The administrators",
                      "user": [ "admin" ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public OnmsGroup getGroup(
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
        readLock();
        
        try {
            return getOnmsGroup(groupName);
        } finally {
            readUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Operation(
            deprecated = true,
            summary = "Add or replace a group",
            description = """
                    Create a group from an XML `<group>` document. Only `application/xml` is consumed; a JSON body
                    is rejected with 415.
                    Posting a name that already exists overwrites that group rather than failing. A new group's name
                    must not be reserved or contain markup, whitespace or the characters `: / \\ % ? #`, and every
                    member must be an existing user. Both answer 400. The stored group's duty schedules and default
                    map are kept when it is overwritten.
                    The response carries no entity; the new group's URI is in the `Location` header.""",
            operationId = "addGroupV1"
    )
    @RequestBody(required = true, description = "The group to store.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = OnmsGroup.class),
                    examples = @ExampleObject(value = """
                    <group>
                      <name>NOC</name>
                      <comments>Second-line network operations</comments>
                      <user>admin</user>
                    </group>""")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group stored. `Location` points at the new group."),
            @ApiResponse(responseCode = "400", description = "The name is reserved or invalid, or a member is not a user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "jroe is not a user."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "409", description = "The group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/xml`."),
            @ApiResponse(responseCode = "500", description = "The body could not be unmarshalled, or the group could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to marshal/unmarshal XML file while unmarshalling an object (OnmsGroup)")))
    })
    public Response addGroup(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsGroup group) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        
        try {
            LOG.debug("addGroup: Adding group {}", group);
            try {
                checkNewGroup(group);
                m_groupService.saveGroup(group);
            } catch (final Throwable t) {
                throw translate(t);
            }
            return Response.created(getRedirectUri(uriInfo, group.getName())).build();
        } finally {
            writeUnlock();
        }
    }
    
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{groupName}")
    @Operation(
            deprecated = true,
            summary = "Update a group",
            description = """
                    Apply form-encoded fields to an existing group. Keys are matched against the writable
                    `OnmsGroup` bean properties; a key that matches none is skipped, and a request that wrote
                    nothing comes back as 304. Sending `name` answers 400, because renaming is not supported
                    here; use `PUT /api/v2/groups/{name}/rename`. A key with an index or a nested path, such as
                    `users[0]`, answers 400. Members have to be existing users (400).
                    The stored duty schedules and default map are kept. A stale save answers 409.
                    `users` is a list property that the conversion fills with a single element, so a
                    comma-separated value becomes one element containing the commas.""",
            operationId = "updateGroupV1"
    )
    @RequestBody(required = true, description = "Fields to apply.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = GroupUpdateForm.class),
                    examples = @ExampleObject(value = "comments=Second-line+network+operations")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The group was updated."),
            @ApiResponse(responseCode = "304", description = "No supplied key matched a writable property, so nothing was written."),
            @ApiResponse(responseCode = "400", description = "The form carries `name` (renaming is not supported here) or an indexed or nested key, or a member is not a user.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/groups/{name}/rename."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist."))),
            @ApiResponse(responseCode = "409", description = "The group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response updateGroup(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName, final MultivaluedMapImpl params) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        
        try {
            OnmsGroup group = getOnmsGroup(groupName);
            LOG.debug("updateGroup: updating group {}", group);
            if (params.containsKey("name")) {
                throw getException(Status.BAD_REQUEST, "Renaming is not supported by the deprecated v1 API; use PUT /api/v2/groups/{name}/rename.");
            }
            UserRestService.checkFormKeys(params);
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
                try {
                    checkMembers(group);
                    m_groupService.saveGroup(group);
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
    @Path("{groupName}")
    @Operation(
            deprecated = true,
            summary = "Delete a group",
            description = "Delete a group. Its category authorizations go with it; the member users are left alone. "
                    + "Deleting the membership group of an on-call role or a target of a notification destination path "
                    + "answers 409.",
            operationId = "deleteGroupV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The group was deleted."),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist."))),
            @ApiResponse(responseCode = "409", description = "The group is the membership group of an on-call role or a target of a destination path, or the group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group \"NOC\" is the membership group of the on-call role \"Weekend\". Change the membership group first.")))
    })
    public Response deleteGroup(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            final OnmsGroup group = getOnmsGroup(groupName);
            LOG.debug("deleteGroup: deleting group {}", group);
            try {
                // the checks run before the manager call and without m_support.lockForWrite(): the manager saves in
                // its own REQUIRES_NEW transaction and takes the version locks itself, so holding them here would deadlock
                checkDeletable(groupName);
                m_groupService.deleteGroup(groupName);
            } catch (final Throwable t) {
                throw translate(t);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{groupName}/users/{userName}")
    @Operation(
            deprecated = true,
            summary = "Add a user to a group",
            description = """
                    Add one user to a group. The request takes no body.
                    A user name that is already a member and a user name that does not exist share
                    the same 400 response.""",
            operationId = "addGroupUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was added to the group."),
            @ApiResponse(responseCode = "400", description = "The user is already a member, or no such user exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User with name 'jroe' already added or does not exist."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist."))),
            @ApiResponse(responseCode = "409", description = "The group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response addUser(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "User name.", example = "admin", required = true)
            @PathParam("userName") final String userName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            getOnmsGroup(groupName); // just ensure that group exists
            final boolean success;
            try {
                success = m_groupService.addUser(groupName, userName);
            } catch (final Throwable t) {
                throw translate(t);
            }
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
    @Operation(
            deprecated = true,
            summary = "Remove a user from a group",
            description = "Drop one user from a group's member list. The user itself is not deleted.",
            operationId = "removeGroupUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was removed from the group."),
            @ApiResponse(responseCode = "400", description = "The user is not a member of the group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User with name 'jroe' does not exist in group 'NOC'"))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist."))),
            @ApiResponse(responseCode = "409", description = "The group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response removeUser(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "User name.", example = "admin", required = true)
            @PathParam("userName") final String userName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            final OnmsGroup group = getOnmsGroup(groupName);
            if (group.getUsers().contains(userName)) {
                group.removeUser(userName);
                try {
                    m_groupService.saveGroup(group);
                } catch (final Throwable t) {
                    throw translate(t);
                }
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
    @Operation(
            deprecated = true,
            summary = "List the users in a group",
            description = """
                    Return the full user records for the group's members. Names in the group that have no
                    user record are skipped rather than reported.
                    A caller without `ROLE_ADMIN` sees the literal `xxxxxxxx` in place of every password hash
                    except their own.""",
            operationId = "listGroupUsersV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The group's members.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsUserList.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
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
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public OnmsUserList listUsersOfGroup(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
        getOnmsGroup(groupName); // check if group exists.
        final OnmsUserList users = m_groupService.getUsersOfGroup(groupName);
        for (final OnmsUser user : users) {
            UserRestService.filterUserPassword(securityContext, user);
        }
        return users;
    }
    
    @GET
    @Path("{groupName}/users/{userName}")
    @Operation(
            deprecated = true,
            summary = "Get one user of a group",
            description = "Return the full user record for one member of the group. The password hash is masked "
                    + "for non-admin callers, except their own.",
            operationId = "getGroupUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The group member.",
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
            @ApiResponse(responseCode = "404", description = "No such group, or the user is not a member of it.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User with name 'jroe' does not exist in group 'NOC'")))
    })
    public OnmsUser getUser(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "User name.", example = "admin", required = true)
            @PathParam("userName") final String userName) {
        getOnmsGroup(groupName); // check if group exists.
        OnmsUser user = m_groupService.getUserForGroup(groupName,  userName);
        if (user != null) return UserRestService.filterUserPassword(securityContext, user);
        throw getException(Status.NOT_FOUND, "User with name '{}' does not exist in group '{}'", userName, groupName);
    }

    @PUT
    @Path("{groupName}/categories/{categoryName}")
    @Operation(
            deprecated = true,
            summary = "Authorize a group for a category",
            description = """
                    Add the group to a surveillance category's authorized-groups list. The request takes no body.
                    An already-authorized category and a category that does not exist share the same 400
                    response.""",
            operationId = "addGroupCategoryV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The group was authorized for the category."),
            @ApiResponse(responseCode = "400", description = "The group is already authorized, or no such category exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'Routers' already added or does not exist."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist."))),
            @ApiResponse(responseCode = "409", description = "The group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response addCategory(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "Surveillance category name.", example = "Routers", required = true)
            @PathParam("categoryName") final String categoryName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            getOnmsGroup(groupName); // check if group exists.
            final boolean success;
            try {
                success = m_groupService.addCategory(groupName, categoryName);
            } catch (final Throwable t) {
                throw translate(t);
            }
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
    @Operation(
            deprecated = true,
            summary = "Revoke a group's authorization for a category",
            description = "Remove the group from a surveillance category's authorized-groups list.",
            operationId = "removeGroupCategoryV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The authorization was removed."),
            @ApiResponse(responseCode = "400", description = "The group was not authorized for that category, or no such category exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'Routers' does not exist. Remove failed."))),
            @ApiResponse(responseCode = "403", description = "The caller does not hold `ROLE_ADMIN`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group management requires the admin role."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist."))),
            @ApiResponse(responseCode = "409", description = "The group data was changed by another process since it was loaded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))
    })
    public Response removeCategory(
            @Context final SecurityContext securityContext,
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "Surveillance category name.", example = "Routers", required = true)
            @PathParam("categoryName") final String categoryName) {
        m_support.assertAdmin(securityContext, WHAT);
        writeLock();
        try {
            getOnmsGroup(groupName); // check if group exists.
            final boolean success;
            try {
                success = m_groupService.removeCategory(groupName, categoryName);
            } catch (final Throwable t) {
                throw translate(t);
            }
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
    @Operation(
            deprecated = true,
            summary = "Get one category a group is authorized for",
            description = "Return the category record, provided the group is authorized for it.",
            operationId = "getGroupCategoryV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The category.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "authorizedGroups": [ "NOC" ],
                      "name": "Routers"
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No such group, or the group is not authorized for that category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'Switches' does not exist for group 'NOC'.")))
    })
    public OnmsCategory getCategoryForGroup(
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "Surveillance category name.", example = "Routers", required = true)
            @PathParam("categoryName") final String categoryName) {
        getOnmsGroup(groupName); // check if group exists.
        List<OnmsCategory> categories = m_groupService.getAuthorizedCategories(groupName);
        for (OnmsCategory eachCategory : categories) {
            if (eachCategory.getName().equals(categoryName)) return eachCategory;
        }
        throw getException(Status.NOT_FOUND, "Category with name '{}' does not exist for group '{}'.", categoryName, groupName);
    }
    
    @GET
    @Path("{groupName}/categories")
    @Operation(
            deprecated = true,
            summary = "List the categories a group is authorized for",
            description = "Return every surveillance category whose authorized-groups list names this group. "
                    + "`totalCount` and `count` are `null` when the list is empty.",
            operationId = "listGroupCategoriesV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The authorized categories.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsCategoryCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "category": [
                        {
                          "id": 1,
                          "authorizedGroups": [ "NOC" ],
                          "name": "Routers"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public OnmsCategoryCollection listCategories(
            @Parameter(description = "Group name.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
        writeLock();
        try {
            getOnmsGroup(groupName); // check if group exists.
            return new OnmsCategoryCollection(m_groupService.getAuthorizedCategories(groupName));
        } finally {
            writeUnlock();
        }
    }
    
    /** Checks a group that a POST creates or overwrites. */
    private void checkNewGroup(final OnmsGroup group) throws Exception {
        final String name = group == null ? null : group.getName();
        if (name == null || name.isBlank()) {
            throw m_support.badRequest("A group name is required.");
        }
        // a stored name is grandfathered: a POST that overwrites it must not fail on today's rules
        if (!m_groupService.existsGroup(name)) {
            final String problem = UserMgmtValidation.validateName(name, "group name", ReservedNames.GROUP_NAMES);
            if (problem != null) {
                throw m_support.badRequest(problem);
            }
            m_support.checkNotLookAlike(name, GroupsRestService.PROTECTED_GROUPS, "group name");
        }
        // OnmsGroup carries no duty schedules, so there are none to validate here
        checkMembers(group);
    }

    /** Every member must be a user, as the database store requires: a member that is not is a 400, not a 500. */
    private void checkMembers(final OnmsGroup group) throws IOException {
        if (group.getUsers() == null) {
            return;
        }
        for (final String user : group.getUsers()) {
            if (user == null || !m_userManager.hasUser(user)) {
                throw m_support.badRequest(user + " is not a user.");
            }
        }
    }

    private void checkDeletable(final String name) throws Exception {
        // the manager refuses this too, with this message, but as an IllegalStateException that is not told apart from a failure
        final List<OnCallRole> roles = m_onCallRoleDao.findByMembershipGroup(name);
        if (!roles.isEmpty()) {
            throw m_support.conflict("Group \"" + name + "\" is the membership group of the on-call role \""
                    + roles.get(0).getName() + "\". Change the membership group first.");
        }
        m_support.checkNotDestinationPathTarget("Group " + name, name);
    }

    /** Answers what the checks and the manager refuse with 400 or 409; only a real failure is a 500. */
    private static WebApplicationException translate(final Throwable t) {
        if (t instanceof WebApplicationException) {
            return (WebApplicationException) t;
        }
        if (t instanceof IllegalArgumentException || t instanceof UserMgmtValidationException) {
            return getException(Status.BAD_REQUEST, t);
        }
        // the group DAO wraps what the manager throws, so the stale cache is found along the cause chain
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            if (cause instanceof UserDataChangedException) {
                return getException(Status.CONFLICT, cause);
            }
        }
        return getException(Status.INTERNAL_SERVER_ERROR, t);
    }

    protected OnmsGroup getOnmsGroup(final String groupName) {
        OnmsGroup group = m_groupService.getOnmsGroup(groupName);
        if (group == null) throw getException(Status.NOT_FOUND, "Group with name '{}' does not exist.", groupName);
        return group;
    }

}
