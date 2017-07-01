/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

public class TopoFRLayout<V, E>  extends AbstractLayout<V, E> implements IterativeContext {
    private static double PERCENTAGE = 0.25;
    private double forceConstant;

    private double temperature;

    private int currentIteration;

    private int mMaxIterations = 700;

    private Map<V, FRVertexData> frVertexData =
            LazyMap.decorate(new HashMap<V, FRVertexData>(), new Factory<FRVertexData>() {
                public FRVertexData create() {
                    return new FRVertexData();
                }
            });

    private double attraction_multiplier = 0.75;

    private double attraction_constant;

    private double repulsion_multiplier = 0.75;

    private double repulsion_constant;

    private double max_dimension;

    /**
     * Creates an instance for the specified graph.
     */
    public TopoFRLayout(Graph<V, E> g) {
        super(g);
    }


    @Override
    public void setSize(Dimension size) {
        if(initialized == false) {
            setInitializer(new RandomLocationTransformer<V>(size));
        }
        super.setSize(size);
        max_dimension = Math.max(size.height, size.width);
    }

    /**
     * Sets the attraction multiplier.
     */
    public void setAttractionMultiplier(double attraction) {
        this.attraction_multiplier = attraction;
    }

    /**
     * Sets the repulsion multiplier.
     */
    public void setRepulsionMultiplier(double repulsion) {
        this.repulsion_multiplier = repulsion;
    }

    public void reset() {
        doInit();
    }

    public void initialize() {
        doInit();
    }

    private void doInit() {
        Graph<V,E> graph = getGraph();
        Dimension d = getSize();
        if(graph != null && d != null) {
            currentIteration = 0;
            temperature = d.getWidth() / 10;

            forceConstant =
                    Math.sqrt(d.getHeight()
                                    * d.getWidth()
                                    / graph.getVertexCount());

            attraction_constant = attraction_multiplier * forceConstant;
            repulsion_constant = repulsion_multiplier * forceConstant;
        }
    }

    private double EPSILON = 0.00000000001D;

