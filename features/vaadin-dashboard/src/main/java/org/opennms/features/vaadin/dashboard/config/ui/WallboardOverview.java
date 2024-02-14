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

import org.opennms.features.vaadin.dashboard.model.Wallboard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;

/**
 * This class is used to display an brief overview about existing {@link Wallboard} configurations.
 *
 * @author Christian Pape
 */
public class WallboardOverview extends VerticalLayout {
    /**
     * The {@link Table} this component uses to display {@link Wallboard} configurations
     */
    private Table m_table;
    /**
     * The {@link WallboardConfigView} this component belongs to.
     */
    private WallboardConfigView m_wallboardConfigView;
    /**
     * The {@link BeanItemContainer} this component uses for {@link Wallboard} configurations
     */
    BeanItemContainer<Wallboard> m_beanItemContainer;

    /**
     * Constructor for creating new instances.
     *
     * @param wallboardConfigView the {@link WallboardConfigView}
     */
    public WallboardOverview(WallboardConfigView wallboardConfigView) {
        /**
         * Setting the member fields
         */
        this.m_wallboardConfigView = wallboardConfigView;

        /**
         * Setting up the layout component
         */
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        Label label = new Label("Overview");
        label.addStyleName("configuration-title");

        Button button = new Button("Help");
        button.setStyleName("small");
        button.setDescription("Display help and usage");

        button.addClickListener(new HelpClickListener(this, m_wallboardConfigView.getDashletSelector()));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(label);
        horizontalLayout.addComponent(button);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);

        horizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);

        addComponent(horizontalLayout);

        /**
         * Adding the table with the required {@link Table.ColumnGenerator} objects
         */
        m_table = new Table();
        m_table.setSizeFull();

        m_table.addGeneratedColumn("Edit", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                Button button = new Button("Edit");
                button.setDescription("Edit this Ops Board configuration");
                button.setStyleName("small");
                button.addClickListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        m_wallboardConfigView.openWallboardEditor((Wallboard) itemId);
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
                        m_wallboardConfigView.removeTab(((Wallboard) itemId).getTitle());
                        WallboardProvider.getInstance().removeWallboard((Wallboard) itemId);
                        refreshTable();
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
                button.addClickListener(new PreviewClickListener(WallboardOverview.this, (Wallboard) itemId));
                return button;
            }
        });

        m_table.addGeneratedColumn("Default", new Table.ColumnGenerator() {
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                CheckBox checkBox = new CheckBox();
                checkBox.setImmediate(true);
                checkBox.setDescription("Make this Ops Board configuration the default");

                final Wallboard wallboard = m_beanItemContainer.getItem(itemId).getBean();
                checkBox.setValue(wallboard.isDefault());

                checkBox.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                        boolean newValue = ((Boolean) valueChangeEvent.getProperty().getValue());

                        if (newValue) {
                            for (Wallboard wallboard1 : m_beanItemContainer.getItemIds()) {
                                wallboard1.setDefault(false);
                            }
                        }

                        wallboard.setDefault(newValue);

                        m_table.refreshRowCache();

                        WallboardProvider.getInstance().save();
                    }
                });
                return checkBox;
            }
        });

        refreshTable();

        /**
         * Adding the table
         */
        addComponent(m_table);

        setExpandRatio(m_table, 1.0f);
    }

    void refreshTable() {
        if (m_table != null) {
            m_beanItemContainer = WallboardProvider.getInstance().getBeanContainer();
            m_table.setContainerDataSource(m_beanItemContainer);
            m_table.setVisibleColumns(new Object[]{"title", "Edit", "Remove", "Preview", "Default"});
            m_table.setColumnHeader("title", "Title");
            m_table.sort();
            m_table.refreshRowCache();
        }
    }
}
