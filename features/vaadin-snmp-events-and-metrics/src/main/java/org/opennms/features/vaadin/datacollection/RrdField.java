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

import org.opennms.netmgt.config.datacollection.Rrd;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;
import de.steinwedel.vaadin.MessageBox.EventListener;

/**
 * The RRD Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class RrdField extends CustomField implements Button.ClickListener {

    /** The Step. */
    private TextField step = new TextField();

    /** The RRA Table. */
    private Table table = new Table();

    /** The Container. */
    private BeanItemContainer<RRA> container = new BeanItemContainer<RRA>(RRA.class);

    /** The Toolbar. */
    private HorizontalLayout toolbar = new HorizontalLayout();

    /** The add button. */
    private Button add;

    /** The delete button. */
    private Button delete;

    /**
     * Instantiates a new RRD field.
     */
    public RrdField() {
        step.setCaption("RRD Step (in seconds)");
        step.setRequired(true);
        step.setImmediate(true);
        step.setValidationVisible(true);
        step.setNullSettingAllowed(false);
        step.addValidator(new IntegerValidator("Invalid integer {0}"));

        table.setCaption("RRA List");
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"cf", "xff", "steps", "rows"});
        table.setColumnHeaders(new String[]{"Consolidation Function", "XFF", "Steps", "Rows"});
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setImmediate(true);
        table.setHeight("125px");
        table.setWidth("100%");
        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
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
                    field.addValidator(new IntegerValidator("Invalid integer {0}"));
                    return field;
                }
                if (propertyId.equals("xff")) {
                    final TextField field = new TextField();
                    field.setImmediate(true);
                    field.setRequired(true);
                    field.setNullSettingAllowed(false);
                    field.addValidator(new DoubleValidator("Invalid double {0}"));
                    return field;
                }
                return null;
            }
        });

        add = new Button("Add", (Button.ClickListener) this);
        delete = new Button("Delete", (Button.ClickListener) this);
        toolbar.addComponent(add);
        toolbar.addComponent(delete);
        toolbar.setVisible(table.isEditable());

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(step);
        layout.addComponent(table);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.MIDDLE_RIGHT);

        setWriteThrough(false);
        setCompositionRoot(layout);
    }

    /**
     * Instantiates a new RRD field.
     * 
     * @Param caption the field's caption
     */
    public RrdField(String caption) {
        this();
        setCaption(caption);
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#getType()
     */
    @Override
    public Class<?> getType() {
        return Rrd.class;
    }

    /* (non-Javadoc)
     * @see org.vaadin.addon.customfield.CustomField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object value = newDataSource.getValue();
        if (value instanceof Rrd) {
            Rrd dto = (Rrd) value;
            step.setValue(dto.getStep().toString());
            container.removeAllItems();
            List<RRA> rras = new ArrayList<RRA>();
            for (String rra : dto.getRraCollection()) {
                rras.add(new RRA(rra));
            }
            container.addAll(rras);
            table.setPageLength(dto.getRraCount());
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
        Rrd dto = new Rrd();
        dto.setStep(new Integer((String) step.getValue()));
        for (Object itemId: container.getItemIds()) {
            dto.addRra(container.getItem(itemId).getBean().getRra());
        }
        return dto;
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
        container.addBean(new RRA());
    }

    /**
     * Delete handler.
     */
    private void deleteHandler() {
        final Object itemId = table.getValue();
        if (itemId == null) {
            getApplication().getMainWindow().showNotification("Please select a RRA from the table.");
        } else {
            MessageBox mb = new MessageBox(getApplication().getMainWindow(),
                                           "Are you sure?",
                                           MessageBox.Icon.QUESTION,
                                           "Do you really want to remove the selected RRA?<br/>This action cannot be undone.",
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
