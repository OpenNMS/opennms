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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.IpAddressCriteriaBehavior;
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
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * <p>This end-point exist to retrieve and update a set of IP interfaces,
 * based on a given criteria.</p>
 */
@Component
@Path("ipinterfaces")
@Transactional
@Tag(name = "IpInterfaces", description = "Ip Interfaces API")
public class IpInterfaceRestService extends AbstractDaoRestService<OnmsIpInterface,SearchBean,Integer,String> {

    @Autowired
    private IpInterfaceDao m_dao;

    @Override
    protected IpInterfaceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsIpInterface> getDaoClass() {
        return OnmsIpInterface.class;
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
        builder.alias("snmpInterface", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        return builder;
    }

    @Override
    protected final JaxbListWrapper<OnmsIpInterface> createListWrapper(Collection<OnmsIpInterface> list) {
        return new OnmsIpInterfaceList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.IP_INTERFACE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.IP_INTERFACE_BEHAVIORS);
        // iplike patterns on ipAddress (10.0.*.*) next to literal equality;
        // the restriction names the ipaddr column directly, not the attribute
        map.put("ipAddress", new IpAddressCriteriaBehavior("ipAddr"));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));

        return map;
    }

    @Override
    protected final OnmsIpInterface doGet(UriInfo uriInfo, String ipAddress) {
        final List<OnmsIpInterface> addresses = getDao().findByIpAddress(ipAddress);
        if (addresses.isEmpty()) {
            return null;
        } else if (addresses.size() == 1) {
            final OnmsIpInterface iface = addresses.get(0);
            getDao().initialize(iface.getSnmpInterface());
			return iface;
        }
        throw new WebApplicationException("More than one IP address matches " + ipAddress, Status.BAD_REQUEST);
    }

    @Override
    @Operation(summary = "List IP interfaces",
            description = """
                    IP interfaces matching the query, ordered by database identifier unless `orderBy` says otherwise. The query joins the node, its asset record and location, and the SNMP interface, so properties of those are searchable; `category.*` and `monitoredService.*` terms are not joined here and fail with 500.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=node.label==loopback-001` or `_s=isManaged==M`.""",
            operationId = "ipInterfacesList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching IP interfaces.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsIpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 3688,
                              "count": 1,
                              "offset": 0,
                              "ipInterface": [ {
                                "id": "1",
                                "ipAddress": "127.0.0.4",
                                "hostName": "localhost.",
                                "ifIndex": null,
                                "isManaged": "M",
                                "snmpPrimary": "N",
                                "isDown": false,
                                "monitoredServiceCount": 1,
                                "lastCapsdPoll": 1787145520003,
                                "lastIngressFlow": null,
                                "lastEgressFlow": null,
                                "nodeId": 1
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsIpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                            <ipInterfaces count="1" offset="0" totalCount="3688">
                              <ipInterface isDown="false" id="1" isManaged="M" monitoredServiceCount="1" snmpPrimary="N">
                                <ipAddress>127.0.0.4</ipAddress>
                                <hostName>localhost.</hostName>
                                <lastCapsdPoll>2026-08-19T09:18:40.003-04:00</lastCapsdPoll>
                                <nodeId>1</nodeId>
                              </ipInterface>
                            </ipInterfaces>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No IP interface matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count IP interfaces",
            description = """
                    Number of IP interfaces matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=node.label==loopback-001` or `_s=isManaged==M`.""",
            operationId = "ipInterfacesCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching IP interfaces, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "3688"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of IP interfaces",
            description = """
                    The properties an IP-interface query can filter and sort on, including the joined node, SNMP interface and asset properties.""",
            operationId = "ipInterfacesSearchProperties")
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
                                { "id": "ipHostName", "name": "Hostname", "type": "STRING", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="STRING" orderBy="true" iplike="false" id="ipHostName" name="Hostname"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one IP-interface property. The `value` entries are typed after the property: numbers for `INTEGER`, `LONG` and `FLOAT`, epoch milliseconds for `TIMESTAMP`, strings otherwise.""",
            operationId = "ipInterfacesSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 3,
                              "count": 3,
                              "offset": 0,
                              "value": [ "10.0.10.0", "10.0.10.1", "10.0.10.10" ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one IP interface",
            description = """
                    One IP interface, looked up by IP address rather than by database identifier.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "ipInterfacesGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested IP interface.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": "2",
                              "ipAddress": "127.0.0.1",
                              "hostName": "localhost",
                              "ifIndex": null,
                              "isManaged": "M",
                              "snmpPrimary": "P",
                              "isDown": false,
                              "monitoredServiceCount": 2,
                              "lastCapsdPoll": 1786541086168,
                              "lastIngressFlow": null,
                              "lastEgressFlow": null,
                              "nodeId": 2
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                            <ipInterface isDown="false" id="2" isManaged="M" monitoredServiceCount="2" snmpPrimary="P">
                              <ipAddress>127.0.0.1</ipAddress>
                              <hostName>localhost</hostName>
                              <lastCapsdPoll>2026-08-12T09:24:46.168-04:00</lastCapsdPoll>
                              <nodeId>2</nodeId>
                            </ipInterface>"""))
                    }),
            @ApiResponse(responseCode = "400", description = """
                    More than one node holds that IP address, so the lookup is ambiguous. The response has no body."""),
            @ApiResponse(responseCode = "404", description = """
                    No interface holds that address, or the value is not a parseable address. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    IP address of the interface, not its database identifier. Addresses are not unique across nodes, and an address held by more than one node is answered with 400.""",
                    required = true, example = "127.0.0.1")
            final String id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create an IP interface",
            description = """
                    Answered with 501 for every body.""",
            operationId = "ipInterfacesCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                            <ipInterface/>"""))
                    })
            final OnmsIpInterface object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create an IP interface at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "ipInterfacesCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "127.0.0.1")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the IP interfaces matching a query",
            description = """
                    Not supported here: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=node.label==loopback-001` or `_s=isManaged==M`.""",
            operationId = "ipInterfacesUpdateMany")
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
                            isManaged=M""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final OnmsIpInterface object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one IP interface",
            description = """
                    Both the JSON or XML replacement form and the form-parameter form answer 501.""",
            operationId = "ipInterfacesUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = """
                    More than one node holds that IP address, so the lookup is ambiguous. The response has no body."""),
            @ApiResponse(responseCode = "404", description = """
                    No interface holds that address, or the value is not a parseable address. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    This endpoint does not support update. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    IP address of the interface, not its database identifier. Addresses are not unique across nodes, and an address held by more than one node is answered with 400.""",
                    required = true, example = "127.0.0.1")
            final String id,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                            <ipInterface/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            isManaged=M"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the IP interfaces matching a query",
            description = """
                    Not supported here: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=node.label==loopback-001` or `_s=isManaged==M`.""",
            operationId = "ipInterfacesDeleteMany")
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
    @Operation(summary = "Delete one IP interface",
            description = """
                    Answered with 501 once the address resolves, and 404 when it does not.""",
            operationId = "ipInterfacesDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = """
                    More than one node holds that IP address, so the lookup is ambiguous. The response has no body."""),
            @ApiResponse(responseCode = "404", description = """
                    No interface holds that address, or the value is not a parseable address. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    This endpoint does not support deletion. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    IP address of the interface, not its database identifier. Addresses are not unique across nodes, and an address held by more than one node is answered with 400.""",
                    required = true, example = "127.0.0.1")
            final String id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
