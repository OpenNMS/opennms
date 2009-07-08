/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2004-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides set functionality for ints.
 */
public class IntSet {
    private Set<Integer> m_set = new HashSet<Integer>();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(int n) {
        return m_set.add(new Integer(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(IntSet s) {
        return m_set.addAll(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#clear()
     */
    public void clear() {
        m_set.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(int n) {
        return m_set.contains(new Integer(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(IntSet s) {
        return m_set.containsAll(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return m_set.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#iterator()
     */
    public Iterator<Integer> iterator() {
        return m_set.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(int n) {
        return m_set.remove(new Integer(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(IntSet s) {
        return m_set.remove(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(IntSet s) {
        return m_set.retainAll(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#size()
     */
    public int size() {
        return m_set.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#toArray()
     */
    public int[] toArray() {
        int[] array = new int[size()];

        int i = 0;
        for (Iterator<Integer> it = m_set.iterator(); it.hasNext(); i++) {
            Integer element = it.next();
            array[i] = element.intValue();
        }
        return array;
    }
}
