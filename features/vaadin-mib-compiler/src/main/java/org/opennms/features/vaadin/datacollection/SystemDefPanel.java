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

import org.opennms.features.vaadin.datacollection.model.CollectDTO;
import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.datacollection.model.SystemDefDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class SystemDefPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefPanel extends VerticalLayout {

    /** The form. */
    private final SystemDefForm form;

    /** The table. */
    private final SystemDefTable table;

    /** The add button. */
    private final Button add;

    /**
     * Instantiates a new system definition panel.
     *
     * @param source the data collection group DTO
     * @param logger the logger
     */
    public SystemDefPanel(final DataCollectionGroupDTO source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);

        form = new SystemDefForm(source) {
            @Override
            public void saveSystemDef(SystemDefDTO systemDef) {
                logger.info("System Definition " + systemDef.getName() + " has been updated.");
                table.refreshRowCache();
            }
            @Override
            public void deleteSystemDef(SystemDefDTO systemDef) {
                logger.info("System Definition " + systemDef.getName() + " has been removed.");
                table.removeItem(systemDef);
                table.refreshRowCache();
            }
        };

        table = new SystemDefTable(source) {
            @Override
            public void updateExternalSource(BeanItem<SystemDefDTO> item) {
                form.setItemDataSource(item, Arrays.asList(SystemDefForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
            }
        };

        add = new Button("Add System Definition", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                SystemDefDTO sysDef = new SystemDefDTO();
                sysDef.setName("New System Definition");
                sysDef.setSysoidMask(".1.3.4.1.4.1.");
                sysDef.setCollect(new CollectDTO());
                table.addItem(sysDef);
                table.select(sysDef);
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
