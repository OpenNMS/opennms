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

package org.opennms.netmgt.graph.domain.simple;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.graph.api.info.NodeInfo;

public class TestObjectCreator {

    public final static String NAMESPACE = TestObjectCreator.class.getSimpleName();
    private final static AtomicInteger ID_SUPPLIER = new AtomicInteger(1);

    public static SimpleDomainVertex createVertex() {
        return createVertex(NAMESPACE, Integer.toString(ID_SUPPLIER.getAndIncrement()));
    }

    public static SimpleDomainVertex createVertex(String namespace, String id) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);

        Integer nodeId = ID_SUPPLIER.getAndIncrement();
        NodeInfo nodeInfo = NodeInfo.builder()
                .id(nodeId)
                .label("Node"+nodeId).build();
        return createVertex(namespace, id, nodeInfo);
    }

    public static SimpleDomainVertex createVertex(String namespace, String id, NodeInfo nodeInfo) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(id);
        Objects.requireNonNull(nodeInfo);
        return SimpleDomainVertex.builder()
                .namespace(namespace)
                .id(id)
                .label("SimpleVertex-" + namespace + "-" + id)
                .nodeInfo(nodeInfo)
                .build();
    }

    public static SimpleDomainEdge createEdge(SimpleDomainVertex sourceVertex, SimpleDomainVertex targetVertex) {
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);
        return createEdge(NAMESPACE, sourceVertex, targetVertex);
    }

    public static SimpleDomainEdge createEdge(String namespace, SimpleDomainVertex sourceVertex, SimpleDomainVertex targetVertex) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(sourceVertex);
        Objects.requireNonNull(targetVertex);
        return SimpleDomainEdge.builder()
                .namespace(namespace)
                .source(sourceVertex.getVertexRef())
                .target(targetVertex.getVertexRef())
                .label("SimpleEdge-" + namespace + "-" + sourceVertex.getVertexRef() + "->" + targetVertex.getVertexRef())
                .build();
    }
}
