/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.dashboard.ui;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;

/**
 * The top heading layout for the wallboard view.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class HeaderLayout extends HorizontalLayout {

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
        Image logo = new Image(null, new ThemeResource("img/logo.png"));
        addComponent(logo);
        setExpandRatio(logo, 1.0f);

        /**
         * Adding the selection box
         */
        final NativeSelect nativeSelect = new NativeSelect();
        nativeSelect.setContainerDataSource(WallboardProvider.getInstance().getBeanContainer());
        nativeSelect.setItemCaptionPropertyId("title");
        nativeSelect.setNullSelectionAllowed(false);

        /*
        Button dashboardButton = new Button("Dashboard", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().getNavigator().navigateTo("dashboard/" + nativeSelect.getContainerProperty(nativeSelect.getValue(), "title"));
            }
        });
        */

        /**
         * Adding the wallboard button
         */
        Button wallboardButton = new Button("Wallboard", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().getNavigator().navigateTo("wallboard/" + nativeSelect.getContainerProperty(nativeSelect.getValue(), "title"));
            }
        });

        addComponents(nativeSelect, /*dashboardButton,*/ wallboardButton);
        setComponentAlignment(nativeSelect, Alignment.MIDDLE_CENTER);
        //setComponentAlignment(dashboardButton, Alignment.MIDDLE_CENTER);
        setComponentAlignment(wallboardButton, Alignment.MIDDLE_CENTER);
    }
}
