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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.svclayer.api.RequisitionAccessService;
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
 * Basic Web Service using REST for {@link OnmsMinion} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("minions")
@Transactional
@Tag(name = "Minion", description = "Minion API")
public class MinionRestService extends AbstractDaoRestService<OnmsMinion,OnmsMinion,String,String> {

    private static final Logger LOG = LoggerFactory.getLogger(MinionRestService.class);
    private static final String PROVISIONING_FOREIGN_SOURCE_PATTERN = System.getProperty("opennms.minion.provisioning.foreignSourcePattern", "Minions");

    @Autowired
    private MinionDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Autowired
    private RequisitionAccessService m_requisitionAccessService;

    @Override
    protected MinionDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMinion> getDaoClass() {
        return OnmsMinion.class;
    }

    @Override
    protected Class<OnmsMinion> getQueryBeanClass() {
        return OnmsMinion.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMinion.class);

        // Order by label by default
        builder.orderBy("label").desc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMinion> createListWrapper(Collection<OnmsMinion> list) {
        return new OnmsMinionCollection(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.MINION_SERVICE_PROPERTIES;
    }

    @Override
    protected Response doUpdate(SecurityContext securityContext, UriInfo uriInfo, String key, OnmsMinion targetObject) {
        if (!key.equals(targetObject.getId())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getId(), key);
        }
        getDao().saveOrUpdate(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMinion minion) {
        final String location = minion.getLocation();
        final String id = minion.getId();
        getDao().delete(minion);

        final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_DELETED_UEI, "ReST");
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, id);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, location);
        try {
            m_eventProxy.send(eventBuilder.getEvent());
        } catch (final EventProxyException e) {
            LOG.warn("Failed to send Event on Minion deletion " + e.getMessage(), e);
        }

        /*
        In the heartbeat code a minion is automatically added to a requisition for monitoring. The requisition's name
        to be used is defined by a system property and the minion's location. So, we will also delete the minion from
        it's requisition here...
         */

        final String foreignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, minion.getLocation());
        m_requisitionAccessService.deleteNode(foreignSource, minion.getId());
    }

    @Override
    protected OnmsMinion doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    @Override
    @Operation(summary = "List Minions",
            description = """
                    Minions matching the query, by descending label unless `orderBy` says otherwise. Minions register themselves through heartbeats, so an instance with no Minion deployed answers 204 here.

                    The `date` member is the last heartbeat: epoch milliseconds in JSON, ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=location==Default`.""",
            operationId = "minionsList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching Minions.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMinionCollection.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 1,
                              "count": 1,
                              "offset": 0,
                              "minion": [ {
                                "id": "00000000-0000-0000-0000-000000ddba11",
                                "label": "minion-01",
                                "location": "Default",
                                "type": "Minion",
                                "status": "Started",
                                "version": "36.0.4-SNAPSHOT",
                                "date": 1787727804037,
                                "lastCheckedIn": null,
                                "properties": { }
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMinionCollection.class),
                                    examples = @ExampleObject(value = """
                            <minions count="1" offset="0" totalCount="1">
                              <minion id="00000000-0000-0000-0000-000000ddba11" label="minion-01" location="Default" type="Minion" date="2026-08-26T03:03:24.037-04:00" status="Started" version="36.0.4-SNAPSHOT">
                                <properties/>
                              </minion>
                            </minions>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No Minion matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count Minions",
            description = """
                    Number of Minions matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=location==Default`.""",
            operationId = "minionsCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching Minions, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "0"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of Minions",
            description = """
                    The properties a Minion query can filter and sort on.""",
            operationId = "minionsSearchProperties")
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
                                { "id": "label", "name": "Label", "type": "STRING", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="STRING" orderBy="true" iplike="false" id="label" name="Label"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one Minion property. With no Minion registered the `value` array is empty and `totalCount` and `count` are `null`.""",
            operationId = "minionsSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": null,
                              "count": null,
                              "offset": 0,
                              "value": [ ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one Minion",
            description = """
                    One Minion by identifier.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "minionsGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested Minion.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": "00000000-0000-0000-0000-000000ddba11",
                              "label": "minion-01",
                              "location": "Default",
                              "type": "Minion",
                              "status": "Started",
                              "version": "36.0.4-SNAPSHOT",
                              "date": 1787727804037,
                              "lastCheckedIn": null,
                              "properties": { }
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                            <minion status="Started" version="36.0.4-SNAPSHOT" id="00000000-0000-0000-0000-000000ddba11" label="minion-01" location="Default" type="Minion" date="2026-08-26T03:03:24.037-04:00">
                              <properties/>
                            </minion>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No Minion has that identifier. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Identifier the Minion reports for itself, which is also its primary key.""",
                    required = true, example = "00000000-0000-0000-0000-000000ddba11")
            final String id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create a Minion",
            description = """
                    Not supported: Minions register themselves by heartbeat. The endpoint answers 501 for every body.""",
            operationId = "minionsCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                            <minion/>"""))
                    })
            final OnmsMinion object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create a Minion at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "minionsCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "00000000-0000-0000-0000-000000ddba11")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the Minions matching a query",
            description = """
                    Not supported for Minions: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=location==Default`.""",
            operationId = "minionsUpdateMany")
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
                            label=minion-01""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final String id,
            final OnmsMinion object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one Minion",
            description = """
                    Replaces a Minion record. With a JSON or XML body the stored row is replaced wholesale, so members left out of the body are cleared, and the `id` in the body has to equal the identifier in the path. The form-parameter form is not implemented and answers 501.

                    Heartbeats overwrite these fields again, so an edit made here lasts only until the Minion next checks in.""",
            operationId = "minionsUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = """
                    The Minion record was replaced. The response has no body."""),
            @ApiResponse(responseCode = "400", description = """
                    The `id` in the body does not match the identifier in the path. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            The ID of the object doesn't match the ID of the path: Other != ApiDocMinion"""))),
            @ApiResponse(responseCode = "404", description = """
                    No Minion has that identifier, or the body was empty. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    Reached only by a form-encoded body: updating named properties is not implemented here. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Identifier the Minion reports for itself, which is also its primary key.""",
                    required = true, example = "00000000-0000-0000-0000-000000ddba11")
            final String id,
            @RequestBody(description = """
                    JSON or XML replaces the whole Minion record. A form-encoded body is accepted by the router but answered with 501.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": "00000000-0000-0000-0000-000000ddba11",
                              "label": "minion-01",
                              "location": "Default",
                              "type": "Minion",
                              "status": "Stopped",
                              "version": "36.0.4-SNAPSHOT"
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMinion.class),
                                    examples = @ExampleObject(value = """
                            <minion id="00000000-0000-0000-0000-000000ddba11" label="minion-01" location="Default" type="Minion" status="Stopped"/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            label=minion-01"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the Minions matching a query",
            description = """
                    Deletes every Minion matching the query. At most `limit` entities are affected, so this touches ten entities per call unless a larger `limit` is given.

                    For example, `_s=location==Default`.""",
            operationId = "minionsDeleteMany")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Every matching Minion was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response deleteMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Delete one Minion",
            description = """
                    Deletes one Minion record. A `monitoringSystemDeleted` event is sent, and the Minion's node is removed from the requisition named by `opennms.minion.provisioning.foreignSourcePattern` (`Minions` by default).""",
            operationId = "minionsDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The Minion was deleted. The response has no body."),
            @ApiResponse(responseCode = "404", description = """
                    No Minion has that identifier. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Identifier the Minion reports for itself, which is also its primary key.""",
                    required = true, example = "00000000-0000-0000-0000-000000ddba11")
            final String id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
