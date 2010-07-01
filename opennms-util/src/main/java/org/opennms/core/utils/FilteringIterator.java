/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.utils;

import java.util.Iterator;

/**
 * <p>Abstract FilteringIterator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
abstract public class FilteringIterator<T> implements Iterator<T>, Iterable<T> {
    
    private static final class PeekableIterator<T> implements Iterator<T> {
        
        Iterator<T> m_it;
        T m_peeked = null;

        
        public PeekableIterator(Iterator<T> it) {
            m_it = it;
        }

        public boolean hasNext() {
            return (m_peeked != null || m_it.hasNext());
        }

        public T next() {
            if (m_peeked != null) {
                T next = m_peeked;
                m_peeked = null;
                return next;
            } else {
                return m_it.next();
            }
        }
        
        public T peek() {
            if (m_peeked == null && m_it.hasNext()) {
                m_peeked = m_it.next();
            }
                
            return m_peeked;
        }

        public void remove() {
            m_it.remove();
        }

    }
    
    private PeekableIterator<T> m_it;
    
    /**
     * <p>Constructor for FilteringIterator.</p>
     *
     * @param iterable a {@link java.lang.Iterable} object.
     */
    public FilteringIterator(Iterable<T> iterable) {
        this(iterable.iterator());
    }

    /**
     * <p>Constructor for FilteringIterator.</p>
     *
     * @param iterator a {@link java.util.Iterator} object.
     */
    public FilteringIterator(Iterator<T> iterator) {
        m_it = new PeekableIterator<T>(iterator);
    }

    /**
     * <p>hasNext</p>
     *
     * @return a boolean.
     */
    public boolean hasNext() {
        skipNonMatching();
        return m_it.hasNext();
    }
    
    private void skipNonMatching() {
        while(m_it.hasNext() && !matches(m_it.peek())) {
            m_it.next();
        }
    }

    /*
     * This iterator will only return objects for which matches returns true;
     */
    /**
     * <p>matches</p>
     *
     * @param item a T object.
     * @return a boolean.
     */
    abstract protected boolean matches(T item);

    /**
     * <p>next</p>
     *
     * @return a T object.
     */
    public T next() {
        skipNonMatching();
        return m_it.next();
    }

    /**
     * <p>remove</p>
     */
    public void remove() {
        m_it.remove();
    }

    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<T> iterator() {
        return this;
    }
}
