/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
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
    final TextField name = new TextField("Resource Type Name");

    /** The label. */
    final TextField label = new TextField("Resource Type Label");

    /** The resource label. */
    final TextField resourceLabel = new TextField("Resource Label");

    /** The storage strategy. */
    final StorageStrategyField storageStrategy = new StorageStrategyField("Storage Strategy");

    /** The persistence selector strategy. */
    final PersistSelectorStrategyField persistenceSelectorStrategy = new PersistSelectorStrategyField("Persist Selector Strategy");

    /** The Event editor. */
    final BeanFieldGroup<ResourceType> resourceTypeEditor = new BeanFieldGroup<ResourceType>(ResourceType.class);

    /** The event layout. */
    final FormLayout resourceTypeLayout = new FormLayout();

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

        resourceTypeEditor.bind(name, "name");
        resourceTypeEditor.bind(label, "label");
        resourceTypeEditor.bind(resourceLabel, "resourceLabel");
        resourceTypeEditor.bind(storageStrategy, "storageStrategy");
        resourceTypeEditor.bind(persistenceSelectorStrategy,  "persistenceSelectorStrategy");

        setCompositionRoot(resourceTypeLayout);
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public ResourceType getResourceType() {
        return resourceTypeEditor.getItemDataSource().getBean();
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the new resource type
     */
    public void setResourceType(ResourceType resourceType) {
        resourceTypeEditor.setItemDataSource(resourceType);
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
        persistence.setClazz("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"); // To avoid requires opennms-services
        rt.setPersistenceSelectorStrategy(persistence);
        StorageStrategy storage = new StorageStrategy();
        storage.setClazz(IndexStorageStrategy.class.getName());
        rt.setStorageStrategy(storage);
        return rt;
    }

    /**
     * Discard.
     */
    public void discard() {
        resourceTypeEditor.discard();
    }

    /**
     * Commit.
     *
     * @throws CommitException the commit exception
     */
    public void commit() throws CommitException {
        resourceTypeEditor.commit();
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

    /**
     * Gets the resource type name.
     *
     * @return the resource type name
     */
    public String getResourceTypeName() {
        return name.getValue();
    }
}
