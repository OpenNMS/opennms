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

import org.opennms.features.topology.api.browsers.AbstractSelectionLinkGenerator;
import org.opennms.features.topology.plugins.browsers.ToStringColumnGenerator;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceVertex;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

public class BusinessServicesSelectionLinkGenerator extends AbstractSelectionLinkGenerator {
    private static final long serialVersionUID = 1L;

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
                        Long businessServiceId = idProperty.getValue();
                        String businessServiceLabel = labelProperty.getValue();
                        BusinessServiceVertex vertex = new BusinessServiceVertex(
                                businessServiceId,
                                businessServiceLabel,
                                0 /* does not matter in this case*/);
                        fireVertexUpdatedEvent(vertex);
                    }
                });
                return button;
            }
        }
    }
}
