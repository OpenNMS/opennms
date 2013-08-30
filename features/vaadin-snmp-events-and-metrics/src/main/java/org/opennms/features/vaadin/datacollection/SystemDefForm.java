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

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.SystemDef;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * The Class System Definition Form.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// TODO when a new group is added, the group list passed to SystemDefFieldFactory must be updated.
@SuppressWarnings("serial")
public class SystemDefForm extends CustomComponent {

    /** The name. */
    @PropertyId("name")
    final TextField name = new TextField("Group Name");

    /** The system definition choice. */
    @PropertyId("systemDefChoice")
    final SystemDefChoiceField systemDefChoice = new SystemDefChoiceField("System OID/Mask");

    /** The collect field. */
    @PropertyId("collect")
    final CollectField collectField;

    /** The Event editor. */
    private final FieldGroup systemDefEditor = new FieldGroup();

    /** The event layout. */
    private final FormLayout systemDefLayout = new FormLayout();

    /**
     * Instantiates a new system definition form.
     *
     * @param groupNames the group names
     */
    public SystemDefForm(final List<String> groupNames) {
        setCaption("System Definition Detail");
        systemDefLayout.setMargin(true);

        name.setRequired(true);
        name.setWidth("100%");
        systemDefLayout.addComponent(name);

        systemDefChoice.setRequired(true);
        systemDefLayout.addComponent(systemDefChoice);

        collectField = new CollectField("MIB Groups", groupNames);
        collectField.setRequired(true);
        systemDefLayout.addComponent(collectField);

        setSystemDef(createBasicSystemDef());
        systemDefEditor.bindMemberFields(this);

        setCompositionRoot(systemDefLayout);
    }

    /**
     * Gets the system definition.
     *
     * @return the system definition
     */
    @SuppressWarnings("unchecked")
    public SystemDef getSystemDef() {
        return ((BeanItem<SystemDef>) systemDefEditor.getItemDataSource()).getBean();
    }

    /**
     * Sets the system definition.
     *
     * @param systemDef the new system definition
     */
    public void setSystemDef(SystemDef systemDef) {
        systemDefEditor.setItemDataSource(new BeanItem<SystemDef>(systemDef));
    }

    /**
     * Creates the basic system definition.
     *
     * @return the system definition
     */
    public SystemDef createBasicSystemDef() {
        SystemDef sysDef = new SystemDef();
        sysDef.setName("New System Definition");
        sysDef.setSysoidMask(".1.3.6.1.4.1.");
        sysDef.setCollect(new Collect());
        return sysDef;
    }

    /**
     * Gets the field group.
     *
     * @return the field group
     */
    public FieldGroup getFieldGroup() {
        return systemDefEditor;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        systemDefEditor.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() && systemDefEditor.isReadOnly();
    }
}
