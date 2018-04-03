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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;
import org.opennms.netmgt.bsm.service.AlarmProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.StatusWithIndex;
import org.opennms.netmgt.bsm.service.model.StatusWithIndices;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ThresholdResultExplanation;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.bsm.service.model.graph.internal.BusinessServiceGraphImpl;
import org.opennms.netmgt.bsm.service.model.graph.internal.GraphAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class DefaultBusinessServiceStateMachine implements BusinessServiceStateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBusinessServiceStateMachine.class);
    public static final Status MIN_SEVERITY = Status.NORMAL;

    @Autowired
    private AlarmProvider m_alarmProvider;

    private final List<BusinessServiceStateChangeHandler> m_handlers = Lists.newArrayList();
    private final ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
    private BusinessServiceGraph m_g = new BusinessServiceGraphImpl(Collections.emptyList());

    @Override
    public void setBusinessServices(List<BusinessService> businessServices) {
        m_rwLock.writeLock().lock();
        try {
            // Create a new graph
            BusinessServiceGraph g = new BusinessServiceGraphImpl(businessServices);

            // Prime the graph with the state from the previous graph and
            // keep track of the new reductions keys
            Set<String> reductionsKeysToLookup = Sets.newHashSet();
            for (String reductionKey : g.getReductionKeys()) {
                GraphVertex reductionKeyVertex = m_g.getVertexByReductionKey(reductionKey);
                if (reductionKeyVertex != null) {
                    updateAndPropagateVertex(g, g.getVertexByReductionKey(reductionKey), reductionKeyVertex.getStatus());
                } else {
                    reductionsKeysToLookup.add(reductionKey);
                }
            }

            if (m_alarmProvider == null && reductionsKeysToLookup.size() > 0) {
                LOG.warn("There are one or more reduction keys to lookup, but no alarm provider is set.");
            } else {
                // Query the status of the reductions keys that were added
                // We do this so that we can immediately reflect the state of the new
                // graph without having to wait for calls to handleNewOrUpdatedAlarm()
                if (reductionsKeysToLookup.size() > 0) {
                    final Map<String, AlarmWrapper> lookup = m_alarmProvider.lookup(reductionsKeysToLookup);
                    for (Entry<String, AlarmWrapper> eachEntry : lookup.entrySet()) {
                        updateAndPropagateVertex(g, g.getVertexByReductionKey(eachEntry.getKey()), eachEntry.getValue().getStatus());
                    }
                }
            }
            m_g = g;
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    @Override
    public void handleNewOrUpdatedAlarm(AlarmWrapper alarm) {
        m_rwLock.writeLock().lock();
        try {
            // Recursively propagate the status
            updateAndPropagateVertex(m_g, m_g.getVertexByReductionKey(alarm.getReductionKey()), alarm.getStatus());
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    @Override
    public void handleAllAlarms(List<AlarmWrapper> alarms) {
        final Set<String> reductionKeysFromGivenAlarms = new HashSet<>(alarms.size());
        m_rwLock.writeLock().lock();
        try {
            for (AlarmWrapper alarm : alarms) {
                // Recursively propagate the status for all of the given alarms
                updateAndPropagateVertex(m_g, m_g.getVertexByReductionKey(alarm.getReductionKey()), alarm.getStatus());
                // Keep track of the reduction keys that have been processed
                reductionKeysFromGivenAlarms.add(alarm.getReductionKey());
            }

            for (String missingReductionKey : Sets.difference(m_g.getReductionKeys(), reductionKeysFromGivenAlarms)) {
                // There is a vertex on the graph that corresponds to this reduction key
                // but no alarm with this reduction key exists
                updateAndPropagateVertex(m_g, m_g.getVertexByReductionKey(missingReductionKey), Status.INDETERMINATE);
            }
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    private void updateAndPropagateVertex(BusinessServiceGraph graph, GraphVertex vertex, Status newStatus) {
        if (vertex == null) {
            // Nothing to do here
            return;
        }

        // Apply lower bound
        newStatus = newStatus.isLessThan(MIN_SEVERITY) ? MIN_SEVERITY : newStatus;

        // Update the status if necessary
        Status previousStatus = vertex.getStatus();
        if (previousStatus.equals(newStatus)) {
            // The status hasn't changed, there's nothing to propagate
            return;
        }
        vertex.setStatus(newStatus);

        // Notify the listeners
        onStatusUpdated(graph, vertex, previousStatus);

        // Update the edges with the mapped status
        List<GraphEdge> updatedEges = Lists.newArrayList();
        for (GraphEdge edge : graph.getInEdges(vertex)) {
            Status mappedStatus = newStatus;
            if (newStatus.isGreaterThan(MIN_SEVERITY)) {
                // Only apply the map function when the status is > the minimum
                mappedStatus = edge.getMapFunction().map(newStatus).orElse(MIN_SEVERITY);
            } else {
                mappedStatus = newStatus;
            }

            if (mappedStatus.equals(edge.getStatus())) {
                // The status hasn't changed
                continue;
            }

            // Update the status and add it to the list of edges to propagate
            edge.setStatus(mappedStatus);
            updatedEges.add(edge);
        }

        // Propagate once all of the edges have been updated
        for (GraphEdge edge : updatedEges) {
            reduceUpdateAndPropagateVertex(graph, graph.getOpposite(vertex, edge));
        }
    }

    private void reduceUpdateAndPropagateVertex(BusinessServiceGraph graph, GraphVertex vertex) {
        if (vertex == null) {
            // Nothing to do here
            return;
        }

        // Calculate the weighed statuses from the child edges
        List<StatusWithIndex> statuses = weighEdges(graph.getOutEdges(vertex));

        // Reduce
        Optional<StatusWithIndices> reducedStatus = vertex.getReductionFunction().reduce(statuses);

        Status newStatus;
        if (reducedStatus.isPresent()) {
            newStatus = reducedStatus.get().getStatus();
        } else {
            newStatus = MIN_SEVERITY;
        }

        // Update and propagate
        updateAndPropagateVertex(graph, vertex, newStatus);
    }

    public static List<StatusWithIndex> weighEdges(Collection<GraphEdge> edges) {
        return weighStatuses(edges.stream()
                .collect(Collectors.toMap(Function.identity(), GraphEdge::getStatus,
                        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new)));
    }

    /**
     * Apply the edges weights to the associated statuses set in the map,
     * ignoring the actual status stored in the edge. Can be used for simulations
     * without needing to change the actual edge's status.
     *
     * @param edgesWithStatus
     * @return
     */
    public static List<StatusWithIndex> weighStatuses(Map<GraphEdge, Status> edgesWithStatus) {
        // Find the greatest common divisor of all the weights
        int gcd = edgesWithStatus.keySet().stream()
                .map(GraphEdge::getWeight)
                .reduce((a,b) -> BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue())
                .orElse(1);

        // Multiply the statuses based on their relative weight
        List<StatusWithIndex> statuses = Lists.newArrayList();
        int k = 0;
        for (Entry<GraphEdge, Status> entry : edgesWithStatus.entrySet()) {
            int relativeWeight = Math.floorDiv(entry.getKey().getWeight(), gcd);
            for (int i = 0; i < relativeWeight; i++) {
                statuses.add(new StatusWithIndex(entry.getValue(), k));
            }
            k++;
        }

        return statuses;
    }

    private void onStatusUpdated(BusinessServiceGraph graph, GraphVertex vertex, Status previousStatus) {
        BusinessService businessService = vertex.getBusinessService();
        if (businessService == null) {
            // Only send updates for business services (and not for reduction keys)
            return;
        }

        if (graph != m_g) {
            // We're working with a new graph, only send a status update if the new status is different
            // than the one in the previous graph
            GraphVertex previousVertex = m_g.getVertexByBusinessServiceId(businessService.getId());
            if (previousVertex != null && vertex.getStatus().equals(previousVertex.getStatus())) {
                // The vertex for this business service in the previous graph
                // had the same status, don't issue any notifications
                return;
            }
        }

        for (BusinessServiceStateChangeHandler handler : m_handlers) {
            handler.handleBusinessServiceStateChanged(businessService, vertex.getStatus(), previousStatus);
        }
    }

    @Override
    public Status getOperationalStatus(BusinessService businessService) {
        Objects.requireNonNull(businessService);
        m_rwLock.readLock().lock();
        try {
            GraphVertex vertex = m_g.getVertexByBusinessServiceId(businessService.getId());
            if (vertex != null) {
                return vertex.getStatus();
            }
            return null;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public Status getOperationalStatus(IpService ipService) {
        m_rwLock.readLock().lock();
        try {
            GraphVertex vertex = m_g.getVertexByIpServiceId(ipService.getId());
            if (vertex != null) {
                return vertex.getStatus();
            }
            return null;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public Status getOperationalStatus(String reductionKey) {
        m_rwLock.readLock().lock();
        try {
            GraphVertex vertex = m_g.getVertexByReductionKey(reductionKey);
            if (vertex != null) {
                return vertex.getStatus();
            }
            return null;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public Status getOperationalStatus(Edge edge) {
        m_rwLock.readLock().lock();
        try {
            GraphVertex vertex = m_g.getVertexByEdgeId(edge.getId());
            if (vertex != null) {
                return vertex.getStatus();
            }
            return null;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    public void setAlarmProvider(AlarmProvider alarmProvider) {
        m_rwLock.writeLock().lock();
        try {
            m_alarmProvider = alarmProvider;
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    @Override
    public void addHandler(BusinessServiceStateChangeHandler handler, Map<String, String> attributes) {
        m_rwLock.writeLock().lock();
        try {
            m_handlers.add(handler);
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeHandler(BusinessServiceStateChangeHandler handler, Map<String, String> attributes) {
        m_rwLock.writeLock().lock();
        try {
            return m_handlers.remove(handler);
        } finally {
            m_rwLock.writeLock().unlock();
        }
    }

    @Override
    public void renderGraphToPng(File tempFile) {
        m_rwLock.readLock().lock();
        try {
            Layout<GraphVertex,GraphEdge> layout = new KKLayout<GraphVertex,GraphEdge>(m_g);
            layout.setSize(new Dimension(1024,1024)); // Size of the layout

            VisualizationImageServer<GraphVertex, GraphEdge> vv = new VisualizationImageServer<GraphVertex, GraphEdge>(layout, layout.getSize());
            vv.setPreferredSize(new Dimension(1200,1200)); // Viewing area size
            vv.getRenderContext().setVertexLabelTransformer(new Transformer<GraphVertex,String>() {
                @Override
                public String transform(GraphVertex vertex) {
                    if (vertex.getBusinessService() != null) {
                        return String.format("BS[%s]", vertex.getBusinessService().getName());
                    }
                    if (vertex.getIpService() != null) {
                        IpService ipService = vertex.getIpService();
                        return String.format("IP_SERVICE[%s,%s]", ipService.getId(), ipService.getServiceName());
                    }
                    if (vertex.getReductionKey() != null) {
                        return String.format("RK[%s]", vertex.getReductionKey());
                    }
                    return "UNKNOWN";
                }
            });
            vv.getRenderContext().setEdgeLabelTransformer(new Transformer<GraphEdge,String>() {
                @Override
                public String transform(GraphEdge edge) {
                    return String.format("%s", edge.getMapFunction().getClass().getSimpleName());
                }
            });

            // Create the buffered image
            BufferedImage image = (BufferedImage) vv.getImage(
                    new Point2D.Double(vv.getGraphLayout().getSize().getWidth() / 2,
                    vv.getGraphLayout().getSize().getHeight() / 2),
                    new Dimension(vv.getGraphLayout().getSize()));

            // Render
            try {
                ImageIO.write(image, "png", tempFile);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public BusinessServiceGraph getGraph() {
        return m_g;
    }

    @Override
    public Set<GraphEdge> calculateImpacting(BusinessService businessService) {
        m_rwLock.readLock().lock();
        try {
            final GraphVertex vertex = m_g.getVertexByBusinessServiceId(businessService.getId());
            return GraphAlgorithms.calculateImpacting(m_g, vertex);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public BusinessServiceStateMachine clone(boolean preserveState) {
        m_rwLock.readLock().lock();
        try {
            final BusinessServiceStateMachine sm = new DefaultBusinessServiceStateMachine();

            // Rebuild the graph using the business services from the existing state machine
            final BusinessServiceGraph graph = getGraph();
            sm.setBusinessServices(graph.getVertices().stream()
                    .map(GraphVertex::getBusinessService)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            // Prime the state
            if (preserveState) {
                for (String reductionKey : graph.getReductionKeys()) {
                    GraphVertex reductionKeyVertex = graph.getVertexByReductionKey(reductionKey);
                    sm.handleNewOrUpdatedAlarm(new AlarmWrapper() {
                        @Override
                        public String getReductionKey() {
                            return reductionKey;
                        }

                        @Override
                        public Status getStatus() {
                            return reductionKeyVertex.getStatus();
                        }
                    });
                }
            }
            return sm;
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public List<GraphVertex> calculateRootCause(BusinessService businessService) {
        m_rwLock.readLock().lock();
        try {
            final GraphVertex vertex = m_g.getVertexByBusinessServiceId(businessService.getId());
            return GraphAlgorithms.calculateRootCause(m_g, vertex);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public List<GraphVertex> calculateImpact(BusinessService businessService) {
        m_rwLock.readLock().lock();
        try {
            final GraphVertex vertex = m_g.getVertexByBusinessServiceId(businessService.getId());
            return calculateImpact(vertex);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public List<GraphVertex> calculateImpact(IpService ipService) {
        m_rwLock.readLock().lock();
        try {
            final GraphVertex vertex = m_g.getVertexByIpServiceId(ipService.getId());
            return calculateImpact(vertex);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public List<GraphVertex> calculateImpact(String reductionKey) {
        m_rwLock.readLock().lock();
        try {
            final GraphVertex vertex = m_g.getVertexByReductionKey(reductionKey);
            return calculateImpact(vertex);
        } finally {
            m_rwLock.readLock().unlock();
        }
    }

    @Override
    public ThresholdResultExplanation explain(BusinessService businessService, Threshold threshold) {
        final GraphVertex vertex = getGraph().getVertexByBusinessServiceId(businessService.getId());

        // Calculate the weighed statuses from the child edges
        List<StatusWithIndex> statusesWithIndices = weighEdges(getGraph().getOutEdges(vertex));
        List<Status> statuses = statusesWithIndices.stream()
            .map(StatusWithIndex::getStatus)
            .collect(Collectors.toList());

        // Reduce
        Status reducedStatus = threshold.reduce(statusesWithIndices)
            .orElse(new StatusWithIndices(MIN_SEVERITY, Collections.emptyList()))
            .getStatus();

        ThresholdResultExplanation explanation = new ThresholdResultExplanation();
        explanation.setStatus(reducedStatus);
        explanation.setHitsByStatus(threshold.getHitsByStatus(statuses));
        explanation.setGraphEdges(getGraph().getOutEdges(vertex));
        explanation.setWeightStatuses(statuses);
        explanation.setFunction(threshold);

        Map<GraphEdge, GraphVertex> graphEdgeToGraphVertex = new HashMap<>();
        for (Edge eachEdge : businessService.getEdges()) {
            GraphVertex vertexForEdge = getGraph().getVertexByEdgeId(eachEdge.getId());
            GraphEdge graphEdge = getGraph().getGraphEdgeByEdgeId(eachEdge.getId());
            if (vertexForEdge != null && graphEdge != null) {
                graphEdgeToGraphVertex.put(graphEdge, vertexForEdge);
            }
        }
        explanation.setGraphEdgeToGraphVertexMapping(graphEdgeToGraphVertex);
        return explanation;
    }

    private List<GraphVertex> calculateImpact(GraphVertex vertex) {
        return GraphAlgorithms.calculateImpact(m_g, vertex);
    }
}
