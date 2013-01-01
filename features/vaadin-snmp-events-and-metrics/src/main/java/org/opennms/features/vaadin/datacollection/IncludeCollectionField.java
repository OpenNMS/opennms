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
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
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
public class IncludeCollectionField extends CustomField {

    /** The Include Field Table. */
    private final Table table = new Table();

    /** The Container. */
    private final BeanItemContainer<IncludeCollectionWrapper> container = new BeanItemContainer<IncludeCollectionWrapper>(IncludeCollectionWrapper.class);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add;

    /** The edit button. */
    private final Button edit;

    /** The delete button. */
    private final Button delete;

    /**
     * Instantiates a new include collection field.
     * 
     * @param dataCollectionConfigDao the data collection configuration DAO
     */
    public IncludeCollectionField(final DataCollectionConfigDao dataCollectionConfigDao) {
        table.setCaption("Includes List");
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"type", "value"});
        table.setColumnHeaders(new String[]{"Type", "Value"});
        table.setSelectable(true);
        table.setImmediate(true);
        table.setHeight("125px");
        table.setWidth("100%");
        add = new Button("Add", new Button.ClickListener() {
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
                getApplication().getMainWindow().addWindow(w);
            }
        });
        edit = new Button("Edit", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                final Object value = table.getValue();
                if (value == null) {
                    getApplication().getMainWindow().showNotification("Please select a IncludeCollection from the table.");
                    return;
                }
                IncludeCollectionWindow w = new IncludeCollectionWindow(dataCollectionConfigDao, container, (IncludeCollectionWrapper) value) {
                    @Override
                    public void fieldChanged() {
                        table.refreshRowCache();
                    }
                };
                getApplication().getMainWindow().addWindow(w);
            }
        });
        delete = new Button("Delete", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                deleteHandler();
            }
        });

        toolbar.addComponent(add);
        toolbar.addComponent(edit);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

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
     * @see org.vaadin.addon.customfield.CustomField#getValue()
     */
    @Override
    public Object getValue() {
        List<IncludeCollection> list = new ArrayList<IncludeCollection>();
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
            getApplication().getMainWindow().showNotification("Please select a IncludeCollection from the table.");
            return;
        }
        MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                       "Are you sure?",
                                       MessageBox.Icon.QUESTION,
                                       "Do you really want to remove the selected Include Collection field<br/>This action cannot be undone.",
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
