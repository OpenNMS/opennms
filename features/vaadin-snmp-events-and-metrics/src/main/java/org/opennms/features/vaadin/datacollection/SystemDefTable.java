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
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.v7.ui.Table;

/**
 * The Class System Definition Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefTable extends Table {

    /** The SNMP System Definitions Container. */
    private OnmsBeanContainer<SystemDef> container = new OnmsBeanContainer<SystemDef>(SystemDef.class);

    /**
     * Instantiates a new system definition table.
     *
     * @param systemDefs the system definitions
     */
    public SystemDefTable(final List<SystemDef> systemDefs) {
        container.addAll(systemDefs);
        setContainerDataSource(container);
        addStyleName("light");
        setImmediate(true);
        setSelectable(true);
        setWidth("100%");
        setHeight("250px");
        addGeneratedColumn("count", new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final SystemDef s = container.getItem(itemId).getBean();
                return s.getCollect() == null ? 0 : s.getCollect().getIncludeGroups().size();
            }
        });
        addGeneratedColumn("oid", new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final SystemDef s = container.getItem(itemId).getBean();
                final String value = s.getSysoid() == null ? s.getSysoidMask() : s.getSysoid();
                return value == null ? "N/A" : value;
            }
        });
        setVisibleColumns(new Object[] { "name", "oid", "count" });
        setColumnHeaders(new String[] { "System Definition", "OID", "# SystemDefs" });
    }

    /**
     * Gets the systemDef.
     *
     * @param systemDefId the systemDef ID (the Item ID associated with the container)
     * @return the event
     */
    public SystemDef getSystemDef(Object systemDefId) {
        return container.getOnmsBean(systemDefId);
    }

    /**
     * Adds the system definition.
     *
     * @param systemDef the new system definition
     * @return the systemDefId
     */
    public Object addSystemDef(SystemDef systemDef) {
        Object systemDefId = container.addOnmsBean(systemDef);
        select(systemDefId);
        return systemDefId;
    }

    /**
     * Gets the systemDefs.
     *
     * @return the systemDefs
     */
    public List<SystemDef> getSystemDefs() {
        return container.getOnmsBeans();
    }
}
