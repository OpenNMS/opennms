/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.datacollection;

import org.opennms.features.vaadin.api.OnmsBeanContainer;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.converter.StringToDoubleConverter;
import com.vaadin.v7.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.DefaultFieldFactory;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

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
        table.setSizeFull();
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
