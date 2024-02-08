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

import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.ui.dashboard.DashboardView;
import org.opennms.features.vaadin.dashboard.ui.wallboard.WallboardView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.NativeSelect;

/**
 * The top heading layout for the wallboard view.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class HeaderLayout extends HorizontalLayout implements ViewChangeListener {

    private View m_wallboardView = null;
    private Button m_pauseButton, m_wallboardButton, m_dashboardButton;
    private NativeSelect m_nativeSelect;

    /**
     * Default constructor.
     */
    public HeaderLayout() {
        /**
         * Setting up the layout
         */
        addStyleName("header");
        setMargin(new MarginInfo(false,true,false,false));
        setSpacing(true);
        setWidth("100%");
        setHeight(64, Unit.PIXELS);

        /**
         * Adding the logo
         */
        Label link = new Label();
        link.setContentMode(ContentMode.HTML);
        link.setValue("<a href=\"/opennms/index.jsp\" id=\"onmslogo\"></a>");
        addComponent(link);
        setExpandRatio(link, 1.0f);

        /**
         * Adding the selection box
         */
        m_nativeSelect = new NativeSelect();
        m_nativeSelect.setDescription("Select Ops Board configuration");
        m_nativeSelect.setContainerDataSource(WallboardProvider.getInstance().getBeanContainer());
        m_nativeSelect.setItemCaptionPropertyId("title");
        m_nativeSelect.setNullSelectionAllowed(false);
        m_nativeSelect.setImmediate(true);

        m_nativeSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                m_wallboardButton.setEnabled(true);
                m_dashboardButton.setEnabled(true);
            }
        });

        m_dashboardButton = new Button("Ops Panel", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().getNavigator().addViewChangeListener(HeaderLayout.this);
                UI.getCurrent().getNavigator().navigateTo("dashboard/" + m_nativeSelect.getContainerProperty(m_nativeSelect.getValue(), "title").getValue());
            }
        });
        m_dashboardButton.setDescription("Ops Panel view");

        /**
         * Adding the wallboard button
         */
        m_pauseButton = new Button("Pause", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (m_wallboardView instanceof WallboardView) {
                    if (((WallboardView) m_wallboardView).isPaused()) {
                        ((WallboardView) m_wallboardView).resume();
                    } else {
                        ((WallboardView) m_wallboardView).pause();
                    }
                } else {
                    if (m_wallboardView instanceof DashboardView) {
                        ((DashboardView) m_wallboardView).updateAll();
                    }
                }

                updatePauseButton();
            }
        });

        /**
         * Adding the wallboard button
         */
        m_wallboardButton = new Button("Ops Board", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().getNavigator().addViewChangeListener(HeaderLayout.this);
                UI.getCurrent().getNavigator().navigateTo("wallboard/" + m_nativeSelect.getContainerProperty(m_nativeSelect.getValue(), "title").getValue());
            }
        });
        m_wallboardButton.setDescription("Ops Board view");

        m_pauseButton.setEnabled(false);
        m_wallboardButton.setEnabled(false);
        m_dashboardButton.setEnabled(false);

        addComponents(m_nativeSelect, m_dashboardButton, m_wallboardButton, m_pauseButton);
        setComponentAlignment(m_nativeSelect, Alignment.MIDDLE_CENTER);
        setComponentAlignment(m_dashboardButton, Alignment.MIDDLE_CENTER);
        setComponentAlignment(m_wallboardButton, Alignment.MIDDLE_CENTER);
        setComponentAlignment(m_pauseButton, Alignment.MIDDLE_CENTER);
    }

    public void gotoWallboard(Wallboard wallboard) {
        m_nativeSelect.setValue(wallboard);
        UI.getCurrent().getNavigator().navigateTo("wallboard/" + wallboard.getTitle());
    }

    public void setWallboard(final String wallboardName) {
        final Wallboard wallboard = WallboardProvider.getInstance().getWallboard(wallboardName);
        m_nativeSelect.setValue(wallboard);
    }

    private void updatePauseButton() {
        if (m_wallboardView instanceof WallboardView) {
            if (((WallboardView) m_wallboardView).isPausable()) {
                m_pauseButton.setEnabled(true);

                if (((WallboardView) m_wallboardView).isPaused()) {
                    m_pauseButton.setCaption("Resume");
                    m_pauseButton.setDescription("Resume the execution of the Ops Board");
                } else {
                    m_pauseButton.setCaption("Pause");
                    m_pauseButton.setDescription("Pause the execution of the Ops Board");
                }
            } else {
                m_pauseButton.setEnabled(false);
                m_pauseButton.setCaption("Pause");
                m_pauseButton.setDescription("Pause the execution of the Ops Board");
            }
        } else {
            if (m_wallboardView instanceof DashboardView) {
                m_pauseButton.setCaption("Refresh");
                m_pauseButton.setDescription("Refresh");
                m_pauseButton.setEnabled(true);
            } else {
                m_pauseButton.setCaption("Pause");
                m_pauseButton.setDescription("Pause the execution of the Ops Board");
                m_pauseButton.setEnabled(false);
            }
        }
    }

    @Override
    public boolean beforeViewChange(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        return true;
    }

    @Override
    public void afterViewChange(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        m_wallboardView = viewChangeEvent.getNewView();

        updatePauseButton();
    }
}
