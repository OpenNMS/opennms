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

import java.util.List;

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

import com.vaadin.v7.ui.Table;

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
