//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.core.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * <p>JdbcSet class.</p>
 *
 * @author ranger
 * @version $Id: $
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

        public E next() {
            m_last = super.next();
            return m_last;
        }

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
    public Iterator<E> iterator() {
        return new JdbcSetIterator(m_entries.iterator(), m_added.iterator());
    }

    /**
     * <p>size</p>
     *
     * @return a int.
     */
    public int size() {
        return m_added.size() + m_entries.size();
    }

    /**
     * <p>add</p>
     *
     * @param o a E object.
     * @return a boolean.
     */
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
