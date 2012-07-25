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

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class DataCollectionGroupPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class DataCollectionGroupPanel extends Panel implements TabSheet.SelectedTabChangeListener {

    /**
     * Instantiates a new data collection group panel.
     *
     * @param group the group
     * @param logger the logger
     */
    public DataCollectionGroupPanel(final DatacollectionGroup group, final Logger logger) {
        setCaption("Data Collection");
        addStyleName(Runo.PANEL_LIGHT);

        // Creating DTO Object
        
        final DataCollectionGroupDTO dto = getDataCollectionGroupDTO(group);

        // Data Collection Group - Main Fields

        final TextField groupName = new TextField("Data Collection Group Name");
        groupName.setPropertyDataSource(new ObjectProperty<String>(dto.getName()));
        groupName.setNullSettingAllowed(false);
        groupName.setImmediate(true);

        // Button Toolbar

        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(new Button("Cancel Processing", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                cancelProcessing();
                logger.info("Data collection processing has been canceled");
            }
        }));
        toolbar.addComponent(new Button("Generate Data Collection File", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                generateDataCollectionFile(getOnmsDataCollection(dto));
                logger.info("The data collection have been saved.");
            }
        }));

        // Tab Panel

        final TabSheet tabs = new TabSheet();
        tabs.setStyleName(Runo.TABSHEET_SMALL);
        tabs.setSizeFull();
        tabs.addTab(new ResourceTypePanel(dto, logger), "Resource Types");
        tabs.addTab(new GroupPanel(dto, logger), "MIB Groups");
        tabs.addTab(new SystemDefPanel(dto, logger), "System Definitions");

        // Main Layout

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(toolbar);
        mainLayout.addComponent(groupName);
        mainLayout.addComponent(tabs);
        mainLayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.TabSheet.SelectedTabChangeListener#selectedTabChange(com.vaadin.ui.TabSheet.SelectedTabChangeEvent)
     */
    public void selectedTabChange(SelectedTabChangeEvent event) {
        TabSheet tabsheet = event.getTabSheet();
        Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
        if (tab != null) {
            getWindow().showNotification("Selected tab: " + tab.getCaption());
        }
    }    

    /**
     * Gets the OpenNMS data collection group.
     *
     * @param dto the data collection group DTO
     * @return the OpenNMS data collection group
     */
    public DatacollectionGroup getOnmsDataCollection(DataCollectionGroupDTO dto) {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        return mapper.map(dto, DatacollectionGroup.class);
    }

    /**
     * Gets the data collection group DTO.
     *
     * @param group the OpenNMS data collection group
     * @return the data collection group DTO
     */
    public DataCollectionGroupDTO getDataCollectionGroupDTO(DatacollectionGroup group) {
        MapperFacade mapper = new DefaultMapperFactory.Builder().build().getMapperFacade();
        return mapper.map(group, DataCollectionGroupDTO.class);
    }

    /**
     * Cancel processing.
     */
    public abstract void cancelProcessing();

    /**
     * Generate event file.
     *
     * @param group the OpenNMS Data Collection Group
     */
    public abstract void generateDataCollectionFile(DatacollectionGroup group);

}
