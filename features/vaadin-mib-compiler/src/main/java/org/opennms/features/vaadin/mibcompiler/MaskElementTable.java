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
package org.opennms.features.vaadin.mibcompiler;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.vaadin.mibcompiler.model.MaskElementDTO;

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.Runo;

/**
 * The Class MaskElementTable.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MaskElementTable extends CustomField {

    /** The table. */
    private Table table = new Table();

    /** The container. */
    private BeanItemContainer<MaskElementDTO> container = new BeanItemContainer<MaskElementDTO>(MaskElementDTO.class);

    /**
     * Instantiates a new mask element table.
     */
    public MaskElementTable() {
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"mename", "mevalueCollection"});
        table.setColumnHeader("mename", "Element Name");
        table.setColumnHeader("mevalueCollection", "Element Values");
        table.setColumnExpandRatio("mevalueCollection", 1);
        table.setEditable(!isReadOnly());
        table.setHeight("125px");
        table.setWidth("100%");
        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (propertyId.equals("mevalueCollection")) {
                    return new CustomListField();
                }
                return super.createField(container, itemId, propertyId, uiContext);
            }
        });
        setCompositionRoot(table);
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
            List<MaskElementDTO> beans = (List<MaskElementDTO>) value;
            container.removeAllItems();
            container.addAll(beans);
            table.setPageLength(beans.size());
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
        ArrayList<MaskElementDTO> beans = new ArrayList<MaskElementDTO>(); 
        for (Object itemId: container.getItemIds()) {
            beans.add(container.getItem(itemId).getBean());
        }
        return beans;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        table.setEditable(!readOnly);
        super.setReadOnly(readOnly);
    }

    /*
    private static final Action ACTION_ADD_MASK_ELEMENT = new Action("Add New Element");
    private static final Action ACTION_DELETE_MASK_ELEMENT = new Action("Delete Selected Element");

            table.addActionHandler(new Action.Handler() {
                public Action[] getActions(Object target, Object sender) {
                    return new Action[] { ACTION_ADD_MASK_ELEMENT, ACTION_DELETE_MASK_ELEMENT };
                }
                public void handleAction(Action action, Object sender, Object target) {
                    if (elements.isReadOnly()) {
                        elements.getApplication().getMainWindow().showNotification("This table is read-only. Click on edit.");
                    } else {
                        if (action == ACTION_ADD_MASK_ELEMENT) {
                            BeanItem<MaskElementDTO> item = new BeanItem<MaskElementDTO>(new MaskElementDTO());
                            elements.addItem(item);
                        }
                        if (action == ACTION_DELETE_MASK_ELEMENT) { // TODO Confirm ?
                            elements.removeItem(target);
                            elements.select(null);
                        }
                    }
                }
            });
     */
}
