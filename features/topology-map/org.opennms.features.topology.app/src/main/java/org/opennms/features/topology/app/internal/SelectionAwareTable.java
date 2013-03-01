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

package org.opennms.features.topology.app.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.app.internal.support.OnmsDaoContainer;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class SelectionAwareTable extends Table implements SelectionListener, SelectionNotifier {

	private static final long serialVersionUID = 2761774077365441249L;

	private final OnmsDaoContainer<?,? extends Serializable> m_container;

	/**
	 *  Leave OnmsDaoContainer without generics; the Aries blueprint code cannot match up
	 *  the arguments if you put the generic types in.
	 */
	@SuppressWarnings("unchecked")
	public SelectionAwareTable(String caption, OnmsDaoContainer container) {
		super(caption, container);
		m_container = container;
	}

	@Override
	public void containerItemSetChange(Container.ItemSetChangeEvent event) {
		refreshRowCache();
		super.containerItemSetChange(event);
	}

	@Override
	public void selectionChanged(SelectionContext selectionManager) {
		m_container.selectionChanged(selectionManager);
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		m_container.addSelectionListener(listener);
	}

	@Override
	public void removeSelectionListener(SelectionListener listener) {
		m_container.removeSelectionListener(listener);
	}

	@Override
	public void setSelectionListeners(Set<SelectionListener> listeners) {
		m_container.setSelectionListeners(listeners);
	}

	public void setColumnGenerators(Map generators) {
		for (Object key : generators.keySet()) {
			super.addGeneratedColumn(key, (ColumnGenerator)generators.get(key));
		}
	}
}
