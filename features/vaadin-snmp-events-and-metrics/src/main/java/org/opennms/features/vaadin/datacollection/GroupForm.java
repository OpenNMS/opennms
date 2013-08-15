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

import java.util.List;

import org.opennms.netmgt.config.datacollection.Group;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * The Class Event Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// TODO when a new resource type is added, the resourceType list passed to GroupFieldFactory must be updated.
@SuppressWarnings("serial")
public class GroupForm extends CustomComponent {

    /** The name. */
    @PropertyId("name")
    final TextField name = new TextField("Group Name");

    /** The if type. */
    @PropertyId("ifType")
    final ComboBox ifType = new ComboBox("ifType Filter");

    /** The mib objs. */
    @PropertyId("mibObjCollection")
    final MibObjField mibObjs; 

    /** The Event editor. */
    private final FieldGroup groupEditor = new FieldGroup();

    /** The event layout. */
    private final FormLayout groupLayout = new FormLayout();

    /**
     * Instantiates a new group form.
     *
     * @param resourceTypes the resource types
     */
    public GroupForm(final List<String> resourceTypes) {
        setCaption("MIB Group Detail");
        groupLayout.setMargin(true);

        name.setRequired(true);
        name.setWidth("100%");
        groupLayout.addComponent(name);

        ifType.addItem("ignore");
        ifType.addItem("all");
        ifType.setNullSelectionAllowed(false);
        ifType.setRequired(true);
        ifType.setImmediate(true);
        ifType.setNewItemsAllowed(true);
        groupLayout.addComponent(ifType);

        mibObjs = new MibObjField(resourceTypes);
        mibObjs.setCaption("MIB Objects");
        mibObjs.setRequired(true);
        mibObjs.setImmediate(true);
        mibObjs.setWidth("100%");
        groupLayout.addComponent(mibObjs);

        setGroup(createBasicGroup());
        groupEditor.bindMemberFields(this);

        setCompositionRoot(groupLayout);
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    @SuppressWarnings("unchecked")
    public Group getGroup() {
        return ((BeanItem<Group>) groupEditor.getItemDataSource()).getBean();
    }

    /**
     * Sets the group.
     *
     * @param group the new group
     */
    public void setGroup(Group group) {
        groupEditor.setItemDataSource(new BeanItem<Group>(group));
    }

    /**
     * Creates the basic group.
     *
     * @return the group
     */
    public Group createBasicGroup() {
        Group group = new Group();
        group.setName("New Group");
        group.setIfType("ignore");
        return group;
    }

    /**
     * Gets the field group.
     *
     * @return the field group
     */
    public FieldGroup getFieldGroup() {
        return groupEditor;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        groupEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() && groupEditor.isReadOnly();
    }
}
