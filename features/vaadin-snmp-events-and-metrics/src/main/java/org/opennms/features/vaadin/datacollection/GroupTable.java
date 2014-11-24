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
import org.opennms.netmgt.config.datacollection.Group;

import com.vaadin.ui.Table;

/**
 * The Class Resource Type Table.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class GroupTable extends Table {

    /** The SNMP Group Container. */
    private OnmsBeanContainer<Group> container = new OnmsBeanContainer<Group>(Group.class);

    /**
     * Instantiates a new group table.
     *
     * @param groups the groups
     */
    public GroupTable(final List<Group> groups) {
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
                return container.getItem(itemId).getBean().getMibObjs().size();
            }
        });
        setVisibleColumns(new Object[] { "name", "count" });
        setColumnHeaders(new String[] { "MIB Group", "# MIB Objects" });
    }

    /**
     * Gets the group.
     *
     * @param groupId the group ID (the Item ID associated with the container)
     * @return the event
     */
    public Group getGroup(Object groupId) {
        return container.getItem(groupId).getBean();
    }

    /**
     * Adds the group.
     *
     * @param group the new group
     * @return the groupId
     */
    public Object addGroup(Group group) {
        Object groupId = container.addOnmsBean(group);
        select(groupId);
        return groupId;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<Group> getGroups() {
        return container.getOnmsBeans();
    }
}
