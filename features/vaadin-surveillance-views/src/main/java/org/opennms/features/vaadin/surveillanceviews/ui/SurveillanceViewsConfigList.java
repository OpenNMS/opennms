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
package org.opennms.features.vaadin.surveillanceviews.ui;

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
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to display a list of editable surveillance view configurations.
 *
 * @author Christian Pape
 */
public class SurveillanceViewsConfigList extends VerticalLayout {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewsConfigList.class);
    /**
     * The {@link Table} this component uses to display {@link View} configurations
     */
    private Table m_table;
    /**
     * The {@link com.vaadin.data.util.BeanItemContainer} this component uses for {@link View} configurations
     */
    BeanItemContainer<View> m_beanItemContainer = new BeanItemContainer<View>(View.class);
    /**
     * the surveillance view service
     */
    private SurveillanceViewService m_surveillanceViewService;

    /**
     * Constructor for creating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used
     */
    public SurveillanceViewsConfigList(SurveillanceViewService surveillanceViewService) {
        /**
         * set the fields
         */
        this.m_surveillanceViewService = surveillanceViewService;

        /**
         * Loading the config
         */
        reloadSurveillanceViews();

        /**
         * Setting up the layout component
         */
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        Label label = new Label("Surveillance View Configurations");
        label.addStyleName("configuration-title");

        /*
        Button button = new Button("Help");
        button.setStyleName("small");
        button.setDescription("Display help and usage");
        */

        /**
         * button.addClickListener(new HelpClickListener(this, m_wallboardConfigView.getDashletSelector()));
         */

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
                } while (SurveillanceViewProvider.getInstance().containsView(newName));

                View view = new View();
                view.setName(newName);

                getUI().addWindow(new SurveillanceViewConfigurationWindow(m_surveillanceViewService, view, new SurveillanceViewConfigurationWindow.SaveActionListener() {
                    @Override
                    public void save(View view) {
                        m_beanItemContainer.addItem(view);

                        SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration().getViews().add(view);
                        SurveillanceViewProvider.getInstance().save();

                        ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Data saved", "Surveillance View");

                        m_table.refreshRowCache();
                        m_table.sort(new Object[]{"name"}, new boolean[]{true});
                    }
                }));
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(label);
        // horizontalLayout.addComponent(button);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);

        horizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        // horizontalLayout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);

        addComponent(horizontalLayout);

        addComponent(addButton);

        /**
         * Adding the table with the required {@link com.vaadin.ui.Table.ColumnGenerator} objects
         */
        m_table = new Table();

        m_table.setContainerDataSource(m_beanItemContainer);
        m_table.setSizeFull();
        m_table.sort(new Object[]{"name"}, new boolean[]{true});

        m_table.addGeneratedColumn("Edit", new Table.ColumnGenerator() {
                    public Object generateCell(Table source, final Object itemId, Object columnId) {
                        Button button = new Button("Edit");
                        button.setDescription("Edit this Surveillance View configuration");
                        button.setStyleName("small");
                        button.addClickListener(new Button.ClickListener() {
                            public void buttonClick(Button.ClickEvent clickEvent) {
                                getUI().addWindow(new SurveillanceViewConfigurationWindow(m_surveillanceViewService, m_beanItemContainer.getItem(itemId).getBean(), new SurveillanceViewConfigurationWindow.SaveActionListener() {
                                    @Override
                                    public void save(View view) {
                                        View oldView = m_beanItemContainer.getItem(itemId).getBean();

                                        m_beanItemContainer.removeItem(itemId);
                                        m_beanItemContainer.addItem(view);

                                        SurveillanceViewProvider.getInstance().replaceView(oldView, view);

                                        SurveillanceViewProvider.getInstance().save();
                                        ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Data saved", "Surveillance view");

                                        m_table.refreshRowCache();
                                        m_table.sort(new Object[]{"name"}, new boolean[]{true});
                                    }
                                }));
                            }
                        });
                        return button;
                    }
                }

        );

        m_table.addGeneratedColumn("Remove", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Button button = new Button("Remove");
                button.setDescription("Delete this Surveillance View configuration");
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
                        button.setDescription("Preview this Surveillance View configuration");
                        button.setStyleName("small");

                        button.addClickListener(new PreviewClickListener(m_surveillanceViewService, SurveillanceViewsConfigList.this, (View) itemId));

                        return button;
                    }
                }

        );

        m_table.addGeneratedColumn("Default", new Table.ColumnGenerator() {
                    public Object generateCell(Table source, final Object itemId, Object columnId) {
                        CheckBox checkBox = new CheckBox();
                        checkBox.setImmediate(true);
                        checkBox.setDescription("Make this Surveillance View configuration the default");

                        final View view = m_beanItemContainer.getItem(itemId).getBean();

                        checkBox.setValue(SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration().getDefaultView().equals(view.getName()));

                        checkBox.addValueChangeListener(new Property.ValueChangeListener() {
                            @Override
                            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                                boolean newValue = ((Boolean) valueChangeEvent.getProperty().getValue());

                                if (newValue) {
                                    SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration().setDefaultView(view.getName());
                                }

                                m_table.refreshRowCache();

                                SurveillanceViewProvider.getInstance().save();

                                ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Data saved", "Default surveillance view");
                            }
                        });
                        return checkBox;
                    }
                }

        );

        m_table.setVisibleColumns(new Object[]{"name", "Edit", "Remove", "Preview", "Default"});
        m_table.setColumnHeader("name", "Name");

        /**
         * Adding the table
         */
        addComponent(m_table);

        setExpandRatio(m_table, 1.0f);
    }

    /**
     * Reloads the configuration.
     */
    public void reloadSurveillanceViews() {
        m_beanItemContainer.removeAllItems();
        m_beanItemContainer.addAll(SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration().getViews());
    }
}
