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

import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.Converter.ConversionException;
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
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Persist Selector Strategy Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class PersistSelectorStrategyField extends CustomField<PersistenceSelectorStrategy> implements Button.ClickListener {

    /** The Combo Box. */
    private final ComboBox combo = new ComboBox();

    /** The Table. */
    private final Table table = new Table();

    /** The Container. */
    private final BeanContainer<String,Parameter> container = new BeanContainer<String,Parameter>(Parameter.class);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add;

    /** The delete button. */
    private final Button delete;

    /**
     * Instantiates a new persist selector strategy field.
     */
    public PersistSelectorStrategyField() {
        combo.setCaption("Class Name");
        combo.addItem("org.opennms.netmgt.collectd.PersistAllSelectorStrategy"); // To avoid requires opennms-services
        combo.addItem("org.opennms.netmgt.collectd.PersistRegexSelectorStrategy"); // To avoid requires opennms-services
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

        container.setBeanIdProperty("key");
        table.setCaption("Parameters");
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"key", "value"});
        table.setColumnHeader("key", "Parameter Name");
        table.setColumnHeader("value", "Parameter Value");
        table.setColumnExpandRatio("value", 1);
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("125px");
        table.setWidth("100%");

        add = new Button("Add", (Button.ClickListener) this);
        delete = new Button("Delete", (Button.ClickListener) this);
        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());
    }

    @Override
    public Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(combo);
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    @Override
    public Class<PersistenceSelectorStrategy> getType() {
        return PersistenceSelectorStrategy.class;
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof PersistenceSelectorStrategy) {
            PersistenceSelectorStrategy dto = (PersistenceSelectorStrategy) value;
            combo.setValue(dto.getClazz());
            container.removeAllItems();
            container.addAll(dto.getParameterCollection());
            table.setPageLength(dto.getParameterCollection() == null ? 0 : dto.getParameterCollection().size());
        } else {
            throw new ConversionException("Invalid type");
        }
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    public PersistenceSelectorStrategy getValue() {
        PersistenceSelectorStrategy dto = new PersistenceSelectorStrategy();
        dto.setClazz((String) combo.getValue());
        for (Object itemId: container.getItemIds()) {
            dto.getParameterCollection().add(container.getItem(itemId).getBean());
        }
        return dto;
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
     * Adds the handler.
     */
    private void addHandler() {
        Parameter p = new Parameter();
        p.setKey("New Parameter");
        container.addBean(p);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a Parameter from the table.");
        } else {
            MessageBox mb = new MessageBox(getUI().getWindows().iterator().next(),
                    "Are you sure?",
                    MessageBox.Icon.QUESTION,
                    "Do you really want to remove the selected parameter ?<br/>This action cannot be undone.",
                    new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                    new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                @Override
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        table.removeItem(itemId);
                    }
                }
            });
        }
    }

}
