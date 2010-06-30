//
// This file is part of the OpenNMS(R) Application.
//
// Modifications:
//
// 2007 Jun 23: Use Java 5 generics, rename set to m_set. - dj@opennms.org
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
//
// Tab Size = 8
//
//
//
package org.opennms.core.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides set functionality for ints.
 *
 * @author ranger
 * @version $Id: $
 */
public class IntSet {
    private Set<Integer> m_set = new HashSet<Integer>();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#add(java.lang.Object)
     */
    /**
     * <p>add</p>
     *
     * @param n a int.
     * @return a boolean.
     */
    public boolean add(int n) {
        return m_set.add(new Integer(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    /**
     * <p>addAll</p>
     *
     * @param s a {@link org.opennms.core.utils.IntSet} object.
     * @return a boolean.
     */
    public boolean addAll(IntSet s) {
        return m_set.addAll(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#clear()
     */
    /**
     * <p>clear</p>
     */
    public void clear() {
        m_set.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#contains(java.lang.Object)
     */
    /**
     * <p>contains</p>
     *
     * @param n a int.
     * @return a boolean.
     */
    public boolean contains(int n) {
        return m_set.contains(new Integer(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    /**
     * <p>containsAll</p>
     *
     * @param s a {@link org.opennms.core.utils.IntSet} object.
     * @return a boolean.
     */
    public boolean containsAll(IntSet s) {
        return m_set.containsAll(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#isEmpty()
     */
    /**
     * <p>isEmpty</p>
     *
     * @return a boolean.
     */
    public boolean isEmpty() {
        return m_set.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#iterator()
     */
    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<Integer> iterator() {
        return m_set.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#remove(java.lang.Object)
     */
    /**
     * <p>remove</p>
     *
     * @param n a int.
     * @return a boolean.
     */
    public boolean remove(int n) {
        return m_set.remove(new Integer(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    /**
     * <p>removeAll</p>
     *
     * @param s a {@link org.opennms.core.utils.IntSet} object.
     * @return a boolean.
     */
    public boolean removeAll(IntSet s) {
        return m_set.remove(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    /**
     * <p>retainAll</p>
     *
     * @param s a {@link org.opennms.core.utils.IntSet} object.
     * @return a boolean.
     */
    public boolean retainAll(IntSet s) {
        return m_set.retainAll(s.m_set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#size()
     */
    /**
     * <p>size</p>
     *
     * @return a int.
     */
    public int size() {
        return m_set.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#toArray()
     */
    /**
     * <p>toArray</p>
     *
     * @return an array of int.
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
