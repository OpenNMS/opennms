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

package org.opennms.features.topology.app.internal.support;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.topology.persistence.api.LayoutDao;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;
import org.opennms.netmgt.topology.persistence.api.PointEntity;
import org.opennms.netmgt.topology.persistence.api.VertexPositionEntity;
import org.opennms.netmgt.topology.persistence.api.VertexRefEntity;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.hash.Hashing;

public class LayoutManager {

    private LayoutDao layoutDao;

    public LayoutManager(LayoutDao layoutDao, TransactionOperations transactionOperations) {
        this.layoutDao = new TransactionAwareBeanProxyFactory(transactionOperations).createProxy(layoutDao);
    }

    public void persistLayout(GraphContainer graphContainer) {
        final List<VertexRef> vertexRefs = toVertexRef(graphContainer.getGraph().getDisplayVertices());
        final String id = calculateHash(vertexRefs);
        LayoutEntity layoutEntity = layoutDao.get(id);
        if (layoutEntity == null) {
            layoutEntity = new LayoutEntity();
            layoutEntity.setId(id);
            layoutEntity.setCreated(new Date());
            layoutEntity.setCreator(graphContainer.getApplicationContext().getUsername());
        }
        layoutEntity.setUpdated(new Date());
        layoutEntity.setUpdator(graphContainer.getApplicationContext().getUsername());

        final Layout layout = graphContainer.getGraph().getLayout();
        final List<VertexPositionEntity> vertexPositionEntities = vertexRefs.stream()
                .map(vertexRef -> {
                    final Point p = layout.getLocation(vertexRef);
                    PointEntity pointEntity = new PointEntity();
                    pointEntity.setX((int) p.getX());
                    pointEntity.setY((int) p.getY());

                    final VertexPositionEntity vertexEntity = new VertexPositionEntity();
                    vertexEntity.setVertexRef(toVertexRefEntity(vertexRef));
                    vertexEntity.setPosition(pointEntity);
                    return vertexEntity;
                })
                .collect(Collectors.toList());
        layoutEntity.getVertexPositions().clear();
        for (VertexPositionEntity eachVertexPosition : vertexPositionEntities) {
            layoutEntity.addVertexPosition(eachVertexPosition);
        }
        layoutDao.saveOrUpdate(layoutEntity);
    }

    public LayoutEntity loadLayout(Graph graph) {
        LayoutEntity layoutEntity = findBy(graph);
        if (layoutEntity != null) {
            layoutEntity.setLastUsed(new Date());
            layoutDao.saveOrUpdate(layoutEntity);
        }
        return layoutEntity;
    }

    private LayoutEntity findBy(Graph graph) {
        List<VertexRef> vertexRefs = toVertexRef(graph.getDisplayVertices());
        String id = calculateHash(vertexRefs);
        return layoutDao.get(id);
    }

    protected static List<VertexRef> toVertexRef(Collection<Vertex> input) {
        return input.stream().map(v -> (VertexRef) v).collect(Collectors.toList());
    }

    protected static String calculateHash(Collection<VertexRef> vertices) {
        final String vertexKey = vertices.stream()
                .sorted(Comparator.comparing(VertexRef::getNamespace).thenComparing(VertexRef::getId))
                .map(v -> String.format("%s:%s", v.getNamespace(), v.getId()))
                .collect(Collectors.joining(","));
        return Hashing.sha256().hashString(vertexKey, StandardCharsets.UTF_8).toString();
    }

    public static VertexRefEntity toVertexRefEntity(VertexRef vertexRef) {
        Objects.requireNonNull(vertexRef);

        VertexRefEntity vertexRefEntity = new VertexRefEntity();
        vertexRefEntity.setId(vertexRef.getId());
        vertexRefEntity.setNamespace(vertexRef.getNamespace());
        return vertexRefEntity;
    }

    public boolean isPersistedLayoutEqualToCurrentLayout(Graph graph) {
        LayoutEntity layoutEntity = loadLayout(graph);
        if (layoutEntity != null) {
            // If we have a layout persisted, we verify if it is equal.
            final Map<VertexRef, Point> persistedLocations = layoutEntity.getVertexPositions()
                    .stream()
                    .collect(Collectors.toMap((Function<VertexPositionEntity, VertexRef>) vertexPositionEntity -> {
                        VertexRefEntity vertexRefEntity = vertexPositionEntity.getVertexRef();
                        return new DefaultVertexRef(vertexRefEntity.getNamespace(), vertexRefEntity.getId());
                    }, vertexPositionEntity -> {
                        PointEntity position = vertexPositionEntity.getPosition();
                        return new Point(position.getX(), position.getY());
                    }));

            // The locations may contain elements currently not visible, we filter them
            final Map<VertexRef, Point> manualLocations = new HashMap<>();
            graph.getLayout().getLocations().forEach((key, value) -> {
                if (persistedLocations.containsKey(key)) {
                    // layoutEntity stores int coordinates, but manualLocations are stored as double.
                    // Convert to int to make it comparable.
                    manualLocations.put(key, new Point((int) value.getX(), (int) value.getY()));
                }
            });
            final boolean layoutIsEqual = manualLocations.equals(persistedLocations);
            return layoutIsEqual;
        }
        return false; // We don't have anything persisted, so they are not equal
    }
}
