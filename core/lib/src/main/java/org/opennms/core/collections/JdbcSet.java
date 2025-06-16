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
package org.opennms.core.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.core.utils.IteratorIterator;


/**
 * <p>JdbcSet class.</p>
 */
public class JdbcSet<E> extends AbstractSet<E> {
    
    Set<E> m_added = new LinkedHashSet<>();
    Set<E> m_entries = new LinkedHashSet<>();
    Set<E> m_removed = new LinkedHashSet<>();
    
    /**
     * <p>Constructor for JdbcSet.</p>
     *
     * @param c a {@link java.util.Collection} object.
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
