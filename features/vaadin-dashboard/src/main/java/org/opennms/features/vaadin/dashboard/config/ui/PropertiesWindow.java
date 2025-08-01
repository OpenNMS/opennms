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
package org.opennms.features.vaadin.dashboard.config.ui;

import java.util.Map;

import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.ui.DefaultFieldFactory;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

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
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Unit.PERCENTAGE);

        /**
         * Setting up the table object for displaying the parameters
         */
        final Table table = new Table();

        table.setTableFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                Field<?> field = super.createField(container, itemId, propertyId, uiContext);
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
            table.addItem(new Object[]{entry.getKey(), dashletSpec.getParameters().containsKey(entry.getKey()) ? dashletSpec.getParameters().get(entry.getKey()) : ""}, entry.getKey());
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
        cancel.setDescription("Cancel editing properties");
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
        ok.setDescription("Save properties and close");

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
