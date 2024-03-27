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
package org.opennms.features.topology.plugins.browsers;

import org.opennms.features.topology.api.browsers.AbstractSelectionLinkGenerator;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.v7.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.ColumnGenerator;
import com.vaadin.v7.ui.themes.BaseTheme;

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
