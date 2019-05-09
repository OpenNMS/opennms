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

package org.opennms.netmgt.graph.api.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.aware.LocationAware;
import org.opennms.netmgt.graph.api.aware.NodeAware;
import org.opennms.netmgt.graph.api.info.NodeInfo;

public class GenericVertex extends GenericElement implements Vertex, NodeAware, LocationAware {

    public GenericVertex(String namespace, String id) {
        super(namespace, id);
        Objects.requireNonNull(id, "id cannot be null");
    }

    public GenericVertex(String namespace, String id, Map<String, Object> properties) {
        super(new MapBuilder<String, Object>()
                .withProperties(properties)
                .withProperty(GenericProperties.NAMESPACE, namespace)
                .withProperty(GenericProperties.ID, id)
                .build());
        Objects.requireNonNull(getId(), "id cannot be null");
    }

    /** Copy constructor */
    public GenericVertex(GenericVertex copyMe){
        super(new HashMap<>(copyMe.properties));
    }

    /** Copy constructor with new namespace */
    public GenericVertex(GenericVertex copyMe, String namespace){
        super(new MapBuilder<String, Object>()
                .withProperties(copyMe.properties)
                .withProperty(GenericProperties.NAMESPACE, namespace).build());
    }

    public VertexRef getVertexRef() {
        return new VertexRef(this.getNamespace(), this.getId());
    }

//    // TODO MVR implement me
//    @Override
//    public NodeRef getNodeRef() {
//        String nodeId = getProperty(GenericProperties.NODE_ID);
//        String foreignSource = getProperty(GenericProperties.FOREIGN_SOURCE);
//        String foreignId = getProperty(GenericProperties.FOREIGN_ID);
//        if (nodeId != null) {
//            return NodeRefs.from(nodeId);
//        } else if (foreignSource != null && foreignId != null) {
//            return NodeRefs.from(foreignSource + ":" + foreignId);
//        }
//        return null;
//    }

    // TODO MVR implement me
    @Override
    public NodeInfo getNodeInfo() {
        return null;
//        final Optional<Object> first = graphProperties.getProperties().values().stream().filter(v -> v instanceof NodeInfo).findFirst();
//        if (first.isPresent()) {
//            return (NodeInfo) first.get();
//        }
//        return (NodeInfo) graphProperties.getComputedProperties().values().stream().filter(v -> v instanceof NodeInfo).findFirst().orElse(null);
    }

    @Override
    public GenericVertex asGenericVertex() {
        return this;
    }

    // TODO MVR implement me
    @Override
    public String getLocation() {
        return null;
    }
}
