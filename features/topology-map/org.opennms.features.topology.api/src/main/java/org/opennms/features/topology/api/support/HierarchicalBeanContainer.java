/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanContainer;

public abstract class HierarchicalBeanContainer<K, T> extends BeanContainer<K,T> implements Container.Hierarchical {

	private static final long serialVersionUID = 194248426656888195L;

	public HierarchicalBeanContainer(Class<? super T> type) {
		super(type);
	}

	/**
	 * This is a naive implementation of this method that just checks the size of
	 * the collection returned by {@link #getChildren(Object)}.
	 */
	@Override
	public boolean hasChildren(Object key) {
		return getChildren(key).size() > 0;
	}

	/**
	 * This is a naive implementation of this method that just checks to see if
	 * {@link #getParent(Object)} returns null.
	 */
	@Override
	public boolean isRoot(Object key) {
		return (getParent(key) == null);
	}

	/**
	 * Expose {@link #fireItemSetChange()} as a public method.
	 */
        @Override
	public void fireItemSetChange() {
		super.fireItemSetChange();
	}
}
