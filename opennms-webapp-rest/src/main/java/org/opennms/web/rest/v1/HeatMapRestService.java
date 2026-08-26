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
package org.opennms.web.rest.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.HeatMapDTOCollection;
import org.opennms.netmgt.model.HeatMapDTOItem;
import org.opennms.netmgt.model.HeatMapElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component("heatMapRestService")
@Path("heatmap")
@Tag(name = "Heatmap", description = """
        Heatmap API.

        Every operation returns the same envelope: a `children` array of boxes, each with an `id` (the
        entity name), an `elementId` (the numeric id of the entity, or 0 when the grouping column has no
        id), a single-element `color` array and a single-element `size` array. `color` and `size` are
        arrays only because of the wire format; one value per box is all that is ever produced.

        `size` is the box's share of the total number of monitored services across the boxes that survived
        filtering, so the sizes in one response sum to 1. Entities with no monitored services are omitted
        entirely, which means an empty `children` array is the normal answer for an entity that exists but
        carries nothing monitored.

        `color` differs between the two families. For the `outages` operations it is the fraction of
        services currently down, from 0.0 to 1.0. For the `alarms` operations it is a step value derived
        from the highest unresolved alarm severity on the entity: 0.0 for normal, cleared, indeterminate or
        no alarm, 0.1 warning, 0.2 minor, 0.4 major, 1.0 critical.

        The three top-level groupings honour regular-expression filters taken from system properties, each
        defaulting to `.*`: `org.opennms.heatmap.categoryFilter`, `org.opennms.heatmap.foreignSourceFilter`
        and `org.opennms.heatmap.serviceFilter`. The filter is matched against the whole entity name. The
        per-entity node drilldowns apply no filter. Setting `org.opennms.heatmap.onlyUnacknowledged` to
        true makes the `alarms` operations skip acknowledged alarms.""")
public class HeatMapRestService extends OnmsRestService {
    /**
     * Property and default value for category filtering
     */
    private static final String CATEGORY_FILTER_PROPERTY_KEY = "org.opennms.heatmap.categoryFilter";
    private static final String CATEGORY_FILTER_PROPERTY_DEFAULT = ".*";
    /**
     * Property and default value for foreign source filtering
     */
    private static final String FOREIGNSOURCE_FILTER_PROPERTY_KEY = "org.opennms.heatmap.foreignSourceFilter";
    private static final String FOREIGNSOURCE_FILTER_PROPERTY_DEFAULT = ".*";
    /**
     * Property and default value for service filtering
     */
    private static final String SERVICE_FILTER_PROPERTY_KEY = "org.opennms.heatmap.serviceFilter";
    private static final String SERVICE_FILTER_PROPERTY_DEFAULT = ".*";
    /**
     * Property and default value for handling only unacknowledged alarms
     */
    private static final String ONLY_UNACKNOWLEDGED_PROPERTY_KEY = "org.opennms.heatmap.onlyUnacknowledged";
    private static final String ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT = "false";

    private static final Logger LOG = LoggerFactory.getLogger(HeatMapRestService.class);

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private AlarmDao m_alarmDao;

