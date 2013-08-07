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
import java.util.List;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.ResourceType;

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
 * The Class GroupPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class GroupPanel extends Panel {

    /** The isNew flag. True, if the group is new. */
    private boolean isNew = false;

    /** The group table. */
    private final GroupTable groupTable;

    /**
     * Instantiates a new group panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection Group object
     * @param logger the logger object
     */
    public GroupPanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {

        if (dataCollectionConfigDao == null)
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");

        if (source == null)
            throw new RuntimeException("source cannot be null.");

        addStyleName("light");

        // Adding all resource types already defined on this source
        final List<String> resourceTypes = new ArrayList<String>();
        for (ResourceType type : source.getResourceTypeCollection()) {
            resourceTypes.add(type.getName());
        }

        // Adding all defined resource types
        resourceTypes.addAll(dataCollectionConfigDao.getConfiguredResourceTypes().keySet());

        groupTable = new GroupTable(source.getGroupCollection());

        final GroupForm groupForm = new GroupForm(resourceTypes);
        groupForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public void save() {
                Group group = groupForm.getGroup();
                logger.info("SNMP Group " + group.getName() + " has been " + (isNew ? "created." : "updated."));
                try {
                    groupForm.getFieldGroup().commit();
                    groupForm.setReadOnly(true);
                    groupTable.refreshRowCache();
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                }
            }
            @Override
            public void delete() {
                Group group = groupForm.getGroup();
                logger.info("SNMP Group " + group.getName() + " has been removed.");
                groupTable.select(null);
                groupTable.removeItem(group.getName());
                groupTable.refreshRowCache();
            }
            @Override
            public void edit() {
                groupForm.setReadOnly(false);
            }
            @Override
            public void cancel() {
                groupForm.getFieldGroup().discard();
                groupForm.setReadOnly(true);
            }
        };
        bottomToolbar.setVisible(false);

        groupTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Object groupId = groupTable.getValue();
                if (groupId != null) {
                    groupForm.setGroup(groupTable.getGroup(groupId));
                }
                groupForm.setReadOnly(true);
                groupForm.setVisible(groupId != null);
                bottomToolbar.setReadOnly(true);
                bottomToolbar.setVisible(groupId != null);
            }
        });   

        final Button add = new Button("Add SNMP Group", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Group group = groupForm.createBasicGroup();
                groupTable.getContainer().addBean(group);
                groupTable.select(group.getName());
                groupForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(groupTable);
        mainLayout.addComponent(add);
        mainLayout.addComponent(groupForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the group is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<Group> getGroups() {
        return groupTable.getGroups();
    }

}
