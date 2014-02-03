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

import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.support.IndexStorageStrategy;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * The Class Event Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypeForm extends CustomComponent {

    /** The name. */
    @PropertyId("name")
    final TextField name = new TextField("Resource Type Name");

    /** The label. */
    @PropertyId("label")
    final TextField label = new TextField("Resource Type Label");

    /** The resource label. */
    @PropertyId("resourceLabel")
    final TextField resourceLabel = new TextField("Resource Label");

    /** The storage strategy. */
    @PropertyId("storageStrategy")
    StorageStrategyField storageStrategy = new StorageStrategyField("Storage Strategy");

    /** The persistence selector strategy. */
    @PropertyId("persistenceSelectorStrategy")
    final PersistSelectorStrategyField persistenceSelectorStrategy = new PersistSelectorStrategyField("Persist Selector Strategy");

    /** The Event editor. */
    private final FieldGroup resourceTypeEditor = new FieldGroup();

    /** The event layout. */
    private final FormLayout resourceTypeLayout = new FormLayout();

    /**
     * Instantiates a new resource type form.
     */
    public ResourceTypeForm() {
        setCaption("Resource Type Detail");
        resourceTypeLayout.setMargin(true);

        name.setRequired(true);
        name.setWidth("100%");
        resourceTypeLayout.addComponent(name);

        label.setRequired(true);
        label.setWidth("100%");
        resourceTypeLayout.addComponent(label);

        resourceLabel.setRequired(false);
        resourceLabel.setWidth("100%");
        resourceTypeLayout.addComponent(resourceLabel);

        resourceTypeLayout.addComponent(storageStrategy);
        resourceTypeLayout.addComponent(persistenceSelectorStrategy);

        setResourceType(createBasicResourceType());
        resourceTypeEditor.bindMemberFields(this);

        setCompositionRoot(resourceTypeLayout);

    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    @SuppressWarnings("unchecked")
    public ResourceType getResourceType() {
        return ((BeanItem<ResourceType>) resourceTypeEditor.getItemDataSource()).getBean();
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the new resource type
     */
    public void setResourceType(ResourceType resourceType) {
        resourceTypeEditor.setItemDataSource(new BeanItem<ResourceType>(resourceType));
    }

    /**
     * Creates the basic resource type.
     *
     * @return the resource type
     */
    public ResourceType createBasicResourceType() {
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
        return rt;
    }

    /**
     * Gets the field group.
     *
     * @return the field group
     */
    public FieldGroup getFieldGroup() {
        return resourceTypeEditor;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        resourceTypeEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() && resourceTypeEditor.isReadOnly();
    }
}
