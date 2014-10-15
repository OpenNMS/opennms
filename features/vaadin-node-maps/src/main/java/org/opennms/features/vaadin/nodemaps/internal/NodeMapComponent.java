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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MapNode;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMapState;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.NodeIdSelectionRpc;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.UI;

public class NodeMapComponent extends AbstractComponent {
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

    public NodeMapComponent() {
        registerRpc(m_rpc);
    }

    protected NodeIdSelectionRpc getRpc() {
        return m_rpc;
    }

    public void setGroupByState(final boolean groupByState) {
        getState().groupByState = groupByState;
    }

    public void showNodes(final Map<Integer, NodeEntry> nodeEntries) {
        LOG.info("Updating map node list: {} entries.", nodeEntries.size());

        final List<MapNode> nodes = new LinkedList<MapNode>();
        for (final NodeEntry node : nodeEntries.values()) {
            nodes.add(node.createNode());
        }
        getState().nodes = nodes;

        LOG.info("Finished updating map node list.");
    }

    @Override
    protected NodeMapState getState() {
        return (NodeMapState) super.getState();
    }

    public void setSearchString(final String searchString) {
        LOG.debug("setSearchString(" + searchString + ")");
        getState().searchString = searchString;
    }

    public void setSelectedNodes(final List<Integer> nodeIds) {
        LOG.debug("setSelectedNodes(" + nodeIds + ")");
        getState().nodeIds = nodeIds;
    }

    // TODO: combine this class and MapNode?
    protected static final class NodeEntry {

        private Float m_longitude;
        private Float m_latitude;
        private Integer m_nodeId;
        private String m_nodeLabel;
        private String m_foreignSource;
        private String m_foreignId;
        private String m_description;
        private String m_maintcontract;
        private String m_ipAddress;
        private OnmsSeverity m_severity = OnmsSeverity.NORMAL;
        private List<String> m_categories = new ArrayList<String>();
        private int m_unackedCount = 0;

        NodeEntry(final Integer nodeId, final String nodeLabel, final float longitude, final float latitude) {
            m_nodeId = nodeId;
            m_nodeLabel = nodeLabel;
            m_longitude = longitude;
            m_latitude = latitude;
        }

        public NodeEntry(final OnmsNode node) {
            final OnmsAssetRecord assetRecord = node.getAssetRecord();
            if (assetRecord != null && assetRecord.getGeolocation() != null) {
                final OnmsGeolocation geolocation = assetRecord.getGeolocation();
                m_longitude = geolocation.getLongitude();
                m_latitude  = geolocation.getLatitude();
            }

            m_nodeId        = node.getId();
            m_nodeLabel     = node.getLabel();
            m_foreignSource = node.getForeignSource();
            m_foreignId     = node.getForeignId();

            if (assetRecord != null) {
                m_maintcontract = assetRecord.getMaintcontract();
                m_description   = assetRecord.getDescription();
            }

            if (node.getPrimaryInterface() != null) {
                m_ipAddress = InetAddressUtils.str(node.getPrimaryInterface().getIpAddress());
            }

            if (node.getCategories() != null && node.getCategories().size() > 0) {
                for (final OnmsCategory category : node.getCategories()) {
                    m_categories.add(category.getName());
                }
            }
        }

        public Integer getNodeId() {
            return m_nodeId;
        }

        public String getNodeLabel() {
            return m_nodeLabel;
        }

        public void setSeverity(final OnmsSeverity severity) {
            m_severity = severity;
        }

        public MapNode createNode() {
            final MapNode node = new MapNode();

            node.setLatitude(m_latitude);
            node.setLongitude(m_longitude);
            node.setNodeId(String.valueOf(m_nodeId));
            node.setNodeLabel(m_nodeLabel);
            node.setForeignSource(m_foreignSource);
            node.setForeignId(m_foreignId);
            node.setIpAddress(m_ipAddress);
            node.setDescription(m_description);
            node.setMaintcontract(m_maintcontract);

            node.setSeverityLabel(m_severity.getLabel());
            node.setSeverity(String.valueOf(m_severity.getId()));
            node.setUnackedCount(m_unackedCount);

            node.setCategories(m_categories);
            return node;
        }

        public void setUnackedCount(final int unackedCount) {
            m_unackedCount = unackedCount;
        }
    }
}
