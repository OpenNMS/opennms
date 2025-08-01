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
package org.opennms.features.vaadin.surveillanceviews.ui;

import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

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
