/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.geolocation.api.GeolocationInfo;
import org.opennms.features.geolocation.api.GeolocationQueryBuilder;
import org.opennms.features.geolocation.api.GeolocationService;
import org.opennms.features.geolocation.api.GeolocationSeverity;
import org.opennms.features.geolocation.api.NodeInfo;
import org.opennms.features.geolocation.api.StatusCalculationStrategy;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MapNode;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMapState;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.NodeIdSelectionRpc;
import org.opennms.netmgt.dao.api.NodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.UI;

public class NodeMapComponent extends AbstractComponent implements GeoAssetProvider {
    private static final long serialVersionUID = 3L;
    private static final Logger LOG = LoggerFactory.getLogger(NodeMapComponent.class);

    private NodeIdSelectionRpc m_rpc = new NodeIdSelectionRpc() {
        private static final long serialVersionUID = 3263343063196874423L;
        @Override
        public void setSelectedNodes(final List<Integer> nodeIds) {
            ((NodeMapsApplication)UI.getCurrent()).setFocusedNodes(nodeIds);
        }
        @Override
        public void refresh() {
            ((NodeMapsApplication)UI.getCurrent()).refresh();
        }
    };

    private NodeDao m_nodeDao;
    private GeolocationService geolocationService;
    private Boolean m_aclsEnabled = false;
    private NodeMapConfiguration configuration;
    private Map<Integer,MapNode> m_activeNodes = new HashMap<>();

    public NodeMapComponent() {
        registerRpc(m_rpc);
        m_aclsEnabled = Boolean.valueOf(System.getProperty("org.opennms.web.aclsEnabled", "false"));
    }

    public void setGeolocationService(GeolocationService geolocationService) {
        this.geolocationService = geolocationService;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Override
    public Collection<VertexRef> getNodesWithCoordinates() {
        final List<VertexRef> nodes = new ArrayList<>();
        for (final Map.Entry<Integer,MapNode> entry : m_activeNodes.entrySet()) {
            nodes.add(new AbstractVertex("nodes", entry.getKey().toString(), entry.getValue().getNodeLabel()));
        }
        return nodes;
    }

    public void refresh() {
        List<GeolocationInfo> locations = geolocationService.getLocations(new GeolocationQueryBuilder()
                .withIncludeAcknowledgedAlarms(false)
                .withStatusCalculationStrategy(StatusCalculationStrategy.Alarms)
                .withSeverity(GeolocationSeverity.Normal)
                .build());

        // apply acl filter if enabled
        if (m_aclsEnabled) {
            Map<Integer, String> nodes = m_nodeDao.getAllLabelsById();
            locations = locations.stream()
                    .filter(l -> nodes.containsKey(l.getNodeInfo().getNodeId()))
                    .collect(Collectors.toList());
        }

        // Convert
        m_activeNodes = locations.stream()
                .map(NodeMapComponent::createMapNode)
                .collect(Collectors.toMap(l -> Integer.valueOf(l.getNodeId()), Function.identity()));
        showNodes(m_activeNodes);
    }

    public void setMaxClusterRadius(final Integer radius) {
        getState().maxClusterRadius = radius;
    }

    public void setConfiguration(NodeMapConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        getState().tileServerUrl = configuration.getTileServerUrl();
        getState().tileLayerOptions = configuration.getOptions();
    }

    public void setSearchString(final String searchString) {
        LOG.debug("setSearchString(" + searchString + ")");
        getState().searchString = searchString;
    }

    public void setSelectedNodes(final List<Integer> nodeIds) {
        LOG.debug("setSelectedNodes(" + nodeIds + ")");
        getState().nodeIds = nodeIds;
    }


    public void setGroupByState(final boolean groupByState) {
        getState().groupByState = groupByState;
    }

    public void showNodes(final Map<Integer, MapNode> nodeEntries) {
        LOG.info("Updating map node list: {} entries.", nodeEntries.size());
        final List<MapNode> nodeEntryList = new ArrayList<>(nodeEntries.values());
        if (getState().nodes != null &&
                nodeEntryList.containsAll(getState().nodes) &&
                getState().nodes.containsAll(nodeEntryList)) {
            LOG.info("Skipping update. Map node list is unchanged.");
            return;
        }

        getState().nodes = new ArrayList<>(nodeEntries.values());
        LOG.info("Finished updating map node list.");
    }

    @Override
    protected NodeMapState getState() {
        return (NodeMapState) super.getState();
    }

    protected NodeIdSelectionRpc getRpc() {
        return m_rpc;
    }

    private static MapNode createMapNode(GeolocationInfo geolocationInfo) {
        final MapNode node = new MapNode();
        
        // Coordinates
        if (geolocationInfo.getCoordinates() != null) {
            node.setLatitude(geolocationInfo.getCoordinates().getLatitude());
            node.setLongitude(geolocationInfo.getCoordinates().getLongitude());
        }

        // Node Info
        final NodeInfo nodeInfo = geolocationInfo.getNodeInfo();
        node.setNodeId(String.valueOf(nodeInfo.getNodeId()));
        node.setNodeLabel(nodeInfo.getNodeLabel());
        node.setForeignSource(nodeInfo.getForeignSource());
        node.setForeignId(nodeInfo.getForeignId());
        node.setIpAddress(nodeInfo.getIpAddress());
        node.setDescription(nodeInfo.getDescription());
        node.setMaintcontract(nodeInfo.getMaintcontract());
        node.setCategories(new ArrayList<>(nodeInfo.getCategories()));
        
        // Severity
        node.setSeverityLabel(geolocationInfo.getSeverityInfo().getLabel());
        node.setSeverity(String.valueOf(geolocationInfo.getSeverityInfo().getId()));
        
        // Count
        node.setUnackedCount(geolocationInfo.getAlarmUnackedCount());

        return node;
    }
}
