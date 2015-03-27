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

package org.opennms.features.vaadin.dashboard.config.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.Wallboard;

import com.vaadin.data.Container;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * This class represents the base editing component for {@link Wallboard} instances.
 *
 * @author Christian Pape
 */
public class WallboardConfigView extends HorizontalLayout implements TabSheet.CloseHandler, DashletSelector.ServiceListChangedListener {

    /**
     * The {@link TabSheet} for displaying the {@link WallboardEditor} components
     */
    private WallboardTabSheet m_tabSheet;
    /**
     * The {@link DashletSelector} used for querying the configuration data
     */
    private DashletSelector m_dashletSelector;
    /**
     * A map used to store {@link Wallboard} and {@link TabSheet.Tab} instances
     */
    private Map<Wallboard, TabSheet.Tab> m_wallboardEditorMap = new HashMap<Wallboard, TabSheet.Tab>();

    /**
     * The constructor used for instantiating new objects.
     *
     * @param dashletSelector the {@link DashletSelector} to be used
     */
    public WallboardConfigView(DashletSelector dashletSelector) {
        /**
         * Setting the member fields
         */
        m_dashletSelector = dashletSelector;

        /**
         * Setting up the layout components and the {@link TabSheet}
         */
        setSizeFull();

        m_tabSheet = new WallboardTabSheet() {
            @Override
            protected void addNewTabComponent() {
                WallboardConfigView.this.addNewTabComponent();
            }
        };

        m_tabSheet.setSizeFull();

        /**
         * Adding the {@link WallboardOverview}
         */
        WallboardOverview dashboardOverview = new WallboardOverview(this);

        Tab overviewTab = m_tabSheet.addTab(dashboardOverview, "Overview");

        overviewTab.setClosable(false);

        m_tabSheet.setSelectedTab(overviewTab);
        m_tabSheet.setCloseHandler(this);

        addComponent(m_tabSheet);

        dashletSelector.addServiceListChangedListener(this);

        /**
         * Adding the listeners
         */
        WallboardProvider.getInstance().getBeanContainer().addItemSetChangeListener(new Container.ItemSetChangeListener() {
            public void containerItemSetChange(Container.ItemSetChangeEvent itemSetChangeEvent) {
                List<Wallboard> wallboardsToRemove = new ArrayList<Wallboard>();
                List<TabSheet.Tab> tabsToRemove = new ArrayList<TabSheet.Tab>();
                for (Map.Entry<Wallboard, TabSheet.Tab> entry : m_wallboardEditorMap.entrySet()) {
                    WallboardEditor wallboardEditor = (WallboardEditor) entry.getValue().getComponent();
                    if (!WallboardProvider.getInstance().containsWallboard(wallboardEditor.getWallboard())) {
                        wallboardsToRemove.add(wallboardEditor.getWallboard());
                        tabsToRemove.add(entry.getValue());
                    }
                }
                for (TabSheet.Tab tab : tabsToRemove) {
                    m_tabSheet.removeTab(tab);
                }
                for (Wallboard wallboard : wallboardsToRemove) {
                    m_wallboardEditorMap.remove(wallboard);
                }
            }
        });
    }

    /**
     * This method opens a {@link WallboardEditor} for a given {@link Wallboard}.
     *
     * @param wallboard the wallboard to be edited
     */
    public void openWallboardEditor(Wallboard wallboard) {
        if (m_wallboardEditorMap.containsKey(wallboard)) {
            m_tabSheet.setSelectedTab(m_wallboardEditorMap.get(wallboard));
        } else {
            WallboardEditor wallboardEditor = new WallboardEditor(m_dashletSelector, wallboard);

            TabSheet.Tab tab = m_tabSheet.addTab(wallboardEditor, wallboard.getTitle(), null);
            wallboardEditor.setTab(tab);
            tab.setClosable(true);

            m_wallboardEditorMap.put(wallboard, tab);

            m_tabSheet.setSelectedTab(tab);
        }
    }

