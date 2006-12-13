//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.core.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


public class LazySet<E> extends JdbcSet<E> {
	
	public static interface Loader<E> {
		Set<E> load();
	}

	private Loader<E> m_loader;
	private boolean m_loaded = false;

	public LazySet(Loader<E> loader) {
		m_loader = loader;
	}

        @Override
	public Iterator<E> iterator() {
		load();
		return super.iterator();
	}

        @Override
	public int size() {
		load();
		return super.size();
	}

        @Override
	public boolean removeAll(Collection<?> arg0) {
		load();
		return super.removeAll(arg0);
	}

        @Override
	public boolean addAll(Collection<? extends E> arg0) {
		load();
		return super.addAll(arg0);
	}

        @Override
	public void clear() {
		load();
		super.clear();
	}

        @Override
	public boolean contains(Object o) {
		load();
		return super.contains(o);
	}

        @Override
	public boolean containsAll(Collection<?> arg0) {
		load();
		return super.containsAll(arg0);
	}

        @Override
	public boolean isEmpty() {
		load();
		return super.isEmpty();
	}

        @Override
	public boolean remove(Object o) {
		load();
		return super.remove(o);
	}

        @Override
	public boolean retainAll(Collection<?> arg0) {
		load();
		return super.retainAll(arg0);
	}

        @Override
	public Object[] toArray() {
		load();
		return super.toArray();
	}

        @Override
	public <T> T[] toArray(T[] arg0) {
		load();
		return super.toArray(arg0);
	}

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
	
	public boolean isLoaded() {
		return m_loaded;
	}
	
	

}
