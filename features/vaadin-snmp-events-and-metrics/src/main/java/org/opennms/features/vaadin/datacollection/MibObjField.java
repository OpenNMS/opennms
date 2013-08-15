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

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * The MIB Object Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibObjField extends CustomField<ArrayList<MibObj>> implements Button.ClickListener {

    /** The Container. */
    private final OnmsBeanContainer<MibObj> container = new OnmsBeanContainer<MibObj>(MibObj.class);

    /** The Table. */
    private final Table table = new Table(null, container);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add = new Button("Add", this);

    /** The delete button. */
    private final Button delete = new Button("Delete", this);

    /**
     * Instantiates a new MIB object field.
     *
     * @param resourceTypes the available resource types
     */
    public MibObjField(final List<String> resourceTypes) {
        table.addStyleName("light");
        table.setVisibleColumns(new Object[] { "oid", "instance", "alias", "type" });
        table.setColumnHeaders(new String[] { "OID", "Instance", "Alias", "Type" });
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("250px");
        table.setWidth("100%");
        table.setTableFieldFactory(new MibObjFieldFactory(resourceTypes));

        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        setValidationVisible(true);
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
    public Class<ArrayList<MibObj>> getType() {
        return (Class<ArrayList<MibObj>>) new ArrayList<MibObj>().getClass();
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(ArrayList<MibObj> mibObjects) {
        super.setInternalValue(mibObjects); // TODO Is this required ?
        container.removeAllItems();
        container.addAll(mibObjects);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected ArrayList<MibObj> getInternalValue() {
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

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#isValid()
     */
    @Override
    public boolean isValid() {
        return super.isValid() && table.isValid(); // FIXME This is not working
    }

    /**
     * Adds the handler.
     */
    private void addHandler() {
        MibObj obj = new MibObj();
        obj.setOid(".1.1.1.1");
        obj.setInstance("0");
        obj.setType("gauge");
        obj.setAlias("test");
        table.select(container.addOnmsBean(obj));
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a MIB Object from the table.");
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to remove the selected MIB Object?\nThis action cannot be undone.",
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

}
