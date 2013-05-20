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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class IteratorIterator<T> implements Iterator<T>, Iterable<T> {
    
    private Iterator<Iterator<T>> m_iterIter;
    private Iterator<T> m_currentIter;
    
    /**
     * <p>Constructor for IteratorIterator.</p>
     *
     * @param iterators a {@link java.util.Iterator} object.
     * @param <T> a T object.
     */
    public IteratorIterator(Iterator<T>... iterators) {
        /*
         * We create an ArrayList to hold the list of iterators instead of
         * just calling Arrays.asList(..) because we cannot call the remove()
         * method on an Iterator that we get from Arrays.asList (it is not
         * modifyable).
         */ 
        List<Iterator<T>> iters = new ArrayList<Iterator<T>>(Arrays.asList(iterators));
        m_iterIter = iters.iterator();
    }
    
    /**
     * <p>Constructor for IteratorIterator.</p>
     *
     * @param iterators a {@link java.util.List} object.
     */
    public IteratorIterator(List<Iterator<T>> iterators) {
        List<Iterator<T>> iters = new ArrayList<Iterator<T>>(iterators);
        m_iterIter = iters.iterator();
    }
    
    /**
     * <p>hasNext</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean hasNext() {
        while ((m_currentIter == null || !m_currentIter.hasNext())
                && m_iterIter.hasNext()) {
            m_currentIter = m_iterIter.next();
            m_iterIter.remove();
        }
        
        return (m_currentIter == null ? false : m_currentIter.hasNext());
    }
    
    /**
     * <p>next</p>
     *
     * @return a T object.
     */
    @Override
    public T next() {
        if (m_currentIter == null) {
            m_currentIter = m_iterIter.next();
        }
        return m_currentIter.next();
    }
    
    /**
     * <p>remove</p>
     */
    @Override
    public void remove() {
        m_currentIter.remove();
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
