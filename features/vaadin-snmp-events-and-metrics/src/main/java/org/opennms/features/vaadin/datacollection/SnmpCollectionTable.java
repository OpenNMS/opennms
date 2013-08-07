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

import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;

/**
 * The Class SNMP Collection Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SnmpCollectionTable extends Table {

    /** The SNMP Collection Container. */
    private BeanContainer<String, SnmpCollection> container = new BeanContainer<String, SnmpCollection>(SnmpCollection.class);

    /**
     * Instantiates a new SNMP collection table.
     *
     * @param snmpCollections the snmp collections
     */
    public SnmpCollectionTable(final List<SnmpCollection> snmpCollections) {
        container.setBeanIdProperty("name");
        setSnmpCollections(snmpCollections);
        setContainerDataSource(container);
        addStyleName("light");
        setImmediate(true);
        setSelectable(true);
        setVisibleColumns(new Object[] { "name", "snmpStorageFlag" });
        setColumnHeaders(new String[] { "SNMP Collection Name", "SNMP Storage Flag" });
        setWidth("100%");
        setHeight("250px");
    }

    /**
     * Gets the SNMP collection.
     *
     * @param snmpCollectionId the SNMP collection ID (the Item ID associated with the container, in this case, the SnmpCollection's name)
     * @return the SNMP collection
     */
    @SuppressWarnings("unchecked")
    public SnmpCollection getSnmpCollection(Object snmpCollectionId) {
        return ((BeanItem<SnmpCollection>)getItem(snmpCollectionId)).getBean();
    }

    /**
     * Gets the container.
     *
     * @return the container
     */
    @SuppressWarnings("unchecked")
    public BeanContainer<String, SnmpCollection> getContainer() {
        return (BeanContainer<String, SnmpCollection>) getContainerDataSource();
    }

    /**
     * Sets the SNMP collections.
     *
     * @param snmpCollections the new SNMP collections
     */
    public void setSnmpCollections(List<SnmpCollection> snmpCollections) {
        container.removeAllItems();
        container.addAll(snmpCollections);
        container.removeItem("__resource_type_collection"); // This is a protected collection and should not be edited.
    }

    /**
     * Gets the SNMP collections.
     *
     * @return the SNMP collections
     */
    public List<SnmpCollection> getSnmpCollections() {
        List<SnmpCollection> snmpCollections = new ArrayList<SnmpCollection>();
        for (String itemId : container.getItemIds()) {
            snmpCollections.add(container.getItem(itemId).getBean());
        }
        return snmpCollections;
    }

}
