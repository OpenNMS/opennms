/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.graph.api.info.NodeInfo;

/** */
public class TestObjectCreator {

    public final static String NAMESPACE = TestObjectCreator.class.getSimpleName();
    private final static AtomicInteger ID_SUPPLIER = new AtomicInteger(1);

    public static SimpleVertex createVertex() {
        return createVertex(NAMESPACE, Integer.toString(ID_SUPPLIER.getAndIncrement()));
    }

    public static SimpleVertex createVertex(String namespace, String id) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(ID_SUPPLIER.getAndIncrement());
        nodeInfo.setLabel("Node"+nodeInfo.getId());
        return createVertex(namespace, id, nodeInfo);
    }

    public static SimpleVertex createVertex(String namespace, String id, NodeInfo nodeInfo) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);
        Objects.requireNonNull(nodeInfo);
        final SimpleVertex vertex = new SimpleVertex(namespace, id);
        vertex.setLabel("SimpleVertex-" + namespace + "-" + id);
        vertex.setNodeInfo(nodeInfo);
        return vertex;
    }

    public static SimpleEdge createEdge(SimpleVertex sourceVertex, SimpleVertex targetVertex) {
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);
        return createEdge(NAMESPACE, sourceVertex, targetVertex);
    }

    public static SimpleEdge createEdge(String namespace, SimpleVertex sourceVertex, SimpleVertex targetVertex) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);
        SimpleEdge edge = new SimpleEdge(namespace, sourceVertex, targetVertex);
        edge.setLabel("SimpleEdge-" + namespace + "-" + edge.getId());
        return edge;
    }


}
