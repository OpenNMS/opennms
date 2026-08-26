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
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * Basic Web Service using REST for {@link OnmsMonitoringLocation} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("monitoringLocations")
@Transactional
@Tag(name = "MonitoringLocations", description = "Monitoring Locations API")
public class MonitoringLocationRestService extends AbstractDaoRestService<OnmsMonitoringLocation,OnmsMonitoringLocation,String,String> {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationRestService.class);

    @Autowired
    private MonitoringLocationDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    protected MonitoringLocationDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoringLocation> getDaoClass() {
        return OnmsMonitoringLocation.class;
    }

    @Override
    protected Class<OnmsMonitoringLocation> getQueryBeanClass() {
        return OnmsMonitoringLocation.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoringLocation.class);

        // Order by location name by default
        builder.orderBy("locationName").asc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMonitoringLocation> createListWrapper(Collection<OnmsMonitoringLocation> list) {
        return new OnmsMonitoringLocationDefinitionList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.LOCATION_SERVICE_PROPERTIES;
    }

    @Override
    protected OnmsMonitoringLocation doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsMonitoringLocation location) {

        final String id = getDao().save(location);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected Response doUpdate(final SecurityContext securityContext, final UriInfo uriInfo, final String key, final OnmsMonitoringLocation targetObject) {

        if (!key.equals(targetObject.getLocationName())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getLocationName(), key);
        }

        m_dao.clear();

        getDao().saveOrUpdate(targetObject);

        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoringLocation location) {
        getDao().delete(location);
    }

    @Override
    @Operation(summary = "List monitoring locations",
            description = """
                    Monitoring locations matching the query, ordered by name unless `orderBy` says otherwise. In JSON the name and area are the members `location-name` and `monitoring-area`; in XML they are attributes of the same names.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=monitoringArea==localhost`. `_s=id==...` does not parse here; the key property is `locationName`.""",
            operationId = "monitoringLocationsList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching monitoring locations.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMonitoringLocationDefinitionList.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 1,
                              "count": 1,
                              "offset": 0,
                              "location": [ {
                                "location-name": "Default",
                                "monitoring-area": "localhost",
                                "priority": 100,
                                "latitude": null,
                                "longitude": null,
                                "geolocation": null,
                                "tags": []
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMonitoringLocationDefinitionList.class),
                                    examples = @ExampleObject(value = """
                            <locations xmlns:ns2="http://xmlns.opennms.org/xsd/config/monitoring-locations" count="1" offset="0" totalCount="1">
                              <location location-name="Default" monitoring-area="localhost" priority="100">
                                <ns2:tags/>
                              </location>
                            </locations>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No monitoring location matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count monitoring locations",
            description = """
                    Number of monitoring locations matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=monitoringArea==localhost`. `_s=id==...` does not parse here; the key property is `locationName`.""",
            operationId = "monitoringLocationsCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching monitoring locations, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "1"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of monitoring locations",
            description = """
                    The properties a monitoring-location query can filter and sort on.""",
            operationId = "monitoringLocationsSearchProperties")
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
                                { "id": "latitude", "name": "Latitude", "type": "FLOAT", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="FLOAT" orderBy="true" iplike="false" id="latitude" name="Latitude"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one monitoring-location property.""",
            operationId = "monitoringLocationsSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 1,
                              "count": 1,
                              "offset": 0,
                              "value": [ "Default" ]
                            }"""))),
            @ApiResponse(responseCode = "500", description = """
                    The property is declared as an integer but read back as a bigint, which is the case for `priority`. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            class java.lang.Long cannot be cast to class java.lang.Integer (java.lang.Long and java.lang.Integer are in module java.base of loader 'bootstrap') (through reference chain: org.opennms.web.rest.support.IntegerCollection["value"]->java.util.ArrayList[0])"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one monitoring location",
            description = """
                    One monitoring location by name.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "monitoringLocationsGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested monitoring location.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMonitoringLocation.class),
                                    examples = @ExampleObject(value = """
                            {
                              "location-name": "Default",
                              "monitoring-area": "localhost",
                              "priority": 100,
                              "latitude": null,
                              "longitude": null,
                              "geolocation": null,
                              "tags": []
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMonitoringLocation.class),
                                    examples = @ExampleObject(value = """
                            <location xmlns="http://xmlns.opennms.org/xsd/config/monitoring-locations" location-name="Default" monitoring-area="localhost" priority="100">
                              <tags/>
                            </location>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No monitoring location has that name. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Name of the monitoring location, which is also its primary key.""",
                    required = true, example = "Default")
            final String id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create a monitoring location",
            description = """
                    Creates a monitoring location. The name in the body becomes the primary key and is returned in the `Location` header.""",
            operationId = "monitoringLocationsCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = """
                    The monitoring location was created. The response has no body.""",
                    headers = @Header(name = "Location", description = "URI of the created monitoring location.",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = """
                    A monitoring location with that name already exists. The body is a `text/plain` message naming the violated constraint.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            could not execute statement; SQL [n/a]; constraint [monitoringlocations_pkey]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement""")))
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    The monitoring location to create. `location-name` and `monitoring-area` are required; omitted `priority` defaults to 100.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMonitoringLocation.class),
                                    examples = @ExampleObject(value = """
                            {
                              "location-name": "Raleigh",
                              "monitoring-area": "raleigh",
                              "priority": 100
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMonitoringLocation.class),
                                    examples = @ExampleObject(value = """
                            <location location-name="Raleigh" monitoring-area="raleigh" priority="100"/>"""))
                    })
            final OnmsMonitoringLocation object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create a monitoring location at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "monitoringLocationsCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "Default")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the monitoring locations matching a query",
            description = """
                    Not supported for monitoring locations: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=monitoringArea==localhost`. `_s=id==...` does not parse here; the key property is `locationName`.""",
            operationId = "monitoringLocationsUpdateMany")
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
                            monitoring-area=localhost""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final String id,
            final OnmsMonitoringLocation object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one monitoring location",
            description = """
                    Replaces a monitoring location. With a JSON or XML body the stored row is replaced wholesale, so members left out of the body revert to their defaults, and the name in the body has to equal the name in the path. The form-parameter form is not implemented and answers 501.""",
            operationId = "monitoringLocationsUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = """
                    The monitoring location was replaced. The response has no body."""),
            @ApiResponse(responseCode = "400", description = """
                    The name in the body does not match the name in the path. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            The ID of the object doesn't match the ID of the path: Other != Default"""))),
            @ApiResponse(responseCode = "404", description = """
                    No monitoring location has that name, or the body was empty. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    Reached only by a form-encoded body: updating named properties is not implemented here. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Name of the monitoring location, which is also its primary key.""",
                    required = true, example = "Default")
            final String id,
            @RequestBody(description = """
                    JSON or XML replaces the whole monitoring location. A form-encoded body is accepted by the router but answered with 501.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMonitoringLocation.class),
                                    examples = @ExampleObject(value = """
                            {
                              "location-name": "Default",
                              "monitoring-area": "localhost",
                              "priority": 100,
                              "latitude": 35.8,
                              "longitude": -78.6
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMonitoringLocation.class),
                                    examples = @ExampleObject(value = """
                            <location location-name="Default" monitoring-area="localhost" priority="100"/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            monitoring-area=localhost"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the monitoring locations matching a query",
            description = """
                    Deletes every monitoring location matching the query. At most `limit` entities are affected, so this touches ten entities per call unless a larger `limit` is given.

                    For example, `_s=monitoringArea==localhost`. `_s=id==...` does not parse here; the key property is `locationName`.""",
            operationId = "monitoringLocationsDeleteMany")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Every matching monitoring location was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response deleteMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Delete one monitoring location",
            description = """
                    Deletes one monitoring location by name.""",
            operationId = "monitoringLocationsDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The monitoring location was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = """
                    No monitoring location has that name. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Name of the monitoring location, which is also its primary key.""",
                    required = true, example = "Default")
            final String id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
