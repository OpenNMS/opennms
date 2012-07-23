/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import org.opennms.features.vaadin.datacollection.model.ParameterDTO;
import org.opennms.features.vaadin.datacollection.model.StorageStrategyDTO;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Storage Strategy Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class StorageStrategyField extends CustomField implements Button.ClickListener {

    /** The Combo Box. */
    private ComboBox combo = new ComboBox();

    /** The Table. */
    private Table table = new Table();

    /** The Container. */
    private BeanItemContainer<ParameterDTO> container = new BeanItemContainer<ParameterDTO>(ParameterDTO.class);

    /** The Toolbar. */
    private HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private Button add;

    /** The delete button. */
    private Button delete;

    /**
     * Instantiates a new mask element table.
     */
    public StorageStrategyField() {
        combo.setCaption("Class Name");
        combo.addItem("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        combo.addItem("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy");
        combo.setNullSelectionAllowed(false);
        combo.setRequired(true);
        combo.setNewItemsAllowed(true);
        combo.setNewItemHandler(new NewItemHandler() {
            public void addNewItem(String newItemCaption) {
                if (!combo.containsId(newItemCaption)) {
                    combo.addItem(newItemCaption);
                    combo.setValue(newItemCaption);
                }
            }
        });

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

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(combo);
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        setCompositionRoot(layout);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getType()
     */
    @Override
    public Class<?> getType() {
        return StorageStrategyDTO.class;
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof StorageStrategyDTO) {
            StorageStrategyDTO dto = (StorageStrategyDTO) value;
            combo.setValue(dto.getClazz());
            container.removeAllItems();
            container.addAll(dto.getParameterCollection());
            table.setPageLength(dto.getParameterCollection() == null ? 0 : dto.getParameterCollection().size());
        } else {
            throw new ConversionException("Invalid type");
        }
        super.setPropertyDataSource(newDataSource);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getValue()
     */
    @Override
    public Object getValue() {
        StorageStrategyDTO dto = new StorageStrategyDTO();
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
        Object itemId = container.addBean(new ParameterDTO());
        table.addItem(itemId);
        table.setPageLength(container.size()); // TODO: Is this really necessary?
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            getApplication().getMainWindow().showNotification("Please select a Parameter from the table.");
        } else {
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                           "Are you sure?",
                                           MessageBox.Icon.QUESTION,
                                           "Do you really want to continue?",
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.YES, "Yes"),
                                           new MessageBox.ButtonConfig(MessageBox.ButtonType.NO, "No"));
            mb.addStyleName(Runo.WINDOW_DIALOG);
            mb.show(new EventListener() {
                public void buttonClicked(ButtonType buttonType) {
                    if (buttonType == MessageBox.ButtonType.YES) {
                        table.removeItem(itemId);
                    }
                }
            });
        }
    }

}
