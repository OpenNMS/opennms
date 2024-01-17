/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class D3TopoLayout<V, E> extends AbstractLayout<V, E> implements IterativeContext {

    private static final double LINK_DISTANCE = 150.0;
    private static final double LINK_STRENGTH = 2.0;
    private static final int DEFAULT_CHARGE = -1200;
    private int m_charge = -30;

    private double m_alpha = 0.1;
    private Map<V, VertexData> m_vertexData = LazyMap.lazyMap(new HashMap<V, VertexData>(), VertexData::new);

    private Map<E, EdgeData> m_edgeData = LazyMap.lazyMap(new HashMap<E, EdgeData>(), EdgeData::new);



    protected D3TopoLayout(Graph<V, E> graph) {
        super(graph);
    }

    @Override
    public void initialize() {
        //initialize the weights
        for(V v : getGraph().getVertices()) {
            VertexData vData = getVertexData(v);
            vData.setWeight(1);
            Point2D location = apply(v);
            vData.setLocation(location);
            vData.setPrevious(location);
        }

        //initialize the vertices that have edges with weight
        for (E e : getGraph().getEdges()) {
            Pair<V> endPoints = getGraph().getEndpoints(e);
            V v1 = endPoints.getFirst();
            V v2 = endPoints.getSecond();
            VertexData vData1 = getVertexData(v1);
            vData1.setWeight(vData1.getWeight() + 1);

            VertexData vData2 = getVertexData(v2);
            vData2.setWeight(vData2.getWeight() + 1);
        }

        //Do we need to do an initial layout, we can rely on the initialized position


    }

    @Override
    public void reset() {

    }

    @Override
    public void step() {

        double currentForce;

        //Gauss-Seidel relaxation for links
        for (E e : getGraph().getEdges()) {
            Pair<V> endPoints = getGraph().getEndpoints(e);
            VertexData srcVertexData = getVertexData(endPoints.getFirst());
            VertexData targetVertexData = getVertexData(endPoints.getSecond());

            double xDelta = targetVertexData.getX() - srcVertexData.getX();
            double yDelta = targetVertexData.getY() - srcVertexData.getY();
            double l = xDelta * xDelta + yDelta * yDelta;
            if (l != 0) {
                EdgeData edgeData = getEdgeData(e);
                double lSqrt = Math.sqrt(l);
                double distance = m_alpha * edgeData.getStrength() * (lSqrt - edgeData.getDistance()) / lSqrt;

                xDelta *= distance;
                yDelta *= distance;

                currentForce = (double)srcVertexData.getWeight() / (double)(targetVertexData.getWeight() + srcVertexData.getWeight());
                targetVertexData.offset(-(xDelta * currentForce), -(yDelta * currentForce));

                currentForce = 1 - currentForce;
                srcVertexData.offset(xDelta * currentForce, yDelta * currentForce);
            }

        }

        //Apply gravity forces
        currentForce = m_alpha * getGravity();
        if(currentForce != 0){
            double centerX = getSize().getWidth() / 2;
            double centerY = getSize().getHeight() / 2;

            for (V v : getGraph().getVertices()) {
                VertexData vData = getVertexData(v);
                vData.offset((centerX - vData.getX()) * currentForce, (centerY - vData.getY()) * currentForce);
            }
            
        }

        //Compute quad tree center of mass and apply charge force
        if(getDefaultCharge() != 0){

            for(V v1 : getGraph().getVertices()) {
                VertexData vData1 = getVertexData(v1); 
                for(V v2 : getGraph().getVertices()) {
                    VertexData vData2 = getVertexData(v2);
                    
                    double dx = vData2.getX() - vData1.getX();
                    double dy = vData2.getY() - vData1.getY();
                    double d = dx*dx + dy*dy;
                    
                    if (d > 0) {
                        double k = m_alpha * vData2.getCharge() / d;
                        double px = dx*k;
                        double py = dy*k;

                        vData1.offset(px, py);
                    } else {
                        vData1.offset(0.5-Math.random(), 0.5-Math.random());
                    }
                    
                }
            }
        }
        
        
        // position verlet integration
        for(V v : getGraph().getVertices()) {
            VertexData vData = getVertexData(v);
            double tempX = vData.getX();
            double tempY = vData.getY();
            double x = vData.getX() + (vData.getPrevious().getX() - vData.getX())*getFriction();
            double y = vData.getY() + (vData.getPrevious().getY() - vData.getY())*getFriction();
            vData.setLocation(x, y);
            vData.setPrevious(tempX, tempY);
            Point2D location = apply(v);
            location.setLocation(vData.getX(), vData.getY());
        }
        
        m_alpha *= 0.99;


    }
    
    private double getGravity() {
        return 0.1;
    }
    
    private double getFriction() {
        return 0.9;
    }

    @Override
    public boolean done() {
        return
                // if we have no objects in our graph to layout we are done:
                (getGraph().getEdges().isEmpty() && getGraph().getVertices().isEmpty())
             || m_alpha < 0.005;
    }

    private VertexData getVertexData(V v) {
        return m_vertexData.get(v);
    }

    private EdgeData getEdgeData(E e) {
        return m_edgeData.get(e);
    }

    public int getDefaultCharge() {
        return m_charge;
    }

    public void setDefaultCharge(int charge) {
        this.m_charge = charge;
    }

    protected static class VertexData extends Point2D.Double {
        private static final long serialVersionUID = 1L;

        private int m_weight;
        private double m_distance = LINK_DISTANCE;
        private double m_strength = LINK_STRENGTH;
        private int m_charge = DEFAULT_CHARGE;
        private Point2D m_previous = null;

        protected void offset(final double x, final double y) {
            this.x += x;
            this.y += y;
        }
        
        protected void offsetPrevious(final double x, final double y) {
            if (m_previous == null) {
                m_previous = new Point2D.Double(this.x, this.y);
            }
            m_previous.setLocation(m_previous.getX()+x, m_previous.getY()+y);
        }
        
        public void setPrevious(final Point2D location) {
            m_previous = (Point2D) location.clone();
        }
        
        public void setPrevious(final double x, final double y) {
            m_previous = new Point2D.Double(x, y);
        }

        protected double norm()
        {
            return Math.sqrt(x*x + y*y);
        }

        protected void setWeight(final int weight) {
            m_weight = weight;
        }

        protected int getWeight(){
            return m_weight;
        }

        protected void setDistance(final int distance) {
            m_distance = distance;
        }

        protected double getDistance() {
            return m_distance;
        }

        protected void setStrength(final double strength) {
            m_strength = strength;
        }

        protected double getStrength() {
            return m_strength;
        }

        protected void setCharge(final int charge) {
            m_charge = charge;
        }

        protected int getCharge() {
            return m_charge;
        }
        
        protected Point2D getPrevious() {
            return m_previous;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(m_charge, m_distance, m_previous, m_strength, m_weight);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof VertexData)) {
                return false;
            }
            final VertexData that = (VertexData) obj;
            return m_charge == that.m_charge
                    && java.lang.Double.doubleToLongBits(m_distance) == java.lang.Double.doubleToLongBits(that.m_distance)
                    && Objects.equals(m_previous, that.m_previous) && java.lang.Double.doubleToLongBits(m_strength) == java.lang.Double.doubleToLongBits(that.m_strength)
                    && m_weight == that.m_weight;
        }
        
    }

    protected static class EdgeData {
        private double m_distance = LINK_DISTANCE;
        private double m_strength = LINK_STRENGTH;

        protected void setDistance(final double distance) {
            m_distance = distance;
        }

        protected double getDistance() {
            return m_distance;
        }

        public double getStrength() {
            return m_strength;
        }

        public void setStrength(final double strength) {
            this.m_strength = strength;
        }
    }
}
