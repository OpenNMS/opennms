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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.model.GroupUpdateForm;
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
@Tag(name = "Groups", description = "Groups API")
@Transactional
public class GroupRestService extends OnmsRestService {
	
    private static final Logger LOG = LoggerFactory.getLogger(GroupRestService.class);

    @Autowired
    private GroupService m_groupService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List groups",
            description = "Return every group defined in `groups.xml`, sorted by name. The list is not paged and "
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
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
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
            summary = "Add or replace a group",
            description = """
                    Create a group from an XML `<group>` document. Only `application/xml` is consumed; a JSON body
                    is rejected with 415.
                    Posting a name that already exists overwrites that group rather than failing. Member user names
                    are stored as given and are not checked against `users.xml`.
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
            @ApiResponse(responseCode = "415", description = "The body was not `application/xml`."),
            @ApiResponse(responseCode = "500", description = "The body could not be unmarshalled, or the group could not be saved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to marshal/unmarshal XML file while unmarshalling an object (OnmsGroup)")))
    })
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
    @Operation(
            summary = "Update a group",
            description = """
                    Apply form-encoded fields to an existing group. Keys are matched against the writable
                    `OnmsGroup` bean properties; a key that matches none is skipped, and a request that wrote
                    nothing comes back as 304.
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
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public Response updateGroup(
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName, final MultivaluedMapImpl params) {
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
    @Operation(
            summary = "Delete a group",
            description = "Remove a group from `groups.xml`. Its category authorizations go with it; the member "
                    + "users are left alone.",
            operationId = "deleteGroupV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The group was deleted."),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public Response deleteGroup(
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
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
    @Operation(
            summary = "Add a user to a group",
            description = """
                    Add one user to a group. The request takes no body.
                    A user name that is already a member and a user name that does not exist in `users.xml` share
                    the same 400 response.""",
            operationId = "addGroupUserV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The user was added to the group."),
            @ApiResponse(responseCode = "400", description = "The user is already a member, or no such user exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User with name 'jroe' already added or does not exist."))),
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public Response addUser(
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "admin", required = true)
            @PathParam("userName") final String userName) {
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
    @Operation(
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
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public Response removeUser(
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "admin", required = true)
            @PathParam("userName") final String userName) {
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
    @Operation(
            summary = "List the users in a group",
            description = """
                    Return the full user records for the group's members. Names in the group that have no
                    `users.xml` entry are skipped rather than reported.
                    Password hashes are not masked for non-admin callers.""",
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
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
        getOnmsGroup(groupName); // check if group exists.
        return m_groupService.getUsersOfGroup(groupName);
    }
    
    @GET
    @Path("{groupName}/users/{userName}")
    @Operation(
            summary = "Get one user of a group",
            description = "Return the full user record for one member of the group. The password hash is not "
                    + "masked for non-admin callers.",
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
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "User name as it appears in `users.xml`.", example = "admin", required = true)
            @PathParam("userName") final String userName) {
        getOnmsGroup(groupName); // check if group exists.
        OnmsUser user = m_groupService.getUserForGroup(groupName,  userName);
        if (user != null) return user;
        throw getException(Status.NOT_FOUND, "User with name '{}' does not exist in group '{}'", userName, groupName);
    }

    @PUT
    @Path("{groupName}/categories/{categoryName}")
    @Operation(
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
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public Response addCategory(
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "Surveillance category name.", example = "Routers", required = true)
            @PathParam("categoryName") final String categoryName) {
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
    @Operation(
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
            @ApiResponse(responseCode = "404", description = "No such group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NOC' does not exist.")))
    })
    public Response removeCategory(
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName,
            @Parameter(description = "Surveillance category name.", example = "Routers", required = true)
            @PathParam("categoryName") final String categoryName) {
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
    @Operation(
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
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
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
            @Parameter(description = "Group name as it appears in `groups.xml`.", example = "Admin", required = true)
            @PathParam("groupName") final String groupName) {
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
