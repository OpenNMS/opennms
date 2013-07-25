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
package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.data.Container;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import java.util.Map;

/**
 * Class representing the properties window used for editing dashlet parameters.
 *
 * @author Christian Pape
 */
public class PropertiesWindow extends DashletConfigurationWindow {

    /**
     * Constructor for instantiating a {@link PropertiesWindow} for a given {@link DashletSpec}.
     *
     * @param dashletSpec    the {@link DashletSpec} to edit
     * @param dashletFactory the {@link DashletFactory} for querying the property data
     */
    public PropertiesWindow(final DashletSpec dashletSpec, final DashletFactory dashletFactory) {
        /**
         * Using a vertical layout for content
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        //verticalLayout.addStyleName("debug");
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Unit.PERCENTAGE);

        /**
         * Setting up the table object for displaying the parameters
         */
        final Table table = new Table();

        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                Field field = super.createField(container, itemId, propertyId, uiContext);
                if (propertyId.equals("Key")) {
                    field.setReadOnly(true);
                } else {
                    field.setSizeFull();
                }
                return field;
            }
        });

        table.setEditable(true);
        table.setSizeFull();
        table.setImmediate(true);

        table.addContainerProperty("Key", String.class, "");
        table.addContainerProperty("Value", String.class, "");

        /**
         * Filling the date with parameter data
         */
        final Map<String, String> requiredParameters = dashletFactory.getRequiredParameters();

        for (Map.Entry<String, String> entry : requiredParameters.entrySet()) {
            table.addItem(new Object[]{entry.getKey(), dashletSpec.getParameters().get(entry.getKey())}, entry.getKey());
        }

        table.setColumnWidth("Key", 100);
        table.setColumnWidth("Value", -1);
        table.setSizeFull();
        verticalLayout.addComponent(table);

        /**
         * Using an additional {@link HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);

        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        horizontalLayout.addComponent(cancel);
        horizontalLayout.setExpandRatio(cancel, 1);
        horizontalLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");

        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Map.Entry<String, String> entry : requiredParameters.entrySet()) {
                    String newValue = table.getItem(entry.getKey()).getItemProperty("Value").getValue().toString();
                    dashletSpec.getParameters().put(entry.getKey(), newValue);
                }

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        horizontalLayout.addComponent(ok);
        //horizontalLayout.addStyleName("debug");

        /**
         * Adding the layout and setting the content
         */
        verticalLayout.addComponent(horizontalLayout);

        verticalLayout.setExpandRatio(table, 1.0f);

        setContent(verticalLayout);
    }
}
