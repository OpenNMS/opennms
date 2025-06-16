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
package org.opennms.netmgt.bsm.service.model.functions.reduce;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class ThresholdResultExplanation {
    private Threshold function;
    private List<Status> weightStatuses;
    private Status result;
    private Map<Status, Integer> hitsByStatus;
    private Collection<GraphEdge> edges;
    private Map<GraphEdge, GraphVertex> graphEdgeToGraphVertexMapping;

    public void setFunction(Threshold function) {
        this.function = function;
    }

    public Threshold getFunction() {
        return function;
    }

    public void setWeightStatuses(List<Status> weightStatuses) {
        this.weightStatuses = weightStatuses;
    }

    public List<Status> getWeightStatuses() {
        return weightStatuses;
    }

    public void setStatus(Status result) {
        this.result = result;
    }

    public void setHitsByStatus(Map<Status, Integer> hitsByStatus) {
        this.hitsByStatus = hitsByStatus;
    }

    public Map<Status, Integer> getHitsByStatus() {
        return hitsByStatus;
    }

    public Status getStatus() {
        return result;
    }

    public void setGraphEdges(Collection<GraphEdge> edges) {
        this.edges = edges;
    }

    public Collection<GraphEdge> getGraphEdges() {
        return edges;
    }

    public double getWeightFactor(GraphEdge eachEdge) {
        int weightSum = getWeightSum();
        return (double) eachEdge.getWeight() / (double) weightSum;
    }

    public double getStatusFactor(GraphEdge eachEdge, Status status) {
        if (eachEdge.getStatus().isGreaterThanOrEqual(status)) {
            return getWeightFactor(eachEdge);
        }
        return 0;
    }

    public double getStatusResult(Status status) {
        if (getHitsByStatus().get(status) != null) {
            return (double) getHitsByStatus().get(status) / (double) getWeightStatuses().size();
        }
        return 0;
    }

    public void setGraphEdgeToGraphVertexMapping(Map<GraphEdge, GraphVertex> graphEdgeToGraphVertexMapping) {
        this.graphEdgeToGraphVertexMapping = graphEdgeToGraphVertexMapping;
    }

    public Map<GraphEdge, GraphVertex> getGraphEdgeToGraphVertexMapping() {
        return graphEdgeToGraphVertexMapping;
    }

    public GraphVertex getGraphVertex(GraphEdge graphEdge) {
        return graphEdgeToGraphVertexMapping.get(graphEdge);
    }

    public int getWeightSum() {
        return getGraphEdges().stream().mapToInt(GraphEdge::getWeight).sum();
    }

    public double getWeightSumFactor() {
        return getGraphEdges().stream().mapToDouble(this::getWeightFactor).sum();
    }
}
