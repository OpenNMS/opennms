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
package org.opennms.features.vaadin.dashboard.ui;

import com.google.common.base.Strings;
import com.vaadin.shared.ui.MarginInfo;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.ui.dashboard.DashboardView;
import org.opennms.features.vaadin.dashboard.ui.wallboard.WallboardView;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.util.BeanItemContainer;

/**
 * The wallboard application's "main" class
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Ops Board")
public class WallboardUI extends UI implements DashletSelectorAccess {
    /**
     * The {@link DashletSelector} for querying configuration data
     */
    DashletSelector m_dashletSelector;

    /**
     * Assigns the associated {@link DashletSelector}.
     *
     * @param dashletSelector the dashlet selector to be used
     */
    public void setDashletSelector(DashletSelector dashletSelector) {
        this.m_dashletSelector = dashletSelector;
    }

    /**
     * Returns the associated {@link DashletSelector}.
     *
     * @return the dashlet selector
     */
    public DashletSelector getDashletSelector() {
        return m_dashletSelector;
    }

    /**
     * Entry point for a VAADIN application.
     *
     * @param request the {@link VaadinRequest} instance
     */
    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        rootLayout.setMargin(false);

        HeaderLayout headerLayout = new HeaderLayout();
        MarginInfo headerMargin = headerLayout.getMargin();
        MarginInfo newMargin = new MarginInfo(
                true,
                headerMargin.hasRight(),
                headerMargin.hasBottom(),
                headerMargin.hasLeft());
        headerLayout.setMargin(newMargin);
        rootLayout.addComponent(headerLayout);

        VerticalLayout portalWrapper = new VerticalLayout();
        portalWrapper.setSizeFull();
        portalWrapper.setMargin(false);

        rootLayout.addComponent(portalWrapper);
        rootLayout.setExpandRatio(portalWrapper, 1);
        setContent(rootLayout);

        addVueMenu();

        Navigator navigator = new Navigator(this, portalWrapper);
        navigator.addView("dashboard", DashboardView.class);
        navigator.addView("wallboard", WallboardView.class);

        navigator.addViewChangeListener(new ViewChangeListener() {
            @Override
            public void afterViewChange(ViewChangeEvent viewChangeEvent) {
                headerLayout.setWallboard(viewChangeEvent.getParameters());
            }

            @Override
            public boolean beforeViewChange(ViewChangeEvent viewChangeEvent) {
                return true;
            }
        });

        BeanItemContainer<Wallboard> beanItemContainer = WallboardProvider.getInstance().getBeanContainer();

        if (Strings.isNullOrEmpty(navigator.getState())) {
            navigator.navigateTo("wallboard");

            for (Wallboard wallboard : beanItemContainer.getItemIds()) {
                if (wallboard.isDefault()) {
                    headerLayout.gotoWallboard(wallboard);
                    break;
                }
            }
        }
    }

    private void addVueMenu() {
        // Add stylesheet for Vue side menu to <head> element
        // <link rel="stylesheet" href="${baseHref}/opennms/ui-components/assets/index.css" media="screen" />
        this.getPage().getJavaScript().execute(
            "var link = document.createElement('link');" +
            "link.rel = \"stylesheet\";" +
            "link.type = \"text/css\";" +
            "link.media = \"screen\";" +
            "link.href = \"/opennms/ui-components/assets/index.css\"; " +
            "document.head.appendChild(link);"
        );

        // Add Vue menu mount div and script reference
        // <div id="opennms-sidemenu-container"></div>
        // <script type="module" src="${baseHref}/opennms/ui-components/assets/index.js"></script>
        this.getPage().getJavaScript().execute(
            "var div = document.createElement('div');" +
            "div.id = \"opennms-sidemenu-container\";" +
            "document.body.appendChild(div);" +
            "var s = document.createElement('script');" +
            "s.type = \"module\";" +
            "s.src = \"/opennms/ui-components/assets/index.js\";" +
            "document.body.appendChild(s);"
        );
    }
}
