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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * The Class System Definition Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefPanel extends Panel {

    /** The isNew flag. True, if the system definition is new. */
    private boolean isNew = false;

    /** The system definition table. */
    private final SystemDefTable systemDefTable;

    /** The selected systemDef ID. */
    private Object selectedSystemDefId;

    /**
     * Instantiates a new system definition panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection SystemDef object
     * @param logger the logger object
     */
    public SystemDefPanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {

        if (dataCollectionConfigDao == null) {
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");
        }

        if (source == null) {
            throw new RuntimeException("source cannot be null.");
        }

        addStyleName("light");

        // Adding all systemDefs already defined on this source
        final List<String> groupNames = new ArrayList<>();
        for (Group group : source.getGroups()) {
            groupNames.add(group.getName());
        }

        // Adding all defined groups
        groupNames.addAll(dataCollectionConfigDao.getAvailableMibGroups());

        systemDefTable = new SystemDefTable(source.getSystemDefs());

        final SystemDefForm systemDefForm = new SystemDefForm(groupNames);
        systemDefForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public boolean save() {
                SystemDef systemDef = systemDefForm.getSystemDef();
                if (!isNew && !systemDef.getName().equals(systemDefForm.getSystemDefName())) {
                    Set<String> collections = getParentCollections(dataCollectionConfigDao, systemDef.getName());
                    if (!collections.isEmpty()) {
                        final String msg = "The systemDef cannot be renamed because it is being referenced by:\n" + collections.toString();
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
                logger.info("SNMP SystemDef " + systemDef.getName() + " has been " + (isNew ? "created." : "updated."));
                try {
                    systemDefForm.commit();
                    systemDefForm.setReadOnly(true);
                    systemDefTable.refreshRowCache();
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
                Object systemDefId = systemDefTable.getValue();
                if (systemDefId != null) {
                    SystemDef systemDef = systemDefTable.getSystemDef(systemDefId);
                    Set<String> collections = getParentCollections(dataCollectionConfigDao, systemDef.getName());
                    if (!collections.isEmpty()) {
                        final String msg = "The systemDef cannot be deleted because it is being referenced by:\n" + collections.toString();
                        Notification.show(msg, Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    logger.info("SNMP SystemDef " + systemDef.getName() + " has been removed.");
                    systemDefTable.select(null);
                    systemDefTable.removeItem(systemDefId);
                    systemDefTable.refreshRowCache();
                }
                return true;
            }
            @Override
            public boolean edit() {
                systemDefForm.setReadOnly(false);
                return true;
            }
            @Override
            public boolean cancel() {
                systemDefForm.discard();
                systemDefForm.setReadOnly(true);
                return true;
            }
        };
        bottomToolbar.setVisible(false);

        systemDefTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (systemDefForm.isVisible() && !systemDefForm.isReadOnly()) {
                    systemDefTable.select(selectedSystemDefId);
                    Notification.show("A system definition seems to be being edited.\nPlease save or cancel your current changes.", Notification.Type.WARNING_MESSAGE);
                } else {
                    Object systemDefId = systemDefTable.getValue();
                    if (systemDefId != null) {
                        selectedSystemDefId = systemDefId;
                        systemDefForm.setSystemDef(systemDefTable.getSystemDef(systemDefId));
                    }
                    systemDefForm.setReadOnly(true);
                    systemDefForm.setVisible(systemDefId != null);
                    bottomToolbar.setReadOnly(true);
                    bottomToolbar.setVisible(systemDefId != null);
                }
            }
        });

        final Button add = new Button("Add SNMP SystemDef", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                systemDefTable.addSystemDef(systemDefForm.createBasicSystemDef());
                systemDefForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(systemDefTable);
        mainLayout.addComponent(add);
        mainLayout.addComponent(systemDefForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the systemDef is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Gets the systemDefs.
     *
     * @return the systemDefs
     */
    public List<SystemDef> getSystemDefs() {
        return systemDefTable.getSystemDefs();
    }

    /**
     * Gets the parent SNMP Collections.
     * <p>The list of SNMP collection that are referencing a given systemDefName</p>
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     * @param systemDefName the system definition name
     * @return the parent collections.
     */
    private Set<String> getParentCollections(final DataCollectionConfigDao dataCollectionConfigDao, String systemDefName) {
        Set<String> collectionMap = new TreeSet<>();
        for (final SnmpCollection collection : dataCollectionConfigDao.getRootDataCollection().getSnmpCollections()) {
            for (final SystemDef systemDef : collection.getSystems().getSystemDefs()) {
                if (systemDefName.equals(systemDef.getName())) {
                    collectionMap.add(collection.getName());
                }
            }
        }
        return collectionMap;
    }

}
