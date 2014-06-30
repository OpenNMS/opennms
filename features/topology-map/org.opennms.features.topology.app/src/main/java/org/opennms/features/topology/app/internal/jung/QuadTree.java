/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.jung;

public class QuadTree<Key extends Comparable, Value> {
    private Node root;


    // helper node data type
    private class Node {
        private Key m_cx;
        private Key m_cy;

        private Key m_x;

        private Key m_y;        // x- and y- coordinates
        private Node m_NW;
        private Node m_NE;
        private Node m_SE;
        private Node m_SW;      // four subtrees
        private Value m_value;  // associated data
        private double m_mass;
        private boolean m_isLeaf;


        Node(Key x, Key y, Value value) {
            setX(x);
            setY(y);
            setCx(x);
            setCy(y);
            setValue(value);
        }

        public Key getCx() {
            return m_cx;
        }

        public void setCx(Key cx) {
            m_cx = cx;
        }

        public Key getCy() {
            return m_cy;
        }

        public void setCy(Key cy) {
            m_cy = cy;
        }

        public void setX(Key x) {
            m_x = x;
        }

        public void setY(Key y) {
           m_y = y;
        }

        public void setNW(Node NW) {
            m_NW = NW;
        }

        public void setNE(Node NE) {
            m_NE = NE;
        }

        public void setSE(Node SE) {
            m_SE = SE;
        }

        public void setSW(Node SW) {
            m_SW = SW;
        }

        public Value getValue() {
            return m_value;
        }

        public void setValue(Value value) {
            m_value = value;
        }

        public Key getX() {
            return m_x;
        }

        public Key getY() {
            return m_y;
        }

        public Node getNW() {
            return m_NW;
        }

        public Node getNE() {
            return m_NE;
        }

        public Node getSE() {
            return m_SE;
        }

        public Node getSW() {
            return m_SW;
        }
    }


    /***********************************************************************
     *  Insert (x, y) into appropriate quadrant
     ***********************************************************************/
    public void insert(Key x, Key y, Value value) {
        root = insert(root, x, y, value);
    }

    private Node insert(Node h, Key x, Key y, Value value) {
        if (h == null) return new Node(x, y, value);
            //// if (eq(x, h.x) && eq(y, h.y)) h.value = value;  // duplicate
        else if ( less(x, h.getX()) &&  less(y, h.getY())) h.setSW(insert(h.getSW(), x, y, value));
        else if ( less(x, h.getX()) && !less(y, h.getY())) h.setNW(insert(h.getNW(), x, y, value));
        else if (!less(x, h.getX()) &&  less(y, h.getY())) h.setSE(insert(h.getSE(), x, y, value));
        else if (!less(x, h.getX()) && !less(y, h.getY())) h.setNE(insert(h.getNE(), x, y, value));
        return h;
    }


    /***********************************************************************
     *  Range search.
     ***********************************************************************/
/*

    public void query2D(Interval2D<Key> rect) {
        query2D(root, rect);
    }

    private void query2D(Node h, Interval2D<Key> rect) {
        if (h == null) return;
        Key xmin = rect.intervalX.low;
        Key ymin = rect.intervalY.low;
        Key xmax = rect.intervalX.high;
        Key ymax = rect.intervalY.high;
        if (rect.contains(h.x, h.y))
            System.out.println("    (" + h.x + ", " + h.y + ") " + h.value);
        if ( less(xmin, h.x) &&  less(ymin, h.y)) query2D(h.SW, rect);
        if ( less(xmin, h.x) && !less(ymax, h.y)) query2D(h.NW, rect);
        if (!less(xmax, h.x) &&  less(ymin, h.y)) query2D(h.SE, rect);
        if (!less(xmax, h.x) && !less(ymax, h.y)) query2D(h.NE, rect);
    }
*/


    /*************************************************************************
     *  helper comparison functions
     *************************************************************************/

    private boolean less(Key k1, Key k2) { return k1.compareTo(k2) <  0; }
    private boolean eq  (Key k1, Key k2) { return k1.compareTo(k2) == 0; }
}
