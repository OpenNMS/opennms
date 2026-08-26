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
package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.Optional;

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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsCategory} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeCategoriesRestService extends AbstractNodeDependentRestService<OnmsCategory,OnmsCategory,Integer,String> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeCategoriesRestService.class);

    @Autowired
    private CategoryDao m_dao;

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Override
    protected CategoryDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsCategory> getDaoClass() {
        return OnmsCategory.class;
    }

    @Override
    protected Class<OnmsCategory> getQueryBeanClass() {
        return OnmsCategory.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        return new CriteriaBuilder(getDaoClass()).distinct();
    }

    @Override
    protected JaxbListWrapper<OnmsCategory> createListWrapper(Collection<OnmsCategory> list) {
        return new OnmsCategoryCollection(list);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the categories of a node",
            description = """
        Return the surveillance categories the node belongs to. This operation reads the node's category
        set directly, so `_s`, `limit`, `offset`, `orderBy` and `order` are accepted but have no effect
        and the whole set is always returned. The order is the set's iteration order, which is not
        stable across calls.

        A node with no categories gives a 200 whose `totalCount` and `count` are `null` rather than
        `0`.""",
            operationId = "NodeCategoriesRestServiceGETCategories",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.", example = "257"))
    @ApiResponses({
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
                        {"id": 1, "authorizedGroups": [], "name": "Routers"},
                        {"id": 4, "authorizedGroups": [], "name": "Production"}
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    <categories count="2" offset="0" totalCount="2">
                      <category id="1" name="Routers"/>
                      <category id="4" name="Production"/>
                    </categories>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No such node. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        final OnmsNode node = getNode(uriInfo);
        if (node == null) return Response.status(Status.NOT_FOUND).build();
        return Response.ok(new OnmsCategoryCollection(node.getCategories())).build();
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count the categories of a node",
            description = """
        Return the size of the node's category set as a bare decimal string. `_s` is accepted but has no
        effect, so this is not a filtered count.""",
            operationId = "NodeCategoriesRestServiceGETCategoryCount",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number of categories the node belongs to.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "2"))),
            @ApiResponse(responseCode = "404", description = "No such node. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        final OnmsNode node = getNode(uriInfo);
        if (node == null) return Response.status(Status.NOT_FOUND).build();
        return Response.ok(node.getCategories().size()).build();
    }

    // The generic operations below are inherited from AbstractDaoRestServiceWithDTO. They are
    // overridden here only so that each concrete path carries its own OpenAPI documentation; the
    // bodies delegate unchanged.
    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get category search properties",
            description = """
        This resource declares no search property set, so the operation answers 204 with no body. The
        category collection ignores `_s` in any case.""",
            operationId = "NodeCategoriesRestServiceGETCategorySearchProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponse(responseCode = "204", description = "No search properties are declared for this resource. No body is returned.")
    @Override
    public Response getProperties(
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Case-insensitive substring matched against the property `name`, not its id. Has no effect while the property set is empty.", example = "Name")
            @QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get values of a category search property",
            description = "Answered with 404 for every `propertyId`, because this resource declares no search property set.",
            operationId = "NodeCategoriesRestServiceGETCategorySearchPropertyValues",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponse(responseCode = "404", description = "No search property has that id. No body is returned.")
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId", description = "Property id.", example = "name")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q", description = "Substring the value must contain.", example = "Rout")
            @QueryParam("q") final String query,
            @Parameter(in = ParameterIn.QUERY, name = "limit", description = "Maximum number of values to return.", example = "10")
            @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one category of a node",
            description = """
        Return the named category if the node belongs to it. The name is matched exactly and
        case-sensitively. A category that exists in the system but is not on this node is a 404.""",
            operationId = "NodeCategoriesRestServiceGETCategoryByName",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The category.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    {"id": 1, "authorizedGroups": [], "name": "Routers"}""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    <category id="1" name="Routers"/>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "The node does not belong to a category with that name. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response get(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Category name, not its database id.", example = "Routers")
            @PathParam("id") final String id) {
        return super.get(uriInfo, id);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: add a category at a caller-chosen path",
            description = "Always answered with 404, whether or not the category exists.",
            operationId = "NodeCategoriesRestServicePOSTCategorySpecific",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria", required = true,
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                            description = "Category name. The value is not read: every request to this path is answered with 404.",
                            example = "Routers")
            })
    @ApiResponse(responseCode = "404", description = "Not supported. No body is returned.")
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Add a node to a category",
            description = """
        Add the node to the named surveillance category and send a
        `nodeCategoryMembershipChanged` event. A category that does not yet exist is created. Adding a
        category the node already has is also a 201. The membership URI is returned in the `Location`
        header.""",
            operationId = "NodeCategoriesRestServicePOSTCategory",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "The category to add. Only `name` is read.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = """
                    {"name": "Production"}""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = """
                    <category name="Production"/>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The node was added to the category. `Location` carries the membership URI.",
                    headers = @Header(name = "Location", description = "URI of the category on this node.",
                            schema = @Schema(type = "string", example = "http://localhost:8980/opennms/api/v2/nodes/257/categories/Production"))),
            @ApiResponse(responseCode = "400", description = "The node was not found, or the body carried no category name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "no name", value = "Category's name cannot be null"),
                                    @ExampleObject(name = "no node", value = "Node was not found.")
                            })),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsCategory object) {
        return super.create(securityContext, uriInfo, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several categories",
            description = """
        Apply the form parameters as bean properties to a set of category rows. The criteria for this
        resource are not restricted to the node in the path, so the selection is every category in the
        system, capped by the default `limit` of 10. The node in the path affects only the 404 check.
        `name` is rejected with 400, so the writable property is `description`, and it is written on
        the shared category definition rather than on anything belonging to this node.""",
            operationId = "NodeCategoriesRestServicePUTCategories",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "Category bean properties to set, form-encoded. `name` is rejected.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "description=Managed+by+the+network+team")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The selected category rows were updated."),
            @ApiResponse(responseCode = "400", description = "The body tried to change `name`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot rename category."))),
            @ApiResponse(responseCode = "404", description = "No category row matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(
            summary = "Not implemented: replace a category from a document",
            description = """
        Answered with 501. This variant binds `{id}` as an integer while the collection addresses
        categories by name, so a name in the path is answered with 404 before the handler runs.""",
            operationId = "NodeCategoriesRestServicePUTCategoryDocument",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(description = "Ignored.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = "{\"name\": \"Routers\"}")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = OnmsCategory.class),
                            examples = @ExampleObject(value = "<category name=\"Routers\"/>"))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "`{id}` is not an integer, which is the case for every category name."),
            @ApiResponse(responseCode = "501", description = "Replacing a category from a document is not implemented. No body is returned.")
    })
    @Override
    public Response update(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Bound as an integer here, unlike every other operation on this path.", example = "1")
            @PathParam("id") final Integer id,
            final OnmsCategory object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Update properties of a category",
            description = """
        Apply the form parameters as bean properties to the named category. The node in the path selects
        the category and is checked for membership, but the row that is written is the shared category
        definition, so the change is visible on every node in that category. `name` is rejected.""",
            operationId = "NodeCategoriesRestServicePUTCategoryProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "Category bean properties to set, form-encoded. `name` is rejected.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "description=Managed+by+the+network+team")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The category was updated."),
            @ApiResponse(responseCode = "400", description = "The body tried to change `name`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot rename category."))),
            @ApiResponse(responseCode = "404", description = "The node does not belong to a category with that name. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response updateProperties(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Category name.", example = "Routers")
            @PathParam("id") final String id,
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Delete several categories",
            description = """
        Delete category rows outright. The criteria for this resource are not restricted to the node in
        the path, so the selection is every category in the system, capped by the default `limit` of 10.
        Each deletion removes the shared category definition and therefore its membership on every node,
        not only on the node in the path.""",
            operationId = "NodeCategoriesRestServiceDELETECategories",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The selected category rows were deleted."),
            @ApiResponse(responseCode = "404", description = "No category row matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete a category",
            description = """
        Remove the named category from the node and then delete the category row itself, so the category
        disappears from every other node that carried it as well. A `nodeCategoryMembershipChanged`
        event is sent for the node in the path only.""",
            operationId = "NodeCategoriesRestServiceDELETECategoryByName",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The category was deleted."),
            @ApiResponse(responseCode = "404", description = "The node does not belong to a category with that name. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response delete(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Category name.", example = "ApiDoc-Cat")
            @PathParam("id") final String id) {
        return super.delete(securityContext, uriInfo, id);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsCategory source) {
        OnmsNode node = getNode(uriInfo);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node was not found.");
        } else if (source == null) {
            throw getException(Status.BAD_REQUEST, "Category object cannot be null");
        } else if (source.getName() == null) {
            throw getException(Status.BAD_REQUEST, "Category's name cannot be null");
        }
        final OnmsCategory category = getCategory(source.getName());
        node.addCategory(category);
        m_nodeDao.saveOrUpdate(node);

        final Event event = EventUtils.createNodeCategoryMembershipChangedEvent("ReST", node.getId(), node.getLabel(), new String[] { category.getName() }, null);
        sendEvent(event);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, category.getName())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsCategory targetObject, MultivaluedMapImpl params) {
        if (RestUtils.containsProperty(params, "name")) {
            throw getException(Status.BAD_REQUEST, "Cannot rename category.");
        }
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsCategory category) {
        getNode(uriInfo).removeCategory(category);
        getDao().delete(category);

        final OnmsNode node = getNode(uriInfo);
        final Event event = EventUtils.createNodeCategoryMembershipChangedEvent("ReST", node.getId(), node.getLabel(), null, new String[] { category.getName() });
        sendEvent(event);
    }

    @Override
    protected OnmsCategory doGet(UriInfo uriInfo, String categoryName) {
        final OnmsNode node = getNode(uriInfo);
        if (node == null) return null;
        Optional<OnmsCategory> optional = node.getCategories().stream().filter(c -> c.getName().equals(categoryName)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    private OnmsCategory getCategory(final String categoryName) {
        final OnmsCategory category = new CreateIfNecessaryTemplate<OnmsCategory, CategoryDao>(m_transactionManager, m_dao) {
            @Override
            protected OnmsCategory query() {
                return m_dao.findByName(categoryName);
            }
            @Override
            protected OnmsCategory doInsert() {
                LOG.info("getCategory: creating category {}", categoryName);
                final OnmsCategory c = new OnmsCategory(categoryName);
                m_dao.saveOrUpdate(c);
                return c;
            }
        }.execute();
        return category;
    }

}
