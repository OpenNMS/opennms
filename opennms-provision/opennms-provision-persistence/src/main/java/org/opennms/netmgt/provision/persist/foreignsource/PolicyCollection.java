/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>PolicyCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="policies")
public class PolicyCollection implements List<PluginConfig> {
    LinkedList<PluginConfig> m_list = null;

	/**
	 * <p>Constructor for PolicyCollection.</p>
	 */
	public PolicyCollection() {
	    m_list = new LinkedList<PluginConfig>();
    }

    /**
     * <p>Constructor for PolicyCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public PolicyCollection(Collection<? extends PluginConfig> c) {
        m_list = new LinkedList<PluginConfig>(c);
    }

    /**
     * <p>getPolicies</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="policy")
    public List<PluginConfig> getPolicies() {
        return this;
    }

    /**
     * <p>setPolicies</p>
     *
     * @param policies a {@link java.util.List} object.
     */
    public void setPolicies(List<PluginConfig> policies) {
        clear();
        addAll(policies);
    }
    
    public boolean add(final PluginConfig config) {
        return m_list.add(config);
    }

    public void add(final int index, final PluginConfig config) {
        m_list.add(index, config);
    }

    public boolean addAll(final Collection<? extends PluginConfig> configs) {
        return m_list.addAll(configs);
    }

    public boolean addAll(final int index, final Collection<? extends PluginConfig> configs) {
        return m_list.addAll(index, configs);
    }

    public void clear() {
        m_list.clear();
    }

    public boolean contains(final Object object) {
        return m_list.contains(object);
    }

    public boolean containsAll(final Collection<?> objects) {
        return m_list.containsAll(objects);
    }

    public PluginConfig get(final int index) {
        return m_list.get(index);
    }

    public int indexOf(final Object object) {
        return m_list.indexOf(object);
    }

    public boolean isEmpty() {
        return m_list.isEmpty();
    }

    public Iterator<PluginConfig> iterator() {
        return m_list.iterator();
    }

    public int lastIndexOf(final Object object) {
        return m_list.lastIndexOf(object);
    }

    public ListIterator<PluginConfig> listIterator() {
        return m_list.listIterator();
    }

    public ListIterator<PluginConfig> listIterator(final int index) {
        return m_list.listIterator(index);
    }

    public boolean remove(final Object object) {
        return m_list.remove(object);
    }

    public PluginConfig remove(final int index) {
        return m_list.remove(index);
    }

    public boolean removeAll(final Collection<?> objects) {
        return m_list.removeAll(objects);
    }

    public boolean retainAll(final Collection<?> objects) {
        return m_list.retainAll(objects);
    }

    public PluginConfig set(final int index, PluginConfig config) {
        return m_list.set(index, config);
    }

    public int size() {
        return m_list.size();
    }

    public List<PluginConfig> subList(final int start, int end) {
        return m_list.subList(start, end);
    }

    public Object[] toArray() {
        return m_list.toArray();
    }

    public <T> T[] toArray(final T[] type) {
        return m_list.toArray(type);
    }
}

