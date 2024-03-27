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
	 * @param loader a {@link org.opennms.core.collections.LazySet.Loader} object.
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
