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

import org.opennms.netmgt.config.datacollection.MibObj;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The MIB Object Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibObjField extends CustomField implements Button.ClickListener {

    /** The Table. */
    private Table table = new Table();

    /** The Container. */
    private BeanContainer<String,MibObj> container = new BeanContainer<String,MibObj>(MibObj.class);

    /** The Toolbar. */
    private HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private Button add;

    /** The delete button. */
    private Button delete;

    /**
     * Instantiates a new MIB object field.
     *
     * @param resourceTypes the available resource types
     */
    public MibObjField(final List<String> resourceTypes) {
        container.setBeanIdProperty("oid");
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"oid", "instance", "alias", "type"});
        table.setColumnHeader("oid", "OID");
        table.setColumnHeader("instance", "Instance");
        table.setColumnHeader("alias", "Alias");
        table.setColumnHeader("type", "Type");
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("250px");
        table.setWidth("100%");
        table.setTableFieldFactory(new MibObjFieldFactory(resourceTypes));

        add = new Button("Add", (Button.ClickListener) this);
        delete = new Button("Delete", (Button.ClickListener) this);
        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        setWriteThrough(false);
        setValidationVisible(true);
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
    @SuppressWarnings("unchecked")
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof List<?>) {
            List<MibObj> beans = (List<MibObj>) value;
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
        ArrayList<MibObj> beans = new ArrayList<MibObj>(); 
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

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#isValid()
     */
    @Override
    public boolean isValid() {
        return table.isValid(); // FIXME This is not working
    }

    /**
     * Adds the handler.
     */
    private void addHandler() {
        MibObj obj = new MibObj();
        obj.setOid("1.1.1.1");
        container.addBean(obj);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            getApplication().getMainWindow().showNotification("Please select a MIB Object from the table.");
        } else {
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                           "Are you sure?",
                                           MessageBox.Icon.QUESTION,
                                           "Do you really want to remove the selected MIB Object?<br/>This action cannot be undone.",
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
