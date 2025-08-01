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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LazyList<E> implements List<E> {
    public static interface Loader<E> {
        List<E> load();
    }

    private Loader<E> m_loader;
    private List<E> m_list;
    private boolean m_loaded = false;

    /**
     * <p>Constructor for LazyList.</p>
     *
     * @param loader a {@link org.opennms.core.collections.LazyList.Loader} object.
     */
    public LazyList(Loader<E> loader) {
        m_loader = loader;
    }

    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<E> iterator() {
        load();
        return m_list.iterator();
    }

    /**
     * <p>size</p>
     *
     * @return a int.
     */
    @Override
    public int size() {
        load();
        return m_list.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> arg0) {
        load();
        return m_list.removeAll(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends E> arg0) {
        load();
        return m_list.addAll(arg0);
    }

    /**
     * <p>clear</p>
     */
    @Override
    public void clear() {
        load();
        m_list.clear();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o) {
        load();
        return m_list.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> arg0) {
        load();
        return m_list.containsAll(arg0);
    }

    /**
     * <p>isEmpty</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isEmpty() {
        load();
        return m_list.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object o) {
        load();
        return m_list.remove(o);
    }

    /**
     * <p>toArray</p>
     *
     * @return an array of {@link java.lang.Object} objects.
     */
    @Override
    public Object[] toArray() {
        load();
        return m_list.toArray();
    }

    /**
     * <p>toArray</p>
     *
     * @param arg0 an array of T objects.
     * @param <T> a T object.
     * @return an array of T objects.
     */
    @Override
    public <T> T[] toArray(T[] arg0) {
        load();
        return m_list.toArray(arg0);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        load();
        return super.toString();
    }

    private void load() {
        if (!m_loaded) {
            m_list = m_loader.load();
            m_loaded = true;
        }
    }

    /**
     * <p>isLoaded</p>
     *
     * @return a boolean.
     */
    public boolean isLoaded() {
        return m_loaded;
    }

    /** {@inheritDoc} */
    @Override
    public E get(int arg0) {
        load();
        return m_list.get(arg0);
    }

    /**
     * <p>add</p>
     *
     * @param o a E object.
     * @return a boolean.
     */
    @Override
    public boolean add(E o) {
        load();
        return m_list.add(o);
    }

    /**
     * <p>add</p>
     *
     * @param index a int.
     * @param element a E object.
     */
    @Override
    public void add(int index, E element) {
        load();
        m_list.add(index, element);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        load();
        return m_list.addAll(index, c);
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(Object o) {
        load();
        return m_list.indexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public int lastIndexOf(Object o) {
        load();
        return m_list.lastIndexOf(o);
    }

    /**
     * <p>listIterator</p>
     *
     * @return a {@link java.util.ListIterator} object.
     */
    @Override
    public ListIterator<E> listIterator() {
        load();
        return m_list.listIterator();
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator<E> listIterator(int index) {
        load();
        return m_list.listIterator(index);
    }

    /**
     * <p>remove</p>
     *
     * @param index a int.
     * @return a E object.
     */
    @Override
    public E remove(int index) {
        load();
        return m_list.remove(index);
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> c) {
        load();
        return m_list.retainAll(c);
    }

    /**
     * <p>set</p>
     *
     * @param index a int.
     * @param element a E object.
     * @return a E object.
     */
    @Override
    public E set(int index, E element) {
        load();
        return m_list.set(index, element);
    }

    /** {@inheritDoc} */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        load();
        return m_list.subList(fromIndex, toIndex);
    }
}
