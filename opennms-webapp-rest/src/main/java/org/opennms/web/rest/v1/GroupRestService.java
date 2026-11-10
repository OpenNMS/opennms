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
import java.util.HashSet;
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
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.netmgt.model.usermgmt.ReservedNames;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.model.GroupUpdateForm;
import org.opennms.web.rest.v2.UserMgmtRestSupport;
import org.opennms.web.rest.v2.UserMgmtValidation;
import org.opennms.web.svclayer.api.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        Groups are stored in the database. The checks are those of the v2 API. A new group name is validated,
        must not be reserved and must fit its column. Comments must not contain markup (stored comments are
        accepted unchanged), and every member must be an existing user, named once. All of these answer 400, as
        does renaming a group (use `PUT /api/v2/groups/{name}/rename`). A save through this API keeps the duty
        schedules and the default map of the stored group. Deleting the `Admin` group answers 400. Deleting a group
        that is the membership group of an on-call role or a target of a notification destination path answers
        409, as does a stale save.
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
                    must not be reserved, be too long, or contain markup, whitespace or the characters
                    `: / \\ % ? #`. Comments must not contain markup, and every member must be an existing user, named
                    once and not blank. All of these answer 400. The stored group's duty schedules and default map are
                    kept when it is overwritten.
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
            @ApiResponse(responseCode = "400", description = "The request is invalid (see the operation description).",
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
                // read before the checks read the stored group: see groupsVersion
                final long version = m_groupService.getVersion();
                checkNewGroup(group);
                m_groupService.saveGroup(group, version);
            } catch (final Throwable t) {
                throw V1UserMgmtForms.translate(t);
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
                    Apply form-encoded fields to an existing group. The accepted keys are `comments` and `users`,
                    spelled exactly; any other key answers 400, before anything is applied. Sending `name` answers
                    400, because renaming is not supported here; use `PUT /api/v2/groups/{name}/rename`. An empty
                    form comes back as 304. Comments must not contain markup (stored comments are accepted
                    unchanged), and members have to be existing users (400).
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
            @ApiResponse(responseCode = "304", description = "The form carried no field, so nothing was written."),
            @ApiResponse(responseCode = "400", description = "The form carries a key other than `comments` and `users`, or a field is invalid.",
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
            final long version = groupsVersion();
            final OnmsGroup group = getOnmsGroup(groupName);
            LOG.debug("updateGroup: updating group {}", group);
            V1UserMgmtForms.checkFormKeys(params, V1UserMgmtForms.GROUP_FORM_KEYS, V1UserMgmtForms.RENAME_GROUP);
            if (V1UserMgmtForms.applyForm(group, params, Set.of())) {
                LOG.debug("updateGroup: group {} updated", group);
                try {
                    checkFields(group);
                    m_groupService.saveGroup(group, version);
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
    @Path("{groupName}")
    @Operation(
            deprecated = true,
            summary = "Delete a group",
            description = "Delete a group. Its category authorizations go with it; the member users are left alone. "
                    + "Deleting the `Admin` group answers 400. Deleting the membership group of an on-call role or a "
                    + "target of a notification destination path answers 409.",
            operationId = "deleteGroupV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The group was deleted."),
            @ApiResponse(responseCode = "400", description = "The group is the `Admin` group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "The system group Admin cannot be deleted."))),
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
                            examples = @ExampleObject(value = "Group NOC is the membership group of on-call role(s) Weekend; delete or reassign those roles first.")))
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
                m_support.checkGroupDeletable(groupName);
                m_groupService.deleteGroup(groupName);
            } catch (final Throwable t) {
                throw V1UserMgmtForms.translate(t);
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
                throw V1UserMgmtForms.translate(t);
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
            final long version = groupsVersion();
            final OnmsGroup group = getOnmsGroup(groupName);
            if (group.getUsers().contains(userName)) {
                group.removeUser(userName);
                try {
                    m_groupService.saveGroup(group, version);
                } catch (final Throwable t) {
                    throw V1UserMgmtForms.translate(t);
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
            V1UserMgmtForms.filterUserPassword(securityContext, user);
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
        if (user != null) return V1UserMgmtForms.filterUserPassword(securityContext, user);
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
                throw V1UserMgmtForms.translate(t);
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
                throw V1UserMgmtForms.translate(t);
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
            m_support.checkNotLookAlike(name, UserMgmtRestSupport.PROTECTED_GROUPS, "group name");
            UserMgmtValidation.checkLength(owner(name), "group name", name, UserMgmtJaxbMapper.GROUP_NAME_LENGTH);
        }
        // OnmsGroup carries no duty schedules, so there are none to validate here
        checkFields(group);
    }

    /**
     * The v2 checks of the fields an OnmsGroup carries: comments (stored ones are grandfathered), and members, each
     * an existing user, named once and not blank.
     */
    private void checkFields(final OnmsGroup group) throws IOException {
        final Group stored = m_groupService.getGroup(group.getName());
        UserMgmtValidation.checkComments(owner(group.getName()), group.getComments(),
                stored == null ? null : stored.getComments().orElse(null));
        if (group.getUsers() == null) {
            return;
        }
        final Set<String> seen = new HashSet<>();
        for (final String user : group.getUsers()) {
            if (StringUtils.isBlank(user)) {
                throw m_support.badRequest("Group members must not be blank.");
            }
            if (!seen.add(user)) {
                throw m_support.badRequest("Duplicate group member: " + user);
            }
            if (!m_userManager.hasUser(user)) {
                throw m_support.badRequest(user + " is not a user.");
            }
        }
    }

    private static String owner(final String name) {
        return "Group \"" + name + "\"";
    }

    /**
     * The version of the groups, read before the group that a write saves back: if another writer changes the groups
     * in between, the save answers 409 instead of overwriting that change with the older members.
     */
    private long groupsVersion() {
        try {
            return m_groupService.getVersion();
        } catch (final Throwable t) {
            throw V1UserMgmtForms.translate(t);
        }
    }

    protected OnmsGroup getOnmsGroup(final String groupName) {
        OnmsGroup group = m_groupService.getOnmsGroup(groupName);
        if (group == null) throw getException(Status.NOT_FOUND, "Group with name '{}' does not exist.", groupName);
        return group;
    }

}
