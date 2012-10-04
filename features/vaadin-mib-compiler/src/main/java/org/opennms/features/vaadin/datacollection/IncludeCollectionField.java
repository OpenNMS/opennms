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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The Include Collection Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class IncludeCollectionField extends CustomField implements Button.ClickListener {

    /** The Include Field Table. */
    private final Table table = new Table("Include Field");

    /** The Include Field Form. */
    private final Form form = new Form();

    /** The Container. */
    private final BeanItemContainer<IncludeObject> container = new BeanItemContainer<IncludeObject>(IncludeObject.class);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add;

    /** The delete button. */
    private final Button delete;

    /**
     * Instantiates a new include collection field.
     * 
     * @param dataCollectionConfigDao the data collection configuration DAO
     */
    public IncludeCollectionField(final DataCollectionConfigDao dataCollectionConfigDao) {
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"type", "value"});
        table.setColumnHeaders(new String[]{"Type", "Value"});
        table.setSelectable(true);
        table.setImmediate(true);
        table.setHeight("125px");
        table.setWidth("100%");
        table.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (table.getValue() != null) {
                    IncludeObject obj = (IncludeObject) table.getValue();
                    form.setItemDataSource(new BeanItem<IncludeObject>(obj));
                }
            }
        });

        form.setImmediate(true);
        form.getLayout().setMargin(true);

        final ComboBox valueField = new ComboBox("Value");
        valueField.setEnabled(false);
        valueField.setRequired(true);
        valueField.setImmediate(true);
        valueField.setNewItemsAllowed(false);
        valueField.setNullSelectionAllowed(false);

        final ComboBox typeField = new ComboBox("Type");
        typeField.setImmediate(true);
        typeField.setRequired(true);
        typeField.setNewItemsAllowed(false);
        typeField.setNullSelectionAllowed(false);
        typeField.addItem(IncludeObject.DC_GROUP);
        typeField.addItem(IncludeObject.SYSTEM_DEF);
        typeField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String selected = (String) typeField.getValue();
                if (selected == null)
                    return;
                List<String> values = selected.equals(IncludeObject.SYSTEM_DEF) ? dataCollectionConfigDao.getAvailableSystemDefs()
                        : dataCollectionConfigDao.getAvailableDataCollectionGroups();
                valueField.removeAllItems();
                for (String v : values)
                    valueField.addItem(v);
                valueField.setEnabled(values.size() > 1);
            }
        });

        form.setFormFieldFactory(new FormFieldFactory() {
            @Override
            public Field createField(Item item, Object propertyId, Component uiContext) {
                if (propertyId.equals("type"))
                    return typeField;
                if (propertyId.equals("value"))
                    return valueField;
                return null;
            }
        });

        add = new Button("Add", (Button.ClickListener) this);
        delete = new Button("Delete", (Button.ClickListener) this);
        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        VerticalLayout vlayout = new VerticalLayout();
        vlayout.addComponent(table);
        vlayout.addComponent(toolbar);
        vlayout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.addComponent(vlayout);
        layout.addComponent(form);
        layout.setExpandRatio(vlayout, 2);
        layout.setExpandRatio(form, 1);
        setWriteThrough(false);
        setCompositionRoot(layout);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getType()
     */
    @Override
    public Class<?> getType() {
        return ArrayList.class;
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<IncludeCollection> list = (List<IncludeCollection>) value;
            List<IncludeObject> groups = new ArrayList<IncludeObject>();
            for (IncludeCollection ic : list) {
                groups.add(new IncludeObject(ic));
            }
            container.removeAllItems();
            container.addAll(groups);
            table.setPageLength(groups.size());
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
        List<IncludeCollection> list = new ArrayList<IncludeCollection>();
        for (Object itemId: container.getItemIds()) {
            IncludeObject obj = container.getItem(itemId).getBean();
            list.add(obj.createIncludeCollection());
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        toolbar.setVisible(!readOnly);
        form.setReadOnly(readOnly);
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
        IncludeObject obj = new IncludeObject();
        BeanItem<IncludeObject> item = container.addBean(obj);
        form.setItemDataSource(item);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            getApplication().getMainWindow().showNotification("Please select a IncludeCollection from the table.");
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
