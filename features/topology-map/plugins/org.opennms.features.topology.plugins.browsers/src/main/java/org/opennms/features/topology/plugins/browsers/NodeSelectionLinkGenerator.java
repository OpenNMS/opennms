/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.browsers;

import org.opennms.features.topology.api.browsers.AbstractSelectionLinkGenerator;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.BaseTheme;

public class NodeSelectionLinkGenerator extends AbstractSelectionLinkGenerator {

	private static final long serialVersionUID = -1072007643387089006L;

	private final String m_nodeIdProperty;
    private final String m_nodeLabelProperty;
	private final ColumnGenerator m_generator;

    public NodeSelectionLinkGenerator(String nodeIdProperty, String nodeLabelProperty) {
		this(nodeIdProperty, nodeLabelProperty, new ToStringColumnGenerator());
	}

	private NodeSelectionLinkGenerator(String nodeIdProperty, String nodeLabelProperty, ColumnGenerator generator) {
		m_nodeIdProperty = nodeIdProperty;
        m_nodeLabelProperty = nodeLabelProperty;
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
                        Integer nodeId = nodeIdProperty.getValue();
                        String nodeLabel = (String)source.getContainerProperty(itemId, m_nodeLabelProperty).getValue();
						VertexRef vertexRef = new DefaultVertexRef("nodes", String.valueOf(nodeId), nodeLabel);
                        fireVertexUpdatedEvent(vertexRef);
                    }
                });
				return button;
			}
		}
	}
}
