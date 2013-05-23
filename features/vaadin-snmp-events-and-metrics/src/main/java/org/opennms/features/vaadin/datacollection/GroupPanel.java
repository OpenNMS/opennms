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
import java.util.Arrays;
import java.util.Collection;

import org.opennms.features.vaadin.api.Logger;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * The Class GroupPanel.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class GroupPanel extends VerticalLayout {

    /** The group form. */
    private final GroupForm form;

    /** The group table. */
    private final GroupTable table;

    /** The add button. */
    private final Button add;

    /** The isNew flag. True, if the group is new. */
    private boolean isNew = false;

    /**
     * Instantiates a new group panel.
     *
     * @param dataCollectionConfigDao the OpenNMS Data Collection Configuration DAO
     * @param source the OpenNMS Data Collection Group object
     * @param logger the logger object
     */
    public GroupPanel(final DataCollectionConfigDao dataCollectionConfigDao, final DatacollectionGroup source, final Logger logger) {
        addStyleName(Runo.PANEL_LIGHT);

        form = new GroupForm(dataCollectionConfigDao, source) {
            @Override
            public void saveGroup(Group group) {
                if (isNew) {
                    table.addGroup(group);
                    logger.info("MIB Group " + group.getName() + " has been created.");
                } else {
                    logger.info("MIB Group " + group.getName() + " has been updated.");
                }
                table.refreshRowCache();
            }
            @Override
            public void deleteGroup(Group group) {
                logger.info("MIB Group " + group.getName() + " has been updated.");
                table.removeItem(group.getName());
                table.refreshRowCache();
            }
        };

        table = new GroupTable(source) {
            @Override
            public void updateExternalSource(BeanItem<Group> item) {
                form.setItemDataSource(item, Arrays.asList(GroupForm.FORM_ITEMS));
                form.setVisible(true);
                form.setReadOnly(true);
                setIsNew(false);
            }
        };

        add = new Button("Add Group", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Group group = new Group();
                group.setName("New Group");
                group.setIfType("ignore");
                table.updateExternalSource(new BeanItem<Group>(group));
                form.setReadOnly(false);
                setIsNew(true);
            }
        });

        setSpacing(true);
        setMargin(true);
        addComponent(table);
        addComponent(add);
        addComponent(form);

        setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    @SuppressWarnings("unchecked")
    public Collection<Group> getGroups() {
        final Collection<Group> groups = new ArrayList<Group>();
        for (Object itemId : table.getContainerDataSource().getItemIds()) {
            groups.add(((BeanItem<Group>)table.getContainerDataSource().getItem(itemId)).getBean());
        }
        return groups;
    }

    /**
     * Sets the value of the ifNew flag.
     *
     * @param isNew true, if the group is new.
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

}
