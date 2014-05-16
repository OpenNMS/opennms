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
        Key x, y;              // x- and y- coordinates
        Node NW, NE, SE, SW;   // four subtrees
        Value value;           // associated data

        Node(Key x, Key y, Value value) {
            this.x = x;
            this.y = y;
            this.value = value;
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
        else if ( less(x, h.x) &&  less(y, h.y)) h.SW = insert(h.SW, x, y, value);
        else if ( less(x, h.x) && !less(y, h.y)) h.NW = insert(h.NW, x, y, value);
        else if (!less(x, h.x) &&  less(y, h.y)) h.SE = insert(h.SE, x, y, value);
        else if (!less(x, h.x) && !less(y, h.y)) h.NE = insert(h.NE, x, y, value);
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