    /**
     * Moves the iteration forward one notch, calculation attraction and
     * repulsion between vertices and edges and cooling the temperature.
     */
    public synchronized void step() {
        currentIteration++;

        /**
         * Calculate repulsion
         */
        while(true) {
            try {
                for(V v1 : getGraph().getVertices()) {
                    calcRepulsion(v1);
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }

        /**
         * Calculate attraction
         */
        while(true) {
            try {
                for(E e : getGraph().getEdges()) {

                    calcAttraction(e);
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }

        while(true) {
            try {
                for(V v : getGraph().getVertices()) {
                    if (isLocked(v)) continue;
                    calcPositions(v);
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }
        cool();
    }

    protected synchronized void calcPositions(V v) {
        FRVertexData fvd = getFRData(v);
        if(fvd == null) return;
        Point2D xyd = transform(v);
        double deltaLength = fvd.norm();
        if(deltaLength <= 0.005) return;

        double newXDisp = fvd.getX() * percentage() / deltaLength
                * Math.min(deltaLength , temperature);

        if (Double.isNaN(newXDisp)) {
            throw new IllegalArgumentException(
                    "Unexpected mathematical result in FRLayout:calcPositions [xdisp]"); }

        double newYDisp = fvd.getY() * percentage() / deltaLength * Math.min(deltaLength, temperature);
        xyd.setLocation(xyd.getX() + newXDisp, xyd.getY() + newYDisp);

        double borderWidth = getSize().getWidth() / 50.0;
        double newXPos = xyd.getX();
        if (newXPos < borderWidth) {
            newXPos = borderWidth + Math.random() * borderWidth * 2.0;
        } else if (newXPos > (getSize().getWidth() - borderWidth)) {
            newXPos = getSize().getWidth() - borderWidth - Math.random() * borderWidth * 2.0;
        }

        double newYPos = xyd.getY();
        if (newYPos < borderWidth) {
            newYPos = borderWidth + Math.random() * borderWidth * 2.0;
        } else if (newYPos > (getSize().getHeight() - borderWidth)) {
            newYPos = getSize().getHeight() - borderWidth
                    - Math.random() * borderWidth * 2.0;
        }

        xyd.setLocation(newXPos, newYPos);
    }

    private double percentage() {
        return PERCENTAGE;
    }

    private double epsilon() {
        double e = 2*EPSILON*Math.signum(Math.random()-0.5);
        return e == 0 ? EPSILON : e;
    }

    protected void calcAttraction(E e) {
        Pair<V> endpoints = getGraph().getEndpoints(e);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        boolean v1_locked = isLocked(v1);
        boolean v2_locked = isLocked(v2);

        if(v1_locked && v2_locked) {
            // both locked, do nothing
            return;
        }
        Point2D p1 = transform(v1);
        Point2D p2 = transform(v2);
        if(p1 == null || p2 == null) return;
        double xDelta = p1.getX() - p2.getX();
        double yDelta = p1.getY() - p2.getY();

        xDelta = Math.abs(xDelta) > EPSILON ? xDelta : xDelta == 0 ? epsilon() : Math.signum(xDelta) * EPSILON;
        yDelta = Math.abs(yDelta) > EPSILON ? yDelta : yDelta == 0 ? epsilon() : Math.signum(yDelta) * EPSILON;

        double deltaLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));

        double force = (deltaLength * deltaLength) / attraction_constant;

        if (Double.isNaN(force)) {
            throw new IllegalArgumentException("Unexpected mathematical result in FRLayout:calcPositions [force]");
        }

        double dx = (xDelta / deltaLength) * force;
        double dy = (yDelta / deltaLength) * force;
        if(v1_locked == false) {
            FRVertexData fvd1 = getFRData(v1);
            fvd1.offset(-dx, -dy);
        }
        if(v2_locked == false) {
            FRVertexData fvd2 = getFRData(v2);
            fvd2.offset(dx, dy);
        }
    }

    protected void calcRepulsion(V v1) {
        FRVertexData fvd1 = getFRData(v1);
        if(fvd1 == null)
            return;
        fvd1.setLocation(0, 0);

        try {
            for(V v2 : getGraph().getVertices()) {
                if (v1 != v2) {
                    Point2D p1 = transform(v1);
                    Point2D p2 = transform(v2);
                    if(p1 == null || p2 == null) continue;
                    double xDelta = p1.getX() - p2.getX();
                    double yDelta = p1.getY() - p2.getY();

                    xDelta = Math.abs(xDelta) > EPSILON ? xDelta : xDelta == 0 ? epsilon() : Math.signum(xDelta) * EPSILON;
                    yDelta = Math.abs(yDelta) > EPSILON ? yDelta : yDelta == 0 ? epsilon() : Math.signum(yDelta) * EPSILON;

                    double deltaLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));

                    double force = (repulsion_constant * repulsion_constant) / deltaLength;

                    if (Double.isNaN(force)) {
                        throw new RuntimeException("Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
                    }
                    fvd1.offset((xDelta / deltaLength) * force, (yDelta / deltaLength) * force);
                }
            }
        } catch(ConcurrentModificationException cme) {
            calcRepulsion(v1);
        }
    }

    private void cool() {
        temperature *= (1.0 - currentIteration / (double) mMaxIterations);
    }

    /**
     * Sets the maximum number of iterations.
     */
    public void setMaxIterations(int maxIterations) {
        mMaxIterations = maxIterations;
    }

    protected FRVertexData getFRData(V v) {
        return frVertexData.get(v);
    }

    /**
     * This one is an incremental visualization.
     */
    public boolean isIncremental() {
        return true;
    }

    /**
     * Returns true once the current iteration has passed the maximum count,
     * <tt>MAX_ITERATIONS</tt>.
     */
    public boolean done() {
        if (currentIteration > mMaxIterations || temperature < 1.0/max_dimension) {
            return true;
        }
        return false;
    }

    protected static class FRVertexData extends Point2D.Double {
        protected void offset(double x, double y) {
            this.x += x;
            this.y += y;
        }

        protected double norm()
        {
            return Math.sqrt(x*x + y*y);
        }
    }
}
