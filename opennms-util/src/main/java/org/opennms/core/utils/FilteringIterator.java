/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

        @Override
        public boolean hasNext() {
            return (m_peeked != null || m_it.hasNext());
        }

        @Override
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

        @Override
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
    @Override
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
    @Override
    public T next() {
        skipNonMatching();
        return m_it.next();
    }

    /**
     * <p>remove</p>
     */
    @Override
    public void remove() {
        m_it.remove();
    }

    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
