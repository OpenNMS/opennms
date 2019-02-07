/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.simple;

import java.util.Objects;

import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.aware.LocationAware;
import org.opennms.netmgt.graph.api.aware.NodeAware;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;

public class SimpleVertex implements Vertex, NodeAware, LocationAware {

    private final String id;
    private String namespace; // TODO MVR this should be enforced, shouldn't it?
    private String iconKey; // TODO MVR remove me
    private String tooltip; // TODO MVR remove me
    private String label;
    // Either nodeId as string or foreignSource:foreignId combination
    private String nodeRefString;

//    @Enrich(name="node", processor = NodeInfoEnrichmentProcessor.class)
    private NodeInfo nodeInfo;

    public SimpleVertex(String namespace, String id) {
        this.namespace = namespace;
        this.id = id;
    }

    public SimpleVertex(SimpleVertex copyMe) {
        this(copyMe.getNamespace(), copyMe.getId());
        setLabel(copyMe.getLabel());
        setNodeInfo(copyMe.getNodeInfo()); // TODO MVR also clone this
        setNodeRefString(copyMe.getNodeRefString());
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public GenericVertex asGenericVertex() {
        final GenericVertex vertex = new GenericVertex();
        vertex.setId(getId());
        vertex.setNamespace(getNamespace());
        if (getLabel() != null) {
            vertex.setProperty(GenericProperties.LABEL, getLabel());
        }
        if (getNodeRefString() != null) {
            vertex.setProperty(GenericProperties.NODE_REF, getNodeRefString());
        }
        return vertex;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

//    @Override
//    public NodeRef getNodeRef() {
//        return NodeRefs.from(getNodeRefString());
//    }

    public String getNodeRefString() {
        return nodeRefString;
    }

    public void setNodeRefString(String nodeRefString) {
        this.nodeRefString = nodeRefString;
    }

    @Override
    public String toString() {
        return asGenericVertex().toString();
    }

    @Override
    public String getLocation() {
        if (getNodeInfo() != null) {
            return getNodeInfo().getLocation();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVertex that = (SimpleVertex) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(iconKey, that.iconKey) &&
                Objects.equals(tooltip, that.tooltip) &&
                Objects.equals(label, that.label) &&
                Objects.equals(nodeRefString, that.nodeRefString) &&
                Objects.equals(nodeInfo, that.nodeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, namespace, iconKey, tooltip, label, nodeRefString, nodeInfo);
    }
}
