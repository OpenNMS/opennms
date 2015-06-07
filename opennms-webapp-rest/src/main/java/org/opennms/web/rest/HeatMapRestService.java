/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.PerRequest;
import org.json.JSONObject;
import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.provision.persist.ForeignSourceService;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.web.svclayer.ManualProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
@PerRequest
@Scope("prototype")
@Path("heatmap")
public class HeatMapRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(HeatMapRestService.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    private ManualProvisioningService m_provisioningService;

    @Autowired
    private ForeignSourceService m_foreignSourceService;

    /**
     * Returns the node ids of the nodes with outages.
     *
     * @return the set of node ids
     */
    private Set<Integer> getOutagesNodeIds() {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsOutage.class);
        criteriaBuilder.alias("monitoredService", "monitoredService", Alias.JoinType.LEFT_JOIN);
        criteriaBuilder.alias("monitoredService.ipInterface", "ipInterface", Alias.JoinType.LEFT_JOIN);
        criteriaBuilder.alias("ipInterface.node", "node", Alias.JoinType.LEFT_JOIN);
        criteriaBuilder.alias("ipInterface.snmpInterface", "snmpInterface", Alias.JoinType.LEFT_JOIN);
        criteriaBuilder.alias("monitoredService.serviceType", "serviceType", Alias.JoinType.LEFT_JOIN);
        criteriaBuilder.isNull("ifRegainedService");

        List<OnmsOutage> onmsOutages = m_outageDao.findMatching(criteriaBuilder.toCriteria());

        Set<Integer> nodeIds = new TreeSet<>();

        for (OnmsOutage onmsOutage : onmsOutages) {
            nodeIds.add(onmsOutage.getNodeId());
        }

        return nodeIds;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("categories")
    public Response categories() throws IOException {
        /**
         * the item list
         */
        final List<Map<String, Object>> itemList = new ArrayList<>();

        /**
         * retrieve the node ids for nodes with outages
         */
        Set<Integer> nodeIds = getOutagesNodeIds();

        /**
         * Helper field for sizes
         */
        HashMap<String, Integer> categorySizes = new HashMap<>();

        /**
         * counter for total of nodes
         */
        int nodesTotal = 0;

        /**
         * create data based on categories
         */
        List<OnmsCategory> categoryList = m_categoryDao.findAll();

        for (OnmsCategory onmsCategory : categoryList) {
            CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class);

            criteriaBuilder.alias("categories", "category");
            criteriaBuilder.eq("category.id", onmsCategory.getId());

            int nodesInCategory = m_nodeDao.countMatching(criteriaBuilder.toCriteria());

            int nodesWithOutages = 0;

            if (nodeIds.size() > 0) {
                criteriaBuilder.in("id", nodeIds);
                nodesWithOutages = m_nodeDao.countMatching(criteriaBuilder.toCriteria());
            }

            System.out.println(onmsCategory.getName() + " -> Nodes: " + nodesWithOutages + "/" + nodesInCategory);

            if (nodesInCategory > 0) {

                Double color = 0.0;

                if (nodesWithOutages == 1 && nodesInCategory > nodesWithOutages) {
                    color = 0.5;
                } else {
                    if (nodesWithOutages > 1 || nodesInCategory == nodesWithOutages) {
                        color = 1.0;
                    }
                }

                categorySizes.put(onmsCategory.getName(), nodesInCategory);

                Map<String, Object> item = new HashMap<>();
                item.put("id", onmsCategory.getName());
                item.put("color", Lists.newArrayList(color));

                itemList.add(item);

                nodesTotal += nodesInCategory;
            }
        }

        for (Map<String, Object> map : itemList) {
            int nodesInCategory = categorySizes.get(map.get("id"));
            double size = (double) nodesInCategory / (double) nodesTotal;
            map.put("size", Lists.newArrayList(Double.valueOf(size)));
        }

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("children", itemList);
        final JSONObject jo = new JSONObject(map);
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Retrieves the defined foreignSource names.
     *
     * @return the set of foreignSource names
     */
    private Set<String> getForeignSourceNames() {
        Set<String> foreignSourceNames = new TreeSet<String>();

        for (Requisition requisition : m_provisioningService.getAllGroups()) {
            if (requisition != null) {
                foreignSourceNames.add(requisition.getForeignSource());
            }
        }

        for (ForeignSource foreignSource : m_foreignSourceService.getAllForeignSources()) {
            if (!foreignSource.isDefault()) {
                foreignSourceNames.add(foreignSource.getName());
            }
        }

        return foreignSourceNames;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("foreignSources")
    public Response foreignsources() throws IOException {
        /**
         * the item list
         */
        final List<Map<String, Object>> itemList = new ArrayList<>();

        /**
         * retrieve the node ids for nodes with outages
         */
        Set<Integer> nodeIds = getOutagesNodeIds();

        /**
         * Helper field for sizes
         */
        HashMap<String, Integer> foreignSourceSizes = new HashMap<>();

        /**
         * counter for total of nodes
         */
        int nodesTotal = 0;

        /**
         * create data based on foreignSource names
         */
        Set<String> foreignSourceNames = getForeignSourceNames();

        for (String foreignSourceName : foreignSourceNames) {
            int nodesInForeignSource = m_nodeDao.getNodeCountForForeignSource(foreignSourceName);

            int nodesWithOutages = 0;

            if (nodeIds.size() > 0) {
                CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNode.class);

                criteriaBuilder.eq("foreignSource", foreignSourceName);
                criteriaBuilder.in("id", nodeIds);

                nodesWithOutages = m_nodeDao.countMatching(criteriaBuilder.toCriteria());
            }

            System.out.println(foreignSourceName + " -> Nodes: " + nodesWithOutages + "/" + nodesInForeignSource);

            if (nodesInForeignSource > 0) {

                Double color = 0.0;

                if (nodesWithOutages == 1 && nodesInForeignSource > nodesWithOutages) {
                    color = 0.5;
                } else {
                    if (nodesWithOutages > 1 || nodesInForeignSource == nodesWithOutages) {
                        color = 1.0;
                    }
                }

                foreignSourceSizes.put(foreignSourceName, nodesInForeignSource);

                Map<String, Object> item = new HashMap<>();
                item.put("id", foreignSourceName);
                item.put("color", Lists.newArrayList(color));

                itemList.add(item);

                nodesTotal += nodesInForeignSource;
            }
        }

        for (Map<String, Object> map : itemList) {
            int nodesInForeignSource = foreignSourceSizes.get(map.get("id"));
            double size = (double) nodesInForeignSource / (double) nodesTotal;
            map.put("size", Lists.newArrayList(Double.valueOf(size)));
        }

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("children", itemList);
        final JSONObject jo = new JSONObject(map);
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("nodesByCategory/{category}")
    public Response nodesByCategory(@PathParam("category") final String category) throws IOException {
        List<Map<String, Object>> itemList = getNodeItemData(m_nodeDao.findByCategory(m_categoryDao.findByName(category)));

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("children", itemList);
        final JSONObject jo = new JSONObject(map);
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("nodesByForeignSource/{foreignSource}")
    public Response nodesByForeignSource(@PathParam("foreignSource") final String foreignSource) throws IOException {
        List<Map<String, Object>> itemList = getNodeItemData(m_nodeDao.findByForeignSource(foreignSource));

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("children", itemList);
        final JSONObject jo = new JSONObject(map);
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }

    private List<Map<String, Object>> getNodeItemData(List<OnmsNode> onmsNodeList) {
        /**
         * the item list
         */
        final List<Map<String, Object>> itemList = new ArrayList<>();

        /**
         * retrieve the node ids for nodes with outages
         */
        Set<Integer> nodeIds = getOutagesNodeIds();

        /**
         * Helper field for sizes
         */
        HashMap<String, Integer> interfaceCounts = new HashMap<>();

        /**
         * counter for total of nodes
         */
        int interfacesTotal = 0;

        for (OnmsNode onmsNode : onmsNodeList) {
            if (onmsNode.getIpInterfaces().size() > 0) {
                Map<String, Object> item = new HashMap<>();

                item.put("id", onmsNode.getLabel());
                item.put("nodeId", onmsNode.getId());
                item.put("color", Lists.newArrayList(nodeIds.contains(onmsNode.getId()) ? 1.0 : 0.0));

                interfaceCounts.put(onmsNode.getLabel(), onmsNode.getIpInterfaces().size());
                interfacesTotal += onmsNode.getIpInterfaces().size();

                itemList.add(item);
            }
        }

        for (Map<String, Object> map : itemList) {
            int nodeInterfaceCount = interfaceCounts.get(map.get("id"));
            double size = (double) nodeInterfaceCount / (double) interfacesTotal;
            map.put("size", Lists.newArrayList(Double.valueOf(size)));
        }

        return itemList;
    }
}

