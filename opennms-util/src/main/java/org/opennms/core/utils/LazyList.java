
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

public class LazyList<E> implements List<E> {
    public static interface Loader<E> {
        List<E> load();
    }

    private Loader<E> m_loader;
    private List<E> m_list;
    private boolean m_loaded = false;

    public LazyList(Loader<E> loader) {
        m_loader = loader;
    }

    public Iterator<E> iterator() {
        load();
        return m_list.iterator();
    }

    public int size() {
        load();
        return m_list.size();
    }

    public boolean removeAll(Collection<?> arg0) {
        load();
        return m_list.removeAll(arg0);
    }

    public boolean addAll(Collection<? extends E> arg0) {
        load();
        return m_list.addAll(arg0);
    }

    public void clear() {
        load();
        m_list.clear();
    }

    public boolean contains(Object o) {
        load();
        return m_list.contains(o);
    }

    public boolean containsAll(Collection<?> arg0) {
        load();
        return m_list.containsAll(arg0);
    }

    public boolean isEmpty() {
        load();
        return m_list.isEmpty();
    }

    public boolean remove(Object o) {
        load();
        return m_list.remove(o);
    }

    public Object[] toArray() {
        load();
        return m_list.toArray();
    }

    public <T> T[] toArray(T[] arg0) {
        load();
        return m_list.toArray(arg0);
    }

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

    public boolean isLoaded() {
        return m_loaded;
    }

    public E get(int arg0) {
        load();
        return m_list.get(arg0);
    }

    public boolean add(E o) {
        load();
        return m_list.add(o);
    }

    public void add(int index, E element) {
        load();
        m_list.add(index, element);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        load();
        return m_list.addAll(index, c);
    }

    public int indexOf(Object o) {
        load();
        return m_list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        load();
        return m_list.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        load();
        return m_list.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        load();
        return m_list.listIterator(index);
    }

    public E remove(int index) {
        load();
        return m_list.remove(index);
    }

    public boolean retainAll(Collection<?> c) {
        load();
        return m_list.retainAll(c);
    }

    public E set(int index, E element) {
        load();
        return m_list.set(index, element);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        load();
        return m_list.subList(fromIndex, toIndex);
    }
}
