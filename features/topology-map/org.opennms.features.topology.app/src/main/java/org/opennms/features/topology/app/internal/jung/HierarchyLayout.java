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

package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;
import org.opennms.features.topology.api.topo.LevelAware;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;

/**
 * A custom layout to be able to draw a hierarchy.
 * This implementation is inspired by {@link edu.uci.ics.jung.algorithms.layout.TreeLayout}, which only
 * works if 2 parents do not have the same child (see {@link edu.uci.ics.jung.graph.util.TreeUtils#getRoots(Forest)}).
 *
 * @param <V>
 * @param <E>
 */
public class HierarchyLayout<V, E> implements Layout<V, E> {

    private Dimension size = new Dimension(600, 600);
    private Graph<V, E> graph;
    private Map<V, Integer> basePositions = new HashMap<V, Integer>();

    private Map<V, Point2D> locations =
            LazyMap.decorate(new HashMap<V, Point2D>(),
                    new Transformer<V, Point2D>() {
                        public Point2D transform(V arg0) {
                            return new Point2D.Double();
                        }
                    });

    private final int distX;
    private int distY;
    private final Map<V, Integer> levelMap = Maps.newHashMap();
    private transient Point m_currentPoint = new Point();
    private transient Set<V> alreadyDone = new HashSet<>();

    /**
     * Used for sorting vertices from left to right
     */
    private final Comparator<V> pointBasedComparator = new Comparator<V>() {
        @Override
        public int compare(V v1, V v2) {
            final Point2D p1 = locations.get(v1);
            final Point2D p2 = locations.get(v2);

            int xcomp = Double.compare(p1.getX(), p2.getX());
            if (xcomp != 0) {
                return xcomp;
            }
            return Double.compare(p1.getY(), p2.getY());
        }
    };

    /**
     * Creates an instance for the specified graph, X distance, and Y distance.
     */
    public HierarchyLayout(Graph<V, E> g, int distx, int disty) {
        Preconditions.checkArgument(distx >= 1 && disty >= 1, "X and Y distances must each be positive");
        this.graph = Objects.requireNonNull(g, "Graph must not be null");
        this.distX = distx;
        this.distY = disty;
        buildTree();
    }

    // we may have 1 to n root vertices
    private Set<V> getRoots() {
        Set<V> roots = graph.getVertices().stream()
                .filter(v -> graph.getInEdges(v).isEmpty())
                // Preserve the order of the roots
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return roots;
    }

    private void buildTree() {
        // we index the levels
        graph.getVertices().forEach(v -> {
            if (v instanceof LevelAware) {
                int level = ((LevelAware)v).getLevel();
                levelMap.put(v, level);
            } else {
                levelMap.put(v, 0);
            }
        });

        // now we calculate the vertex
        this.m_currentPoint = new Point(0, 20);
        if (graph != null) {
            for (V eachRoot : getRoots()) {
                if (eachRoot != null) {
                    calculateDimensionX(eachRoot);
                    m_currentPoint.x += this.basePositions.get(eachRoot) / 2 + this.distX;
                    buildTree(eachRoot, this.m_currentPoint.x);
                }
            }
        }
    }

    private void buildTree(V v, int x) {
        if (!alreadyDone.contains(v)) {
            alreadyDone.add(v);

            //go one level further down
            this.m_currentPoint.y = (levelMap.get(v) + 1) * this.distY;
            this.m_currentPoint.x = x;

            this.setCurrentPositionFor(v);

            int sizeXofCurrent = basePositions.get(v);
            int lastX = x - sizeXofCurrent / 2;
            int sizeXofChild;
            int startXofChild;

            for (V element : graph.getSuccessors(v)) {
                sizeXofChild = this.basePositions.get(element);
                startXofChild = lastX + sizeXofChild / 2;
                buildTree(element, startXofChild);
                lastX = lastX + sizeXofChild + distX;
            }
            this.m_currentPoint.y -= this.distY;
        }
    }

    private int calculateDimensionX(V v) {
        int size = 0;
        int childrenNum = graph.getSuccessors(v).size();
        if (childrenNum != 0) {
            for (V element : graph.getSuccessors(v)) {
                size += calculateDimensionX(element) + distX;
            }
        }
        size = Math.max(0, size - distX);
        basePositions.put(v, size);
        return size;
    }

    private void setCurrentPositionFor(V vertex) {
        int x = m_currentPoint.x;
        int y = m_currentPoint.y;
        if (x < 0) size.width -= x;

        if (x > size.width - distX)
            size.width = x + distX;

        if (y < 0) size.height -= y;
        if (y > size.height - distY)
            size.height = y + distY;
        locations.get(vertex).setLocation(m_currentPoint);
    }

    /**
     * Shifts the vertices horizontally ensuring that there is no more
     * than "distx" units between each column of vertices.
     *
     * The resulting layout is a denser visual that maintains the existing
     * structure of the original layout.
     *
     * For the purposes of this algorithm, vertices with the same value
     * of the X coordinate are deemed to be in the same "column".
     *
     * @param vertices list of vertices that will be displayed
     */
    public void horizontalSqueeze(List<V> vertices) {
        // Determine the target distance between the column
        final double targetDelta = this.distX;

        // Order the list of vertices based on their current
        // position in the X-Y plane, from left to right
        final List<V> orderedVertices = new ArrayList<>(vertices);
        Collections.sort(orderedVertices, pointBasedComparator);

        Double setXTo = null;
        Double lastX = null;
        for (V v : orderedVertices) {
            final Point2D currentPoint = locations.get(v);
            final Double currentX = currentPoint.getX();

            if (lastX == null) {
                // This is the first vertex
                // Capture the X value, and skip to the next one
                lastX = setXTo = currentX;
            } else if (Double.compare(lastX, currentX) == 0) {
                // We're still in the same column as the last vertex
                if (Double.compare(currentX, setXTo) != 0) {
                    locations.get(v).setLocation(setXTo, currentPoint.getY());
                }
            } else {
                // We've hit the first vertex in a new column
                // Calculate the distance between the current column and the last column
                lastX = setXTo;
                final double actualDelta = currentX - lastX;
                if (Double.compare(actualDelta, targetDelta) <= 0) {
                    // The distance is <= the target delta, so we don't need to update
                    // anything in this column
                    setXTo = currentX;
                } else {
                    // The distance is > the target delta, so we need to update
                    // all the X values for vertices in this column
                    setXTo = lastX + targetDelta;
                    // Update the X value for the current vertex
                    locations.get(v).setLocation(setXTo, currentPoint.getY());
                }
                lastX = currentX;
            }
        }
    }

    @Override
    public Graph<V, E> getGraph() {
        return graph;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean isLocked(V v) {
        return false;
    }

    @Override
    public void lock(V v, boolean state) {
    }

    @Override
    public void reset() {
    }

    @Override
    public void setSize(Dimension d) {
        throw new IllegalArgumentException("setSize(Dimension) is not supported");
    }

    @Override
    public void setGraph(Graph<V, E> graph) {
        Objects.requireNonNull(graph, "Graph must not be null");
        this.graph = graph;
        buildTree();
    }

    @Override
    public void setInitializer(Transformer<V, Point2D> initializer) {
    }

    @Override
    public void setLocation(V v, Point2D location) {
        locations.get(v).setLocation(location);
    }

    @Override
    public Point2D transform(V v) {
        return locations.get(v);
    }
}
