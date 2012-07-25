/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.datacollection;

import java.util.Arrays;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.datacollection.model.GroupDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class GroupPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class GroupPanel extends VerticalLayout {

    /** The group form. */
    private final GroupForm form;

    /** The group table. */
    private final GroupTable table;

    /** The add button. */
    private final Button add;

    /**
     * Instantiates a new group panel.
     *
     * @param source the data collection group DTO
     * @param logger the logger
     */
    public GroupPanel(final DataCollectionGroupDTO source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);

        form = new GroupForm(source) {
            @Override
            public void saveGroup(GroupDTO group) {
                logger.info("MIB Group " + group.getName() + " has been updated.");
                table.refreshRowCache();
            }
            @Override
            public void deleteGroup(GroupDTO group) {
                logger.info("MIB Group " + group.getName() + " has been updated.");
                table.removeItem(group);
                table.refreshRowCache();
            }
        };

        table = new GroupTable(source) {
            @Override
            public void updateExternalSource(BeanItem<GroupDTO> item) {
                form.setItemDataSource(item, Arrays.asList(GroupForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
            }
        };

        add = new Button("Add Group", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                GroupDTO group = new GroupDTO();
                group.setName("New Group");
                table.addItem(group);
                table.select(group);
                form.setReadOnly(false);
            }
        });

        setSpacing(true);
        setMargin(true);
        addComponent(table);
        addComponent(add);
        addComponent(form);

        setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
    }

}
