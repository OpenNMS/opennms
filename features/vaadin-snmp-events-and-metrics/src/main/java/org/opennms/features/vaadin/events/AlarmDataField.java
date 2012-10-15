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
package org.opennms.features.vaadin.events;

import java.util.Arrays;

import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Buffered;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextField;

/**
 * The Event's AlarmData Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class AlarmDataField extends CustomField {

    /** The Constant FORM_ITEMS. */
    public static final String[] FORM_ITEMS = new String[] {
        "reductionKey",
        "clearKey",
        "alarmType",
//        "autoClean" // FIXME Is not working, I'm not sure why.
    };

    /** The alarm data form. */
    private Form alarmDataForm;

    /**
     * Instantiates a new alarm data field.
     *
     */
    public AlarmDataField() {
        alarmDataForm = new Form();
        alarmDataForm.setWriteThrough(false);
        alarmDataForm.setFormFieldFactory(new FormFieldFactory() {
            public Field createField(Item item, Object propertyId, Component uiContext) {
                if ("alarmType".equals(propertyId)) {
                    final ComboBox f = new ComboBox("Alarm Type");
                    f.addItem(new Integer(1));
                    f.addItem(new Integer(2));
                    f.addItem(new Integer(3));
                    f.setNewItemHandler(new NewItemHandler() {
                        @Override
                        public void addNewItem(String newItemCaption) {
                            try {
                                f.addItem(new Integer(newItemCaption));
                            } catch (Exception e) {}
                        }
                    });
                    f.setDescription("<b>1</b> to be a problem that has a possible resolution, alarm-type set to <b>2</b> to be a resolution event, and alarm-type set to <b>3</b> for events that have no possible resolution");
                    f.setNullSelectionAllowed(false);
                    return f;
                }
                if ("autoClean".equals(propertyId)) {
                    final CheckBox f = new CheckBox("Auto Clean");
                    return f;
                }
                if ("reductionKey".equals(propertyId)) {
                    final TextField f = new TextField("Reduction Key");
                    f.setWidth("100%");
                    f.setNullRepresentation("");
                    return f;
                }
                if ("clearKey".equals(propertyId)) {
                    final TextField f = new TextField("Clear Key");
                    f.setWidth("100%");
                    f.setNullRepresentation("");
                    return f;
                }
                return DefaultFieldFactory.get().createField(item, propertyId, uiContext);
            }
        });
        setCompositionRoot(alarmDataForm);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof AlarmData) {
            alarmDataForm.setItemDataSource(new BeanItem<AlarmData>((AlarmData) value, Arrays.asList(FORM_ITEMS)));
        }
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getValue()
     */
    @Override
    public Object getValue() {
        return alarmDataForm.getValue();
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#commit()
     */
    @Override
    public void commit() throws Buffered.SourceException, InvalidValueException {
        alarmDataForm.commit();
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#discard()
     */
    @Override
    public void discard() throws Buffered.SourceException {
        alarmDataForm.discard();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        alarmDataForm.setReadOnly(readOnly);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getType()
     */
    @Override
    public Class<?> getType() {
        return AlarmData.class;
    }

}
