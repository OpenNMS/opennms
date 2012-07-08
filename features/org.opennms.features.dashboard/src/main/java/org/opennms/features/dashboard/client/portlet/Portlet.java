/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.dashboard.client.portlet;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public class Portlet extends BasicPortlet {
    protected FlexTable headerPanel;

    protected Label titleLbl;

    protected PushButton closeBtn;
    
    private Widget widget;

    public Portlet() {
        headerPanel = new FlexTable();
        titlePanel.add(headerPanel);
        headerPanel.setCellSpacing(0);
        headerPanel.setCellPadding(0);
        headerPanel.setWidth("100%");

        titleLbl = new Label();
        titleLbl.setStyleName("popup-WindowPanel-title");
        titleLbl.setSize("100%", TITLE_HEIGHT + "px");
        headerPanel.setWidget(0, 0, titleLbl);

        closeBtn = new PushButton(new Image(imageResource.dropSmall()));
        closeBtn.setSize("auto", "auto");
        closeBtn.setStyleName("popup-WindowPanel-close");
        closeBtn.addClickHandler(closeHandler);
        headerPanel.setWidget(0, 1, closeBtn);
        headerPanel.getCellFormatter().setWidth(0, 1, "30px");

    }

    public Portlet(String title) {
        this();
        setTitle(title);
    }

    @Override
    public void setTitle(String title) {
        titleLbl.setText(title);
    }

    @Override
    public void setContent(Widget w) {
        clearPortletContent();
        addPortletContent(w);
    }

    @Override
    public Widget getContentWidget() {
        return widget;
    }

    @Override
    public void restoreWidget() {
        clearPortletContent();
        addPortletContent(widget);
    }
}
