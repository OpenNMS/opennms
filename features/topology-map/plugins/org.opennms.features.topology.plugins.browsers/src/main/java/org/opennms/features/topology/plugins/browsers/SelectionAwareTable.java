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

package org.opennms.features.topology.plugins.browsers;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class SelectionAwareTable extends Table implements SelectionListener, SelectionNotifier {

	private static final long serialVersionUID = 2761774077365441249L;

	private final OnmsDaoContainer<?,? extends Serializable> m_container;
	private final Set<SelectionNotifier> m_selectionNotifiers = new CopyOnWriteArraySet<SelectionNotifier>();

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

	/**
	 * Delegate {@link SelectionNotifier} calls to the container.
	 */
	@Override
	public void addSelectionListener(SelectionListener listener) {
		if (listener != null) {
			m_container.addSelectionListener(listener);
			for (SelectionNotifier notifier : m_selectionNotifiers) {
				notifier.addSelectionListener(listener);
			}
		}
	}

	/**
	 * Delegate {@link SelectionNotifier} calls to the container.
	 */
	@Override
	public void removeSelectionListener(SelectionListener listener) {
		m_container.removeSelectionListener(listener);
		for (SelectionNotifier notifier : m_selectionNotifiers) {
			notifier.removeSelectionListener(listener);
		}
	}

	/**
	 * Delegate {@link SelectionNotifier} calls to the container.
	 */
	@Override
	public void setSelectionListeners(Set<SelectionListener> listeners) {
		m_container.setSelectionListeners(listeners);
		for (SelectionNotifier notifier : m_selectionNotifiers) {
			notifier.setSelectionListeners(listeners);
		}
	}

	/**
	 * Call this method before any of the {@link SelectionNotifier} methods to ensure
	 * that the {@link SelectionListener} instances are registered with all of the
	 * {@link ColumnGenerator} classes that also implement {@link SelectionNotifier}.
	 */
	public void setColumnGenerators(Map generators) {
		for (Object key : generators.keySet()) {
			super.addGeneratedColumn(key, (ColumnGenerator)generators.get(key));
			// If any of the column generators are {@link SelectionNotifier} instances,
			// then register this component as a listener for events that they generate.
			try {
				m_selectionNotifiers.add((SelectionNotifier)generators.get(key));
			} catch (ClassCastException e) {}
		}
	}

	/**
	 * Call this method before any of the {@link SelectionNotifier} methods to ensure
	 * that the {@link SelectionListener} instances are registered with all of the
	 * {@link ColumnGenerator} classes that also implement {@link SelectionNotifier}.
	 */
	@Override
	public void setCellStyleGenerator(CellStyleGenerator generator) {
		try {
			((TableAware)generator).setTable(this);
		} catch (ClassCastException e) {}
		super.setCellStyleGenerator(generator);
	}
}
