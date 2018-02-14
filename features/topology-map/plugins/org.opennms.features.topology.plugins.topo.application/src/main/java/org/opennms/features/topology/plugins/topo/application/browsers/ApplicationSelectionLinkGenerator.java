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

package org.opennms.features.topology.plugins.topo.application.browsers;

import org.opennms.features.topology.api.browsers.AbstractSelectionLinkGenerator;
import org.opennms.features.topology.plugins.browsers.ToStringColumnGenerator;
import org.opennms.features.topology.plugins.topo.application.ApplicationVertex;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

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

		Object cellValue = columnGenerator.generateCell(source, itemId, columnId);
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
						Integer applicationId = idProperty.getValue();
						String applicationName = labelProperty.getValue();
						ApplicationVertex vertex = new ApplicationVertex(applicationId.toString(), applicationName);
						fireVertexUpdatedEvent(vertex);
					}
				});
				return button;
			}
		}
	}
}
