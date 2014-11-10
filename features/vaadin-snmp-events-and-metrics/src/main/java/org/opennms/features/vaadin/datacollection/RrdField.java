/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.datacollection;

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Container;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The RRD Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class RrdField extends CustomField<Rrd> implements Button.ClickListener {

    /** The Step. */
    private final TextField step = new TextField("RRD Step (in seconds)");

    /** The Container. */
    private final OnmsBeanContainer<RRA> container = new OnmsBeanContainer<RRA>(RRA.class);

    /** The RRA Table. */
    private final Table table = new Table("RRA List", container);

    /** The Toolbar. */
    private final HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private final Button add = new Button("Add", this);

    /** The delete button. */
    private final Button delete = new Button("Delete", this);

    /**
     * Instantiates a new RRD field.
     */
    public RrdField() {
        step.setRequired(true);
        step.setImmediate(true);
        step.setValidationVisible(true);
        step.setNullSettingAllowed(false);
        step.setConverter(new StringToIntegerConverter());

        table.addStyleName("light");
        table.setVisibleColumns(new Object[]{"cf", "xff", "steps", "rows"});
        table.setColumnHeaders(new String[]{"Consolidation Function", "XFF", "Steps", "Rows"});
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setImmediate(true);
        table.setHeight("125px");
        table.setWidth("100%");
        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (propertyId.equals("cf")) {
                    final ComboBox field = new ComboBox();
                    field.setImmediate(true);
                    field.setRequired(true);
                    field.setNullSelectionAllowed(false);
                    field.addItem("AVERAGE");
                    field.addItem("MIN");
                    field.addItem("MAX");
                    field.addItem("LAST");
                    return field;
                }
                if (propertyId.equals("steps") || propertyId.equals("rows")) {
                    final TextField field = new TextField();
                    field.setImmediate(true);
                    field.setRequired(true);
                    field.setNullSettingAllowed(false);
                    field.setConverter(new StringToIntegerConverter());
                    return field;
                }
                if (propertyId.equals("xff")) {
                    final TextField field = new TextField();
                    field.setImmediate(true);
                    field.setRequired(true);
                    field.setNullSettingAllowed(false);
                    field.setConverter(new StringToDoubleConverter());
                    return field;
                }
                return null;
            }
        });

        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(step);
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    /**
     * Instantiates a new RRD field.
     *
     * @param caption the caption
     * @Param caption the field's caption
     */
    public RrdField(String caption) {
        this();
        setCaption(caption);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<Rrd> getType() {
        return Rrd.class;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(Rrd rrd) {
        boolean stepState = step.isReadOnly();
        step.setReadOnly(false);
        step.setValue(rrd.getStep().toString());
        if (stepState) {
            step.setReadOnly(true);
        }
        container.removeAllItems();
        for (String rra : rrd.getRras()) {
            container.addOnmsBean(new RRA(rra));
        }
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected Rrd getInternalValue() {
        Rrd rrd = new Rrd();
        try {
            rrd.setStep(Integer.valueOf((String) step.getValue()));
        } catch (NumberFormatException e) {
            rrd.setStep(null);
        }
        for (RRA rra: container.getOnmsBeans()) {
            rrd.addRra(rra.getRra());
        }
        return rrd;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        step.setReadOnly(readOnly);
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

    /**
     * Adds the handler.
     */
    private void addHandler() {
        RRA rra = new RRA();
        rra.setCf("AVERAGE");
        rra.setXff(0.5);
        rra.setSteps(0);
        rra.setRows(0);
        container.addOnmsBean(rra);
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            Notification.show("Please select a RRA from the table.");
        } else {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to remove the selected RRA?\nThis action cannot be undone.",
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

    /**
     * Gets the step value.
     *
     * @return the step value
     */
    public Integer getStepValue() {
        final String value = step.getValue();
        return value == null ? null : Integer.valueOf(value);
    }
}
