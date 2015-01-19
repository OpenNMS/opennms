package org.opennms.features.vaadin.surveillanceviews.ui;

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import com.vaadin.data.Property;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.View;

public class SurveillanceViewConfigEntry extends Panel {
    /**
     * Title textfield
     */
    private TextField m_titleField;

    public SurveillanceViewConfigEntry(final View view) {
        /**
         * Setting up this component with size and layout
         */
        setWidth(100.0f, Unit.PERCENTAGE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.setColumns(6);
        gridLayout.setRows(1);
        gridLayout.setMargin(true);

        m_titleField = new TextField();
        m_titleField.setValue(view.getName());
        m_titleField.setImmediate(true);
        m_titleField.setCaption("Title");
        m_titleField.setDescription("Title of this surveillance view");

        m_titleField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                view.setName((String) valueChangeEvent.getProperty().getValue());
                SurveillanceViewProvider.getInstance().save();
                ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Data saved", "Title");
            }
        });

        /**
         * Duration field setup, layout and adding listener and validator
         */
        final TextField refreshSecondsField = new TextField();
        refreshSecondsField.setValue(String.valueOf(view.getRefreshSeconds()));
        refreshSecondsField.setImmediate(true);
        refreshSecondsField.setCaption("Refresh seconds");
        refreshSecondsField.setDescription("Refresh duration in seconds");

        refreshSecondsField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return true;
            }
        });

        refreshSecondsField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (refreshSecondsField.isValid()) {
                    view.setRefreshSeconds(Integer.parseInt(valueChangeEvent.getProperty().getValue().toString()));
                    SurveillanceViewProvider.getInstance().save();
                    ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Data saved", "Refresh seconds");

                }
            }
        });


        Table columnsTable = new Table();
        columnsTable.setWidth(25, Unit.PERCENTAGE);

        /**
         * Adding the  button...
         */
        Button columnsAddButton = new Button("Add column");
        columnsAddButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        columnsAddButton.setEnabled(true);
        columnsAddButton.setStyleName("small");
        columnsAddButton.setDescription("Add something");

        Button columnsEditButton = new Button("Edit column");
        columnsEditButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        columnsEditButton.setEnabled(true);
        columnsEditButton.setStyleName("small");
        columnsEditButton.setDescription("Add something");

        Button columnsRemoveButton = new Button("Remove column");
        columnsRemoveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        columnsRemoveButton.setEnabled(true);
        columnsRemoveButton.setStyleName("small");
        columnsRemoveButton.setDescription("Add something");

        Table rowsTable = new Table();
        rowsTable.setWidth(25, Unit.PERCENTAGE);

        /**
         * Adding the  button...
         */
        Button rowsAddButton = new Button("Add row");
        rowsAddButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        rowsAddButton.setEnabled(true);
        rowsAddButton.setStyleName("small");
        rowsAddButton.setDescription("Add something");

        Button rowsEditButton = new Button("Edit row");
        rowsEditButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        rowsEditButton.setEnabled(true);
        rowsEditButton.setStyleName("small");
        rowsEditButton.setDescription("Add something");

        Button rowsRemoveButton = new Button("Remove row");
        rowsRemoveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        rowsRemoveButton.setEnabled(true);
        rowsRemoveButton.setStyleName("small");
        rowsRemoveButton.setDescription("Add something");

        Button previewButton = new Button("Preview");
        previewButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        previewButton.setEnabled(true);
        previewButton.setStyleName("small");
        previewButton.setDescription("Add something");

        Button removeButton = new Button("Remove");
        removeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        removeButton.setEnabled(true);
        removeButton.setStyleName("small");
        removeButton.setDescription("Add something");

        FormLayout f1 = new FormLayout();
        f1.addComponent(m_titleField);
        f1.addComponent(refreshSecondsField);

        FormLayout f2 = new FormLayout();
        f2.addComponent(columnsAddButton);
        f2.addComponent(columnsEditButton);
        f2.addComponent(columnsRemoveButton);

        FormLayout f3 = new FormLayout();
        f3.addComponent(rowsAddButton);
        f3.addComponent(rowsEditButton);
        f3.addComponent(rowsRemoveButton);

        FormLayout f4 = new FormLayout();
        f4.addComponent(previewButton);
        f4.addComponent(removeButton);

        /**
         * Adding the different {@link com.vaadin.ui.FormLayout} instances to a {@link com.vaadin.ui.GridLayout}
         */
        f1.setMargin(true);
        f2.setMargin(true);
        f3.setMargin(true);
        f4.setMargin(true);

        gridLayout.addComponent(f1);
        gridLayout.addComponent(columnsTable);
        gridLayout.addComponent(f2);
        gridLayout.addComponent(rowsTable);
        gridLayout.addComponent(f3);
        gridLayout.addComponent(f4);

        setContent(gridLayout);
    }
}
