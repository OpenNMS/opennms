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
package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;

/**
 * The Vaadin application used for wallboard configuration. This application instantiates a {@link WallboardConfigView} instance.
 *
 * @author Christian Pape
 */
@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Dashboard")
public class WallboardConfigUI extends UI implements DashletSelectorAccess {
    /**
     * The {@link DashletSelector} instance used for querying configuration data
     */
    private DashletSelector m_dashletSelector;

    /**
     * A {@link Notification} instance for displaying messages
     */
    private static Notification m_notification = new Notification("Message", Notification.Type.TRAY_NOTIFICATION);

    /**
     * Default constructor for instantiating a new instance
     */
    public WallboardConfigUI() {
    }

    /**
     * Method for setting the required {@link DashletSelector}.
     *
     * @param dashletSelector the {@link DashletSelector} to be set
     */
    public void setDashletSelector(DashletSelector dashletSelector) {
        this.m_dashletSelector = dashletSelector;
    }

    /**
     * Method for setting up the application.
     *
     * @param request the {@link VaadinRequest} object
     */
    @Override
    protected void init(VaadinRequest request) {
        setContent(new WallboardConfigView(m_dashletSelector));
    }

    /**
     * Returns the associated {@link DashletSelector} instance.
     *
     * @return the {@link DashletSelector} instance
     */
    public DashletSelector getDashletSelector() {
        return m_dashletSelector;
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
