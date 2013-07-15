/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal;

import com.vaadin.ui.AbstractComponent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MapNode;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMapState;
import org.opennms.netmgt.model.*;

import java.util.*;

public class NodeMap extends AbstractComponent {

    private static final long serialVersionUID = 2L;

    public NodeMap() {
    }

    public void showNodes(final Map<Integer, NodeEntry> nodeEntries) {
        List<MapNode> nodes = new LinkedList<MapNode>();
        for (final NodeEntry node : nodeEntries.values()) {
            nodes.add(node.createNode());
        }
        getState().nodes = nodes;
    }

    @Override
    protected NodeMapState getState() {
        return (NodeMapState) super.getState();
    }

    public void setInitialSearchString(String searchString) {
        getState().initialSearch = searchString;
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

        public void setSeverity(final OnmsSeverity severity) {
            m_severity = severity;
        }

        public MapNode createNode(){
            MapNode node = new MapNode();
            node.setLatitude(m_latitude);
            node.setLongitude(m_longitude);
            if (m_nodeId != null) {
                node.setNodeId(m_nodeId.toString());
            }
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
