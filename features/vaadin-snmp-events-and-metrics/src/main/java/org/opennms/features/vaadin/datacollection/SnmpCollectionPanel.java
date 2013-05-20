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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Class SNMP Collection Panel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SnmpCollectionPanel extends VerticalLayout {

    /** The form. */
    private final SnmpCollectionForm form;

    /** The table. */
    private final SnmpCollectionTable table;

    /** The isNew flag. True, if the SNMP collection is new. */
    private boolean isNew;

    /**
     * Instantiates a new SNMP collection panel.
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     * @param logger the logger
     */
    public SnmpCollectionPanel(final DataCollectionConfigDao dataCollectionConfigDao, final Logger logger) {
        setCaption("SNMP Collections");
        addStyleName(Runo.PANEL_LIGHT);

        form = new SnmpCollectionForm(dataCollectionConfigDao) {
            @Override
            public void saveSnmpCollection(SnmpCollection snmpCollection) {
                if (isNew) {
                    table.addSnmpCollection(snmpCollection);
                    logger.info("SNMP Collection " + snmpCollection.getName() + " has been created.");
                } else {
                    logger.info("SNMP Collection " + snmpCollection.getName() + " has been updated.");
                }
                table.refreshRowCache();
                saveSnmpCollections(dataCollectionConfigDao, logger);
            }
            @Override
            public void deleteSnmpCollection(SnmpCollection snmpCollection) {
                logger.info("SNMP Collection " + snmpCollection.getName() + " has been removed.");
                table.removeItem(snmpCollection.getName());
                table.refreshRowCache();
                saveSnmpCollections(dataCollectionConfigDao, logger);
            }
        };

        table = new SnmpCollectionTable(dataCollectionConfigDao) {
            @Override
            public void updateExternalSource(BeanItem<SnmpCollection> item) {
                form.setItemDataSource(item, Arrays.asList(SnmpCollectionForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
                setIsNew(false);
            }
        };

        final Button add = new Button("Add SNMP Collection", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                SnmpCollection collection = new SnmpCollection();
                collection.setName("New Collection");
                collection.setSnmpStorageFlag("select");
                Rrd rrd = new Rrd();
                rrd.setStep(300);
                rrd.addRra("RRA:AVERAGE:0.5:1:2016");
                rrd.addRra("RRA:AVERAGE:0.5:12:1488");
                rrd.addRra("RRA:AVERAGE:0.5:288:366");
                rrd.addRra("RRA:MAX:0.5:288:366");
                rrd.addRra("RRA:MIN:0.5:288:366");
                collection.setRrd(rrd);
                table.updateExternalSource(new BeanItem<SnmpCollection>(collection));
                form.setReadOnly(false);
                setIsNew(true);
            }
        });

        final Button refresh = new Button("Refresh SNMP Collections", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                MessageBox mb = new MessageBox(getUI().getWindows().iterator().next(),
                                               "Are you sure?",
                                               MessageBox.Icon.QUESTION,
                                               "By doing this all unsafed changes in SNMP collection will be lost.",
                                               new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                               new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
                mb.addStyleName(Runo.WINDOW_DIALOG);
                mb.show(new EventListener() {
                    @Override
                    public void buttonClicked(ButtonType buttonType) {
                        if (buttonType == MessageBox.ButtonType.YES) {
                            table.refreshSnmpCollections();
                            table.select(null);
                            form.setVisible(false);

                        }
                    }
                });
            }
        });

        setSpacing(true);
        setMargin(true);
        addComponent(table);
        final HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(add);
        toolbar.addComponent(refresh);
        addComponent(toolbar);
        addComponent(form);

        setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Gets the SNMP collections.
     *
     * @return the SNMP collections
     */
    @SuppressWarnings("unchecked")
    public List<SnmpCollection> getSnmpCollections() {
        final List<SnmpCollection> collections = new ArrayList<SnmpCollection>();
        for (Object itemId : table.getContainerDataSource().getItemIds()) {
            SnmpCollection c = ((BeanItem<SnmpCollection>)table.getContainerDataSource().getItem(itemId)).getBean();
            c.setGroups(null);
            c.setSystems(null);
            collections.add(c);
        }
        return collections;
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
     * @param dataCollectionConfigDao the OpenNMS data collection configuration DAO
     * @param logger the logger
     */
    public void saveSnmpCollections(final DataCollectionConfigDao dataCollectionConfigDao, Logger logger) {
        try {
            final DatacollectionConfig dataCollectionConfig = dataCollectionConfigDao.getRootDataCollection();
            File file = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
            logger.info("Saving data colleciton configuration on " + file);
            dataCollectionConfig.setSnmpCollection(getSnmpCollections());
            JaxbUtils.marshal(dataCollectionConfig, new FileWriter(file));
            logger.info("The data collection configuration has been saved.");
        } catch (Exception e) {
            logger.error("An error ocurred while saving the data collection configuration, " + e.getMessage());
            Notification.show("Can't save data collection configuration. " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
}
