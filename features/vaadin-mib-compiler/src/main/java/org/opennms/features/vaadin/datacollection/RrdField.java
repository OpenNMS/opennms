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
package org.opennms.features.vaadin.datacollection;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.datacollection.Rrd;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
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
     * The Class RRA.
     */
    public class RRA {

        /** The consolidation function. */
        private String cf;

        /** The XFF. */
        private Double xff;

        /** The steps. */
        private Integer steps;

        /** The rows. */
        private Integer rows;

        /**
         * Instantiates a new RRA.
         */
        public RRA() {}

        /**
         * Instantiates a new RRA.
         *
         * @param rra the RRA
         */
        public RRA(String rra) {
            setRra(rra);
        }

        /**
         * Gets the consolidation function.
         *
         * @return the consolidation function
         */
        public String getCf() {
            return cf;
        }

        /**
         * Sets the consolidation function.
         *
         * @param cf the new consolidation function
         */
        public void setCf(String cf) {
            this.cf = cf;
        }

        /**
         * Gets the XFF.
         *
         * @return the XFF
         */
        public Double getXff() {
            return xff;
        }

        /**
         * Sets the XFF.
         *
         * @param xff the new XFF
         */
        public void setXff(Double xff) {
            this.xff = xff;
        }

        /**
         * Gets the steps.
         *
         * @return the steps
         */
        public Integer getSteps() {
            return steps;
        }

        /**
         * Sets the steps.
         *
         * @param steps the new steps
         */
        public void setSteps(Integer steps) {
            this.steps = steps;
        }

        /**
         * Gets the rows.
         *
         * @return the rows
         */
        public Integer getRows() {
            return rows;
        }

        /**
         * Sets the rows.
         *
         * @param rows the new rows
         */
        public void setRows(Integer rows) {
            this.rows = rows;
        }

        /**
         * Gets the RRA.
         *
         * @return the RRA
         */
        public String getRra() {
            return "RRA:" + cf + ':' + xff + ':' + steps + ':' + rows;
        }

        /**
         * Sets the RRA.
         *
         * @param rra the new RRA
         */
        public void setRra(String rra) {
            String [] parts = rra.split(":");
            if (parts.length < 5)
                throw new IllegalArgumentException("Malformed RRA");
            try {
                setCf(parts[1]);
                setXff(new Double(parts[2]));
                setSteps(new Integer(parts[3]));
                setRows(new Integer(parts[4]));
            } catch (Exception e) {
                throw new IllegalArgumentException("Malformed RRA");
            }
        }
    }

    /**
     * Instantiates a new RRD field.
     */
    public RrdField() {
        step.setCaption("Step");
        step.setRequired(true);
        step.setNullSettingAllowed(false);

        table.setCaption("RRAs");
        table.setContainerDataSource(container);
        table.setStyleName(Runo.TABLE_SMALL);
        table.setVisibleColumns(new Object[]{"cf", "xff", "steps", "rows"});
        table.setColumnHeaders(new String[]{"Consolidation Function", "XFF", "Steps", "Rows"});
        table.setEditable(!isReadOnly());
        table.setSelectable(true);
        table.setHeight("125px");
        table.setWidth("100%");

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
            step.setValue(dto.getStep());
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
        dto.setStep((Integer) step.getValue());
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
                    "Do you really want to continue?",
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
