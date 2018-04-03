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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class ResourceTypePanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class ResourceTypePanel extends Panel {

    /** The isNew flag. True, if the resource type is new. */
    private boolean isNew;

    /** The resource type table. */
    private final ResourceTypeTable resourceTypeTable;

    /** The selected resourceType ID. */
    private Object selectedResourceTypeId;

    /**
     * Instantiates a new resource type panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection ResourceType object
     * @param logger the logger object
     */
    public ResourceTypePanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {

        if (dataCollectionConfigDao == null) {
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");
        }

        if (source == null) {
            throw new RuntimeException("source cannot be null.");
        }

        addStyleName("light");

        resourceTypeTable = new ResourceTypeTable(source.getResourceTypes());

        final ResourceTypeForm resourceTypeForm = new ResourceTypeForm();
        resourceTypeForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public boolean save() {
                ResourceType resourceType = resourceTypeForm.getResourceType();
                if (!isNew && !resourceType.getName().equals(resourceTypeForm.getResourceTypeName())) {
                    Set<String> groups = getParentGroups(dataCollectionConfigDao, resourceType.getName());
                    if (!groups.isEmpty()) {
                        final String msg = "The resourceType cannot be renamed because it is being referenced by:\n" + groups.toString();
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
                logger.info("Resource Type " + resourceType.getName() + " has been " + (isNew ? "created." : "updated."));
                try {
                    resourceTypeForm.commit();
                    resourceTypeForm.setReadOnly(true);
                    resourceTypeTable.refreshRowCache();
                    return true;
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                    return false;
                }
            }
            @Override
            public boolean delete() {
                Object resourceTypeId = resourceTypeTable.getValue();
                if (resourceTypeId != null) {
                    ResourceType resourceType = resourceTypeTable.getResourceType(resourceTypeId);
                    Set<String> groups = getParentGroups(dataCollectionConfigDao, resourceType.getName());
                    if (!groups.isEmpty()) {
                        final String msg = "The resourceType cannot be deleted because it is being referenced by:\n" + groups.toString();
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    logger.info("SNMP ResourceType " + resourceType.getName() + " has been removed.");
                    resourceTypeTable.select(null);
                    resourceTypeTable.removeItem(resourceTypeId);
                    resourceTypeTable.refreshRowCache();
                }
                return true;
            }
            @Override
            public boolean edit() {
                resourceTypeForm.setReadOnly(false);
                return true;
            }
            @Override
            public boolean cancel() {
                resourceTypeForm.discard();
                resourceTypeForm.setReadOnly(true);
                return true;
            }
        };
        bottomToolbar.setVisible(false);

        resourceTypeTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (resourceTypeForm.isVisible() && !resourceTypeForm.isReadOnly()) {
                    resourceTypeTable.select(selectedResourceTypeId);
                    Notification.show("A resource type seems to be being edited.\nPlease save or cancel your current changes.", Notification.Type.WARNING_MESSAGE);
                } else {
                    Object resourceTypeId = resourceTypeTable.getValue();
                    if (resourceTypeId != null) {
                        selectedResourceTypeId = resourceTypeId;
                        resourceTypeForm.setResourceType(resourceTypeTable.getResourceType(resourceTypeId));
                    }
                    resourceTypeForm.setReadOnly(true);
                    resourceTypeForm.setVisible(resourceTypeId != null);
                    bottomToolbar.setReadOnly(true);
                    bottomToolbar.setVisible(resourceTypeId != null);
                }
            }
        });   

        final Button add = new Button("Add Resource Type", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                resourceTypeTable.addResourceType(resourceTypeForm.createBasicResourceType());
                resourceTypeForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(resourceTypeTable);
        mainLayout.addComponent(add);
        mainLayout.addComponent(resourceTypeForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the resourceType is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Gets the resource types.
     *
     * @return the resource types
     */
    public List<ResourceType> getResourceTypes() {
        return resourceTypeTable.getResourceTypes();
    }

    /**
     * Gets the parent groups.
     * <p>The list of groups per SNMP collection that are referencing a given resourceTypeName</p>
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     * @param resourceTypeName the resource type name
     * @return the parent groups.
     */
    private Set<String> getParentGroups(final DataCollectionConfigDao dataCollectionConfigDao, String resourceTypeName) {
        Set<String> groupMap = new TreeSet<>();
        for (final SnmpCollection collection : dataCollectionConfigDao.getRootDataCollection().getSnmpCollections()) {
            for (final Group group : collection.getGroups().getGroups()) {
                for (final MibObj mibObj : group.getMibObjs()) {
                    if (mibObj.getInstance().equals(resourceTypeName)) {
                        groupMap.add(group.getName() + '@' + collection.getName());
                    }
                }
            }
        }
        return groupMap;
    }

}
