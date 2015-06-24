/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.config.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>A simple list wrapper for ensuring lists of JAXB objects serialize correctly.
 * You <b>must</b> annotate subclasses with {@link XmlRootElement} and
 * then implement {@link #getObjects()} with an {@link XmlElement} annotation
 * and a call to super.getObjects() so that it gets serialized properly.</p>
 * <p>Example implementation:</p>
 * <blockquote><pre>
 * {@code
 * \@XmlRootElement(name="thingies")
 * public static class ThingyCollection extends JaxbListWrapper<Thingy> {
 *     private static final long serialVersionUID = 1L;
 *
 *     public ThingyCollection() { super(); }
 *     public ThingyCollection(final Collection<? extends Thingy> thingies) {
 *         super(thingies);
 *     }
 *     \@XmlElement(name="thingy")
 *     public List<Thingy> getObjects() {
 *         return super.getObjects();
 *     }
 * }
 * }</pre></blockquote>
 */
@XmlAccessorType(XmlAccessType.NONE)
public class JaxbListWrapper<T> implements Serializable, Iterable<T> {
    private static final long serialVersionUID = 1L;

    private List<T> m_objects = new ArrayList<T>();
    private Integer m_totalCount;

    public List<T> getObjects() {
        return m_objects;
    };
    public void setObjects(final List<? extends T> objects) {
        if (objects == m_objects) return;
        m_objects.clear();
        m_objects.addAll(objects);
    };

    public JaxbListWrapper() {}
    public JaxbListWrapper(final Collection<? extends T> objects) {
        m_objects.addAll(objects);
    }

    @Override
    public Iterator<T> iterator() {
        return m_objects.iterator();
    }

    public T get(final int index) {
        return m_objects.get(index);
    }
    public boolean add(final T obj) {
        return m_objects.add(obj);
    }

    @XmlAttribute(name="count")
    public Integer getCount() {
        if (m_objects.size() == 0) {
            return null;
        } else {
            return m_objects.size();
        }
    }
    public void setCount(final Integer count) {
        // dummy to make JAXB happy
    }
    public int size() {
        return m_objects.size();
    }
    
    @XmlAttribute(name="totalCount")
    public Integer getTotalCount() {
        return m_totalCount == null? getCount() : m_totalCount;
    }
    public void setTotalCount(final Integer totalCount) {
        m_totalCount = totalCount;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_objects == null) ? 0 : m_objects.hashCode());
        result = prime * result + ((m_totalCount == null) ? 0 : m_totalCount.hashCode());
        return result;
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JaxbListWrapper)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final JaxbListWrapper<T> other = (JaxbListWrapper<T>) obj;
        if (m_objects == null) {
            if (other.m_objects != null) {
                return false;
            }
        } else if (!m_objects.equals(other.m_objects)) {
            return false;
        }
        if (getTotalCount() == null) {
            if (other.getTotalCount() != null) {
                return false;
            }
        } else if (!getTotalCount().equals(other.getTotalCount())) {
            return false;
        }
        return true;
    }

    public void add(final int index, final T obj) {
        m_objects.add(index, obj);
    }
    @SuppressWarnings("unchecked")
    public boolean addAll(final Collection<? extends T> objs) {
        if (objs instanceof JaxbListWrapper) {
            return m_objects.addAll(((JaxbListWrapper<? extends T>) objs).getObjects());
        } else {
            return m_objects.addAll(objs);
        }
    }
    @SuppressWarnings("unchecked")
    public boolean addAll(final int index, final Collection<? extends T> objs) {
        if (objs instanceof JaxbListWrapper) {
            return m_objects.addAll(index, ((JaxbListWrapper<? extends T>) objs).getObjects());
        } else {
            return m_objects.addAll(index, objs);
        }
    }
    public void clear() {
        m_objects.clear();
    }
    public boolean contains(final Object obj) {
        return m_objects.contains(obj);
    }
    @SuppressWarnings("unchecked")
    public boolean containsAll(final Collection<?> objs) {
        if (objs instanceof JaxbListWrapper) {
            return m_objects.containsAll(((JaxbListWrapper<? extends T>) objs).getObjects());
        } else {
            return m_objects.containsAll(objs);
        }
    }
    public int indexOf(final Object obj) {
        return m_objects.indexOf(obj);
    }
    public boolean isEmpty() {
        return m_objects.isEmpty();
    }
    public int lastIndexOf(final Object obj) {
        return m_objects.lastIndexOf(obj);
    }
    public ListIterator<T> listIterator() {
        return m_objects.listIterator();
    }
    public ListIterator<T> listIterator(final int index) {
        return m_objects.listIterator(index);
    }
    public boolean remove(final Object obj) {
        return m_objects.remove(obj);
    }
    public T remove(final int index) {
        return m_objects.remove(index);
    }
    @SuppressWarnings("unchecked")
    public boolean removeAll(final Collection<?> objs) {
        if (objs instanceof JaxbListWrapper) {
            return m_objects.removeAll(((JaxbListWrapper<? extends T>) objs).getObjects());
        } else {
            return m_objects.removeAll(objs);
        }
    }
    @SuppressWarnings("unchecked")
    public boolean retainAll(final Collection<?> objs) {
        if (objs instanceof JaxbListWrapper) {
            return m_objects.retainAll(((JaxbListWrapper<? extends T>) objs).getObjects());
        } else {
            return m_objects.retainAll(objs);
        }
    }
    public T set(final int index, final T obj) {
        return m_objects.set(index, obj);
    }
    public List<T> subList(final int fromIndex, final int toIndex) {
        return m_objects.subList(fromIndex, toIndex);
    }
    public Object[] toArray() {
        return m_objects.toArray();
    }
    @SuppressWarnings("hiding")
    public <T> T[] toArray(final T[] type) {
        return m_objects.toArray(type);
    }
}
