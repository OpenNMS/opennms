/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS SystemDef, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS SystemDef, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS SystemDef, Inc.
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

    /** The system def table. */
    private final SystemDefTable systemDefTable;

    /**
     * Instantiates a new system definition panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection SystemDef object
     * @param logger the logger object
     */
    public SystemDefPanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {

        if (dataCollectionConfigDao == null)
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");

        if (source == null)
            throw new RuntimeException("source cannot be null.");

        addStyleName("light");

        // Adding all systemDefs already defined on this source
        final List<String> groupNames = new ArrayList<String>();
        for (Group group : source.getGroupCollection()) {
            groupNames.add(group.getName());
        }
        // Adding all defined systemDefs
        groupNames.addAll(dataCollectionConfigDao.getAvailableMibGroups());

        systemDefTable = new SystemDefTable(source.getSystemDefCollection());

        final SystemDefForm systemDefForm = new SystemDefForm(groupNames);
        systemDefForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public void save() {
                SystemDef systemDef = systemDefForm.getSystemDef();
                logger.info("SNMP SystemDef " + systemDef.getName() + " has been " + (isNew ? "created." : "updated."));
                try {
                    systemDefForm.getFieldGroup().commit();
                    systemDefForm.setReadOnly(true);
                    systemDefTable.refreshRowCache();
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                }
            }
            @Override
            public void delete() {
                SystemDef systemDef = systemDefForm.getSystemDef();
                logger.info("SNMP SystemDef " + systemDef.getName() + " has been removed.");
                systemDefTable.select(null);
                systemDefTable.removeItem(systemDef.getName());
                systemDefTable.refreshRowCache();
            }
            @Override
            public void edit() {
                systemDefForm.setReadOnly(false);
            }
            @Override
            public void cancel() {
                systemDefForm.getFieldGroup().discard();
                systemDefForm.setReadOnly(true);
            }
        };
        bottomToolbar.setVisible(false);

        systemDefTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Object systemDefId = systemDefTable.getValue();
                if (systemDefId != null) {
                    systemDefForm.setSystemDef(systemDefTable.getSystemDef(systemDefId));
                }
                systemDefForm.setReadOnly(true);
                systemDefForm.setVisible(systemDefId != null);
                bottomToolbar.setReadOnly(true);
                bottomToolbar.setVisible(systemDefId != null);
            }
        });   

        final Button add = new Button("Add SNMP SystemDef", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                SystemDef systemDef = systemDefForm.createBasicSystemDef();
                systemDefTable.getContainer().addBean(systemDef);
                systemDefTable.select(systemDef.getName());
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

}
