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