    /**
     * This method is used to add a new {@link TabSheet.Tab} component. It creates a new window querying the user for the name of the new {@link Wallboard}.
     */
    protected void addNewTabComponent() {
        final Window window = new Window("New Ops Board");

        window.setModal(true);
        window.setClosable(false);
        window.setResizable(false);

        getUI().addWindow(window);

        window.setContent(new VerticalLayout() {
            TextField name = new TextField("Ops Board Name");

            {
                addComponent(new FormLayout() {
                    {
                        setSizeUndefined();
                        setMargin(true);

                        String newName = "Untitled";
                        int i = 1;
                        if (WallboardProvider.getInstance().containsWallboard(newName)) {
                            do {
                                i++;
                                newName = "Untitled #" + i;
                            } while (WallboardProvider.getInstance().containsWallboard(newName));
                        }
                        name.setValue(newName);
                        addComponent(name);
                        name.focus();
                        name.selectAll();

                        name.addValidator(new AbstractStringValidator("Title must be unique") {
                            @Override
                            protected boolean isValidValue(String s) {
                                return (!WallboardProvider.getInstance().containsWallboard(s) && !"".equals(s));
                            }
                        });
                    }
                });

                addComponent(new HorizontalLayout() {
                    {
                        setMargin(true);
                        setSpacing(true);
                        setWidth("100%");

                        Button cancel = new Button("Cancel");
                        cancel.setDescription("Cancel editing");
                        cancel.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                // NMS-7560: Toggle the tab in order to allow us to click it again
                                m_tabSheet.togglePlusTab();
                                window.close();
                            }
                        });

                        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
                        addComponent(cancel);
                        setExpandRatio(cancel, 1);
                        setComponentAlignment(cancel, Alignment.TOP_RIGHT);

                        Button ok = new Button("Save");
                        ok.setDescription("Save configuration");
                        ok.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                if (name.isValid()) {
                                    Wallboard wallboard = new Wallboard();
                                    wallboard.setTitle(name.getValue());

                                    WallboardProvider.getInstance().addWallboard(wallboard);
                                    WallboardProvider.getInstance().save();

                                    WallboardEditor wallboardEditor = new WallboardEditor(m_dashletSelector, wallboard);
                                    TabSheet.Tab tab = m_tabSheet.addTab(wallboardEditor, wallboard.getTitle());

                                    wallboardEditor.setTab(tab);

                                    m_wallboardEditorMap.put(wallboard, tab);

                                    tab.setClosable(true);

                                    m_tabSheet.setSelectedTab(tab);

                                    window.close();
                                }
                            }
                        });

                        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);

                        addComponent(ok);
                    }
                });
            }
        });
    }

    /**
     * This method is used for updating the available {@link DashletFactory} instances.
     *
     * @param factoryList the available {@link DashletFactory} instances.
     */
    public void serviceListChanged(List<DashletFactory> factoryList) {
        for (Map.Entry<Wallboard, TabSheet.Tab> entry : m_wallboardEditorMap.entrySet()) {
            WallboardEditor wallboardEditor = (WallboardEditor) entry.getValue().getComponent();
            wallboardEditor.updateServiceList(factoryList);
        }

        ((WallboardConfigUI) getUI()).notifyMessage("Configuration change", "Dashlet list modified");
    }


    /**
     * Method to invoke when a {@link TabSheet.Tab} is closed.
     *
     * @param tabsheet
     * @param tabContent
     */
    public void onTabClose(final TabSheet tabsheet, final Component tabContent) {
        tabsheet.removeComponent(tabContent);
        m_wallboardEditorMap.remove(((WallboardEditor) tabContent).getWallboard());
    }

    /**
     * Returns the associated {@link DashletSelector}.
     *
     * @return the associated {@link DashletSelector}
     */
    public DashletSelector getDashletSelector() {
        return m_dashletSelector;
    }
}
