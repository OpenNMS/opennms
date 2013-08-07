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

import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Table;

/**
 * The Class System Definition Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefTable extends Table {

    /**
     * Instantiates a new system definition table.
     *
     * @param systemDefs the system definitions
     */
    public SystemDefTable(final List<SystemDef> systemDefs) {
        BeanContainer<String,SystemDef> container = new BeanContainer<String,SystemDef>(SystemDef.class);
        container.setBeanIdProperty("name");
        container.addAll(systemDefs);
        setContainerDataSource(container);
        addStyleName("light");
        setImmediate(true);
        setSelectable(true);
        setWidth("100%");
        setHeight("250px");
        addGeneratedColumn("count", new ColumnGenerator() {
            @Override
            @SuppressWarnings("unchecked")
            public Object generateCell(Table source, Object itemId, Object columnId) {
                BeanItem<SystemDef> item = (BeanItem<SystemDef>) getContainerDataSource().getItem(itemId);
                return item.getBean().getCollect() == null ? 0 : item.getBean().getCollect().getIncludeGroupCount();
            }
        });
        addGeneratedColumn("oid", new ColumnGenerator() {
            @Override
            @SuppressWarnings("unchecked")
            public Object generateCell(Table source, Object itemId, Object columnId) {
                BeanItem<SystemDef> item = (BeanItem<SystemDef>) getContainerDataSource().getItem(itemId);
                final SystemDef s = item.getBean();
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
     * @param systemDefId the systemDef ID (the Item ID associated with the container, in this case, the SystemDef's name)
     * @return the event
     */
    @SuppressWarnings("unchecked")
    public SystemDef getSystemDef(Object systemDefId) {
        return ((BeanItem<SystemDef>)getItem(systemDefId)).getBean();
    }

    /**
     * Gets the event container.
     *
     * @return the event container
     */
    @SuppressWarnings("unchecked")
    public BeanContainer<String, SystemDef> getContainer() {
        return (BeanContainer<String, SystemDef>) getContainerDataSource();
    }

    /**
     * Gets the systemDefs.
     *
     * @return the systemDefs
     */
    public List<SystemDef> getSystemDefs() {
        List<SystemDef> systemDefs = new ArrayList<SystemDef>();
        for (String itemId : getContainer().getItemIds()) {
            systemDefs.add(getContainer().getItem(itemId).getBean());
        }
        return systemDefs;
    }
}
