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

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;

/**
 * The Collect Field.
 * 
 * TODO: when a new group is added, the groupField must be updated.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class CollectField extends CustomField<Collect> implements Button.ClickListener {

    /** The group field. */
    private final ComboBox groupField = new ComboBox();

    /** The list field. */
    private final ListSelect listField = new ListSelect();

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add;

    /** The delete button. */
    private final Button delete;

    /**
     * Instantiates a new collect field.
     *
     * @param groups the available groups
     */
    public CollectField(List<String> groups) {
        listField.setRows(10);

        for (String group : groups) {
            groupField.addItem(group);
        }

        add = new Button("Add Group", (Button.ClickListener) this);
        delete = new Button("Delete Selected", (Button.ClickListener) this);
        toolbar.addComponent(delete);
        toolbar.addComponent(groupField);
        toolbar.addComponent(add);
        toolbar.setVisible(listField.isReadOnly());

        setBuffered(true);
    }

    @Override
    public Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(listField);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.BOTTOM_RIGHT);
        return layout;
    }

    @Override
    public Class<Collect> getType() {
        return Collect.class;
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof Collect) {
            Collect dto = (Collect) value;
            listField.removeAllItems();
            for (String group : dto.getIncludeGroupCollection()) {
                listField.addItem(group);
            }
        } else {
            throw new ConversionException("Invalid type");
        }
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    public Collect getValue() {
        Collect dto = new Collect();
        for (Object itemId: listField.getItemIds()) {
            dto.getIncludeGroupCollection().add((String) itemId);
        }
        return dto;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        listField.setReadOnly(readOnly);
        toolbar.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button btn = event.getButton();
        if (btn == add) {
            addHandler();
        }
        if (btn == delete) {
            deleteHandler();
        }
    }

    /**
     * Adds the handler.
     */
    private void addHandler() {
        listField.addItem((String) groupField.getValue());
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = listField.getValue();
        if (itemId == null) {
            Notification.show("Please select a MIB Group from the table.");
        } else {
            listField.removeItem(itemId);
        }
    }

}
