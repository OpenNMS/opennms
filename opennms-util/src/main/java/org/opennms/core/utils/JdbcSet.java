/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * <p>JdbcSet class.</p>
 */
public class JdbcSet<E> extends AbstractSet<E> {
    
    LinkedHashSet<E> m_added = new LinkedHashSet<E>();
    LinkedHashSet<E> m_entries = new LinkedHashSet<E>();
    LinkedHashSet<E> m_removed = new LinkedHashSet<E>();
    
    /**
     * <p>Constructor for JdbcSet.</p>
     *
     * @param c a {@link java.util.Collection} object.
     * @param <E> a E object.
     */
    public JdbcSet(Collection<E> c) {
        m_entries.addAll(c);
    }
    
    /**
     * <p>Constructor for JdbcSet.</p>
     */
    public JdbcSet() {
    	
    }
    
    /**
     * <p>setElements</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    protected void setElements(Collection<E> c) {
    	m_entries.addAll(c);
    }
    
    public class JdbcSetIterator extends IteratorIterator<E> {

        private E m_last;
        
        @SuppressWarnings("unchecked")
        public JdbcSetIterator(Iterator<E> entriesIter, Iterator<E> addedIter) {
            super(entriesIter, addedIter);
        }

        @Override
        public E next() {
            m_last = super.next();
            return m_last;
        }

        @Override
        public void remove() {
            m_removed.add(m_last);
            super.remove();
        }
        
    }
    
    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<E> iterator() {
        return new JdbcSetIterator(m_entries.iterator(), m_added.iterator());
    }

    /**
     * <p>size</p>
     *
     * @return a int.
     */
    @Override
    public int size() {
        return m_added.size() + m_entries.size();
    }

    /**
     * <p>add</p>
     *
     * @param o a E object.
     * @return a boolean.
     */
    @Override
    public boolean add(E o) {
        if (contains(o)) {
            return false;
        }
        m_added.add(o);
        return true;
    }
    
    /**
     * <p>getRemoved</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<E> getRemoved() {
        return m_removed;
    }
    
    /**
     * <p>getAdded</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<E> getAdded() {
        return m_added;
    }
    
    /**
     * <p>getRemaining</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<E> getRemaining() {
        return m_entries;
    }
    
    /**
     * <p>reset</p>
     */
    public void reset() {
        m_entries.addAll(m_added);
        m_added.clear();
        m_removed.clear();
    }


}
