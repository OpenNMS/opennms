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

package org.opennms.features.topology.plugins.topo.bsm.browsers;

import java.util.Objects;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.plugins.browsers.ToStringColumnGenerator;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceCriteria;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

public class BusinessServicesSelectionLinkGenerator implements Table.ColumnGenerator {

    public BusinessServicesSelectionLinkGenerator(String idPropertyName) {
		this.idPropertyName = idPropertyName;
		this.columnGenerator = new ToStringColumnGenerator();
	}

	private final String idPropertyName;
	private final Table.ColumnGenerator columnGenerator;

	@Override
	public Object generateCell(final Table source, final Object itemId, Object columnId) {
		// TODO MVR this is exactly the same code as ApplicationSElectionLinkGenerator -> generalize
		final Property<Long> idProperty = source.getContainerProperty(itemId, idPropertyName);
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
						// Retrieve the graph container associated with the current application context
						UI ui = UI.getCurrent();
						WidgetContext context = (WidgetContext)ui;
						GraphContainer graphContainer = context.getGraphContainer();

						BusinessServiceCriteria businessServiceCriteria = graphContainer.findSingleCriteria(BusinessServiceCriteria.class);
						if (businessServiceCriteria == null) {
							businessServiceCriteria = new BusinessServiceCriteria();
						}

						String businessServiceId = String.valueOf(idProperty.getValue());
						if (!Objects.equals(businessServiceCriteria.getBusinessServiceId(), businessServiceId)) {
							businessServiceCriteria.setBusinessServiceId(businessServiceId);
							graphContainer.setDirty(true);
							graphContainer.redoLayout();
						}
					}
				});
				return button;
			}
		}
	}
}
