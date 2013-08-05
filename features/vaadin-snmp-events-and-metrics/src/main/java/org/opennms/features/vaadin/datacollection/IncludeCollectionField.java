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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * The Include Collection Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class IncludeCollectionField extends CustomField<ArrayList<IncludeCollection>> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3677540981240383672L;

    /** The Container. */
    private final BeanItemContainer<IncludeCollectionWrapper> container = new BeanItemContainer<IncludeCollectionWrapper>(IncludeCollectionWrapper.class);

    /** The Include Field Table. */
    private final Table table = new Table("Includes List", container);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /**
     * Instantiates a new include collection field.
     * 
     * @param dataCollectionConfigDao the data collection configuration DAO
     */
    public IncludeCollectionField(final DataCollectionConfigDao dataCollectionConfigDao) {
        table.addStyleName("light");
        table.setVisibleColumns(new Object[]{"type", "value"});
        table.setColumnHeaders(new String[]{"Type", "Value"});
        table.setSelectable(true);
        table.setImmediate(true);
        table.setHeight("125px");
        table.setWidth("100%");
        final Button add = new Button("Add", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                final IncludeCollectionWrapper obj = new IncludeCollectionWrapper();
                IncludeCollectionWindow w = new IncludeCollectionWindow(dataCollectionConfigDao, container, obj) {
                    @Override
                    public void fieldChanged() {
                        container.addBean(obj);
                        table.select(obj);
                    }
                };
                getUI().addWindow(w);
            }
        });
        final Button edit = new Button("Edit", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                final Object value = table.getValue();
                if (value == null) {
                    Notification.show("Please select a IncludeCollection from the table.");
                    return;
                }
                IncludeCollectionWindow w = new IncludeCollectionWindow(dataCollectionConfigDao, container, (IncludeCollectionWrapper) value) {
                    @Override
                    public void fieldChanged() {
                        table.refreshRowCache();
                    }
                };
                getUI().addWindow(w);
            }
        });
        final Button delete = new Button("Delete", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                deleteHandler();
            }
        });

        toolbar.addComponent(add);
        toolbar.addComponent(edit);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        setBuffered(true);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<IncludeCollection>> getType() {
        return (Class<ArrayList<IncludeCollection>>) new ArrayList<IncludeCollection>().getClass();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<IncludeCollection> list = (List<IncludeCollection>) value;
            List<IncludeCollectionWrapper> groups = new ArrayList<IncludeCollectionWrapper>();
            for (IncludeCollection ic : list) {
                groups.add(new IncludeCollectionWrapper(ic));
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
     * @see com.vaadin.ui.AbstractField#getValue()
     */
    @Override
    public ArrayList<IncludeCollection> getValue() {
        ArrayList<IncludeCollection> list = new ArrayList<IncludeCollection>();
        for (Object itemId: container.getItemIds()) {
            IncludeCollectionWrapper obj = container.getItem(itemId).getBean();
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
        super.setReadOnly(readOnly);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a IncludeCollection from the table.");
            return;
        }
        ConfirmDialog.show(getUI(),
                           "Are you sure?",
                           "Do you really want to remove the selected Include Collection field?\nThis action cannot be undone.",
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
