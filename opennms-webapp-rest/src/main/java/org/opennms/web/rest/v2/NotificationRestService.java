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
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsNotificationCollection;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.IpLikeCriteriaBehavior;
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
 * Basic Web Service using REST for {@link OnmsNotification} entity.
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("notifications")
@Transactional
@Tag(name = "Notifications", description = "Notifications API")
public class NotificationRestService extends AbstractDaoRestService<OnmsNotification,SearchBean,Integer,Integer> {

    @Autowired
    private NotificationDao m_dao;

    @Override
    protected NotificationDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsNotification> getDaoClass() {
        return OnmsNotification.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNotification.class, Aliases.notification.toString());

        // 1st level JOINs
        builder.alias("event", Aliases.event.toString(), JoinType.LEFT_JOIN);
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.event.prop("distPoller"), Aliases.distPoller.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("ipInterfaces"), Aliases.ipInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eq(Aliases.ipInterface.prop("ipAddress"), Aliases.notification.prop("ipAddress")), Restrictions.isNull(Aliases.ipInterface.prop("ipAddress"))));
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("snmpInterfaces"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.snmpInterface.prop("ifIndex"), Aliases.event.prop("ifIndex")), Restrictions.isNull(Aliases.snmpInterface.prop("ifIndex"))));

        // Order by ID by default
        builder.orderBy("notifyId").desc();

        return builder;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.NOTIFICATION_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.NOTIFICATION_BEHAVIORS);
        // Allow iplike queries on ipAddress
        map.put("ipAddress", new IpLikeCriteriaBehavior("interfaceId"));

        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.notification, CriteriaBehaviors.NOTIFICATION_BEHAVIORS));
        // Allow iplike queries on notification.ipAddress
        map.put(Aliases.notification.prop("ipAddress"), new IpLikeCriteriaBehavior("interfaceId"));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.event, CriteriaBehaviors.EVENT_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.distPoller, CriteriaBehaviors.DIST_POLLER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.eventParameter, CriteriaBehaviors.EVENT_PARAMETER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        return map;
    }

    @Override
    protected JaxbListWrapper<OnmsNotification> createListWrapper(Collection<OnmsNotification> list) {
        return new OnmsNotificationCollection(list);
    }

    @Override
    protected OnmsNotification doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    @Operation(summary = "List notifications",
            description = """
                    Notifications matching the query. The query joins the triggering event, the node and its asset record, location and categories, the IP interface, the SNMP interface, the service type and the notification destinations, so properties of all of those are searchable.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.

                    For example, `_s=notifyId=gt=1` or `_s=node.label==loopback-001`. The key property is `notifyId`; `_s=id==...` fails with 500.""",
            operationId = "notificationsList")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of matching notifications.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsNotificationCollection.class),
                                    examples = @ExampleObject(value = """
                            {
                              "totalCount": 2613,
                              "count": 1,
                              "offset": 0,
                              "notification": [ {
                                "id": 2615,
                                "uei": "uei.opennms.org/nodes/nodeUp",
                                "notificationName": "nodeUp",
                                "subject": "Notice #2615: Node loopback-001 has been cleared.",
                                "numericMessage": "111-2615",
                                "severity": "NORMAL",
                                "type": "NOTIFICATION",
                                "queueId": "default",
                                "pageTime": 1787074522047,
                                "eventId": 9650,
                                "nodeId": 2,
                                "nodeLabel": "loopback-001",
                                "serviceType": null,
                                "ackUser": null,
                                "ackTime": null,
                                "ackId": 2615,
                                "destinations": [ {
                                  "id": 4738,
                                  "userId": "admin",
                                  "media": "javaEmail",
                                  "contactInfo": "",
                                  "autoNotify": "C",
                                  "notifyTime": 1787074527313
                                } ]
                              } ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsNotificationCollection.class),
                                    examples = @ExampleObject(value = """
                            <notifications count="1" offset="0" totalCount="2613">
                              <notification id="2615" severity="NORMAL">
                                <eventId>9650</eventId>
                                <uei>uei.opennms.org/nodes/nodeUp</uei>
                                <nodeId>2</nodeId>
                                <nodeLabel>loopback-001</nodeLabel>
                                <notificationName>nodeUp</notificationName>
                                <numericMessage>111-2615</numericMessage>
                                <pageTime>2026-08-18T13:35:22.047-04:00</pageTime>
                                <queueId>default</queueId>
                                <subject>Notice #2615: Node loopback-001 has been cleared.</subject>
                                <destinations>
                                  <destination autoNotify="C" id="4738">
                                    <contactInfo></contactInfo>
                                    <media>javaEmail</media>
                                    <notifyTime>2026-08-18T13:35:27.313-04:00</notifyTime>
                                    <userId>admin</userId>
                                  </destination>
                                </destinations>
                              </notification>
                            </notifications>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No notification matched the query. The response has no body."),
            @ApiResponse(responseCode = "500", description = DOC_SEARCH_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response get(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "Count notifications",
            description = """
                    Number of notifications matching the query.

                    Only `text/plain` is produced. A request that sends `Accept: application/json` does not match this operation and falls through to the single-entity GET with `count` as the identifier.

                    For example, `_s=notifyId=gt=1` or `_s=node.label==loopback-001`. The key property is `notifyId`; `_s=id==...` fails with 500.""",
            operationId = "notificationsCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching notifications, as a decimal string.",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "2613"))),
            @ApiResponse(responseCode = "500", description = DOC_COUNT_ERROR,
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    public Response getCount(final UriInfo uriInfo, final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    @Operation(summary = "List the queryable properties of notifications",
            description = """
                    The properties a notification query can filter and sort on, including the joined event, node and interface properties.""",
            operationId = "notificationsSearchProperties")
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
                                { "id": "queueId", "name": "Queue Name", "type": "STRING", "orderBy": true, "iplike": false }
                              ]
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = SearchPropertyCollection.class),
                                    examples = @ExampleObject(value = """
                            <searchProperties count="1" offset="0" totalCount="1">
                              <searchProperty type="STRING" orderBy="true" iplike="false" id="queueId" name="Queue Name"/>
                            </searchProperties>"""))
                    })
    })
    public Response getProperties(final String query) {
        return super.getProperties(query);
    }

    @Override
    @Operation(summary = "List the values a queryable property takes",
            description = """
                    Distinct values held by one notification property. The `value` entries are typed after the property: numbers for `INTEGER`, `LONG` and `FLOAT`, epoch milliseconds for `TIMESTAMP`, strings otherwise.""",
            operationId = "notificationsSearchPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values, typed after the property.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                            {
                              "totalCount": 1,
                              "count": 1,
                              "offset": 0,
                              "value": [ "default" ]
                            }"""))),
            @ApiResponse(responseCode = "404", description = "No property with that `id` is queryable here. The response has no body.")
    })
    public Response getPropertyValues(final String propertyId, final String query, final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @Override
    @Operation(summary = "Get one notification",
            description = """
                    One notification by database identifier.

                    Timestamps are epoch milliseconds in JSON and ISO-8601 with a UTC offset in XML.

                    `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "notificationsGet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The requested notification.",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsNotification.class),
                                    examples = @ExampleObject(value = """
                            {
                              "id": 2615,
                              "uei": "uei.opennms.org/nodes/nodeUp",
                              "notificationName": "nodeUp",
                              "subject": "Notice #2615: Node loopback-001 has been cleared.",
                              "numericMessage": "111-2615",
                              "severity": "NORMAL",
                              "type": "NOTIFICATION",
                              "queueId": "default",
                              "pageTime": 1787074522047,
                              "eventId": 9650,
                              "nodeId": 2,
                              "nodeLabel": "loopback-001",
                              "ackUser": null,
                              "ackTime": null,
                              "ackId": 2615
                            }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsNotification.class),
                                    examples = @ExampleObject(value = """
                            <notification id="2615" severity="NORMAL">
                              <eventId>9650</eventId>
                              <uei>uei.opennms.org/nodes/nodeUp</uei>
                              <nodeId>2</nodeId>
                              <nodeLabel>loopback-001</nodeLabel>
                              <notificationName>nodeUp</notificationName>
                              <numericMessage>111-2615</numericMessage>
                              <pageTime>2026-08-18T13:35:22.047-04:00</pageTime>
                              <queueId>default</queueId>
                              <subject>Notice #2615: Node loopback-001 has been cleared.</subject>
                            </notification>"""))
                    }),
            @ApiResponse(responseCode = "404", description = """
                    No notification has that identifier, or the identifier is not an integer. The response has no body.""")
    })
    public Response get(final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the notification.""",
                    required = true, example = "2615")
            final Integer id) {
        return super.get(uriInfo, id);
    }

    @Override
    @Operation(summary = "Create a notification",
            description = """
                    Answered with 501 for every body.""",
            operationId = "notificationsCreate")
    @ApiResponses({
            @ApiResponse(responseCode = "501", description = DOC_NOT_IMPLEMENTED)
    })
    public Response create(final SecurityContext securityContext, final UriInfo uriInfo,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsNotification.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsNotification.class),
                                    examples = @ExampleObject(value = """
                            <notification/>"""))
                    })
            final OnmsNotification object) {
        return super.create(securityContext, uriInfo, object);
    }

    @Override
    @Operation(summary = "Rejected: create a notification at a caller-chosen identifier",
            description = DOC_POST_WITH_ID,
            operationId = "notificationsCreateWithId")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                    description = "Ignored. Any value produces the same response.",
                    schema = @Schema(type = "string"), example = "2615")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Always. The response has no body.")
    })
    public Response createSpecific() {
        return super.createSpecific();
    }

    @Override
    @Operation(summary = "Update the notifications matching a query",
            description = """
                    Not supported for notifications: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=notifyId=gt=1` or `_s=node.label==loopback-001`. The key property is `notifyId`; `_s=id==...` fails with 500.""",
            operationId = "notificationsUpdateMany")
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
                            ackUser=admin""")))
            final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @Override
    @Hidden
    public Response update(final SecurityContext securityContext, final UriInfo uriInfo, final Integer id,
            final OnmsNotification object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @Override
    @Operation(summary = "Update one notification",
            description = """
                    Both the JSON or XML replacement form and the form-parameter form answer 501.""",
            operationId = "notificationsUpdate")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    The form variant answers 404 when nothing matches the identifier; the JSON or XML
                    variant answers 404 only for an absent body and does not look the identifier up."""),
            @ApiResponse(responseCode = "501", description = """
                    Notifications do not support update. Returned by the JSON or XML variant whether or not the
                    identifier exists. The response has no body.""")
    })
    public Response updateProperties(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the notification.""",
                    required = true, example = "2615")
            final Integer id,
            @RequestBody(description = """
                    Accepted but not acted on: the endpoint answers 501 for every body.""",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = OnmsNotification.class),
                                    examples = @ExampleObject(value = """
                            { }""")),
                            @Content(mediaType = "application/xml", schema = @Schema(implementation = OnmsNotification.class),
                                    examples = @ExampleObject(value = """
                            <notification/>""")),
                            @Content(mediaType = "application/x-www-form-urlencoded",
                                    schema = @Schema(type = "object"),
                                    examples = @ExampleObject(value = """
                            ackUser=admin"""))
                    })
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @Override
    @Operation(summary = "Delete the notifications matching a query",
            description = """
                    Not supported for notifications: the endpoint answers 501 once it has found at least one match, and 404 when nothing matches.

                    For example, `_s=notifyId=gt=1` or `_s=node.label==loopback-001`. The key property is `notifyId`; `_s=id==...` fails with 500.""",
            operationId = "notificationsDeleteMany")
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
    @Operation(summary = "Delete one notification",
            description = """
                    Answered with 501 once the identifier resolves, and 404 when it does not.""",
            operationId = "notificationsDelete")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = """
                    No notification has that identifier. The response has no body."""),
            @ApiResponse(responseCode = "501", description = """
                    Notifications do not support deletion. The response has no body.""")
    })
    public Response delete(final SecurityContext securityContext, final UriInfo uriInfo,
            @Parameter(description = """
                    Database identifier of the notification.""",
                    required = true, example = "2615")
            final Integer id) {
        return super.delete(securityContext, uriInfo, id);
    }
}
