package org.opennms.features.vaadin.surveillanceviews.ui;

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

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Surveillance Views")
public class SurveillanceViewsConfigUI extends UI {

    /**
     * A {@link com.vaadin.ui.Notification} instance for displaying messages
     */
    private static Notification m_notification = new Notification("Message", Notification.Type.TRAY_NOTIFICATION);


    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        rootLayout.addComponent(new SurveillanceViewsConfigList());
        setContent(rootLayout);
/*
        File cfgFile = new File("etc/surveillance-views.xml");

        if (cfgFile.exists()) {
            SurveillanceViewConfiguration surveillanceViewConfiguration = JAXB.unmarshal(cfgFile, SurveillanceViewConfiguration.class);
            for (View view : surveillanceViewConfiguration.getViews()) {
                System.err.print("View: " + view.getName());
                for(ColumnDef columnDef:view.getColumns()) {
                    System.err.println("ColumnDef: "+columnDef.getLabel());
                    for(Category category:columnDef.getCategories()) {
                        System.err.println(category.getName());
                    }
                }
                for(RowDef rowDef:view.getRows()) {
                    System.err.println("RowDef: " + rowDef.getLabel());
                    for(Category category:rowDef.getCategories()) {
                        System.err.println(category.getName());
                    }
                }
            }
        } else {
            System.err.println("BMRHGA: file not found");
        }
*/
    }

    /**
     * Method for displaying notification for the user.
     *
     * @param message     the message to be displayed
     * @param description the description of this message
     */
    public void notifyMessage(String message, String description) {
        m_notification.setCaption(message);
        m_notification.setDescription(description);
        m_notification.setDelayMsec(1000);
        if (getUI() != null) {
            if (getPage() != null) {
                m_notification.show(getUI().getPage());
            }
        }
    }
}
