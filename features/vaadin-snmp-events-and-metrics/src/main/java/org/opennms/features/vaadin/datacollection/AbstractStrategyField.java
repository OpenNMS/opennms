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

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.collection.api.Parameter;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractSelect.NewItemHandler;

/**
 * The Abstract Strategy Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class AbstractStrategyField<T> extends CustomField<T> implements Button.ClickListener {

    /** The Combo Box. */
    protected final ComboBox combo = new ComboBox("Class Name");

    /** The Container. */
    protected final OnmsBeanContainer<Parameter> container = new OnmsBeanContainer<Parameter>(Parameter.class);

    /** The Table. */
    private final Table table = new Table("Parameters", container);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add = new Button("Add", this);

    /** The delete button. */
    private final Button delete = new Button("Delete", this);

    /**
     * Instantiates a new abstract strategy field.
     *
     * @param caption the caption
     */
    public AbstractStrategyField(String caption, String[] strategies) {
        setCaption(caption);
        for (String strategy : strategies) {
            combo.addItem(strategy);
        }
        combo.setNullSelectionAllowed(false);
        combo.setRequired(true);
        combo.setImmediate(true);
        combo.setNewItemsAllowed(true);
        combo.setNewItemHandler(new NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                if (!combo.containsId(newItemCaption)) {
                    combo.addItem(newItemCaption);
                    combo.setValue(newItemCaption);
                }
            }
        });

        table.addStyleName("light");
        table.setVisibleColumns(new Object[]{"key", "value"});
        table.setColumnHeader("key", "Parameter Name");
        table.setColumnHeader("value", "Parameter Value");
        table.setColumnExpandRatio("value", 1);
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("125px");
        table.setWidth("100%");

        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(combo);
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        combo.setReadOnly(readOnly);
        table.setEditable(!readOnly);
        toolbar.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
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
     * Sets the combo value.
     *
     * @param value the new combo value
     */
    protected void setComboValue(String value) {
        boolean comboState = combo.isReadOnly();
        combo.setReadOnly(false);
        if (!combo.containsId(value)) {
            combo.addItem(value);
        }
        combo.setValue(value);
        if (comboState)
            combo.setReadOnly(true);
    }

    /**
     * Adds the handler.
     */
    private void addHandler() {
        org.opennms.netmgt.config.datacollection.Parameter p = new org.opennms.netmgt.config.datacollection.Parameter();
        p.setKey("New Parameter");
        p.setValue("New Value");
        table.select(container.addOnmsBean(p));
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a Parameter from the table.");
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to remove the selected parameter from the strategy?\nThis action cannot be undone.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        table.removeItem(itemId);
                    }
                }
            });
        }
    }

}
