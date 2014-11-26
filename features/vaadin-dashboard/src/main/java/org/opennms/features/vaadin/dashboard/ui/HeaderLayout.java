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

package org.opennms.features.vaadin.dashboard.ui;

import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.ui.dashboard.DashboardView;
import org.opennms.features.vaadin.dashboard.ui.wallboard.WallboardView;

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
        setMargin(true);
        setSpacing(true);
        setWidth("100%");

        /**
         * Adding the logo
         */
        Link link = new Link(null, new ExternalResource("/opennms/index.jsp"));
        link.setIcon(new ThemeResource("img/logo.png"));
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
                UI.getCurrent().getNavigator().navigateTo("dashboard/" + m_nativeSelect.getContainerProperty(m_nativeSelect.getValue(), "title"));
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
                UI.getCurrent().getNavigator().navigateTo("wallboard/" + m_nativeSelect.getContainerProperty(m_nativeSelect.getValue(), "title"));
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
