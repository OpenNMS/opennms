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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.model.v2.MonitoredServiceCollectionDTO;
import org.opennms.web.rest.model.v2.MonitoredServiceDTO;
import org.opennms.web.rest.model.v2.ServiceTypeDTO;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;

/**
 * Basic Web Service using REST for {@link OnmsMonitoredService} entity.
 * 
 * <p>This end-point exist to retrieve and update a set of monitored services at once,
 * based on a given criteria.</p>
 * <p>This facilitates moving services to maintenance mode (and restore the services to be online).</p>
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("ifservices")
@Transactional
@Tag(name = "IfServices", description = "Monitored Services API")
public class IfServiceRestService extends AbstractDaoRestServiceWithDTO<OnmsMonitoredService,MonitoredServiceDTO,SearchBean,Integer,String> {
    @Autowired
    private MonitoredServiceDao m_dao;

    @Autowired
    private MonitoredServicesComponent m_component;

    @Override
    protected MonitoredServiceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoredService> getDaoClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        // 1st level JOINs
        builder.alias("ipInterface", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.ipInterface.prop("node"), Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.ipInterface.prop("snmpInterface"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        // TODO: Only add this alias when filtering so that we can specify a join condition
        //builder.alias("node.categories", Aliases.category.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        return builder;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.IF_SERVICE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.MONITORED_SERVICE_BEHAVIORS);

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 3rd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        //map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));

        return map;
    }

    @Override
    protected JaxbListWrapper<MonitoredServiceDTO> createListWrapper(Collection<MonitoredServiceDTO> list) {
        return new MonitoredServiceCollectionDTO(list);
    }

    @Override
    public MonitoredServiceDTO mapEntityToDTO(OnmsMonitoredService entity) {
        final var dto = new MonitoredServiceDTO();
        dto.setId(entity.getId());
        dto.setDown(entity.isDown());
        dto.setNotify(entity.getNotify());
        dto.setStatus(entity.getStatus());
        dto.setSource(entity.getSource());

        final var serviceType = new ServiceTypeDTO();
        serviceType.setId(entity.getServiceId());
        serviceType.setName(entity.getServiceName());
        dto.setServiceType(serviceType);

        dto.setQualifier(entity.getQualifier());
        dto.setLastFail(entity.getLastFail());
        dto.setLastGood(entity.getLastGood());
        dto.setStatusLong(entity.getStatusLong());
        dto.setIpInterfaceId(entity.getIpInterfaceId());
        dto.setIpAddress(entity.getIpAddress().getHostAddress());
        dto.setNodeId(entity.getNodeId());
        dto.setNodeLabel(entity.getNodeLabel());

        return dto;
    }

    @Override
    public OnmsMonitoredService mapDTOToEntity(MonitoredServiceDTO dto) {
        // currently unused, providing a basic but incomplete mapping of some top-level items
        final var service = new OnmsMonitoredService();
        service.setId(dto.getId());
        service.setNotify(dto.getNotify());
        service.setStatus(dto.getStatus());
        service.setSource(dto.getSource());

        final var serviceType = new OnmsServiceType();
        serviceType.setId(dto.getServiceType().getId());
        serviceType.setName(dto.getServiceType().getName());
        service.setServiceType(serviceType);

        service.setQualifier(dto.getQualifier());
        service.setLastFail(dto.getLastFail());
        service.setLastGood(dto.getLastGood());

        return service;
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService targetObject, MultivaluedMapImpl params) {
        final String previousStatus = targetObject.getStatus();
        final Set<OnmsApplication> applicationsOriginal = new HashSet<>(); // unfortunately applications set is not immutable, let's make a copy.

        if (targetObject.getApplications() != null) {
            applicationsOriginal.addAll(targetObject.getApplications());
        }

        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);

        Set<OnmsApplication> changedApplications = Sets.symmetricDifference(applicationsOriginal, targetObject.getApplications());
        ApplicationEventUtil.getApplicationChangedEvents(changedApplications).forEach(this::sendEvent);

        boolean changed = m_component.hasStatusChanged(previousStatus, targetObject);
        return changed ? Response.noContent().build() : Response.notModified().build();
    }

    @Override
    protected OnmsMonitoredService doGet(UriInfo uriInfo, String serviceName) {
        throw new WebApplicationException(Response.status(Status.NOT_IMPLEMENTED).build());
    }

    @Override
    @Operation(summary = "List monitored services",
            description = """
                    Monitored services matching the query, by ascending identifier unless `orderBy` says otherwise. The query joins the IP interface, the service type, the node and its asset record and location, and the SNMP interface, so properties of all of those are searchable.

                    The returned entity is a projection of the monitored service rather than the stored row: it carries the service type, status, last-good and last-fail times and the owning interface and node, and omits the applications and metadata.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=status==A` or `_s=node.label==loopback-001`.""",
            operationId = "ifServicesList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching monitored services.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = MonitoredServiceCollectionDTO.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 262,
                              "count": 1,
                              "offset": 0,
                              "service": [ {
                                "id": 1017,
                                "status": "A",
                                "statusLong": "Managed",
                                "down": false,
                                "serviceType": { "id": 2, "name": "HTTP-8080" },
                                "lastGood": 1787727384331,
                                "lastFail": 1787685424834,
                                "ipInterfaceId": 2,
                                "ipAddress": "127.0.0.1",
                                "nodeId": 2,
                                "nodeLabel": "loopback-001"
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = MonitoredServiceCollectionDTO.class),
                                    examples = @ExampleObject(value = """
                            <services count="1" offset="0" totalCount="262">
                              <service id="1017">
                                <down>false</down>
                                <status>A</status>
                                <serviceType id="2"><name>HTTP-8080</name></serviceType>
                                <lastFail>2026-08-25T15:17:04.834-04:00</lastFail>
                                <lastGood>2026-08-26T03:11:24.562-04:00</lastGood>
                                <statusLong>Managed</statusLong>
                                <ipInterfaceId>2</ipInterfaceId>
                                <ipAddress>127.0.0.1</ipAddress>
                                <nodeId>2</nodeId>
                                <nodeLabel>loopback-001</nodeLabel>
                              </service>
                            </services>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No monitored service matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count monitored services",
            description = """
                    Number of monitored services matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=status==A` or `_s=node.label==loopback-001`.""",
            operationId = "ifServicesCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching monitored services, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "262"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of monitored services",
            description = """
                    The properties a monitored-service query can filter and sort on, including the joined interface, node and asset properties. Properties with a fixed value set, such as `status`, carry that set as a `values` map.""",
            operationId = "ifServicesSearchProperties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The properties this endpoint can search and sort on.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 1,
                              "count": 1,
                              "offset": 0,
                              "searchProperty": [ {
                                "id": "status",
                                "name": "Management Status",
                                "type": "STRING",
                                "orderBy": true,
                                "iplike": false,
                                "values": {
                                  "A": "Managed",
                                  "R": "Rescan to Resume",
                                  "S": "Rescan to Suspend",
                                  "D": "Deleted",
                                  "U": "Unmanaged",
                                  "F": "Forced Unmanaged",
                                  "X": "Remotely Monitored",
                                  "N": "Not Monitored"
                                }
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="STRING" orderBy="true" iplike="false" id="status" name="Management Status">
                                <values>
                                  <entry><key>A</key><value>Managed</value></entry>
                                  <entry><key>U</key><value>Unmanaged</value></entry>
                                </values>
                              </searchProperty>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one monitored-service property. Properties with a fixed value set return it directly, so `status` returns the eight management-status codes; other properties are read from the database and honour `limit`.""",
            operationId = "ifServicesSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 8,
                              "count": 8,
                              "offset": 0,
                              "value": [ "A", "R", "S", "D", "U", "F", "X", "N" ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one monitored service",
            description = """
                    This endpoint has no single-entity lookup, so every identifier is answered with 501 and no body.""",
            operationId = "ifServicesGet")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = """
                    Always. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Unused: this endpoint has no single-entity lookup, so every value produces the same response.""",
                    required = true, example = "1017")
            final String id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create a monitored service",
            description = """
                    Answered with 501. The body is mapped to an entity before the 501 is raised, and that mapping reads `serviceType.id`, so a body without a `serviceType` fails with 500 instead.""",
            operationId = "ifServicesCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = """
                    The body has no `serviceType`. The body is a `text/plain` message.""",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = """
                            Cannot invoke "org.opennms.web.rest.model.v2.ServiceTypeDTO.getId()" because the return value of "org.opennms.web.rest.model.v2.MonitoredServiceDTO.getServiceType()" is null"""))),
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    Accepted but not acted on. It still has to carry a `serviceType`, since the body is mapped before the request is refused.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = MonitoredServiceDTO.class),
                                    examples = @ExampleObject(value = """
                            { "serviceType": { "id": 1, "name": "ICMP" } }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = MonitoredServiceDTO.class),
                                    examples = @ExampleObject(value = """
                            <service><serviceType id="1"><name>ICMP</name></serviceType></service>"""))
                    })
            final MonitoredServiceDTO object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create a monitored service at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "ifServicesCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "1017")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the monitored services matching a query",
            description = """
                    Sets named properties on every monitored service matching the query. Setting `status=F` forces a service unmanaged and `status=A` returns it to managed. Application membership changes send `applicationChanged` events. At most `limit` entities are affected, ten by default.

                    A service whose status did not actually change is skipped rather than reported, so the operation answers 204 whether or not anything changed.

                    For example, `_s=status==A` or `_s=node.label==loopback-001`.""",
            operationId = "ifServicesUpdateMany")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = """
                    Every matching service was processed. The response has no body."""),
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
                            status=F""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final OnmsMonitoredService object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one monitored service",
            description = """
                    Both the JSON or XML replacement form and the form-parameter form answer 501.""",
            operationId = "ifServicesUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = """
                    Always. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Unused: this endpoint has no single-entity lookup, so every value produces the same response.""",
                    required = true, example = "1017")
            final String id,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsMonitoredService.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsMonitoredService.class),
                                    examples = @ExampleObject(value = """
                            <service/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            status=F"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the monitored services matching a query",
            description = """
                    Not supported here: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=status==A` or `_s=node.label==loopback-001`.""",
            operationId = "ifServicesDeleteMany")
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
    @Operation(summary = "Delete one monitored service",
            description = """
                    Answered with 501 for every identifier.""",
            operationId = "ifServicesDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = """
                    Always. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Unused: this endpoint has no single-entity lookup, so every value produces the same response.""",
                    required = true, example = "1017")
            final String id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
