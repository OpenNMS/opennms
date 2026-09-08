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

import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_ID;
import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_NAME;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.OnmsApplicationList;
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
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;

/**
 * Basic Web Service using REST for {@link OnmsApplication} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("applications")
@Transactional
@Tag(name = "Applications", description = "Applications API")
public class ApplicationRestService extends AbstractDaoRestService<OnmsApplication,OnmsApplication,Integer,Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationRestService.class);

    private final ApplicationDao m_dao;

    private final EventProxy m_eventProxy;

    @Autowired
    public ApplicationRestService(final ApplicationDao dao, final EventProxy eventProxy) {
        this.m_dao = dao;
        this.m_eventProxy = eventProxy;
    }

    @Override
    protected ApplicationDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsApplication> getDaoClass() {
        return OnmsApplication.class;
    }

    @Override
    protected Class<OnmsApplication> getQueryBeanClass() {
        return OnmsApplication.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsApplication.class);

        // Order by application name by default
        builder.orderBy("name").asc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsApplication> createListWrapper(Collection<OnmsApplication> list) {
        return new OnmsApplicationList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.APPLICATION_SERVICE_PROPERTIES;
    }

    @Override
    protected OnmsApplication doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsApplication object) {
        final Integer id = getDao().save(object);
        sendEvent(object, EventConstants.APPLICATION_CREATED_EVENT_UEI);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsApplication object) {
        getDao().delete(object);
        sendEvent(object, EventConstants.APPLICATION_DELETED_EVENT_UEI);
    }

    private void sendEvent(final OnmsApplication application, final String uei) {
        final Event event = new EventBuilder(uei, "Web UI")
                .addParam(PARM_APPLICATION_ID, application.getId())
                .addParam(PARM_APPLICATION_NAME, application.getName())
                .getEvent();
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            LOG.warn("Failed to send event {}: {}", event.getUei(), e.getMessage(), e);
        }
    }

    @Override
    @Operation(summary = "List applications",
            description = """
                    Applications matching the query, ordered by name unless `orderBy` says otherwise.

                    The JSON and XML renderings of an application differ: JSON carries `perspectiveLocations` as full monitoring-location objects and omits the member services, while XML carries `monitoredServices/monitoredServiceId` and `perspectiveLocations/perspectiveLocationId` as identifier lists.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=name==Review*` matches every application whose name starts with `Review`.""",
            operationId = "applicationsList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching applications.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsApplicationList.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 4,
                              "count": 1,
                              "offset": 0,
                              "application": [ {
                                "id": 3,
                                "name": "Review Analytics",
                                "perspectiveLocations": [ {
                                  "tags": [],
                                  "latitude": null,
                                  "geolocation": null,
                                  "longitude": null,
                                  "priority": 100,
                                  "location-name": "Default",
                                  "monitoring-area": "localhost"
                                } ]
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsApplicationList.class),
                                    examples = @ExampleObject(value = """
                            <applications xmlns:ns2="http://xmlns.opennms.org/xsd/config/monitoring-locations" count="1" offset="0" totalCount="4">
                              <application id="3">
                                <monitoredServices>
                                  <monitoredServiceId>1021</monitoredServiceId>
                                  <monitoredServiceId>1039</monitoredServiceId>
                                </monitoredServices>
                                <name>Review Analytics</name>
                                <perspectiveLocations>
                                  <perspectiveLocationId>Default</perspectiveLocationId>
                                </perspectiveLocations>
                              </application>
                            </applications>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No application matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count applications",
            description = """
                    Number of applications matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=name==Review*` matches every application whose name starts with `Review`.""",
            operationId = "applicationsCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching applications, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "4"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of applications",
            description = """
                    The properties an application query can filter and sort on.""",
            operationId = "applicationsSearchProperties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The properties this endpoint can search and sort on.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 1,
                              "count": 1,
                              "offset": 0,
                              "searchProperty": [
                                { "id": "name", "name": "Name", "type": "STRING", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="STRING" orderBy="true" iplike="false" id="name" name="Name"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one application property. The `value` entries are typed after the property: numbers for `INTEGER`, `LONG` and `FLOAT`, epoch milliseconds for `TIMESTAMP`, strings otherwise.""",
            operationId = "applicationsSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 4,
                              "count": 4,
                              "offset": 0,
                              "value": [ "Review Analytics", "Review Billing", "Review Messaging", "Review Storefront" ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one application",
            description = """
                    One application by database identifier.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "applicationsGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested application.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsApplication.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": 3,
                              "name": "Review Analytics",
                              "perspectiveLocations": [ {
                                "tags": [],
                                "latitude": null,
                                "geolocation": null,
                                "longitude": null,
                                "priority": 100,
                                "location-name": "Default",
                                "monitoring-area": "localhost"
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsApplication.class),
                                    examples = @ExampleObject(value = """
                            <application xmlns:ns0="http://xmlns.opennms.org/xsd/config/monitoring-locations" id="3">
                              <name>Review Analytics</name>
                            </application>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No application has that identifier, or the identifier is not an integer. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the application.""",
                    required = true, example = "3")
            final Integer id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create an application",
            description = """
                    Creates an application. The identifier is assigned by the database and returned in the `Location` header. An `applicationCreated` event is sent.""",
            operationId = "applicationsCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = """
                    The application was created. The response has no body.""",
                    headers = @Header(name = "Location", description = "URI of the created application.",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = """
                    The name is already taken. The body is a `text/plain` message naming the violated constraint.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            could not execute statement; SQL [n/a]; constraint [applications_name_idx]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement""")))
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    The application to create. Only `name` is required, and it must be unique.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsApplication.class),
                                    examples = @ExampleObject(value = """
                            { "name": "Review Reporting" }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsApplication.class),
                                    examples = @ExampleObject(value = """
                            <application>
                              <name>Review Reporting</name>
                            </application>"""))
                    })
            final OnmsApplication object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create an application at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "applicationsCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "3")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the applications matching a query",
            description = """
                    Not supported for applications: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=name==Review*` matches every application whose name starts with `Review`.""",
            operationId = "applicationsUpdateMany")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search"))),
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response updateMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext,
            @RequestBody(description = DOC_FORM_BODY,
                    content = @Content(mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(value = """
                            name=Review+Reporting""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final OnmsApplication object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one application",
            description = """
                    Not supported for applications. Both the JSON or XML replacement form and the form-parameter form answer 501.""",
            operationId = "applicationsUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    No application has that identifier. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    Applications do not support update. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the application.""",
                    required = true, example = "3")
            final Integer id,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsApplication.class),
                                    examples = @ExampleObject(value = """
                            { "id": 3, "name": "Review Reporting" }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsApplication.class),
                                    examples = @ExampleObject(value = """
                            <application id="3"><name>Review Reporting</name></application>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            name=Review+Reporting"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the applications matching a query",
            description = """
                    Deletes every application matching the query. At most `limit` entities are affected, ten by default. An `applicationDeleted` event is sent for each one.

                    For example, `_s=name==Review*` matches every application whose name starts with `Review`.""",
            operationId = "applicationsDeleteMany")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Every matching application was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response deleteMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Delete one application",
            description = """
                    Deletes one application by identifier. An `applicationDeleted` event is sent.""",
            operationId = "applicationsDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The application was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = """
                    No application has that identifier. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the application.""",
                    required = true, example = "3")
            final Integer id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
