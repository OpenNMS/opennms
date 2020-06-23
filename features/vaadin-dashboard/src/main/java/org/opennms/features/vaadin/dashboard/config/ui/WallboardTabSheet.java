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

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TabSheet;

/**
 * A helper class for constructing the plus-button in a {@link TabSheet} component.
 *
 * @author
 */
public abstract class WallboardTabSheet extends TabSheet {
    /**
     * The layout this component uses
     */
    private CssLayout m_plusTab;
    /**
     * The last tab
     */
    private Component m_lastTab;

    /**
     * Abstract method for adding a new component.
     */
    protected abstract void addNewTabComponent();

    /**
     * The default constructor.
     */
    public WallboardTabSheet() {
        m_plusTab = new CssLayout();
        m_plusTab.setCaption("+");
        m_plusTab.setDescription("Add a new Ops Board configuration");
        addTab(m_plusTab).setClosable(false);
        addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                Component selectedTab = getSelectedTab();
                if ("+".equals(selectedTab.getCaption())) {
                    setSelectedTab((m_lastTab != null ? m_lastTab : iterator().next()));
                    addNewTabComponent();
                } else {
                    m_lastTab = selectedTab;
                }
            }
        });
    }

    /**
     * This is used a workaround for NMS-7560.
     */
    protected void togglePlusTab() {
        removeComponent(m_plusTab);
        super.addTab(m_plusTab, m_plusTab.getCaption(), null);
    }

    /**
     * This method add a new {@link Component} with the given caption and icon.
     *
     * @param c       the {@link Component} instance
     * @param caption the caption to use
     * @param icon    the icon to be used
     * @return
     */
    @Override
    public TabSheet.Tab addTab(Component c, String caption, Resource icon) {
        removeComponent(m_plusTab);
        TabSheet.Tab tab = super.addTab(c, caption, icon);
        super.addTab(m_plusTab, m_plusTab.getCaption(), null);
        return tab;
    }
}
