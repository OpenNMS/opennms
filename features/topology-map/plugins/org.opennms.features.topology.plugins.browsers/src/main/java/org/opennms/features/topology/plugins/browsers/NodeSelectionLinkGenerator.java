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

import java.util.*;

import org.opennms.features.topology.api.*;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.BaseTheme;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.osgi.EventProxy;

public class NodeSelectionLinkGenerator implements ColumnGenerator {

    private class NSLGVertexRef extends AbstractVertexRef{

        public NSLGVertexRef(String namespace, String id, String label) {
            super(namespace, id, label);
        }
    }

	private static final long serialVersionUID = -1072007643387089006L;

	private final String m_nodeIdProperty;
	private final ColumnGenerator m_generator;

	/**
	 * TODO: Fix concurrent access to this field
	 */
	private Collection<SelectionListener> m_selectionListeners = new HashSet<SelectionListener>();
    private EventProxy m_eventProxy;

    public NodeSelectionLinkGenerator(String nodeIdProperty) {
		this(nodeIdProperty, new ToStringColumnGenerator());
	}

	public NodeSelectionLinkGenerator(String nodeIdProperty, ColumnGenerator generator) {
		m_nodeIdProperty = nodeIdProperty;
		m_generator = generator;
	}

	@Override
	public Object generateCell(final Table source, final Object itemId, Object columnId) {
		final Property<Integer> nodeIdProperty = source.getContainerProperty(itemId, m_nodeIdProperty);
		Object cellValue = m_generator.generateCell(source, itemId, columnId);
		if (cellValue == null) {
			return null;
		} else {
			if (nodeIdProperty.getValue() == null) {
				return cellValue;
			} else {
				Button button = new Button(cellValue.toString());
				button.setStyleName(BaseTheme.BUTTON_LINK);
				button.setDescription(nodeIdProperty.getValue().toString());
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {

                        fireVertexUpdatedEvent(Arrays.asList(nodeIdProperty.getValue()));
                    }
                });
				return button;
			}
		}
	}

    private List<VertexRef> getVertexRefsForIds(Integer nodeIdProperty) {
        VertexRef tempRef = new NSLGVertexRef("nodes", String.valueOf(nodeIdProperty), "");
        return Arrays.asList(tempRef);
    }

    protected void fireVertexUpdatedEvent(List<Integer> nodeIds) {
        Set<VertexRef> vertexRefs = new HashSet<VertexRef>();
        for (Integer id : nodeIds) {
            VertexRef vRef = new AbstractVertexRef("nodes", String.valueOf(id),"");
            vertexRefs.add(vRef);
        }
        getEventProxy().fireEvent(new VerticesUpdateManager.VerticesUpdateEvent(vertexRefs));
    }

    public void setEventProxy(EventProxy eventProxy) {
        this.m_eventProxy = eventProxy;
    }

    public EventProxy getEventProxy() {
        return m_eventProxy;
    }
}
