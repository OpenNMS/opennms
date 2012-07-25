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
import java.util.Collection;

import org.opennms.features.vaadin.datacollection.model.DataCollectionGroupDTO;
import org.opennms.features.vaadin.datacollection.model.PersistenceSelectorStrategyDTO;
import org.opennms.features.vaadin.datacollection.model.ResourceTypeDTO;
import org.opennms.features.vaadin.datacollection.model.StorageStrategyDTO;
import org.opennms.features.vaadin.mibcompiler.api.Logger;
import org.opennms.netmgt.collectd.PersistAllSelectorStrategy;
import org.opennms.netmgt.dao.support.IndexStorageStrategy;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class ResourceTypePanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypePanel extends VerticalLayout {

    /** The form. */
    private final ResourceTypeForm form;

    /** The table. */
    private final ResourceTypeTable table;

    /** The add button. */
    private final Button add;

    /**
     * Instantiates a new resource type panel.
     *
     * @param source the data collection group DTO
     * @param logger the logger
     */
    public ResourceTypePanel(final DataCollectionGroupDTO source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);

        form = new ResourceTypeForm() {
            @Override
            public void saveResourceType(ResourceTypeDTO resourceType) {
                logger.info("Resource type " + resourceType.getName() + " has been updated.");
                table.refreshRowCache();
            }
            @Override
            public void deleteResourceType(ResourceTypeDTO resourceType) {
                logger.info("Resource type " + resourceType.getName() + " has been removed.");
                table.removeItem(resourceType);
                table.refreshRowCache();
            }
        };

        table = new ResourceTypeTable(source) {
            @Override
            public void updateExternalSource(BeanItem<ResourceTypeDTO> item) {
                form.setItemDataSource(item, Arrays.asList(ResourceTypeForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
            }
        };

        add = new Button("Add Resource Type", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                ResourceTypeDTO rt = new ResourceTypeDTO();
                rt.setName("New Resource Type");
                rt.setLabel("New Resource Type");
                PersistenceSelectorStrategyDTO persistence = new PersistenceSelectorStrategyDTO();
                persistence.setClazz(PersistAllSelectorStrategy.class.getName());
                rt.setPersistenceSelectorStrategy(persistence);
                StorageStrategyDTO storage = new StorageStrategyDTO();
                storage.setClazz(IndexStorageStrategy.class.getName());
                rt.setStorageStrategy(storage);
                table.addItem(rt);
                table.select(rt);
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

    /**
     * Gets the resource types.
     *
     * @return the resource types
     */
    @SuppressWarnings("unchecked")
    public Collection<ResourceTypeDTO> getResourceTypes() {
        return ((BeanItemContainer<ResourceTypeDTO>) table.getContainerDataSource()).getItemIds();
    }

}