    /**
     * Transforms a list of heatmap elements to a json map.
     *
     * @param heatMapElements the list of heatmap elements
     * @return the map for the json response
     */
    private HeatMapDTOCollection transformResults(List<HeatMapElement> heatMapElements, String filter) {
        /**
         * the item list
         */
        final List<HeatMapDTOItem> itemList = new ArrayList<>();

        /**
         * Helper field for sizes
         */
        HashMap<String, Integer> elementSizes = new HashMap<>();

        /**
         * counter for total of services
         */
        int totalServices = 0;

        /**
         * iterate over the heatmap elements and increase the total
         * number of services. This is later used to compute the sizes
         * for the heatmap boxes...
         */
        for (HeatMapElement heatMapElement : heatMapElements) {
            if (heatMapElement.getServicesTotal() > 0) {
                /**
                 * Apply filter here if not null
                 */
                if (filter == null || heatMapElement.getName().matches(filter)) {
                    elementSizes.put(heatMapElement.getName(), heatMapElement.getServicesTotal());

                    HeatMapDTOItem heatMapDTOItem = new HeatMapDTOItem();
                    heatMapDTOItem.setId(heatMapElement.getName());
                    heatMapDTOItem.setElementId(heatMapElement.getId());
                    heatMapDTOItem.setColor(Lists.newArrayList(heatMapElement.getColor()));
                    itemList.add(heatMapDTOItem);

                    totalServices += heatMapElement.getServicesTotal();
                }
            }
        }

        /**
         * now iterate over the results and set the size attribute for
         * each entry...
         */
        for (HeatMapDTOItem heatMapDTOItem : itemList) {
            int servicesInEntity = elementSizes.get(heatMapDTOItem.getId());
            double size = (double) servicesInEntity / (double) totalServices;
            heatMapDTOItem.setSize(Lists.newArrayList(Double.valueOf(size)));
        }

        /**
         * create the "outer" map and add the list to it...
         */
        HeatMapDTOCollection heatMapDTOCollection = new HeatMapDTOCollection();
        heatMapDTOCollection.setHeatMapDTOItems(itemList);

        return heatMapDTOCollection;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/categories")
    @Operation(
            summary = "Outage heatmap by surveillance category",
            description = """
        One box per surveillance category that has at least one monitored service, coloured by the fraction of\n        services in the category that are currently down. Filtered by `org.opennms.heatmap.categoryFilter`.""",
            operationId = "getOutageHeatMapByCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the surveillance categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "Routers",
                          "elementId": 1,
                          "color": [
                            0.3333333333333333
                          ],
                          "size": [
                            0.007334963325183374
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response outagesByCategories() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(CATEGORY_FILTER_PROPERTY_KEY, CATEGORY_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/foreignSources")
    @Operation(
            summary = "Outage heatmap by foreign source",
            description = """
        One box per requisition (foreign source) that has at least one monitored service, coloured by the\n        fraction of services in it that are currently down. `elementId` is 0 because a foreign source has no\n        numeric id. Filtered by `org.opennms.heatmap.foreignSourceFilter`.""",
            operationId = "getOutageHeatMapByForeignSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the foreign sources.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-lab",
                          "elementId": 0,
                          "color": [
                            0.00392156862745098
                          ],
                          "size": [
                            1.0
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response outagesByForeignsources() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.foreignsource", null, null, null, "node.foreignsource");
        return Response.ok(transformResults(heatMapElements, System.getProperty(FOREIGNSOURCE_FILTER_PROPERTY_KEY, FOREIGNSOURCE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/monitoredServices")
    @Operation(
            summary = "Outage heatmap by monitored service",
            description = """
        One box per service type that is monitored somewhere, coloured by the fraction of instances of that\n        service that are currently down. Filtered by `org.opennms.heatmap.serviceFilter`.""",
            operationId = "getOutageHeatMapByMonitoredService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the monitored service types.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "SNMP",
                          "elementId": 3,
                          "color": [
                            1.0
                          ],
                          "size": [
                            0.00392156862745098
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response outagesByServices() throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("service.servicename", "service.serviceid", null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(SERVICE_FILTER_PROPERTY_KEY, SERVICE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/nodesByCategory/{category}")
    @Operation(
            summary = "Outage heatmap of the nodes in a category",
            description = """
        One box per node in the given surveillance category, coloured by the fraction of that node's monitored\n        services which are currently down. A category that does not exist, or whose nodes have nothing\n        monitored, yields an empty `children` array rather than a 404.""",
            operationId = "getOutageHeatMapForNodesByCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the nodes in the category, or an empty array.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-001",
                          "elementId": 2,
                          "color": [
                            0.5
                          ],
                          "size": [
                            0.6666666666666666
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response outagesOfNodesByCategory(@Parameter(description = "Surveillance category name, matched exactly. Case sensitive.", required = true, example = "Routers")
            @PathParam("category") final String category) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "categories.categoryname", category);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/nodesByForeignSource/{foreignSource}")
    @Operation(
            summary = "Outage heatmap of the nodes in a foreign source",
            description = """
        One box per node in the given requisition, coloured by the fraction of that node's monitored services\n        which are currently down. A foreign source that does not exist yields an empty `children` array\n        rather than a 404.""",
            operationId = "getOutageHeatMapForNodesByForeignSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the nodes in the foreign source, or an empty array.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-001",
                          "elementId": 2,
                          "color": [
                            0.5
                          ],
                          "size": [
                            0.00784313725490196
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response outagesOfNodesByForeignSource(@Parameter(description = "Foreign source (requisition) name, matched exactly. Case sensitive.", required = true, example = "loopback-lab")
            @PathParam("foreignSource") final String foreignSource) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "node.foreignsource", foreignSource);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("outages/nodesByMonitoredService/{monitoredService}")
    @Operation(
            summary = "Outage heatmap of the nodes running a service",
            description = """
        One box per node on which the given service type is monitored, coloured by the fraction of that node's\n        monitored services which are currently down. A service name that is not monitored anywhere yields an\n        empty `children` array rather than a 404.""",
            operationId = "getOutageHeatMapForNodesByMonitoredService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the nodes running the service, or an empty array.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-001",
                          "elementId": 2,
                          "color": [
                            1.0
                          ],
                          "size": [
                            1.0
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response outagesOfNodesByService(@Parameter(description = "Service type name as it appears in the service table, matched exactly. Case sensitive.", required = true, example = "SNMP")
            @PathParam("monitoredService") final String monitoredService) throws IOException {
        final List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", "service.servicename", monitoredService);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/categories")
    @Operation(
            summary = "Alarm heatmap by surveillance category",
            description = """
        One box per surveillance category that has at least one monitored service, coloured by the highest\n        unresolved alarm severity in the category. Filtered by `org.opennms.heatmap.categoryFilter`.""",
            operationId = "getAlarmHeatMapByCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the surveillance categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "Routers",
                          "elementId": 1,
                          "color": [
                            0.2
                          ],
                          "size": [
                            0.007334963325183374
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response alarmsByCategories() throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", processAcknowledged, null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(CATEGORY_FILTER_PROPERTY_KEY, CATEGORY_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/foreignSources")
    @Operation(
            summary = "Alarm heatmap by foreign source",
            description = """
        One box per requisition (foreign source) that has at least one monitored service, coloured by the\n        highest unresolved alarm severity in it. `elementId` is 0 because a foreign source has no numeric id.\n        Filtered by `org.opennms.heatmap.foreignSourceFilter`.""",
            operationId = "getAlarmHeatMapByForeignSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the foreign sources.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-lab",
                          "elementId": 0,
                          "color": [
                            0.2
                          ],
                          "size": [
                            1.0
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response alarmsByForeignsources() throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.foreignsource", null, processAcknowledged, null, null, "node.foreignsource");
        return Response.ok(transformResults(heatMapElements, System.getProperty(FOREIGNSOURCE_FILTER_PROPERTY_KEY, FOREIGNSOURCE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/monitoredServices")
    @Operation(
            summary = "Alarm heatmap by monitored service",
            description = """
        One box per service type that is monitored somewhere, coloured by the highest unresolved alarm severity\n        recorded against it. Filtered by `org.opennms.heatmap.serviceFilter`.""",
            operationId = "getAlarmHeatMapByMonitoredService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the monitored service types.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "SNMP",
                          "elementId": 3,
                          "color": [
                            0.2
                          ],
                          "size": [
                            0.00392156862745098
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response alarmsByServices() throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("service.servicename", "service.serviceid", processAcknowledged, null, null);
        return Response.ok(transformResults(heatMapElements, System.getProperty(SERVICE_FILTER_PROPERTY_KEY, SERVICE_FILTER_PROPERTY_DEFAULT))).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/nodesByCategory/{category}")
    @Operation(
            summary = "Alarm heatmap of the nodes in a category",
            description = """
        One box per node in the given surveillance category, coloured by the highest unresolved alarm severity\n        on that node. A category that does not exist, or whose nodes have nothing monitored, yields an empty\n        `children` array rather than a 404.""",
            operationId = "getAlarmHeatMapForNodesByCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the nodes in the category, or an empty array.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-001",
                          "elementId": 2,
                          "color": [
                            0.2
                          ],
                          "size": [
                            0.6666666666666666
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response alarmsOfNodesByCategory(@Parameter(description = "Surveillance category name, matched exactly. Case sensitive.", required = true, example = "Routers")
            @PathParam("category") final String category) throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", processAcknowledged, "categories.categoryname", category);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/nodesByForeignSource/{foreignSource}")
    @Operation(
            summary = "Alarm heatmap of the nodes in a foreign source",
            description = """
        One box per node in the given requisition, coloured by the highest unresolved alarm severity on that\n        node. A foreign source that does not exist yields an empty `children` array rather than a 404.""",
            operationId = "getAlarmHeatMapForNodesByForeignSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the nodes in the foreign source, or an empty array.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-001",
                          "elementId": 2,
                          "color": [
                            0.2
                          ],
                          "size": [
                            0.00784313725490196
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response alarmsOfNodesByForeignSource(@Parameter(description = "Foreign source (requisition) name, matched exactly. Case sensitive.", required = true, example = "loopback-lab")
            @PathParam("foreignSource") final String foreignSource) throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", processAcknowledged, "node.foreignsource", foreignSource);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Path("alarms/nodesByMonitoredService/{monitoredService}")
    @Operation(
            summary = "Alarm heatmap of the nodes running a service",
            description = """
        One box per node on which the given service type is monitored, coloured by the highest unresolved alarm\n        severity on that node. A service name that is not monitored anywhere yields an empty `children` array\n        rather than a 404.""",
            operationId = "getAlarmHeatMapForNodesByMonitoredService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Boxes for the nodes running the service, or an empty array.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "children": [
                        {
                          "id": "loopback-001",
                          "elementId": 2,
                          "color": [
                            0.2
                          ],
                          "size": [
                            1.0
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = HeatMapDTOCollection.class))
                    })
    })
    public Response alarmsOfNodesByService(@Parameter(description = "Service type name as it appears in the service table, matched exactly. Case sensitive.", required = true, example = "SNMP")
            @PathParam("monitoredService") final String monitoredService) throws IOException {
        boolean processAcknowledged = !Boolean.parseBoolean(System.getProperty(ONLY_UNACKNOWLEDGED_PROPERTY_KEY, ONLY_UNACKNOWLEDGED_PROPERTY_DEFAULT));
        final List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("node.nodelabel", "node.nodeid", processAcknowledged, "service.servicename", monitoredService);
        return Response.ok(transformResults(heatMapElements, null)).build();
    }
}
