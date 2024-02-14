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
package org.opennms.core.config.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

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
@SuppressWarnings("java:S2162")
public class JaxbListWrapper<T> implements Serializable, Iterable<T> {
    private static final long serialVersionUID = 1L;

    private transient List<T> m_objects = new ArrayList<>();
    private Integer m_totalCount;
    private Integer m_offset = 0;

    public List<T> getObjects() {
        return m_objects;
    }

    public void setObjects(final List<? extends T> objects) {
        if (m_objects.equals(objects)) {
            return;
        }
        m_objects.clear();
        m_objects.addAll(objects);
    }

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
        if (m_objects.isEmpty()) {
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

    @XmlAttribute(name="offset")
    public Integer getOffset() {
        return m_offset == null? 0 : m_offset;
    }

    public void setOffset(final Integer offset) {
        m_offset = offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_objects, m_offset, m_totalCount);
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
        final JaxbListWrapper<T> that = (JaxbListWrapper<T>) obj;
        return Objects.equals(this.m_objects, that.m_objects)
                && Objects.equals(this.getOffset(), that.getOffset())
                && Objects.equals(this.getTotalCount(), that.getTotalCount());
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

    @Override
    public String toString() {
        return this.getClass().getName() + " [offset=" + getOffset() + ", count=" + getCount() + ", totalCount=" + getTotalCount() + ", objects=" + m_objects + "]";
    }
}
