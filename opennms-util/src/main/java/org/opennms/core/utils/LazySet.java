/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
import java.util.Set;


/**
 * <p>LazySet class.</p>
 */
public class LazySet<E> extends JdbcSet<E> {
	
	public static interface Loader<E> {
		Set<E> load();
	}

	private Loader<E> m_loader;
	private boolean m_loaded = false;

	/**
	 * <p>Constructor for LazySet.</p>
	 *
	 * @param loader a {@link org.opennms.core.utils.LazySet.Loader} object.
	 */
	public LazySet(Loader<E> loader) {
		m_loader = loader;
	}

        /** {@inheritDoc} */
        @Override
	public Iterator<E> iterator() {
		load();
		return super.iterator();
	}

        /** {@inheritDoc} */
        @Override
	public int size() {
		load();
		return super.size();
	}

        /** {@inheritDoc} */
        @Override
	public boolean removeAll(Collection<?> arg0) {
		load();
		return super.removeAll(arg0);
	}

        /** {@inheritDoc} */
        @Override
	public boolean addAll(Collection<? extends E> arg0) {
		load();
		return super.addAll(arg0);
	}

        /** {@inheritDoc} */
        @Override
	public void clear() {
		load();
		super.clear();
	}

        /** {@inheritDoc} */
        @Override
	public boolean contains(Object o) {
		load();
		return super.contains(o);
	}

        /** {@inheritDoc} */
        @Override
	public boolean containsAll(Collection<?> arg0) {
		load();
		return super.containsAll(arg0);
	}

        /** {@inheritDoc} */
        @Override
	public boolean isEmpty() {
		load();
		return super.isEmpty();
	}

        /** {@inheritDoc} */
        @Override
	public boolean remove(Object o) {
		load();
		return super.remove(o);
	}

        /** {@inheritDoc} */
        @Override
	public boolean retainAll(Collection<?> arg0) {
		load();
		return super.retainAll(arg0);
	}

        /** {@inheritDoc} */
        @Override
	public Object[] toArray() {
		load();
		return super.toArray();
	}

        /** {@inheritDoc} */
        @Override
	public <T> T[] toArray(T[] arg0) {
		load();
		return super.toArray(arg0);
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
			setElements(m_loader.load());
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
	
	

}
