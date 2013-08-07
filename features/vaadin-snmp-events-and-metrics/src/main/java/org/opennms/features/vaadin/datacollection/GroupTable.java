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

import org.opennms.netmgt.config.datacollection.Group;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Table;

/**
 * The Class Resource Type Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class GroupTable extends Table {

    /**
     * Instantiates a new group table.
     *
     * @param groups the groups
     */
    public GroupTable(final List<Group> groups) {
        BeanContainer<String,Group> container = new BeanContainer<String,Group>(Group.class);
        container.setBeanIdProperty("name");
        container.addAll(groups);
        setContainerDataSource(container);
        addStyleName("light");
        setImmediate(true);
        setSelectable(true);
        setWidth("100%");
        setHeight("250px");
        addGeneratedColumn("count", new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                BeanItem<Group> item = getContainer().getItem(itemId);
                return item.getBean().getMibObjCount();
            }
        });
        setVisibleColumns(new Object[] { "name", "count" });
        setColumnHeaders(new String[] { "MIB Group", "# MIB Objects" });
    }

    /**
     * Gets the group.
     *
     * @param groupId the group ID (the Item ID associated with the container, in this case, the Group's name)
     * @return the event
     */
    @SuppressWarnings("unchecked")
    public Group getGroup(Object groupId) {
        return ((BeanItem<Group>)getItem(groupId)).getBean();
    }

    /**
     * Gets the event container.
     *
     * @return the event container
     */
    @SuppressWarnings("unchecked")
    public BeanContainer<String, Group> getContainer() {
        return (BeanContainer<String, Group>) getContainerDataSource();
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<Group> getGroups() {
        List<Group> groups = new ArrayList<Group>();
        for (String itemId : getContainer().getItemIds()) {
            groups.add(getContainer().getItem(itemId).getBean());
        }
        return groups;
    }
}
