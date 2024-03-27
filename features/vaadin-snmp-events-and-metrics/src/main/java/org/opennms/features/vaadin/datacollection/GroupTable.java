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
import org.opennms.netmgt.config.datacollection.Group;

import com.vaadin.v7.ui.Table;

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
