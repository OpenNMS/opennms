/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opennms.features.topology.api.DefaultSelectionContext;
import org.opennms.features.topology.api.SelectionContext;
import org.opennms.features.topology.api.SelectionListener;
import org.opennms.features.topology.api.SelectionNotifier;
import org.opennms.features.topology.api.topo.AbstractVertexRef;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.BaseTheme;

public class NodeSelectionLinkGenerator implements ColumnGenerator, SelectionNotifier {

	private static final long serialVersionUID = -1072007643387089006L;

	private final String m_nodeIdProperty;
	private final ColumnGenerator m_generator;

	/**
	 * TODO: Fix concurrent access to this field
	 */
	private Collection<SelectionListener> m_selectionListeners = new HashSet<SelectionListener>();

	public NodeSelectionLinkGenerator(String nodeIdProperty) {
		this(nodeIdProperty, new ToStringColumnGenerator());
	}

	public NodeSelectionLinkGenerator(String nodeIdProperty, ColumnGenerator generator) {
		m_nodeIdProperty = nodeIdProperty;
		m_generator = generator;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		final Property nodeIdProperty = source.getContainerProperty(itemId, m_nodeIdProperty);
		Object cellValue = m_generator.generateCell(source, itemId, columnId);
		if (cellValue == null) {
			return null;
		} else {
			if (nodeIdProperty.getValue() == null) {
				return cellValue;
			} else {
				Button button = new Button((String)cellValue);
				button.setStyleName(BaseTheme.BUTTON_LINK);
				button.setDescription(nodeIdProperty.getValue().toString());
				button.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						SelectionContext context = new DefaultSelectionContext();
						context.selectVertexRefs(Collections.singleton(new AbstractVertexRef("nodes", nodeIdProperty.getValue().toString(), nodeIdProperty.getValue().toString())));
						fireSelectionChangedEvent(context);
					}
				});
				return button;
			}
		}
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		if (listener != null) {
			m_selectionListeners.add(listener);
		}
	}
	
	@Override
	public void setSelectionListeners(Set<SelectionListener> listeners) {
		m_selectionListeners = listeners;
	}
	
	@Override
	public void removeSelectionListener(SelectionListener listener) {
		m_selectionListeners.remove(listener);
	}

	protected void fireSelectionChangedEvent(SelectionContext context) {
		for (SelectionListener listener : m_selectionListeners) {
			listener.selectionChanged(context);
		}
	}
}
