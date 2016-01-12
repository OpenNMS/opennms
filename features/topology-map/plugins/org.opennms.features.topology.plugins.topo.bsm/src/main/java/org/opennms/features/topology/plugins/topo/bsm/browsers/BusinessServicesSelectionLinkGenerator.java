/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.WidgetContext;
import org.opennms.features.topology.plugins.browsers.ToStringColumnGenerator;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceCriteria;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

public class BusinessServicesSelectionLinkGenerator implements Table.ColumnGenerator {
    private static final long serialVersionUID = 1L;

    private BusinessServiceManager m_businessServiceManager;

    private final String m_idPropertyName;
    private final String m_labelPropertyName;
    private final Table.ColumnGenerator m_columnGenerator;

    public BusinessServicesSelectionLinkGenerator(String idPropertyName, String labelPropertyName) {
        m_idPropertyName = idPropertyName;
        m_labelPropertyName = labelPropertyName;
        m_columnGenerator = new ToStringColumnGenerator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object generateCell(final Table source, final Object itemId, Object columnId) {
        final Property<Long> idProperty = source.getContainerProperty(itemId, m_idPropertyName);
        final Property<String> labelProperty = source.getContainerProperty(itemId, m_labelPropertyName);

        final Object cellValue = m_columnGenerator.generateCell(source, itemId, columnId);
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
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        String businessServiceId = String.valueOf(idProperty.getValue());
                        String businessServiceLabel = labelProperty.getValue();

                        // Retrieve the graph container associated with the
                        // current application context
                        UI ui = UI.getCurrent();
                        WidgetContext context = (WidgetContext) ui;
                        GraphContainer graphContainer = context.getGraphContainer();

                        // Add criteria use to filter for the selected
                        // application
                        BusinessServiceCriteria businessServiceCriteria = new BusinessServiceCriteria(businessServiceId,
                                businessServiceLabel, m_businessServiceManager);
                        graphContainer.addCriteria(businessServiceCriteria);
                        graphContainer.setDirty(true);
                        graphContainer.redoLayout();
                    }
                });
                return button;
            }
        }
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        m_businessServiceManager = businessServiceManager;
    }
}
