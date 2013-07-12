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

import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Runo;

/**
 * The Class System Definition Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class SystemDefTable extends Table {

    /** The Constant COLUMN_NAMES. */
    public static final String[] COLUMN_NAMES = new String[] { "name", "oid", "count" };

    /** The Constant COLUMN_LABELS. */
    public static final String[] COLUMN_LABELS = new String[] { "System Definition", "OID", "# Groups" };

    /**
     * Instantiates a new system definition table.
     *
     * @param group the OpenNMS Data Collection Group
     */
    public SystemDefTable(final DatacollectionGroup group) {
        BeanContainer<String,SystemDef> container = new BeanContainer<String,SystemDef>(SystemDef.class);
        container.setBeanIdProperty("name");
        container.addAll(group.getSystemDefCollection());
        setContainerDataSource(container);
        setStyleName(Runo.TABLE_SMALL);
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
        addValueChangeListener(new Property.ValueChangeListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (getValue() != null) {
                    BeanItem<SystemDef> item = (BeanItem<SystemDef>) getContainerDataSource().getItem(getValue());
                    updateExternalSource(item);
                }
            }
        });
        setVisibleColumns(COLUMN_NAMES);
        setColumnHeaders(COLUMN_LABELS);
    }

    /**
     * Update external source.
     *
     * @param item the item
     */
    public abstract void updateExternalSource(BeanItem<SystemDef> item);

    /**
     * Adds a system definition.
     *
     * @param systemDef the system definition
     */
    @SuppressWarnings("unchecked")
    public void addSystemDef(SystemDef systemDef) {
        ((BeanContainer<String,SystemDef>) getContainerDataSource()).addBean(systemDef);
    }

}
