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
        if (policies == this) return;
        clear();
        addAll(policies);
    }
    
    @Override
    public boolean add(final PluginConfig config) {
        return m_list.add(config);
    }

    @Override
    public void add(final int index, final PluginConfig config) {
        m_list.add(index, config);
    }

    @Override
    public boolean addAll(final Collection<? extends PluginConfig> configs) {
        return m_list.addAll(configs);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends PluginConfig> configs) {
        return m_list.addAll(index, configs);
    }

    @Override
    public void clear() {
        m_list.clear();
    }

    @Override
    public boolean contains(final Object object) {
        return m_list.contains(object);
    }

    @Override
    public boolean containsAll(final Collection<?> objects) {
        return m_list.containsAll(objects);
    }

    @Override
    public PluginConfig get(final int index) {
        return m_list.get(index);
    }

    @Override
    public int indexOf(final Object object) {
        return m_list.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return m_list.isEmpty();
    }

    @Override
    public Iterator<PluginConfig> iterator() {
        return m_list.iterator();
    }

    @Override
    public int lastIndexOf(final Object object) {
        return m_list.lastIndexOf(object);
    }

    @Override
    public ListIterator<PluginConfig> listIterator() {
        return m_list.listIterator();
    }

    @Override
    public ListIterator<PluginConfig> listIterator(final int index) {
        return m_list.listIterator(index);
    }

    @Override
    public boolean remove(final Object object) {
        return m_list.remove(object);
    }

    @Override
    public PluginConfig remove(final int index) {
        return m_list.remove(index);
    }

    @Override
    public boolean removeAll(final Collection<?> objects) {
        return m_list.removeAll(objects);
    }

    @Override
    public boolean retainAll(final Collection<?> objects) {
        return m_list.retainAll(objects);
    }

    @Override
    public PluginConfig set(final int index, PluginConfig config) {
        return m_list.set(index, config);
    }

    @Override
    public int size() {
        return m_list.size();
    }

    @Override
    public List<PluginConfig> subList(final int start, int end) {
        return m_list.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return m_list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] type) {
        return m_list.toArray(type);
    }
}

