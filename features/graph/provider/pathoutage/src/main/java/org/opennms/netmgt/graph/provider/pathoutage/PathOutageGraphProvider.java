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
package org.opennms.netmgt.graph.provider.pathoutage;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Builds a read-only graph from the node parent / critical-path relationships
 * ({@link OnmsNode#getParent()} -- the {@code nodeParentID} column), the same
 * data the legacy Vaadin "Path Outage" topology renders. Each participating
 * node becomes a vertex and each parent points to its child with a directed
 * edge, so the result is the node-parent forest (one tree per top-level node).
 *
 * <p>Only nodes that take part in a parent-child relationship are included --
 * a node with neither a parent nor any children would render as an isolated
 * dot that conveys nothing here. On an installation where no node parents are
 * set the graph is empty, and the UI shows its discovered-empty state.</p>
 *
 * <p>Registered as a bare {@link GraphProvider}; the graph service wraps it in
 * a single-graph container whose id defaults to the namespace, so it is served
 * at {@code /api/v2/graphs/pathoutage/pathoutage}.</p>
 */
public class PathOutageGraphProvider implements GraphProvider {

    public static final String NAMESPACE = "pathoutage";
    private static final String LABEL = "Path Outage";
    private static final String DESCRIPTION =
            "Node parent / critical-path hierarchy derived from each node's parent relationship.";

    private final NodeDao nodeDao;
    private final SessionUtils sessionUtils;

    public PathOutageGraphProvider(final NodeDao nodeDao, final SessionUtils sessionUtils) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public ImmutableGraph<?, ?> loadGraph() {
        // Two lightweight reads (every id->label, then the parented nodes) share one
        // read-only transaction so they see a consistent snapshot. The parent id is
        // read straight off the nodeParentID column via getNodeParentId() rather than
        // by dereferencing the LAZY getParent() proxy: a node can retain a
        // nodeParentID whose parent row was since deleted (NMS-19971), and touching
        // the proxy for that dangling reference throws ObjectNotFoundException.
        return sessionUtils.withReadOnlyTransaction(() -> {
            final GenericGraph.GenericGraphBuilder builder = GenericGraph.builder()
                    .graphInfo(getGraphInfo())
                    .id(NAMESPACE)
                    // Resolve node + alarm-based status for the vertices, like the other
                    // node-backed providers; the SPA also colors by nodeID independently.
                    .property(GenericProperties.Enrichment.RESOLVE_NODES, true)
                    .property(GenericProperties.Enrichment.DEFAULT_STATUS, true);

            final Map<Integer, String> labels = nodeDao.getAllLabelsById();

            // Only nodes that actually have a parent matter here; the top-level
            // parents are pulled in as edge endpoints below. Filtering in the
            // query keeps parentless nodes out of memory and the loop.
            final List<OnmsNode> nodes = nodeDao.findMatching(
                    new CriteriaBuilder(OnmsNode.class).isNotNull("parent").toCriteria());

            // Collect the participating node ids (each child plus its parent) and
            // remember the parent->child pairs. child -> parent is value-based
            // (unlike a Set of arrays), and a child has exactly one parent, so
            // duplicates are structurally impossible.
            final Set<Integer> participating = new LinkedHashSet<>();
            final Map<Integer, Integer> parentByChild = new LinkedHashMap<>();
            for (final OnmsNode node : nodes) {
                final Integer parentId = node.getNodeParentId();
                if (parentId == null || labels.get(parentId) == null) {
                    // No parent id, or a dangling nodeParentID whose parent row was
                    // deleted (NMS-19971) -- nothing to connect to, so skip it.
                    continue;
                }
                final int childId = node.getId();
                participating.add(childId);
                participating.add(parentId);
                parentByChild.put(childId, parentId);
            }

            // Vertices first (an edge requires its endpoints to already be present).
            for (final Integer nodeId : participating) {
                builder.addVertex(GenericVertex.builder()
                        .namespace(NAMESPACE)
                        .id(String.valueOf(nodeId))
                        .property(GenericProperties.LABEL, labels.get(nodeId))
                        .property(GenericProperties.NODE_ID, String.valueOf(nodeId))
                        .build());
            }

            // No explicit edge id: GenericEdge derives a deterministic one from
            // source->target, stable across loads (a counter would depend on
            // query ordering).
            for (final Map.Entry<Integer, Integer> pc : parentByChild.entrySet()) {
                builder.addEdge(GenericEdge.builder()
                        .namespace(NAMESPACE)
                        .source(NAMESPACE, String.valueOf(pc.getValue()))
                        .target(NAMESPACE, String.valueOf(pc.getKey()))
                        .build());
            }

            // Show the whole hierarchy by default; the SPA frames it to fit.
            builder.focus().all().apply();
            return builder.build();
        });
    }

    @Override
    public GraphInfo getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(NAMESPACE);
        graphInfo.setLabel(LABEL);
        graphInfo.setDescription(DESCRIPTION);
        return graphInfo;
    }
}
