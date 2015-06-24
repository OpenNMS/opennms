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

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.ui.Table;

/**
 * The Class SNMP Collection Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SnmpCollectionTable extends Table {

    /** The SNMP Collection Container. */
    private OnmsBeanContainer<SnmpCollection> container = new OnmsBeanContainer<SnmpCollection>(SnmpCollection.class);

    /**
     * Instantiates a new SNMP collection table.
     *
     * @param snmpCollections the snmp collections
     */
    public SnmpCollectionTable(final List<SnmpCollection> snmpCollections) {
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
     * @param snmpCollectionId the SNMP collection ID (the Item ID associated with the container)
     * @return the SNMP collection
     */
    public SnmpCollection getSnmpCollection(Object snmpCollectionId) {
        return container.getItem(snmpCollectionId).getBean();
    }

    /**
     * Adds the SNMP Collection.
     *
     * @param snmpCollection the new SNMP Collection
     * @return the snmpCollectionId
     */
    public Object addSnmpCollection(SnmpCollection snmpCollection) {
        Object snmpCollectionId = container.addOnmsBean(snmpCollection);
        select(snmpCollectionId);
        return snmpCollectionId;
    }

    /**
     * Sets the SNMP collections.
     *
     * @param snmpCollections the new SNMP collections
     */
    public void setSnmpCollections(List<SnmpCollection> snmpCollections) {
        container.removeAllItems();
        for (SnmpCollection sc : snmpCollections) {
            // Ignoring an internal collection created to handle resource types only if exist
            if (!sc.getName().equals("__resource_type_collection")) {
                container.addOnmsBean(sc);
            }
        }
    }

    /**
     * Gets the SNMP collections.
     *
     * @return the SNMP collections
     */
    public List<SnmpCollection> getSnmpCollections() {
        return container.getOnmsBeans();
    }

}
