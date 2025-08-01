/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;

import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextField;

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
        resourceTypeEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return resourceTypeEditor.isReadOnly();
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
