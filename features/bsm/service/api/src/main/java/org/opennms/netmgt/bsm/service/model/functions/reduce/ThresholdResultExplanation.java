/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
