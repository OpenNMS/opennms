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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
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
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>CategoryRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Component("categoryRestService")
@Path("categories")
@Tag(name = "Categories", description = """
        Categories API.

        Surveillance categories group nodes and gate who may see them. A category is identified by name
        everywhere in this API, and names are unique.

        Deleting a category is unconditional: requisitions, surveillance views, notification rules and filter
        expressions that name it are not consulted and keep the now-dangling name.

        `/categories/nodes/...` and `/categories/groups/...` are fixed sub-resources, so a category whose name is
        literally `nodes` or `groups` cannot be addressed through `/categories/{categoryName}`.""")
@Transactional
public class CategoryRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CategoryRestService.class);

    @Autowired
    private CategoryDao m_categoryDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/nodes/{nodeCriteria}")
    @Operation(
            summary = "List the categories assigned to a node",
            description = """
                    Returns the categories currently on one node. Equivalent to
                    `GET /nodes/{nodeCriteria}/categories`, which this delegates to.

                    On a node with no categories the collection comes back with `totalCount` and `count` null
                    rather than 0.""",
            operationId = "listCategoriesOfNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "category": [
                        { "id": 1, "authorizedGroups": [], "name": "Routers" },
                        { "id": 4, "authorizedGroups": ["Admin"], "name": "Production" }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    <categories count="2" offset="0" totalCount="2">
                      <category id="1" name="Routers"/>
                      <category id="4" name="Production">
                        <authorizedGroups>Admin</authorizedGroups>
                      </category>
                    </categories>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found.")))
    })
    public OnmsCategoryCollection getCategoriesForNode(@Context final ResourceContext context,
                                                       @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                                       @PathParam("nodeCriteria") String nodeCriteria) {
        return context.getResource(NodeRestService.class).getCategoriesForNode(nodeCriteria);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{categoryName}/nodes/{nodeCriteria}")
    @Operation(
            summary = "Check whether a node carries a category",
            description = """
                    Returns the category if it is assigned to the node, so this doubles as a membership test.
                    The lookup is over the node's own categories, so a category that exists but is not on the node
                    is a 404 rather than a 200.""",
            operationId = "getCategoryOfNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The category is assigned to the node.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    { "id": 1, "description": "Routing devices", "authorizedGroups": [], "name": "Routers" }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    <category id="1" name="Routers">
                      <description>Routing devices</description>
                    </category>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node exists but does not carry that category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find category Test for node 258.")))
    })
    public OnmsCategory getCategoryForNode(@Context final ResourceContext context,
                                           @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                           @PathParam("nodeCriteria") String nodeCriteria,
                                           @Parameter(description = "Category name.", example = "Routers")
                                           @PathParam("categoryName") final String categoryName) {
        return context.getResource(NodeRestService.class).getCategoryForNode(nodeCriteria, categoryName);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{categoryName}/nodes/{nodeCriteria}/")
    @Operation(
            summary = "Assign an existing category to a node",
            description = """
                    Adds an already-defined category to a node. The category is not created here; an unknown name
                    is a 400.

                    No request body is read, but the method declares `@Consumes(application/xml)`, so a request
                    that sends no `Content-Type` at all is rejected with 400 before the handler runs. Send
                    `Content-Type: application/xml` with an empty body.

                    The `Location` header of the 201 is built by appending the category name to the request URI,
                    so it comes back with the name repeated
                    (`/categories/Routers/nodes/258/Routers`) and is not a usable URL.""",
            operationId = "assignCategoryToNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The category was added. `Location` repeats the category name and is not a working URL."),
            @ApiResponse(responseCode = "400", description = "No such node, no such category, the category is already on the node, or no `Content-Type` was sent.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "alreadyAssigned", value = "Category 'Routers' already added to node '258'"),
                                    @ExampleObject(name = "unknownCategory", value = "Category NoSuchCat was not found."),
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found.")
                            }))
    })
    public Response addCategoryToNode(@Context final ResourceContext context, @Context final UriInfo uriInfo,
                                      @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") final String nodeCriteria,
                                      @Parameter(description = "Name of an existing category.", example = "Routers")
                                      @PathParam("categoryName") final String categoryName) {
        return context.getResource(NodeRestService.class).addCategoryToNode(uriInfo, nodeCriteria, categoryName);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{categoryName}")
    @Operation(
            summary = "Update a category definition",
            description = """
                    Applies form-encoded fields to the category itself, not to any node-category assignment. Keys
                    are bean property names on `OnmsCategory`; unknown keys are ignored.

                    `id`, `dbId`, `nodeId`, `authorizedGroups`, `foreignSource`, `foreignId` and `type` are
                    protected and dropped with a log warning, so the group ACL cannot be edited here; use
                    `PUT /categories/{categoryName}/groups/{groupName}`.

                    `name` is *not* protected on this endpoint, so sending `name=` renames the category in place.
                    The same field is protected on `PUT /nodes/{nodeCriteria}/categories/{categoryName}`. After a
                    rename, requests that still use the old name have been observed to fail with HTTP 500 rather
                    than 404, because the name-to-category lookup is cached.""",
            operationId = "updateCategory"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded category fields to set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "description", summary = "Set the description",
                                    value = "description=Routing+devices"),
                            @ExampleObject(name = "rename", summary = "Rename the category",
                                    value = "name=Core-Routers")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one field was written. No body."),
            @ApiResponse(responseCode = "304", description = "No key in the body resolved to a writable, unprotected property."),
            @ApiResponse(responseCode = "400", description = "No category has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'NoSuchCat' was not found.")))
    })
    public Response updateCategory(@Parameter(description = "Current name of the category.", example = "Routers")
                                   @PathParam("categoryName") final String categoryName, final MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsCategory category = m_categoryDao.findByName(categoryName);
            if (category == null) {
                throw getException(Status.BAD_REQUEST, "Category with name '{}' was not found.", categoryName);
            }
            LOG.debug("updateCategory: updating category {}", category);
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(category);
            boolean modified = false;
            for(String key : params.keySet()) {
                if (RestUtils.isProtectedProperty(key)) {
                    LOG.warn("updateCategory: ignoring attempt to set protected property '{}'", key);
                    continue;
                }
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            LOG.debug("updateCategory: category {} updated", category);
            if (modified) {
                m_categoryDao.saveOrUpdate(category);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("/{categoryName}/nodes/{nodeCriteria}/")
    @Operation(
            summary = "Remove a category from a node",
            description = """
                    Detaches the category from the node. The category definition itself is left alone.
                    Equivalent to `DELETE /nodes/{nodeCriteria}/categories/{categoryName}`.""",
            operationId = "unassignCategoryFromNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The category is no longer on the node. No body."),
            @ApiResponse(responseCode = "400", description = "No such node, or the node does not carry that category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "notOnNode", value = "Category Routers not found on node 258"),
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found.")
                            }))
    })
    public Response removeCategoryFromNode(@Context final ResourceContext context,
                                           @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                           @PathParam("nodeCriteria") String nodeCriteria,
                                           @Parameter(description = "Category name.", example = "Routers")
                                           @PathParam("categoryName") String categoryName) {
        return context.getResource(NodeRestService.class).removeCategoryFromNode(nodeCriteria, categoryName);
    }

    @GET
    @Path("/{categoryName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get one category by name",
            description = """
                    Looks a category up by name. `authorizedGroups` lists the groups allowed to see nodes in the
                    category; an empty list means no group restriction.""",
            operationId = "getCategoryByName"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The category.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    { "id": 1, "description": "Routing devices", "authorizedGroups": ["Admin"], "name": "Routers" }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    <category id="1" name="Routers">
                      <authorizedGroups>Admin</authorizedGroups>
                      <description>Routing devices</description>
                    </category>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No category has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'NoSuchCat' was not found.")))
    })
    public OnmsCategory getCategory(@Parameter(description = "Category name.", example = "Routers")
                                    @PathParam("categoryName") final String categoryName) {
        OnmsCategory category = m_categoryDao.findByName(categoryName);
        if (category == null) throw getException(Response.Status.NOT_FOUND, "Category with name '{}' was not found.", categoryName);
        return category;
    }
    
    @POST
    @Path("/")
    @Operation(
            summary = "Create a category",
            description = """
                    Creates a surveillance category. The name must not already be in use.

                    The method declares no `@Consumes`, so both an XML and a JSON body are accepted. A
                    form-encoded body is rejected with 415, and a body that fails to parse comes back as 500
                    rather than 400.

                    `id` in the body is ignored; the database assigns it. `authorizedGroups` sent here is not
                    applied, so grant group access afterwards with
                    `PUT /categories/{categoryName}/groups/{groupName}`.""",
            operationId = "createCategory"
    )
    @RequestBody(
            required = true,
            description = "The category to create. Only `name` is required; `description` is optional.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = """
                    <category name="Core-Routers">
                      <description>Spine and core routing devices</description>
                    </category>""")),
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = """
                    {
                      "name": "Core-Routers",
                      "description": "Spine and core routing devices"
                    }"""))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created. `Location` points at `/categories/{name}`."),
            @ApiResponse(responseCode = "400", description = "The body was absent, or a category with that name already exists.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "duplicate", value = "A category with name 'Routers' already exists."),
                                    @ExampleObject(name = "missing", value = "Category must not be null.")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was form-encoded. Send XML or JSON."),
            @ApiResponse(responseCode = "500", description = "The body could not be parsed as a category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to marshal/unmarshal XML file while unmarshalling an object (OnmsCategory): javax.xml.bind.UnmarshalException")))
    })
    public Response createCategory(@Context final UriInfo uriInfo, final OnmsCategory category) {
        if (category == null) throw getException(Response.Status.BAD_REQUEST, "Category must not be null.");
        boolean exists = m_categoryDao.findByName(category.getName()) != null;
        if (!exists) {
            m_categoryDao.save(category);
            return Response.created(getRedirectUri(uriInfo, category.getName())).build();
        }
        throw getException(Response.Status.BAD_REQUEST, "A category with name '{}' already exists.", category.getName());
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List all categories",
            description = """
                    Returns every surveillance category defined in the system. The result is not paged and takes
                    no query parameters; `offset` is always 0 and `count` always equals `totalCount`. The order
                    follows the database and is not guaranteed.""",
            operationId = "listCategories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 3,
                      "count": 3,
                      "offset": 0,
                      "category": [
                        { "id": 1, "authorizedGroups": [], "name": "Routers" },
                        { "id": 2, "authorizedGroups": [], "name": "Switches" },
                        { "id": 4, "description": "Production estate", "authorizedGroups": ["Admin"], "name": "Production" }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    <categories count="2" offset="0" totalCount="2">
                      <category id="1" name="Routers"/>
                      <category id="2" name="Switches"/>
                    </categories>"""))
                    })
    })
    public OnmsCategoryCollection listCategories() {
        return new OnmsCategoryCollection(new ArrayList<OnmsCategory>(m_categoryDao.findAll()));
    }

    @DELETE
    @Path("/{categoryName}")
    @Operation(
            summary = "Delete a category",
            description = """
                    Deletes the category definition and, with it, its assignment to every node and its group ACL
                    entries.

                    Nothing else is checked first. Requisitions, surveillance views, notification rules and
                    filter expressions that reference the name are not consulted and keep referring to a category
                    that no longer exists.""",
            operationId = "deleteCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted. No body."),
            @ApiResponse(responseCode = "400", description = "No category has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "A category with name 'NoSuchCat' does not exist.")))
    })
    public Response deleteCategory(@Parameter(description = "Category name.", example = "Core-Routers")
                                   @PathParam("categoryName") final String categoryName) {
        OnmsCategory category = m_categoryDao.findByName(categoryName);
        if (category != null) {
            m_categoryDao.delete(category);
            return Response.noContent().build();
        }
        throw getException(Response.Status.BAD_REQUEST, "A category with name '{}' does not exist.", categoryName);
    }


    @PUT
    @Path("/{categoryName}/groups/{groupName}")
    @Operation(
            summary = "Authorize a group for a category",
            description = """
                    Adds the category to a group's authorized-category list, which is what lets members of that
                    group see nodes in the category. No request body is read.

                    Both the "category does not exist" and the "category is already authorized for this group"
                    cases come back as the same 400 message.""",
            operationId = "authorizeGroupForCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The group is now authorized for the category. No body."),
            @ApiResponse(responseCode = "400", description = "The category does not exist, or is already authorized for the group.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'Routers' already added or does not exist."))),
            @ApiResponse(responseCode = "404", description = "No group has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NoSuchGroup' does not exist.")))
    })
    public Response addCategoryToGroup(@Context final ResourceContext context,
                                       @Parameter(description = "Name of an existing group.", example = "Admin")
                                       @PathParam("groupName") final String groupName,
                                       @Parameter(description = "Name of an existing category.", example = "Routers")
                                       @PathParam("categoryName") final String categoryName) {
        return context.getResource(GroupRestService.class).addCategory(groupName, categoryName);
    }

    @DELETE
    @Path("/{categoryName}/groups/{groupName}")
    @Operation(
            summary = "Withdraw a group's authorization for a category",
            description = "Removes the category from a group's authorized-category list. The category definition itself is untouched.",
            operationId = "deauthorizeGroupForCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The authorization was removed. No body."),
            @ApiResponse(responseCode = "400", description = "The category was not in the group's authorized list.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category with name 'Routers' does not exist. Remove failed."))),
            @ApiResponse(responseCode = "404", description = "No group has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NoSuchGroup' does not exist.")))
    })
    public Response removeCategoryFromGroup(@Context final ResourceContext context,
                                            @Parameter(description = "Name of an existing group.", example = "Admin")
                                            @PathParam("groupName") final String groupName,
                                            @Parameter(description = "Category name.", example = "Routers")
                                            @PathParam("categoryName") final String categoryName) {
        return context.getResource(GroupRestService.class).removeCategory(groupName, categoryName);
    }

    @GET
    @Path("/groups/{groupName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List the categories a group is authorized for",
            description = """
                    Returns the categories on one group's authorized-category list. A group with none returns an
                    empty collection whose `totalCount` and `count` are null rather than 0.""",
            operationId = "listCategoriesForGroup"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The group's authorized categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "category": [
                        { "id": 1, "description": "Routing devices", "authorizedGroups": ["Admin"], "name": "Routers" }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    <categories count="1" offset="0" totalCount="1">
                      <category id="1" name="Routers">
                        <authorizedGroups>Admin</authorizedGroups>
                        <description>Routing devices</description>
                      </category>
                    </categories>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No group has that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Group with name 'NoSuchGroup' does not exist.")))
    })
    public OnmsCategoryCollection listCategoriesForGroup(@Context final ResourceContext context,
                                                         @Parameter(description = "Group name.", example = "Admin")
                                                         @PathParam("groupName") final String groupName) {
        return context.getResource(GroupRestService.class).listCategories(groupName);
    }
}
