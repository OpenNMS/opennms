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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.features.vaadin.config.EditorToolbar;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class SNMP Collection Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SnmpCollectionPanel extends Panel {

    /** The isNew flag. True, if the SNMP collection is new. */
    private boolean isNew;

    /** The OpenNMS Data Collection Configuration DAO. */
    private final DataCollectionConfigDao dataCollectionConfigDao;

    /** The selected resourceType ID. */
    private Object selectedSnmpCollectionId;

    /**
     * Instantiates a new SNMP collection panel.
     *
     * @param dataCollectionConfigDao the OpenNMS data collection configuration DAO
     * @param logger the logger
     */
    public SnmpCollectionPanel(final DataCollectionConfigDao dataCollectionConfigDao, final Logger logger) {

        if (dataCollectionConfigDao == null)
            throw new RuntimeException("dataCollectionConfigDao cannot be null.");

        this.dataCollectionConfigDao = dataCollectionConfigDao;

        setCaption("SNMP Collections");
        addStyleName("light");

        final List<SnmpCollection> snmpCollections = dataCollectionConfigDao.getRootDataCollection().getSnmpCollections();
        final SnmpCollectionTable snmpCollectionTable = new SnmpCollectionTable(snmpCollections);

        final SnmpCollectionForm snmpCollectionForm = new SnmpCollectionForm(dataCollectionConfigDao);
        snmpCollectionForm.setVisible(false);

        final EditorToolbar bottomToolbar = new EditorToolbar() {
            @Override
            public boolean save() {
                SnmpCollection snmpCollection = snmpCollectionForm.getSnmpCollection();
                logger.info("SNMP Collection " + snmpCollection.getName() + " has been " + (isNew ? "created." : "updated."));
                String oldName = snmpCollection.getName();
                Integer oldStep = snmpCollection.getRrd().getStep();
                try {
                    snmpCollectionForm.commit();
                    snmpCollectionForm.setReadOnly(true);
                    snmpCollectionTable.refreshRowCache();
                    saveSnmpCollections(snmpCollectionTable.getSnmpCollections(), logger);
                    if (!isNew && !oldName.equals(snmpCollectionForm.getSnmpCollectionName())) {
                        Notification.show("Be sure to replace " + oldName + " with " + snmpCollectionForm.getSnmpCollectionName() + " in case the collection is being used in collectd-configuration.xml", Notification.Type.WARNING_MESSAGE); // TODO Is this enough
                    }
                    if (!isNew && oldStep != snmpCollectionForm.getRrdStep()) {
                        Notification.show("Be sure to replace the collection interval from " + oldStep + "s with " + snmpCollectionForm.getRrdStep() + "s in case the collection is being used in collectd-configuration.xml", Notification.Type.WARNING_MESSAGE); // TODO Is this enough
                    }
                } catch (CommitException e) {
                    String msg = "Can't save the changes: " + e.getMessage();
                    logger.error(msg);
                    Notification.show(msg, Notification.Type.ERROR_MESSAGE);
                    return false;
                }
                return true;
            }
            @Override
            public boolean delete() {
                Object snmpCollectionId = snmpCollectionTable.getValue();
                if (snmpCollectionId != null) {
                    SnmpCollection snmpCollection = snmpCollectionTable.getSnmpCollection(snmpCollectionId);
                    logger.info("SNMP Collection " + snmpCollection.getName() + " has been removed.");
                    snmpCollectionTable.select(null);
                    snmpCollectionTable.removeItem(snmpCollectionId);
                    snmpCollectionTable.refreshRowCache();
                    saveSnmpCollections(snmpCollectionTable.getSnmpCollections(), logger);
                    Notification.show("Be sure that " + snmpCollection.getName() + " is not being used in collectd-configuration.xml", Notification.Type.WARNING_MESSAGE); // TODO Is this enough
                }
                return true;
            }
            @Override
            public boolean edit() {
                snmpCollectionForm.setReadOnly(false);
                return true;
            }
            @Override
            public boolean cancel() {
                snmpCollectionForm.discard();
                snmpCollectionForm.setReadOnly(true);
                return true;
            }
        };
        bottomToolbar.setVisible(false);

        snmpCollectionTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (snmpCollectionForm.isVisible() && !snmpCollectionForm.isReadOnly()) {
                    snmpCollectionTable.select(selectedSnmpCollectionId);
                    Notification.show("A SNMP collection type seems to be being edited.\nPlease save or cancel your current changes.", Notification.Type.WARNING_MESSAGE);
                } else {
                    Object snmpCollectionId = snmpCollectionTable.getValue();
                    if (snmpCollectionId != null) {
                        selectedSnmpCollectionId = snmpCollectionId;
                        snmpCollectionForm.setSnmpCollection(snmpCollectionTable.getSnmpCollection(snmpCollectionId));
                    }
                    snmpCollectionForm.setReadOnly(true);
                    snmpCollectionForm.setVisible(snmpCollectionId != null);
                    bottomToolbar.setReadOnly(true);
                    bottomToolbar.setVisible(snmpCollectionId != null);
                }
            }
        });   

        final Button add = new Button("Add SNMP Collection", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                snmpCollectionTable.addSnmpCollection(snmpCollectionForm.createBasicSnmpCollection());
                snmpCollectionForm.setReadOnly(false);
                bottomToolbar.setReadOnly(false);
                setIsNew(true);
            }
        });

        final Button refresh = new Button("Refresh SNMP Collections", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(getUI(),
                                   "Are you sure?",
                                   "By doing this all unsaved changes on the SNMP collection will be lost.",
                                   "Yes",
                                   "No",
                                   new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            final List<SnmpCollection> snmpCollections = dataCollectionConfigDao.getRootDataCollection().getSnmpCollections();
                            snmpCollectionTable.setSnmpCollections(snmpCollections);
                            snmpCollectionTable.select(null);
                            snmpCollectionForm.setVisible(false);
                        }
                    }
                });
            }
        });

        final HorizontalLayout tableToolbar = new HorizontalLayout();
        tableToolbar.addComponent(add);
        tableToolbar.addComponent(refresh);

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.addComponent(snmpCollectionTable);
        mainLayout.addComponent(tableToolbar);
        mainLayout.addComponent(snmpCollectionForm);
        mainLayout.addComponent(bottomToolbar);
        mainLayout.setComponentAlignment(tableToolbar, Alignment.MIDDLE_RIGHT);
        setContent(mainLayout);
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the resource type is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Save SNMP collections.
     *
     * @param snmpCollections the SNMP collections
     * @param logger the logger
     */
    public void saveSnmpCollections(final List<SnmpCollection> snmpCollections, Logger logger) {
        try {
            final DatacollectionConfig dataCollectionConfig = dataCollectionConfigDao.getRootDataCollection();
            File file = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
            logger.info("Saving data colleciton configuration on " + file);
            // TODO: Normalize the SNMP Collections Content, I'm not sure why
            for (SnmpCollection snmpCollection : snmpCollections) {
                snmpCollection.setGroups(null);
                snmpCollection.setSystems(null);
            }
            dataCollectionConfig.setSnmpCollections(snmpCollections);
            JaxbUtils.marshal(dataCollectionConfig, new FileWriter(file));
            logger.info("The data collection configuration has been saved.");
        } catch (Exception e) {
            logger.error("An error ocurred while saving the data collection configuration: " + (e.getMessage() == null ? "[No Details]" : e.getMessage()));
            if (e.getMessage() == null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(sw.toString());
            }
            Notification.show("Can't save data collection configuration. " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
}
