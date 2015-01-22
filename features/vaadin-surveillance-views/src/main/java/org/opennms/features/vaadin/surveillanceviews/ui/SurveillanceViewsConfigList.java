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
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.ColumnDef;
import org.opennms.features.vaadin.surveillanceviews.model.RowDef;
import org.opennms.features.vaadin.surveillanceviews.model.SurveillanceViewConfiguration;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;

public class SurveillanceViewsConfigList extends VerticalLayout {

    /**
     * The {@link Table} this component uses to display {@link View} configurations
     */
    private Table m_table;

    /**
     * The {@link com.vaadin.data.util.BeanItemContainer} this component uses for {@link View} configurations
     */
    BeanItemContainer<View> m_beanItemContainer;

    private SurveillanceViewService m_surveillanceViewService;

    private SurveillanceViewConfiguration m_surveillanceViewConfiguration = SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration();

    public SurveillanceViewsConfigList(SurveillanceViewService surveillanceViewService) {
        this.m_surveillanceViewService = surveillanceViewService;

        /**
         * Setting the member fields
         */
        m_beanItemContainer = SurveillanceViewProvider.getInstance().getBeanContainer();

        /**
         * Setting up the layout component
         */
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        Label label = new Label("Surveillance View Configurations");
        label.addStyleName("configuration-title");

        Button button = new Button("Help");
        button.setStyleName("small");
        button.setDescription("Display help and usage");

        //button.addClickListener(new HelpClickListener(this, m_wallboardConfigView.getDashletSelector()));

        Button addButton = new Button("Add");
        addButton.setStyleName("small");
        addButton.setDescription("Add surveillance view configuration");

        addButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                String newName;
                int i = 0;

                do {
                    i++;
                    newName = "Untitled #" + i;
                }
                while (SurveillanceViewProvider.getInstance().containsView(newName));

                View view = new View();
                view.setName(newName);

                view.getColumns().add(new ColumnDef());
                view.getRows().add(new RowDef());

                m_surveillanceViewConfiguration.getViews().add(view);

                SurveillanceViewProvider.getInstance().save();

                m_beanItemContainer.addItem(view);

                //m_beanItemContainer = SurveillanceViewProvider.getInstance().getBeanContainer();

                getUI().addWindow(new SurveillanceViewConfigurationWindow(m_surveillanceViewService, view));

                m_table.refreshRowCache();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(label);
        horizontalLayout.addComponent(button);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);

        horizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);

        addComponent(horizontalLayout);
        addComponent(addButton);

        /**
         * Adding the table with the required {@link com.vaadin.ui.Table.ColumnGenerator} objects
         */
        m_table = new Table();
        m_table.setContainerDataSource(m_beanItemContainer);
        m_table.setSizeFull();

        m_table.addGeneratedColumn("Edit", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Button button = new Button("Edit");
                button.setDescription("Edit this Ops Board configuration");
                button.setStyleName("small");
                button.addClickListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        getUI().addWindow(new SurveillanceViewConfigurationWindow(m_surveillanceViewService, m_beanItemContainer.getItem(itemId).getBean()));
                    }
                });
                return button;
            }
        });

        m_table.addGeneratedColumn("Remove", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Button button = new Button("Remove");
                button.setDescription("Delete this Ops Board configuration");
                button.setStyleName("small");
                button.addClickListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        SurveillanceViewProvider.getInstance().removeView((View) itemId);
                        m_beanItemContainer.removeItem(itemId);
                    }
                });
                return button;
            }
        });

        m_table.addGeneratedColumn("Preview", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Button button = new Button("Preview");
                button.setDescription("Preview this Ops Board configuration");
                button.setStyleName("small");
                //button.addClickListener(new PreviewClickListener(WallboardOverview.this, (Wallboard) itemId));
                return button;
            }
        });

        m_table.addGeneratedColumn("Default", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                CheckBox checkBox = new CheckBox();
                checkBox.setImmediate(true);
                checkBox.setDescription("Make this Ops Board configuration the default");

                final View view = m_beanItemContainer.getItem(itemId).getBean();

                checkBox.setValue(m_surveillanceViewConfiguration.getDefaultView().equals(view.getName()));

                checkBox.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                        boolean newValue = ((Boolean) valueChangeEvent.getProperty().getValue());


                        if (newValue) {
                            m_surveillanceViewConfiguration.setDefaultView(view.getName());
                        }

                        m_table.refreshRowCache();

                        SurveillanceViewProvider.getInstance().save();
                    }
                });
                return checkBox;
            }
        });

        m_table.setVisibleColumns(new Object[]{"name", "Edit", "Remove", "Preview", "Default"});
        m_table.setColumnHeader("name", "Name");

        /**
         * Adding the table
         */
        addComponent(m_table);

        setExpandRatio(m_table, 1.0f);
    }
}
