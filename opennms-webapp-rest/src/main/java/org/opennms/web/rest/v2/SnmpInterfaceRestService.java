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
import java.util.stream.Collectors;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
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
 * Basic Web Service using REST for {@link OnmsSnmpInterface} entity.
 *
 * <p>This end-point exist to retrieve and update a set of SNMP interfaces,
 * based on a given criteria.</p>
 */
@Component
@Path("snmpinterfaces")
@Transactional
@Tag(name = "SnmpInterfaces", description = "SNMP Interfaces API")
public class SnmpInterfaceRestService extends AbstractDaoRestService<OnmsSnmpInterface,SearchBean,Integer,String> {

    @Autowired
    private SnmpInterfaceDao m_dao;

    @Override
    protected SnmpInterfaceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsSnmpInterface> getDaoClass() {
        return OnmsSnmpInterface.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        // 1st level JOINs
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        return builder;
    }

    @Override
    protected final JaxbListWrapper<OnmsSnmpInterface> createListWrapper(Collection<OnmsSnmpInterface> list) {
        return new OnmsSnmpInterfaceList(list.stream().distinct().collect(Collectors.toList()));
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.SNMP_INTERFACE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS);

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));

        return map;
    }

    @Override
    protected final OnmsSnmpInterface doGet(final UriInfo uriInfo, final String id) {
        return getDao().get(Integer.valueOf(id));
    }

    @Override
    @Operation(summary = "List SNMP interfaces",
            description = """
                    SNMP interfaces matching the query. The query joins the node, its asset record, location and categories, and the IP interfaces, so properties of all of those are searchable. Duplicate rows produced by those joins are collapsed before the page is built, which can make the returned `count` smaller than the requested `limit`.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=ifDescr==enp*` or `_s=node.label==loopback-001`.""",
            operationId = "snmpInterfacesList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching SNMP interfaces.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsSnmpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 4659,
                              "count": 1,
                              "offset": 0,
                              "snmpInterface": [ {
                                "id": 1276,
                                "ifIndex": 2,
                                "ifDescr": "enp113s0",
                                "ifName": "enp113s0",
                                "ifAlias": "",
                                "ifType": 6,
                                "ifSpeed": 0,
                                "ifAdminStatus": 1,
                                "ifOperStatus": 2,
                                "physAddr": "1c697ad4f9d8",
                                "collectFlag": "N",
                                "collect": false,
                                "collectionPolicySpecified": false,
                                "collectionUserSpecified": false,
                                "pollFlag": "N",
                                "poll": false,
                                "hasFlows": false,
                                "hasIngressFlows": false,
                                "hasEgressFlows": false,
                                "lastIngressFlow": null,
                                "lastEgressFlow": null,
                                "lastCapsdPoll": 1786541086168,
                                "lastSnmpPoll": null,
                                "nodeId": 2
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsSnmpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                            <snmpInterfaces count="1" offset="0" totalCount="4659">
                              <snmpInterface collectFlag="N" collect="false" id="1276" ifIndex="2" pollFlag="N" poll="false">
                                <ifAdminStatus>1</ifAdminStatus>
                                <ifAlias></ifAlias>
                                <ifDescr>enp113s0</ifDescr>
                                <ifName>enp113s0</ifName>
                                <ifOperStatus>2</ifOperStatus>
                                <ifSpeed>0</ifSpeed>
                                <ifType>6</ifType>
                                <lastCapsdPoll>2026-08-12T09:24:46.168-04:00</lastCapsdPoll>
                                <nodeId>2</nodeId>
                                <physAddr>1c697ad4f9d8</physAddr>
                              </snmpInterface>
                            </snmpInterfaces>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No SNMP interface matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count SNMP interfaces",
            description = """
                    Number of SNMP interfaces matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=ifDescr==enp*` or `_s=node.label==loopback-001`.""",
            operationId = "snmpInterfacesCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching SNMP interfaces, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "4659"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of SNMP interfaces",
            description = """
                    The properties an SNMP-interface query can filter and sort on, including the joined node, IP interface and asset properties.""",
            operationId = "snmpInterfacesSearchProperties")
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
                                { "id": "ifName", "name": "Interface Name", "type": "STRING", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="STRING" orderBy="true" iplike="false" id="ifName" name="Interface Name"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one SNMP-interface property. The `value` entries are typed after the property: numbers for `INTEGER`, `LONG` and `FLOAT`, epoch milliseconds for `TIMESTAMP`, strings otherwise.""",
            operationId = "snmpInterfacesSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 3,
                              "count": 3,
                              "offset": 0,
                              "value": [ 1, 2, 3 ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one SNMP interface",
            description = """
                    One SNMP interface by database identifier.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "snmpInterfacesGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested SNMP interface.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": 1276,
                              "ifIndex": 2,
                              "ifDescr": "enp113s0",
                              "ifName": "enp113s0",
                              "ifAlias": "",
                              "ifType": 6,
                              "ifSpeed": 0,
                              "ifAdminStatus": 1,
                              "ifOperStatus": 2,
                              "physAddr": "1c697ad4f9d8",
                              "collectFlag": "N",
                              "collect": false,
                              "pollFlag": "N",
                              "poll": false,
                              "lastCapsdPoll": 1786541086168,
                              "lastSnmpPoll": null,
                              "nodeId": 2
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                            <snmpInterface collectFlag="N" collect="false" id="1276" ifIndex="2" pollFlag="N" poll="false">
                              <ifDescr>enp113s0</ifDescr>
                              <ifName>enp113s0</ifName>
                              <ifSpeed>0</ifSpeed>
                              <ifType>6</ifType>
                              <lastCapsdPoll>2026-08-12T09:24:46.168-04:00</lastCapsdPoll>
                              <nodeId>2</nodeId>
                              <physAddr>1c697ad4f9d8</physAddr>
                            </snmpInterface>"""))
                    }),
            @ApiResponse(responseCode = "500", description = """
                    The identifier is not an integer. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"abc\""))),
            @ApiResponse(responseCode = "404", description = """
                    The identifier parsed as an integer but no SNMP interface has it. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the SNMP interface. It is read as a string and then parsed as an integer, so a non-numeric value fails with 500 rather than 404.""",
                    required = true, example = "1276")
            final String id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create an SNMP interface",
            description = """
                    Answered with 501 for every body.""",
            operationId = "snmpInterfacesCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                            <snmpInterface/>"""))
                    })
            final OnmsSnmpInterface object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create an SNMP interface at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "snmpInterfacesCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "1276")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the SNMP interfaces matching a query",
            description = """
                    Not supported here: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=ifDescr==enp*` or `_s=node.label==loopback-001`.""",
            operationId = "snmpInterfacesUpdateMany")
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
                            collect=C""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final OnmsSnmpInterface object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one SNMP interface",
            description = """
                    Both the JSON or XML replacement form and the form-parameter form answer 501.""",
            operationId = "snmpInterfacesUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    No SNMP interface has that identifier. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    This endpoint does not support update. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the SNMP interface. It is read as a string and then parsed as an integer, so a non-numeric value fails with 500 rather than 404.""",
                    required = true, example = "1276")
            final String id,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                            <snmpInterface/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            collect=C"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the SNMP interfaces matching a query",
            description = """
                    Not supported here: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=ifDescr==enp*` or `_s=node.label==loopback-001`.""",
            operationId = "snmpInterfacesDeleteMany")
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
    @Operation(summary = "Delete one SNMP interface",
            description = """
                    Answered with 501 once the identifier resolves, and 404 when it does not.""",
            operationId = "snmpInterfacesDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    No SNMP interface has that identifier. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    This endpoint does not support deletion. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the SNMP interface. It is read as a string and then parsed as an integer, so a non-numeric value fails with 500 rather than 404.""",
                    required = true, example = "1276")
            final String id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
