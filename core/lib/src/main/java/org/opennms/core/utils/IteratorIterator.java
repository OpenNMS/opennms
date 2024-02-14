/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
     */
    @SafeVarargs
    public IteratorIterator(final Iterator<T>... iterators) {
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
        
        return (m_currentIter != null && m_currentIter.hasNext());
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
