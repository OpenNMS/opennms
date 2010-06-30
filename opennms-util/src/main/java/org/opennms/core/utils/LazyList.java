
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/

package org.opennms.core.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>LazyList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
    public Iterator<E> iterator() {
        load();
        return m_list.iterator();
    }

    /**
     * <p>size</p>
     *
     * @return a int.
     */
    public int size() {
        load();
        return m_list.size();
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection<?> arg0) {
        load();
        return m_list.removeAll(arg0);
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection<? extends E> arg0) {
        load();
        return m_list.addAll(arg0);
    }

    /**
     * <p>clear</p>
     */
    public void clear() {
        load();
        m_list.clear();
    }

    /** {@inheritDoc} */
    public boolean contains(Object o) {
        load();
        return m_list.contains(o);
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> arg0) {
        load();
        return m_list.containsAll(arg0);
    }

    /**
     * <p>isEmpty</p>
     *
     * @return a boolean.
     */
    public boolean isEmpty() {
        load();
        return m_list.isEmpty();
    }

    /** {@inheritDoc} */
    public boolean remove(Object o) {
        load();
        return m_list.remove(o);
    }

    /**
     * <p>toArray</p>
     *
     * @return an array of {@link java.lang.Object} objects.
     */
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
    public <T> T[] toArray(T[] arg0) {
        load();
        return m_list.toArray(arg0);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
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
    public void add(int index, E element) {
        load();
        m_list.add(index, element);
    }

    /** {@inheritDoc} */
    public boolean addAll(int index, Collection<? extends E> c) {
        load();
        return m_list.addAll(index, c);
    }

    /** {@inheritDoc} */
    public int indexOf(Object o) {
        load();
        return m_list.indexOf(o);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object o) {
        load();
        return m_list.lastIndexOf(o);
    }

    /**
     * <p>listIterator</p>
     *
     * @return a {@link java.util.ListIterator} object.
     */
    public ListIterator<E> listIterator() {
        load();
        return m_list.listIterator();
    }

    /** {@inheritDoc} */
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
    public E remove(int index) {
        load();
        return m_list.remove(index);
    }

    /** {@inheritDoc} */
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
    public E set(int index, E element) {
        load();
        return m_list.set(index, element);
    }

    /** {@inheritDoc} */
    public List<E> subList(int fromIndex, int toIndex) {
        load();
        return m_list.subList(fromIndex, toIndex);
    }
}
