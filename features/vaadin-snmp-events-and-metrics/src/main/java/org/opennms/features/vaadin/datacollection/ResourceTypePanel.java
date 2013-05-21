/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.support.IndexStorageStrategy;

import com.vaadin.data.util.BeanItem;
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

    /** The isNew flag. True, if the resource type is new. */
    private boolean isNew;

    /**
     * Instantiates a new resource type panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection Group object
     * @param logger the logger object
     */
    public ResourceTypePanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);

        form = new ResourceTypeForm() {
            @Override
            public void saveResourceType(ResourceType resourceType) {
                if (isNew) {
                    table.addResourceType(resourceType);
                    logger.info("Resource type " + resourceType.getName() + " has been created.");
                } else {
                    logger.info("Resource type " + resourceType.getName() + " has been updated.");
                }
                table.refreshRowCache();
            }
            @Override
            public void deleteResourceType(ResourceType resourceType) {
                logger.info("Resource type " + resourceType.getName() + " has been removed.");
                table.removeItem(resourceType.getName());
                table.refreshRowCache();
            }
        };

        table = new ResourceTypeTable(source) {
            @Override
            public void updateExternalSource(BeanItem<ResourceType> item) {
                form.setItemDataSource(item, Arrays.asList(ResourceTypeForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
                setIsNew(false);
            }
        };

        add = new Button("Add Resource Type", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ResourceType rt = new ResourceType();
                rt.setName("New Resource Type");
                rt.setLabel("New Resource Type");
                rt.setResourceLabel("{index}");
                PersistenceSelectorStrategy persistence = new PersistenceSelectorStrategy();
                persistence.setClazz("org.opennms.netmgt.collectd.PersistAllSelectorStrategy"); // To avoid requires opennms-services
                rt.setPersistenceSelectorStrategy(persistence);
                StorageStrategy storage = new StorageStrategy();
                storage.setClazz(IndexStorageStrategy.class.getName());
                rt.setStorageStrategy(storage);
                table.updateExternalSource(new BeanItem<ResourceType>(rt));
                form.setReadOnly(false);
                setIsNew(true);
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
    public Collection<ResourceType> getResourceTypes() {
        final Collection<ResourceType> types = new ArrayList<ResourceType>();
        for (Object itemId : table.getContainerDataSource().getItemIds()) {
            types.add(((BeanItem<ResourceType>)table.getContainerDataSource().getItem(itemId)).getBean());
        }
        return types;
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the resource type is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

}
