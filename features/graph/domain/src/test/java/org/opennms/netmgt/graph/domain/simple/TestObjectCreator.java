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
