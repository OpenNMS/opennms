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

import java.util.Objects;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.plugins.browsers.ToStringColumnGenerator;
import org.opennms.features.topology.plugins.topo.application.ApplicationCriteria;

public class ApplicationSelectionLinkGenerator implements Table.ColumnGenerator {

    public ApplicationSelectionLinkGenerator(String idPropertyName, GraphContainer graphContainer) {
		this.idPropertyName = idPropertyName;
		this.columnGenerator = new ToStringColumnGenerator();
	}

	private final String idPropertyName;
	private final Table.ColumnGenerator columnGenerator;

	@Override
	public Object generateCell(final Table source, final Object itemId, Object columnId) {
		final Property<Integer> idProperty = source.getContainerProperty(itemId, idPropertyName);
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
				/*
				button.addClickListener(new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
							ApplicationCriteria applicationCriteria = graphContainer.findSingleCriteria(ApplicationCriteria.class);
							if (applicationCriteria == null) {
								applicationCriteria = new ApplicationCriteria();
								graphContainer.addCriteria(applicationCriteria);
							}
							if (!Objects.equals(applicationCriteria.getApplicationId(), String.valueOf(idProperty.getValue()))) {
								applicationCriteria.setApplicationId(String.valueOf(idProperty.getValue()));
								graphContainer.setDirty(true);
								graphContainer.redoLayout();
							}
					}
				});
				*/
				return button;
			}
		}
	}
}
