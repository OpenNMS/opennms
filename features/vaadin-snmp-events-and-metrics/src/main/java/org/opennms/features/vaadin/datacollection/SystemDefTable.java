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
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.ui.Table;

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
