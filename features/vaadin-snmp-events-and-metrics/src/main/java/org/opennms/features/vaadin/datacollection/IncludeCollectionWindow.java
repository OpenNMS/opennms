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

import org.opennms.netmgt.config.DataCollectionConfigDao;

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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

/**
 * The Include Collection Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class IncludeCollectionWindow extends Window implements Button.ClickListener {

    /** The Include Field Form. */
    private final Form form = new Form();

    /** The OK button. */
    private final Button okButton;

    /** The CANCEL button. */
    private final Button cancelButton;

    /**
     * Instantiates a new include collection window.
     * 
     * @param dataCollectionConfigDao the data collection configuration DAO
     * @param container the source list of elements
     * @param wrapper the current selected value
     */
    public IncludeCollectionWindow(final DataCollectionConfigDao dataCollectionConfigDao,
            final BeanItemContainer<IncludeCollectionWrapper> container,
            final IncludeCollectionWrapper wrapper) {

        setCaption("Include SystemDef/DataCollectionGroup");
        setModal(true);
        setWidth("400px");
        setHeight("180px");
        setResizable(false);
        setClosable(false);
        addStyleName(Runo.WINDOW_DIALOG);

        form.setImmediate(true);
        form.setWidth("100%");
        form.setBuffered(true);

        final ComboBox valueField = new ComboBox("Value");
        valueField.setEnabled(false);
        valueField.setRequired(true);
        valueField.setImmediate(true);
        valueField.setNewItemsAllowed(false);
        valueField.setNullSelectionAllowed(false);

        final ComboBox typeField = new ComboBox("Type");
        typeField.setRequired(true);
        typeField.setImmediate(true);
        typeField.setNewItemsAllowed(false);
        typeField.setNullSelectionAllowed(false);
        typeField.addItem(IncludeCollectionWrapper.DC_GROUP);
        typeField.addItem(IncludeCollectionWrapper.SYSTEM_DEF);
        typeField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String selected = (String) typeField.getValue();
                if (selected == null) {
                    return;
                }
                // Get available fields.
                // FIXME If a new dcGroup is added, DataCollectionConfigDao is not able to reach it.
                List<String> values = selected.equals(IncludeCollectionWrapper.SYSTEM_DEF) ? dataCollectionConfigDao.getAvailableSystemDefs()
                    : dataCollectionConfigDao.getAvailableDataCollectionGroups();
                // Remove already selected
                for (IncludeCollectionWrapper obj : container.getItemIds()) {
                    if (obj.getType().equals(selected)) {
                        values.remove(obj.getValue());
                    }
                }
                // Updating combo-box
                valueField.removeAllItems();
                for (String v : values) {
                    valueField.addItem(v);
                }
                if (wrapper.getValue() != null) {
                    valueField.addItem(wrapper.getValue());
                }
                valueField.setEnabled(valueField.getItemIds().size() > 1);
            }
        });

        form.setFormFieldFactory(new FormFieldFactory() {
            @Override
            public Field<?> createField(Item item, Object propertyId, Component uiContext) {
                if (propertyId.equals("type"))
                    return typeField;
                if (propertyId.equals("value"))
                    return valueField;
                return null;
            }
        });

        okButton = new Button("Update");
        okButton.addClickListener(this);

        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(okButton);
        toolbar.addComponent(cancelButton);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(form);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.BOTTOM_RIGHT);
        layout.setMargin(true);
        setContent(layout);

        form.setItemDataSource(new BeanItem<IncludeCollectionWrapper>(wrapper));
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button btn = event.getButton();
        if (btn == okButton) {
            form.commit();
            fieldChanged();
        }
        close();
    }

    /**
     * Fired when the field has been chaned.
     */
    public abstract void fieldChanged();

}
