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

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Runo;

/**
 * The Class SNMP Collection Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class SnmpCollectionTable extends Table {

    /** The Constant COLUMN_NAMES. */
    public static final String[] COLUMN_NAMES = new String[] { "name", "snmpStorageFlag" };

    /** The Constant COLUMN_LABELS. */
    public static final String[] COLUMN_LABELS = new String[] { "SNMP Collection Name", "SNMP Storage Flag" };

    /** The OpenNMS Data Collection Config DAO. */
    private final DataCollectionConfigDao dataCollectionConfigDao;

    /** The SNMP Collection Container. */
    private BeanContainer<String,SnmpCollection> container = new BeanContainer<String,SnmpCollection>(SnmpCollection.class);

    /**
     * Instantiates a new SNMP collection table.
     *
     * @param dataCollectionConfigDao the OpenNMS data collection configuration DAO
     */
    public SnmpCollectionTable(final DataCollectionConfigDao dataCollectionConfigDao) {
        this.dataCollectionConfigDao = dataCollectionConfigDao;
        container.setBeanIdProperty("name");
        refreshSnmpCollections();
        setContainerDataSource(container);
        setStyleName(Runo.TABLE_SMALL);
        setImmediate(true);
        setSelectable(true);
        setVisibleColumns(COLUMN_NAMES);
        setColumnHeaders(COLUMN_LABELS);
        setWidth("100%");
        setHeight("250px");
        addValueChangeListener(new Property.ValueChangeListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (getValue() != null) {
                    BeanItem<SnmpCollection> item = (BeanItem<SnmpCollection>) getContainerDataSource().getItem(getValue());
                    updateExternalSource(item);
                }
            }
        });
    }

    /**
     * Update external source.
     *
     * @param item the item
     */
    public abstract void updateExternalSource(BeanItem<SnmpCollection> item);

    /**
     * Adds the SNMP collection.
     *
     * @param snmpCollection the SNMP collection
     */
    @SuppressWarnings("unchecked")
    public void addSnmpCollection(SnmpCollection snmpCollection) {
        ((BeanContainer<String,SnmpCollection>) getContainerDataSource()).addBean(snmpCollection);
    }

    /**
     * Refresh SNMP collections.
     */
    public void refreshSnmpCollections() {
        final DatacollectionConfig dataCollectionConfig = dataCollectionConfigDao.getRootDataCollection();
        container.removeAllItems();
        container.addAll(dataCollectionConfig.getSnmpCollectionCollection());
        container.removeItem("__resource_type_collection"); // This is a protected collection and should not be edited.
    }

}
