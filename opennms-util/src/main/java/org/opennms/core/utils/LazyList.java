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
     * @param loader a {@link org.opennms.core.utils.LazyList.Loader} object.
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
