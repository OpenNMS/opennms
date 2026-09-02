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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.restrictions.SqlRestriction.Type;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;
import org.opennms.web.rest.v2.model.NodeServiceTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsNode} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("nodes")
@Transactional
@Tag(name = "Nodes", description = "Node API")
public class NodeRestService extends AbstractDaoRestService<OnmsNode,SearchBean,Integer,String> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRestService.class);

    // Maps SNMP interface FIQL field names to their lowercase DB column names in the snmpinterface table.
    // Used to build SQL subqueries with ilike for case-insensitive matching (exact and wildcard).
    // Non-string SNMP fields (ifIndex, ifType, etc.) are absent; they use a different join strategy.
    private static final Map<String,String> SNMP_STRING_COLUMN = Map.of(
        "ifAlias",  "snmpifalias",
        "ifDescr",  "snmpifdescr",
        "ifName",   "snmpifname",
        "physAddr", "snmpphysaddr"
    );

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private NodeDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    protected NodeDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsNode> getDaoClass() {
        return OnmsNode.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class, Aliases.node.toString());

        // 1st level JOINs
        builder.alias("assetRecord", Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Add this alias via a CriteriaBehavior so that we can specify a join condition
        //builder.alias("categories", Aliases.category.toString(), JoinType.LEFT_JOIN);
        // Add this alias via a CriteriaBehavior so that we can specify a join condition
        //builder.alias("ipInterfaces", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("location", Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Add this alias via a CriteriaBehavior so that we can specify a join condition
        //builder.alias("snmpInterfaces", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        // TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties
        //builder.alias(Aliases.ipInterface.prop("monitoredServices"), Aliases.monitoredService.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        // TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties
        //builder.alias(Aliases.monitoredService.prop("serviceType"), Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // Order by label by default
        builder.orderBy("label").desc();

        return builder;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.NODE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.NODE_BEHAVIORS);

        // node.label: use ilike for case-insensitive wildcard matching (default like() is case-sensitive)
        CriteriaBehavior<?> labelBehavior = new CriteriaBehavior<>((String)null, String::new, (b, v, c, w) -> {
            switch (c) {
            case EQUALS:
                if (v == null) {
                    b.isNull("label");
                } else if (w) {
                    b.ilike("label", v);
                } else {
                    b.eq("label", v);
                }
                break;
            case NOT_EQUALS:
                if (v == null) {
                    b.isNotNull("label");
                } else if (w) {
                    b.not().ilike("label", v);
                } else {
                    b.or(Restrictions.ne("label", v), Restrictions.isNull("label"));
                }
                break;
            default:
                break;
            }
        });
        labelBehavior.setSkipPropertyByDefault(true);
        map.put("label", labelBehavior);

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));

        // Use join conditions for one-to-many aliases
        for (Map.Entry<String,CriteriaBehavior<?>> entry : CriteriaBehaviors.IP_INTERFACE_BEHAVIORS.entrySet()) {
            map.put(Aliases.ipInterface.prop(entry.getKey()), new CriteriaBehavior(entry.getValue().getPropertyName(), entry.getValue().getConverter(), (b,v,c,w)-> {
                b.alias(
                    "ipInterfaces",
                    Aliases.ipInterface.toString(),
                    JoinType.LEFT_JOIN,
                    Restrictions.or(Restrictions.eq(Aliases.ipInterface.prop(entry.getKey()), v), Restrictions.isNull(Aliases.ipInterface.prop(entry.getKey())))
                );
            }));
        }
        // Also add behaviors for the String properties (which is not normally necessary
        // but is necessary here because they add BeforeVisit operations to add JOINs)
        for (String prop : new String[] { "ipHostName", "isManaged" } ) {
            map.put(Aliases.ipInterface.prop(prop), new CriteriaBehavior<>((String)null, String::new, (b,v,c,w)-> {
                b.alias(
                    "ipInterfaces",
                    Aliases.ipInterface.toString(),
                    JoinType.LEFT_JOIN,
                    Restrictions.or(Restrictions.eq(Aliases.ipInterface.prop(prop), v), Restrictions.isNull(Aliases.ipInterface.prop(prop)))
                );
            }));
        }

        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.NODE_SERVICE_TYPE_BEHAVIORS));

        // Use join conditions for one-to-many aliases
        for (Map.Entry<String,CriteriaBehavior<?>> entry : CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS.entrySet()) {
            final String key = entry.getKey();
            final String dbCol = SNMP_STRING_COLUMN.get(key);
            if (dbCol != null) {
                // String field: use SQL subquery with ilike for case-insensitive matching.
                // A JOIN condition with Restrictions.eq cannot perform LIKE matching, so wildcard
                // searches like snmpInterface.ifAlias==*value* would return nothing with a join approach.
                // skipProperty=true: the SQL subquery handles the full restriction; the snmpInterface
                // alias is never joined, so the default Hibernate property restriction must not be added
                // (it would cause a QueryException: could not resolve property: snmpInterface).
                CriteriaBehavior<?> snmpStringBehavior = new CriteriaBehavior(entry.getValue().getPropertyName(), entry.getValue().getConverter(), (b,v,c,w)-> {
                    switch (c) {
                    case EQUALS:
                        b.sql(String.format("{alias}.nodeid in (select snmpinterface.nodeid from snmpinterface where snmpinterface.snmpcollect != 'D' and snmpinterface.%s ilike ?)", dbCol), v, Type.STRING);
                        break;
                    case NOT_EQUALS:
                        b.sql(String.format("{alias}.nodeid not in (select snmpinterface.nodeid from snmpinterface where snmpinterface.snmpcollect != 'D' and snmpinterface.%s ilike ?)", dbCol), v, Type.STRING);
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal condition type when filtering snmpInterface." + key + ": " + c);
                    }
                });
                snmpStringBehavior.setSkipPropertyByDefault(true);
                map.put(Aliases.snmpInterface.prop(key), snmpStringBehavior);
            } else {
                // Non-string field: join condition for exact matching (wildcards not applicable)
                map.put(Aliases.snmpInterface.prop(key), new CriteriaBehavior(entry.getValue().getPropertyName(), entry.getValue().getConverter(), (b,v,c,w)-> {
                    b.alias(
                        "snmpInterfaces",
                        Aliases.snmpInterface.toString(),
                        JoinType.LEFT_JOIN,
                        Restrictions.or(Restrictions.eq(Aliases.snmpInterface.prop(key), v), Restrictions.isNull(Aliases.snmpInterface.prop(key)))
                    );
                }));
            }
        }
        // There are no extra String properties on node.snmpInterfaces

        // Topology (CDP/LLDP) search: virtual property backed by a SQL subquery across topology tables.
        // Mirrors the legacy node-list topology search. skipProperty=true: no Hibernate alias is joined,
        // so the default property restriction must not be added.
        final String topologySql = "{alias}.nodeId in (" +
            "select nodeId from cdplink where cdpinterfacename ilike ? " +
            "union select nodeId from cdpelement where cdpglobaldeviceid ilike ? " +
            "union select nodeId from lldplink where lldpportid ilike ? or lldpportdescr ilike ? " +
            "union select nodeId from lldpelement where lldpsysname ilike ?)";
        CriteriaBehavior<?> topologyBehavior = new CriteriaBehavior<>((String)null, String::new, (b,v,c,w) ->
            b.sql(topologySql, new Object[]{v, v, v, v, v}, new Type[]{Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING})
        );
        topologyBehavior.setSkipPropertyByDefault(true);
        map.put("topology", topologyBehavior);

        // TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties

        // iplike: PostgreSQL iplike() pattern filter over all IP interfaces of the node.
        // Uses a SQL subquery because IpLikeCriteriaBehavior.b.iplike() only works against the
        // root alias; the ipinterface table is a child association and cannot be aliased there.
        // FIQL delivers '*' wildcards as '%' by the time the lambda fires — reverse with replaceAll().
        CriteriaBehavior<?> iplikeBehavior = new CriteriaBehavior<>((String)null, String::new, (b, v, c, w) -> {
            if (v == null) {
                return;
            }
            final String pattern = ((String)v).replaceAll("%", "*");
            switch (c) {
            case EQUALS:
                b.sql("{alias}.nodeid in (select nodeid from ipinterface where iplike(ipaddr, ?))", pattern, Type.STRING);
                break;
            case NOT_EQUALS:
                b.sql("{alias}.nodeid not in (select nodeid from ipinterface where iplike(ipaddr, ?))", pattern, Type.STRING);
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for iplike expression: " + c);
            }
        });
        iplikeBehavior.setSkipPropertyByDefault(true);
        map.put("iplike", iplikeBehavior);

        // maclike: case-insensitive partial match on SNMP interface physical (MAC) address.
        // Mirrors the legacy node-list MAC-like search — colons and dashes are stripped,
        // then an ANYWHERE ilike is applied. Uses a SQL subquery because snmpinterface is a child
        // association and cannot be aliased against the root criteria here.
        CriteriaBehavior<?> maclikeBehavior = new CriteriaBehavior<>((String)null, String::new, (b, v, c, w) -> {
            if (v == null) {
                return;
            }
            final String pattern = "%" + ((String)v).replaceAll("[:-]", "") + "%";
            switch (c) {
            case EQUALS:
                b.sql("{alias}.nodeid in (select nodeid from snmpinterface where snmpphysaddr ilike ?)", pattern, Type.STRING);
                break;
            case NOT_EQUALS:
                b.sql("{alias}.nodeid not in (select nodeid from snmpinterface where snmpphysaddr ilike ?)", pattern, Type.STRING);
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for maclike expression: " + c);
            }
        });
        maclikeBehavior.setSkipPropertyByDefault(true);
        map.put("maclike", maclikeBehavior);

        // nodesWithDownAggregateStatus: nodes whose aggregate status is "down", i.e. that have at
        // least one active (status='A') monitored service currently in outage (ifRegainedService is null).
        // Mirrors the post-query AggregateStatus.getDownNodes() filter the legacy node list applies.
        // Uses a SQL subquery because the outage/ifservice tables are not aliased on the node criteria.
        final String downStatusSubquery = "(select ip.nodeid from outages o " +
                "join ifservices s on o.ifserviceid = s.id " +
                "join ipinterface ip on s.ipinterfaceid = ip.id " +
                "where o.ifregainedservice is null and s.status = 'A')";
        CriteriaBehavior<?> downStatusBehavior = new CriteriaBehavior<>((String)null, String::new, (b, v, c, w) -> {
            if (v == null) {
                return;
            }
            final boolean wantDown = Boolean.parseBoolean((String)v);
            // (==true) and (!=false) both mean "down nodes only"; (==false)/(!=true) mean "exclude down nodes".
            boolean downOnly;
            switch (c) {
            case EQUALS:
                downOnly = wantDown;
                break;
            case NOT_EQUALS:
                downOnly = !wantDown;
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for nodesWithDownAggregateStatus expression: " + c);
            }
            b.sql("{alias}.nodeid " + (downOnly ? "in " : "not in ") + downStatusSubquery);
        });
        downStatusBehavior.setSkipPropertyByDefault(true);
        map.put("nodesWithDownAggregateStatus", downStatusBehavior);

        // nodesWithOutages: nodes that have at least one current (unresolved, unsuppressed,
        // non-perspective) outage. Mirrors the legacy node-list "nodes with outages" filter,
        // with its operator-precedence bug fixed: the suppresstime disjunction is parenthesized
        // so it no longer overrides "ifregainedservice is null".
        final String currentOutagesSubquery = "(select ip.nodeid from outages o " +
                "join ifservices s on o.ifserviceid = s.id " +
                "join ipinterface ip on s.ipinterfaceid = ip.id " +
                "where o.perspective is null " +
                "and o.ifregainedservice is null " +
                "and (o.suppresstime is null or o.suppresstime < now()))";
        CriteriaBehavior<?> outagesBehavior = new CriteriaBehavior<>((String)null, String::new, (b, v, c, w) -> {
            if (v == null) {
                return;
            }
            final boolean wantOutages = Boolean.parseBoolean((String)v);
            boolean hasOutages;
            switch (c) {
            case EQUALS:
                hasOutages = wantOutages;
                break;
            case NOT_EQUALS:
                hasOutages = !wantOutages;
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for nodesWithOutages expression: " + c);
            }
            b.sql("{alias}.nodeid " + (hasOutages ? "in " : "not in ") + currentOutagesSubquery);
        });
        outagesBehavior.setSkipPropertyByDefault(true);
        map.put("nodesWithOutages", outagesBehavior);

        // nodesWithAssets: nodes whose asset record has at least one non-empty field.
        // Mirrors the legacy AssetModel.searchNodesWithAssets() query ("All nodes with asset info").
        final String[] assetColumns = {
            "manufacturer", "vendor", "modelNumber", "serialNumber", "description", "circuitId",
            "assetNumber", "operatingSystem", "rack", "slot", "port", "region", "division", "department",
            "address1", "address2", "city", "state", "zip", "building", "floor", "room", "vendorPhone",
            "vendorFax", "dateInstalled", "lease", "leaseExpires", "supportPhone", "maintContract",
            "vendorAssetNumber", "maintContractExpires", "displayCategory", "notifyCategory",
            "pollerCategory", "thresholdCategory", "comment", "username", "password", "enable",
            "connection", "autoenable", "cpu", "ram", "storagectrl", "hdd1", "hdd2", "hdd3", "hdd4",
            "hdd5", "hdd6", "numpowersupplies", "inputpower", "additionalhardware", "admin",
            "snmpcommunity", "rackunitheight"
        };
        final String nonEmptyAssets = java.util.Arrays.stream(assetColumns)
                .map(col -> "coalesce(" + col + ",'') != ''")
                .collect(java.util.stream.Collectors.joining(" or "));
        final String assetsSubquery = "(select nodeid from assets where " + nonEmptyAssets + ")";
        CriteriaBehavior<?> withAssetsBehavior = new CriteriaBehavior<>((String)null, String::new, (b, v, c, w) -> {
            if (v == null) {
                return;
            }
            final boolean wantAssets = Boolean.parseBoolean((String)v);
            boolean hasAssets;
            switch (c) {
            case EQUALS:
                hasAssets = wantAssets;
                break;
            case NOT_EQUALS:
                hasAssets = !wantAssets;
                break;
            default:
                throw new IllegalArgumentException("Illegal condition type for nodesWithAssets expression: " + c);
            }
            b.sql("{alias}.nodeid " + (hasAssets ? "in " : "not in ") + assetsSubquery);
        });
        withAssetsBehavior.setSkipPropertyByDefault(true);
        map.put("nodesWithAssets", withAssetsBehavior);

        return map;
    }

    @Override
    protected JaxbListWrapper<OnmsNode> createListWrapper(Collection<OnmsNode> list) {
        return new OnmsNodeList(list);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsNode object) {
        if (object == null) {
            throw getException(Status.BAD_REQUEST, "Node object cannot be null");
        }
        if (object.getLocation() == null) {
            OnmsMonitoringLocation location = m_locationDao.getDefaultLocation();
            LOG.debug("doCreate: Assigning new node to default location: {}", location.getLocationName());
            object.setLocation(location);
        }
        // See NMS-9855
        if (object.getAssetRecord() != null && object.getAssetRecord().getNode() == null) {
            object.getAssetRecord().setNode(object);
        }
        final Integer id = getDao().save(object);
        final Event e = EventUtils.createNodeAddedEvent("Rest", id, object.getLabel(), object.getLabelSource(), null);
        sendEvent(e);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsNode targetObject, MultivaluedMapImpl params) {
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsNode node) {
        getDao().delete(node);
        final Event e = EventUtils.createDeleteNodeEvent("ReST", node.getId(), -1L);
        sendEvent(e);
    }

    @Override
    protected OnmsNode doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    // The generic collection and item operations below are inherited from AbstractDaoRestServiceWithDTO.
    // They are overridden here only so that each concrete path carries its own OpenAPI documentation;
    // the bodies delegate unchanged.
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get nodes",
            description = """
        Return the nodes matching the FIQL expression in `_s`, or the first page of all nodes when `_s`
        is absent. `limit` defaults to 10, so an unfiltered call returns 10 nodes and reports the
        unpaged total in `totalCount`. A `Content-Range` header of the form `items 0-9/3677` accompanies
        a 200. Nodes with `type` of `D` (deleted) are not excluded.

        In JSON, `id` is a string and `createTime`, `lastIngressFlow`, `lastEgressFlow` and the asset
        record timestamps are epoch milliseconds, although the derived schema for those fields shows
        `string`/`date-time`. The XML representation of the same fields is ISO-8601 with an offset.

        Example query: `_s=node.foreignSource==Servers;category.name==Production&orderBy=label`.""",
            operationId = "NodeRestServiceGETNodes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching nodes.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsNodeList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "node": [
                        {
                          "id": "257",
                          "label": "ApiDoc-node",
                          "labelSource": "U",
                          "type": "A",
                          "foreignSource": "ApiDoc",
                          "foreignId": "apidoc-1",
                          "location": "Default",
                          "sysContact": "noc@example.org",
                          "sysName": "apidoc",
                          "sysDescription": "ApiDoc probe node",
                          "createTime": 1787727308166,
                          "lastIngressFlow": null,
                          "lastEgressFlow": null,
                          "nodeParentID": null,
                          "categories": [],
                          "assetRecord": {
                            "id": 23599,
                            "category": "Unspecified",
                            "lastModifiedBy": "",
                            "lastModifiedDate": 1787727308166
                          }
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsNodeList.class),
                                    examples = @ExampleObject(value = """
                    <nodes count="1" offset="0" totalCount="1">
                      <node foreignId="apidoc-1" foreignSource="ApiDoc" label="ApiDoc-node" id="257" type="A">
                        <createTime>2026-08-26T02:55:08.166-04:00</createTime>
                        <labelSource>U</labelSource>
                        <location>Default</location>
                        <sysContact>noc@example.org</sysContact>
                      </node>
                    </nodes>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No node matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The FIQL expression could not be parsed, or it names a property Hibernate cannot resolve.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count nodes",
            description = """
        Return the number of nodes matching `_s` as a bare decimal string. `limit` and `offset` do not
        affect the count.

        Example query: `_s=node.foreignSource==Servers`.""",
            operationId = "NodeRestServiceGETNodeCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number of matching nodes.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "3677"))),
            @ApiResponse(responseCode = "500", description = "The FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get node search properties",
            description = """
        List the properties that may appear in a `_s` expression or in `orderBy` for the node
        resources. A property that carries a closed set of values reports them in `values`, keyed by
        the value stored in the database.""",
            operationId = "NodeRestServiceGETNodeSearchProperties")
    @ApiResponse(responseCode = "200", description = "Supported search properties.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchPropertyCollection.class),
                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "searchProperty": [
                        {"id": "label", "name": "Label", "type": "STRING", "orderBy": true, "iplike": false},
                        {
                          "id": "labelSource",
                          "name": "Label Source",
                          "type": "STRING",
                          "orderBy": true,
                          "iplike": false,
                          "values": {
                            "A": "IP Address",
                            "H": "Hostname",
                            "N": "NetBIOS",
                            "S": "SNMP sysName",
                            " ": "Unknown",
                            "U": "User-Defined"
                          }
                        }
                      ]
                    }""")))
    @Override
    public Response getProperties(
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Case-insensitive substring matched against the property `name`, not its id.",
                    example = "Label")
            @QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get values of a node search property",
            description = """
        Return the distinct values a search property takes across the node table, or its declared value
        list when it has one. The wrapper element is named `value` regardless of the property type;
        numeric and timestamp properties come back in the collection type matching the property, with
        the same envelope.""",
            operationId = "NodeRestServiceGETNodeSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Distinct values of the property.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                    {"totalCount": 3, "count": 3, "offset": 0, "value": ["core-switch-01", "core-switch-02", "edge-router-01"]}"""))),
            @ApiResponse(responseCode = "404", description = "No search property has that id. No body is returned.")
    })
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId",
                    description = "Property id as reported by `GET /nodes/properties`.", example = "label")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Substring the value must contain. Case-sensitive for declared value lists, case-insensitive for values read from the database.",
                    example = "core")
            @QueryParam("q") final String query,
            @Parameter(in = ParameterIn.QUERY, name = "limit",
                    description = "Maximum number of values to return. Applied only when the values are read from the database.", example = "10")
            @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get a node",
            description = """
        Return one node by database id or by `foreignSource:foreignId`. Timestamps are epoch
        milliseconds in JSON and ISO-8601 with an offset in XML.""",
            operationId = "NodeRestServiceGETNodeById")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The node.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsNode.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": "257",
                      "label": "ApiDoc-node",
                      "labelSource": "U",
                      "type": "A",
                      "foreignSource": "ApiDoc",
                      "foreignId": "apidoc-1",
                      "location": "Default",
                      "sysContact": "noc@example.org",
                      "sysName": "apidoc",
                      "sysDescription": "ApiDoc probe node",
                      "createTime": 1787727308166,
                      "lastIngressFlow": null,
                      "lastEgressFlow": null,
                      "nodeParentID": null,
                      "categories": [],
                      "assetRecord": {
                        "id": 23599,
                        "category": "Unspecified",
                        "lastModifiedBy": "",
                        "lastModifiedDate": 1787727308166
                      }
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsNode.class),
                                    examples = @ExampleObject(value = """
                    <node foreignId="apidoc-1" foreignSource="ApiDoc" label="ApiDoc-node" id="257" type="A">
                      <createTime>2026-08-26T02:55:08.166-04:00</createTime>
                      <labelSource>U</labelSource>
                      <location>Default</location>
                      <sysContact>noc@example.org</sysContact>
                    </node>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No such node. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.",
                    example = "257")
            @PathParam("id") final String id) {
        return super.get(uriInfo, id);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: create a node at a caller-chosen id",
            description = "Always answered with 404, whether or not the id exists.",
            operationId = "NodeRestServicePOSTNodeSpecific",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Node database id, or `foreignSource:foreignId`. The value is not read: every request to this path is answered with 404.",
                    example = "257"))
    @ApiResponse(responseCode = "404", description = "Creating a node at a specific id is not supported. No body is returned.")
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Create a node",
            description = """
        Create a node and send a `nodeAdded` event. When `location` is omitted the default monitoring
        location is used, and the new node's URI is returned in the `Location` header. A requisition
        synchronisation does not preserve the node unless the matching `foreignSource` and `foreignId`
        also exist in a requisition. A body that fails to parse is answered with 500, not 400.""",
            operationId = "NodeRestServicePOSTNode")
    @RequestBody(required = true, description = "The node to create.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsNode.class),
                            examples = @ExampleObject(value = """
                    {
                      "label": "ApiDoc-node",
                      "labelSource": "U",
                      "type": "A",
                      "foreignSource": "ApiDoc",
                      "foreignId": "apidoc-1",
                      "sysContact": "noc@example.org",
                      "sysName": "apidoc",
                      "sysDescription": "ApiDoc probe node"
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsNode.class),
                            examples = @ExampleObject(value = """
                    <node label="ApiDoc-node" labelSource="U" type="A" foreignSource="ApiDoc" foreignId="apidoc-1">
                      <location>Default</location>
                      <sysContact>noc@example.org</sysContact>
                    </node>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Node created. `Location` carries the URI of the new node.",
                    headers = @Header(name = "Location", description = "URI of the created node.",
                            schema = @Schema(type = "string", example = "http://localhost:8980/opennms/api/v2/nodes/257"))),
            @ApiResponse(responseCode = "400", description = "The body was absent after deserialisation.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node object cannot be null"))),
            @ApiResponse(responseCode = "500", description = "The body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No content to map to Object due to end of input")))
    })
    @Override
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsNode object) {
        return super.create(securityContext, uriInfo, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several nodes",
            description = """
        Apply the form parameters as bean properties to every node matching `_s`. The default `limit`
        of 10 applies to the selection, so a call without an explicit `limit` updates at most 10 nodes.
        The whole call runs in one transaction, so a per-node failure aborts the batch.

        Example query: `_s=node.foreignSource==ApiDoc&limit=100`.""",
            operationId = "NodeRestServicePUTNodes")
    @RequestBody(required = true, description = "Node bean properties to set, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "sys-contact=noc%40example.org&sys-description=updated+by+ReST")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All selected nodes were updated."),
            @ApiResponse(responseCode = "404", description = "No node matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(
            summary = "Not implemented: replace a node from a document",
            description = """
        Answered with 501. This variant binds `{id}` as an integer, so a non-numeric path segment is
        answered with 404 before the handler runs.""",
            operationId = "NodeRestServicePUTNodeDocument")
    @RequestBody(description = "Ignored.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OnmsNode.class),
                            examples = @ExampleObject(value = "{\"label\": \"ApiDoc-node\"}")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = OnmsNode.class),
                            examples = @ExampleObject(value = "<node label=\"ApiDoc-node\"/>"))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "`{id}` is not an integer."),
            @ApiResponse(responseCode = "501", description = "Replacing a node from a document is not implemented. No body is returned.")
    })
    @Override
    public Response update(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id", description = "Node database id.", example = "257")
            @PathParam("id") final Integer id,
            final OnmsNode object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Update properties of a node",
            description = """
        Apply the form parameters as bean properties to one node. Only the properties present in the
        body are touched. No event is sent.""",
            operationId = "NodeRestServicePUTNodeProperties")
    @RequestBody(required = true, description = "Node bean properties to set, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "sys-contact=noc%40example.org")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The node was updated."),
            @ApiResponse(responseCode = "404", description = "No such node. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response updateProperties(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("id") final String id,
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Delete several nodes",
            description = """
        Delete every node matching `_s` and send a `deleteNode` event for each. The default `limit` of
        10 applies to the selection, so a call without an explicit `limit` deletes at most 10 nodes.

        Example query: `_s=node.foreignSource==ApiDoc&limit=100`.""",
            operationId = "NodeRestServiceDELETENodes")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The selected nodes were deleted."),
            @ApiResponse(responseCode = "404", description = "No node matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete a node",
            description = """
        Delete one node and send a `deleteNode` event. The node's interfaces, services, outages and
        metadata go with it.""",
            operationId = "NodeRestServiceDELETENodeById")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The node was deleted."),
            @ApiResponse(responseCode = "404", description = "No such node. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response delete(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("id") final String id) {
        return super.delete(securityContext, uriInfo, id);
    }

    @GET
    @Path("service-types")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    @Operation(
            summary = "Get all service types",
            description = """
        Return every monitored service type known to the system, sorted by name. The list is
        system-wide, not scoped to a node, and is the set of names accepted by
        `POST /nodes/{nodeCriteria}/ipinterfaces/{ipAddress}/services`.

        This operation produces JSON only. A request with `Accept: application/xml` does not match it
        and falls through to `GET /nodes/{id}` with an id of `service-types`, which is answered with
        500.""",
            operationId = "NodeRestServiceGETServiceTypes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All service types, sorted by name.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = NodeServiceTypeDto.class)),
                            examples = @ExampleObject(value = """
                    [
                      {"name": "DNS", "id": 4},
                      {"name": "HTTP-8080", "id": 2},
                      {"name": "ICMP", "id": 1},
                      {"name": "SNMP", "id": 3}
                    ]"""))),
            @ApiResponse(responseCode = "500", description = "The request asked for `application/xml`, so it matched `GET /nodes/{id}` with an id of `service-types` instead.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"service-types\"")))
    })
    public Response getServiceTypes() {
        final List<Map<String,Object>> result = m_serviceTypeDao.findAll().stream()
            .sorted(Comparator.comparing(OnmsServiceType::getName))
            .map(st -> {
                final Map<String,Object> item = new HashMap<>();
                item.put("id", st.getId());
                item.put("name", st.getName());
                return item;
            })
            .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @Path("{nodeCriteria}/ipinterfaces")
    public NodeIpInterfacesRestService getIpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(NodeIpInterfacesRestService.class);
    }

    @Path("{nodeCriteria}/snmpinterfaces")
    public NodeSnmpInterfacesRestService getSnmpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(NodeSnmpInterfacesRestService.class);
    }

    @Path("{nodeCriteria}/hardwareInventory")
    public NodeHardwareInventoryRestService getHardwareInventoryResource(@Context final ResourceContext context) {
        return context.getResource(NodeHardwareInventoryRestService.class);
    }

    @Path("{nodeCriteria}/categories")
    public NodeCategoriesRestService getCategoriesResource(@Context final ResourceContext context) {
        return context.getResource(NodeCategoriesRestService.class);
    }

    /**
     * <p>rescanNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}/rescan")
    @Operation(
            summary = "Request a rescan of a node",
            description = """
        Send a `forceRescan` event for the node and return as soon as the event is queued. The 200 says
        the event was accepted, not that the scan has run or succeeded. The request body is not read,
        although the operation is declared as consuming `application/x-www-form-urlencoded`.""",
            operationId = "NodeRestServicePUTRescanNodeByNodeId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The rescan event was sent. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "500", description = "The path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response rescanNode(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") final String nodeCriteria) {
        final OnmsNode node = m_dao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "Node {} was not found.", nodeCriteria);
        }
        
        final Event e = EventUtils.createNodeRescanEvent("ReST", node.getId());
        sendEvent(e);
        return Response.ok().build();
    }

    @GET
    @Path("{nodeCriteria}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get all metadata of a node",
            description = """
        Return every metadata entry attached to the node, across all contexts. Contexts populated by
        the system (`requisition`, `snmp` and vendor contexts) appear alongside user-defined `X-`
        contexts. An empty result is still a 200; `totalCount` and `count` are then `null` rather
        than `0`.""",
            operationId = "NodeRestServiceGETMetaDataByNodeId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata entries of the node.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "metaData": [
                        {"context": "X-ApiDoc", "key": "owner", "value": "noc-team"},
                        {"context": "X-ApiDoc", "key": "tier", "value": "gold"}
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    <meta-data-list count="2" offset="0" totalCount="2">
                      <meta-data><context>X-ApiDoc</context><key>owner</key><value>noc-team</value></meta-data>
                      <meta-data><context>X-ApiDoc</context><key>tier</key><value>gold</value></meta-data>
                    </meta-data-list>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.", example = "257")
            @PathParam("nodeCriteria") String nodeCriteria) {
        final OnmsNode node = getDao().get(nodeCriteria);

        if (node == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find node " + nodeCriteria);
        }

        return new OnmsMetaDataList(node.getMetaData());
    }

    @GET
    @Path("{nodeCriteria}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get metadata of a node in one context",
            description = """
        Return the node's metadata entries whose context matches exactly, case-sensitively. A context
        that holds no entries and a context that does not exist are indistinguishable: both give a 200
        with an empty list.""",
            operationId = "NodeRestServiceGETMetaDataByNodeIdAndContext")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata entries in that context, possibly empty.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = {
                                            @ExampleObject(name = "match", value = """
                    {"totalCount": 1, "count": 1, "offset": 0, "metaData": [{"context": "X-ApiDoc", "key": "owner", "value": "noc-team"}]}"""),
                                            @ExampleObject(name = "no match", value = """
                    {"totalCount": null, "count": null, "offset": 0, "metaData": []}""")
                                    }),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    <meta-data-list count="1" offset="0" totalCount="1">
                      <meta-data><context>X-ApiDoc</context><key>owner</key><value>noc-team</value></meta-data>
                    </meta-data-list>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") String nodeCriteria,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context to filter on.", example = "X-ApiDoc")
            @PathParam("context") String context) {
        final OnmsNode node = getDao().get(nodeCriteria);

        if (node == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find node " + nodeCriteria);
        }

        return new OnmsMetaDataList(node.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()))
                .collect(Collectors.toList()));
    }

    @GET
    @Path("{nodeCriteria}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one metadata entry of a node",
            description = """
        Return the node's metadata entries matching both context and key. At most one entry can match,
        but the response is still the list envelope. An unknown context or key gives a 200 with an
        empty list.""",
            operationId = "NodeRestServiceGETMetaDataByNodeIdAndContextAndKey")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The matching entry, or an empty list.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaDataList.class),
                            examples = @ExampleObject(value = """
                    {"totalCount": 1, "count": 1, "offset": 0, "metaData": [{"context": "X-ApiDoc", "key": "owner", "value": "noc-team"}]}"""))),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") String nodeCriteria,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context.", example = "X-ApiDoc")
            @PathParam("context") String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key within the context.", example = "owner")
            @PathParam("key") String key) {
        final OnmsNode node = getDao().get(nodeCriteria);

        if (node == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find node " + nodeCriteria);
        }

        return new OnmsMetaDataList(node.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()) && key.equals(e.getKey()))
                .collect(Collectors.toList()));
    }

    @DELETE
    @Path("{nodeCriteria}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a metadata context of a node",
            description = """
        Remove every metadata entry of the node in the given context. Deleting a context that holds no
        entries is also a 204. The context is checked before the node is looked up, so a non-`X-`
        context is answered with 403 even for a node that does not exist.""",
            operationId = "NodeRestServiceDELETEMetaDataByNodeIdAndContext")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The context was removed."),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "deleteMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`. Only user-defined contexts may be written through this API.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response deleteMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") final String nodeCriteria,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context to remove. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsNode node = getDao().get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find node " + nodeCriteria);
            }
            node.removeMetaData(context);
            getDao().update(node);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{nodeCriteria}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete one metadata entry of a node",
            description = """
        Remove a single metadata entry of the node. Deleting a key that does not exist is also a
        204.""",
            operationId = "NodeRestServiceDELETEMetaDataByNodeIdAndContextAndKey")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was removed."),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "deleteMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response deleteMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") final String nodeCriteria,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key to remove.", example = "owner")
            @PathParam("key") final String key) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsNode node = getDao().get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find node " + nodeCriteria);
            }
            node.removeMetaData(context, key);
            getDao().update(node);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("{nodeCriteria}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace a metadata entry of a node",
            description = """
        Set one metadata entry from the request body. An existing entry with the same context and key is
        overwritten. No `@Consumes` is declared, so both
        JSON and XML bodies are accepted; the XML root element is `meta-data`. The context is checked
        before the node is looked up, so a non-`X-` context is answered with 403 even for a node that
        does not exist.""",
            operationId = "NodeRestServicePOSTMetaDataByNodeId")
    @RequestBody(required = true, description = "The metadata entry to set. The context must start with `X-`.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaData.class),
                            examples = @ExampleObject(value = """
                    {"context": "X-ApiDoc", "key": "owner", "value": "noc-team"}""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsMetaData.class),
                            examples = @ExampleObject(value = """
                    <meta-data><context>X-ApiDoc</context><key>owner</key><value>noc-team</value></meta-data>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was stored."),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "postMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`, or the body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response postMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") final String nodeCriteria,
            final OnmsMetaData entity) {
        checkUserDefinedMetadataContext(entity.getContext());

        writeLock();
        try {
            final OnmsNode node = getDao().get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "postMetaData: Can't find node " + nodeCriteria);
            }
            node.addMetaData(entity.getContext(), entity.getKey(), entity.getValue());
            getDao().update(node);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{nodeCriteria}/metadata/{context}/{key}/{value}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Set a metadata entry of a node from the path",
            description = """
        Set one metadata entry with context, key and value all taken from the path. An existing entry
        with the same context and key is overwritten. A value containing `/` has to be percent-encoded,
        and an empty value cannot be expressed this way.""",
            operationId = "NodeRestServicePUTMetaDataByNodeId")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was stored."),
            @ApiResponse(responseCode = "400", description = "No such node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "putMetaData: Can't find node 999999"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response putMetaData(
            @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257")
            @PathParam("nodeCriteria") final String nodeCriteria,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key.", example = "owner")
            @PathParam("key") final String key,
            @Parameter(in = ParameterIn.PATH, name = "value",
                    description = "Metadata value.", example = "noc-team")
            @PathParam("value") final String value) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsNode node = getDao().get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "putMetaData: Can't find node " + nodeCriteria);
            }
            node.addMetaData(context, key, value);
            getDao().update(node);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }
}
