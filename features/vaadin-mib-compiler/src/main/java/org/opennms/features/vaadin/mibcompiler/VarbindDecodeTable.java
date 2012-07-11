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

import org.opennms.features.vaadin.mibcompiler.model.VarbindsDecodeDTO;

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Runo;

/**
 * The Class VarbindDecodeTable.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class VarbindDecodeTable extends CustomField {

    /** The Table. */
    private Table table = new Table();

    /** The Container. */
    private BeanItemContainer<VarbindsDecodeDTO> container = new BeanItemContainer<VarbindsDecodeDTO>(VarbindsDecodeDTO.class);

    /** The Layout. */
    private VerticalLayout layout = new VerticalLayout();

    /** The Toolbar. */
    private HorizontalLayout toolbar = new HorizontalLayout();

    /**
     * Instantiates a new varbind decode table.
     */
    public VarbindDecodeTable() {
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"parmid", "decodeCollection"});
        table.setColumnHeader("parmid", "Parameter ID");
        table.setColumnHeader("decodeCollection", "Decode Values");
        table.setColumnExpandRatio("decodeCollection", 1);
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("125px");
        table.setWidth("100%");
        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (propertyId.equals("decodeCollection")) {
                    return new VarbindDecodeListField();
                }
                return super.createField(container, itemId, propertyId, uiContext);
            }
        });
        Button add = new Button("Add", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                Object itemId = container.addBean(new VarbindsDecodeDTO());
                table.addItem(itemId);
                table.setPageLength(container.size()); // TODO: Is this really necessary?
            }
        });
        Button delete = new Button("Delete", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                Object itemId = table.getValue();
                if (itemId != null) {
                    table.removeItem(itemId);
                }
            }
        });
        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());
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
            List<VarbindsDecodeDTO> beans = (List<VarbindsDecodeDTO>) value;
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
        ArrayList<VarbindsDecodeDTO> beans = new ArrayList<VarbindsDecodeDTO>(); 
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
        toolbar.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }

}
