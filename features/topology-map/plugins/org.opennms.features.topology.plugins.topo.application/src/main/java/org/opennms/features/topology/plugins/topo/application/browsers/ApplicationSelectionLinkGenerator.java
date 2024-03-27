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
package org.opennms.features.topology.plugins.topo.application.browsers;

import org.opennms.features.topology.api.browsers.AbstractSelectionLinkGenerator;
import org.opennms.features.topology.plugins.browsers.ToStringColumnGenerator;
import org.opennms.features.topology.plugins.topo.application.LegacyApplicationVertex;

import com.vaadin.ui.Button;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.themes.BaseTheme;

public class ApplicationSelectionLinkGenerator extends AbstractSelectionLinkGenerator {

	private final String labelPropertyName;
	private final String idPropertyName;
	private final Table.ColumnGenerator columnGenerator;

    public ApplicationSelectionLinkGenerator(String idPropertyName, String labelPropertyName) {
		this.idPropertyName = idPropertyName;
		this.labelPropertyName = labelPropertyName;
		this.columnGenerator = new ToStringColumnGenerator();
	}

	@Override
	public Object generateCell(final Table source, final Object itemId, Object columnId) {
		final Property<Integer> idProperty = source.getContainerProperty(itemId, idPropertyName);
		final Property<String> labelProperty = source.getContainerProperty(itemId, labelPropertyName);
		final Object cellValue = columnGenerator.generateCell(source, itemId, columnId);
		if (cellValue == null) {
			return null;
		} else {
			if (idProperty.getValue() == null) {
				return cellValue;
			} else {
				Button button = new Button(cellValue.toString());
				button.setStyleName(BaseTheme.BUTTON_LINK);
				button.setDescription(idProperty.getValue().toString());
				button.addClickListener(new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						final Integer applicationId = idProperty.getValue();
						final String applicationName = labelProperty.getValue();
						final LegacyApplicationVertex vertex = new LegacyApplicationVertex(applicationId.toString(), applicationName);
						fireVertexUpdatedEvent(vertex);
					}
				});
				return button;
			}
		}
	}
}
