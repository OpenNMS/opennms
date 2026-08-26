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
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao;
import org.opennms.netmgt.enlinkd.service.api.UserDefinedLinkTopologyService;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;

@Component
@Path("userdefinedlinks")
@Transactional
@Tag(name = "UserDefinedLinks", description = "User Defined Links API")
public class UserDefinedLinkRestService extends AbstractDaoRestService<UserDefinedLink,UserDefinedLink,Integer,Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeCategoriesRestService.class);

    @Autowired(required=false)
    private UserDefinedLinkDao m_dao;

    @Autowired(required=false)
    private UserDefinedLinkTopologyService m_service;

    @Override
    protected OnmsDao<UserDefinedLink, Integer> getDao() {
        return m_dao;
    }

    @Override
    protected Class<UserDefinedLink> getDaoClass() {
        return UserDefinedLink.class;
    }

    @Override
    protected Class<UserDefinedLink> getQueryBeanClass() {
        return UserDefinedLink.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        return new CriteriaBuilder(getDaoClass()).distinct();
    }

    @Override
    protected JaxbListWrapper<UserDefinedLink> createListWrapper(Collection<UserDefinedLink> list) {
        return new UserDefinedLinkCollection(list);
    }

    @Override
    protected UserDefinedLink doGet(UriInfo uriInfo, Integer id) {
        return m_dao.get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final UserDefinedLink udl) {
        if (udl == null) {
            throw getException(Response.Status.BAD_REQUEST, "Link cannot be null");
        }
        m_service.saveOrUpdate(udl);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, udl.getDbId())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, UserDefinedLink udl, MultivaluedMapImpl params) {
        RestUtils.setBeanProperties(udl, params);
        m_service.saveOrUpdate(udl);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, UserDefinedLink udl) {
        m_service.delete(udl);
    }

    @XmlRootElement(name = "user_defined_links")
    @JsonRootName("user_defined_links")
    public static class UserDefinedLinkCollection extends JaxbListWrapper<UserDefinedLink> {
        private static final long serialVersionUID = 1L;

        public UserDefinedLinkCollection() { super(); }

        public UserDefinedLinkCollection(final Collection<? extends UserDefinedLink> udls) {
            super(udls);
        }

        @XmlElement(name="user_defined_link")
        @JsonProperty("user_defined_link")
        public List<UserDefinedLink> getObjects() {
            return super.getObjects();
        }

    }

    @Override
    @Operation(summary = "List user-defined links",
            description = """
                    User-defined links matching the query. These are topology links entered by an operator rather than discovered by enlinkd, and each one names its two endpoints by node identifier.

                    This endpoint declares no search properties, so `_s` and `orderBy` fall back to the bean property names of `UserDefinedLink`: `dbId`, `linkId`, `linkLabel`, `owner`, `nodeIdA`, `nodeIdZ`, `componentLabelA` and `componentLabelZ`. Using `id` there fails to parse.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=owner==review-seed`.""",
            operationId = "userDefinedLinksList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching user-defined links.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserDefinedLinkRestService.UserDefinedLinkCollection.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 8,
                              "count": 1,
                              "offset": 0,
                              "user_defined_link": [ {
                                "db-id": 5,
                                "owner": "review-seed",
                                "link-id": "udl-13",
                                "link-label": "cross-connect 13",
                                "node-id-a": 13,
                                "component-label-a": "Gi1/1",
                                "node-id-z": 24,
                                "component-label-z": "Gi1/2"
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = UserDefinedLinkRestService.UserDefinedLinkCollection.class),
                                    examples = @ExampleObject(value = """
                            <user_defined_links count="1" offset="0" totalCount="8">
                              <user_defined_link>
                                <node-id-a>13</node-id-a>
                                <component-label-a>Gi1/1</component-label-a>
                                <node-id-z>24</node-id-z>
                                <component-label-z>Gi1/2</component-label-z>
                                <link-id>udl-13</link-id>
                                <link-label>cross-connect 13</link-label>
                                <owner>review-seed</owner>
                                <db-id>5</db-id>
                              </user_defined_link>
                            </user_defined_links>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No user-defined link matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count user-defined links",
            description = """
                    Number of user-defined links matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=owner==review-seed`.""",
            operationId = "userDefinedLinksCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching user-defined links, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "8"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of user-defined links",
            description = """
                    This endpoint declares no search properties.""",
            operationId = "userDefinedLinksSearchProperties")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = """
                    Always: no search properties are declared for user-defined links. The response has no body.""")
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    No search properties are declared for user-defined links, so no property identifier resolves here.""",
            operationId = "userDefinedLinksSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    Always, for every property identifier. The response has no body.""")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one user-defined link",
            description = """
                    One user-defined link by database identifier.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "userDefinedLinksGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested user-defined link.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserDefinedLink.class),
                                    examples = @ExampleObject(value = """
                            {
                              "db-id": 5,
                              "owner": "review-seed",
                              "link-id": "udl-13",
                              "link-label": "cross-connect 13",
                              "node-id-a": 13,
                              "component-label-a": "Gi1/1",
                              "node-id-z": 24,
                              "component-label-z": "Gi1/2"
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = UserDefinedLink.class),
                                    examples = @ExampleObject(value = """
                            <user-defined-link>
                              <node-id-a>13</node-id-a>
                              <component-label-a>Gi1/1</component-label-a>
                              <node-id-z>24</node-id-z>
                              <component-label-z>Gi1/2</component-label-z>
                              <link-id>udl-13</link-id>
                              <link-label>cross-connect 13</link-label>
                              <owner>review-seed</owner>
                              <db-id>5</db-id>
                            </user-defined-link>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No link has that identifier, or the identifier is not an integer. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the link, reported as `db-id` in the entity itself.""",
                    required = true, example = "5")
            final Integer id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create a user-defined link",
            description = """
                    Creates a user-defined link and refreshes the topology. The identifier is assigned by the database and returned in the `Location` header.""",
            operationId = "userDefinedLinksCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = """
                    The link was created. The response has no body.""",
                    headers = @Header(name = "Location", description = "URI of the created user-defined link.",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = """
                    The body deserialised to nothing, which a literal JSON `null` does. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            Link cannot be null"""))),
            @ApiResponse(responseCode = "500", description = """
                    The request carried no body at all, or the body left a not-null column unset. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            No content to map to Object due to end of input""")))
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    The link to create. `node-id-a`, `node-id-z`, `link-id` and `owner` are required by the schema; the component labels and `link-label` are optional. In JSON and XML alike the members carry their hyphenated names.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserDefinedLink.class),
                                    examples = @ExampleObject(value = """
                            {
                              "owner": "review-seed",
                              "link-id": "udl-99",
                              "link-label": "cross-connect 99",
                              "node-id-a": 13,
                              "component-label-a": "Gi1/1",
                              "node-id-z": 24,
                              "component-label-z": "Gi1/2"
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = UserDefinedLink.class),
                                    examples = @ExampleObject(value = """
                            <user-defined-link>
                              <node-id-a>13</node-id-a>
                              <node-id-z>24</node-id-z>
                              <link-id>udl-99</link-id>
                              <link-label>cross-connect 99</link-label>
                              <owner>review-seed</owner>
                            </user-defined-link>"""))
                    })
            final UserDefinedLink object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create a user-defined link at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "userDefinedLinksCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "5")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the user-defined links matching a query",
            description = """
                    Sets the same named properties on every link matching the query. At most `limit` entities are affected, so this touches ten entities per call unless a larger `limit` is given.

                    For example, `_s=owner==review-seed`.""",
            operationId = "userDefinedLinksUpdateMany")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = """
                    Every matching link was updated. The response has no body."""),
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response updateMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext,
            @RequestBody(description = DOC_FORM_BODY,
                    content = @Content(mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(value = """
                            link-label=cross-connect+13&component-label-a=Gi1%2F1""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final UserDefinedLink object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one user-defined link",
            description = """
                    Sets named properties on one link and refreshes the topology. Only the form-parameter form is implemented; a JSON or XML body reaches the replacement handler, which answers 501.""",
            operationId = "userDefinedLinksUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = """
                    The named properties were applied, or were all ignored. The response has no body, and an ignored parameter is not reported."""),
            @ApiResponse(responseCode = "404", description = """
                    No link has that identifier. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    Reached only by a JSON or XML body: wholesale replacement is not implemented here. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the link, reported as `db-id` in the entity itself.""",
                    required = true, example = "5")
            final Integer id,
            @RequestBody(description = """
                    Form parameters name the properties to set. A JSON or XML body is accepted by the router but answered with 501.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserDefinedLink.class),
                                    examples = @ExampleObject(value = """
                            {
                              "db-id": 5,
                              "owner": "review-seed",
                              "link-id": "udl-13",
                              "link-label": "cross-connect 13",
                              "node-id-a": 13,
                              "node-id-z": 24
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = UserDefinedLink.class),
                                    examples = @ExampleObject(value = """
                            <user-defined-link>
                              <node-id-a>13</node-id-a>
                              <node-id-z>24</node-id-z>
                              <link-id>udl-13</link-id>
                              <link-label>cross-connect 13</link-label>
                              <owner>review-seed</owner>
                            </user-defined-link>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            link-label=cross-connect+13&component-label-a=Gi1%2F1"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the user-defined links matching a query",
            description = """
                    Deletes every user-defined link matching the query. At most `limit` entities are affected, so this touches ten entities per call unless a larger `limit` is given.

                    For example, `_s=owner==review-seed`.""",
            operationId = "userDefinedLinksDeleteMany")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Every matching user-defined link was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response deleteMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Delete one user-defined link",
            description = """
                    Deletes one user-defined link and refreshes the topology.""",
            operationId = "userDefinedLinksDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The user-defined link was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = """
                    No link has that identifier. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the link, reported as `db-id` in the entity itself.""",
                    required = true, example = "5")
            final Integer id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
