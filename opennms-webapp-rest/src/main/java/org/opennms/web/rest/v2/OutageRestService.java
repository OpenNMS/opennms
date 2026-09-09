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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;

/**
 * Basic Web Service using REST for {@link OnmsOutage} entity.
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("outages")
@Transactional
@Tag(name = "Outages", description = "Outages API")
public class OutageRestService extends AbstractDaoRestService<OnmsOutage,SearchBean,Integer,Integer> {

    @Autowired
    private OutageDao m_dao;

    @Override
    protected OutageDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsOutage> getDaoClass() {
        return OnmsOutage.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class, Aliases.outage.toString());
        // 1st level JOINs
        builder.alias("monitoredService", "monitoredService", JoinType.LEFT_JOIN);
        builder.alias("serviceLostEvent", "serviceLostEvent", JoinType.LEFT_JOIN);
        builder.alias("serviceRegainedEvent", "serviceRegainedEvent", JoinType.LEFT_JOIN);
        builder.alias("perspective", "perspective", JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias("monitoredService.ipInterface", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("monitoredService.serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceLostEvent.distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        builder.alias(Aliases.ipInterface.prop("node"), Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.ipInterface.prop("snmpInterface"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 4th level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        //builder.alias(Aliases.node.prop("categories"), Aliases.category.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        // NOTE: Left joins on a toMany relationship need a join condition so that only one row is returned

        // Order by ID by default
        builder.orderBy("id").desc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsOutage> createListWrapper(Collection<OnmsOutage> list) {
        return new OnmsOutageCollection(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.OUTAGE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.OUTAGE_BEHAVIORS);
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.outage, CriteriaBehaviors.OUTAGE_BEHAVIORS));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.monitoredService, CriteriaBehaviors.MONITORED_SERVICE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("serviceLostEvent", CriteriaBehaviors.EVENT_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("serviceRegainedEvent", CriteriaBehaviors.EVENT_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("perspective", CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.distPoller, CriteriaBehaviors.DIST_POLLER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 3rd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 4th level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        //map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));

        return map;
    }

    @Override
    protected OnmsOutage doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    @Operation(summary = "List outages",
            description = """
                    Outages matching the query, newest identifier first unless `orderBy` says otherwise. The query joins the monitored service, its IP interface, node, SNMP interface, asset record and location, the lost and regained service events and the perspective location, so properties of all of those are searchable.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=ifRegainedService=gt=2026-08-01T00:00:00.000-0400;node.label==loopback-009`.""",
            operationId = "outagesList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching outages.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsOutageCollection.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 4077,
                              "count": 1,
                              "offset": 0,
                              "outage": [ {
                                "id": 900009,
                                "nodeId": 7,
                                "nodeLabel": "loopback-009",
                                "foreignSource": "loopback-lab",
                                "foreignId": "lb-009",
                                "locationName": "Default",
                                "ipAddress": "127.0.0.9",
                                "serviceId": 2,
                                "monitoredService": {
                                  "id": 1025,
                                  "status": "A",
                                  "statusLong": "Managed",
                                  "down": false,
                                  "lastGood": 1787727479370,
                                  "lastFail": 1787685424755,
                                  "serviceType": { "id": 2, "name": "HTTP-8080" },
                                  "ipInterfaceId": 17
                                },
                                "ifLostService": 1787052190798,
                                "ifRegainedService": 1787073778565,
                                "suppressTime": null,
                                "suppressedBy": null,
                                "perspective": "Default"
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsOutageCollection.class),
                                    examples = @ExampleObject(value = """
                            <outages count="1" offset="0" totalCount="4077">
                              <outage id="900009">
                                <foreignId>lb-009</foreignId>
                                <foreignSource>loopback-lab</foreignSource>
                                <ifLostService>2026-08-18T07:23:10.798-04:00</ifLostService>
                                <ifRegainedService>2026-08-18T13:22:58.565-04:00</ifRegainedService>
                                <ipAddress>127.0.0.9</ipAddress>
                                <locationName>Default</locationName>
                                <monitoredService down="false" status="A" statusLong="Managed" id="1025">
                                  <ipInterfaceId>17</ipInterfaceId>
                                  <serviceType id="2"><name>HTTP-8080</name></serviceType>
                                </monitoredService>
                                <nodeId>7</nodeId>
                                <nodeLabel>loopback-009</nodeLabel>
                                <perspective>Default</perspective>
                              </outage>
                            </outages>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No outage matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count outages",
            description = """
                    Number of outages matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=ifRegainedService=gt=2026-08-01T00:00:00.000-0400;node.label==loopback-009`.""",
            operationId = "outagesCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching outages, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "4077"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of outages",
            description = """
                    The properties an outage query can filter and sort on, including the joined node, interface, service, event and asset properties.""",
            operationId = "outagesSearchProperties")
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
                                { "id": "ifLostService", "name": "Lost Service Time", "type": "TIMESTAMP", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="TIMESTAMP" orderBy="true" iplike="false" id="ifLostService" name="Lost Service Time"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one outage property. The `value` entries are typed after the property: numbers for `INTEGER`, `LONG` and `FLOAT`, epoch milliseconds for `TIMESTAMP`, strings otherwise.""",
            operationId = "outagesSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 2,
                              "count": 2,
                              "offset": 0,
                              "value": [ 1786382635102, 1786382635105 ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one outage",
            description = """
                    One outage by database identifier.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "outagesGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested outage.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsOutage.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": 900009,
                              "nodeId": 7,
                              "nodeLabel": "loopback-009",
                              "locationName": "Default",
                              "ipAddress": "127.0.0.9",
                              "serviceId": 2,
                              "ifLostService": 1787052190798,
                              "ifRegainedService": 1787073778565,
                              "suppressTime": null,
                              "suppressedBy": null,
                              "perspective": "Default"
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsOutage.class),
                                    examples = @ExampleObject(value = """
                            <outage id="900009">
                              <ifLostService>2026-08-18T07:23:10.798-04:00</ifLostService>
                              <ifRegainedService>2026-08-18T13:22:58.565-04:00</ifRegainedService>
                              <ipAddress>127.0.0.9</ipAddress>
                              <locationName>Default</locationName>
                              <nodeId>7</nodeId>
                              <nodeLabel>loopback-009</nodeLabel>
                              <perspective>Default</perspective>
                            </outage>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No outage has that identifier, or the identifier is not an integer. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the outage.""",
                    required = true, example = "900009")
            final Integer id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create an outage",
            description = """
                    Answered with 501 for every body.""",
            operationId = "outagesCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsOutage.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsOutage.class),
                                    examples = @ExampleObject(value = """
                            <outage/>"""))
                    })
            final OnmsOutage object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create an outage at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "outagesCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "900009")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the outages matching a query",
            description = """
                    Not supported for outages: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=ifRegainedService=gt=2026-08-01T00:00:00.000-0400;node.label==loopback-009`.""",
            operationId = "outagesUpdateMany")
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
                            suppressedBy=admin""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final OnmsOutage object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one outage",
            description = """
                    Both the JSON or XML replacement form and the form-parameter form answer 501.""",
            operationId = "outagesUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    The form variant answers 404 when nothing matches the identifier; the JSON or XML
                    variant answers 404 only for an absent body and does not look the identifier up."""),
            @ApiResponse(responseCode = "501", description = """
                    Outages do not support update. Returned by the JSON or XML variant whether or not the
                    identifier exists. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the outage.""",
                    required = true, example = "900009")
            final Integer id,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsOutage.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsOutage.class),
                                    examples = @ExampleObject(value = """
                            <outage/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            suppressedBy=admin"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the outages matching a query",
            description = """
                    Not supported for outages: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=ifRegainedService=gt=2026-08-01T00:00:00.000-0400;node.label==loopback-009`.""",
            operationId = "outagesDeleteMany")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = DOC_NO_MATCH),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search"))),
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response deleteMany(final SecurityContext securityContext, final UriInfo uriInfo, final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Delete one outage",
            description = """
                    Answered with 501 once the identifier resolves, and 404 when it does not.""",
            operationId = "outagesDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    No outage has that identifier. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    Outages do not support deletion. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the outage.""",
                    required = true, example = "900009")
            final Integer id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
