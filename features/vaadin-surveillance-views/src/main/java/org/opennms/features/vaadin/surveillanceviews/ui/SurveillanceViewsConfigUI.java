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
package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The surveillance view config application's "main" class
 *
 * @author Christian Pape
 */
@SuppressWarnings("serial")
@Theme("opennms")
@Title("OpenNMS Surveillance Views")
public class SurveillanceViewsConfigUI extends UI {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewsConfigUI.class);
    /**
     * the surveillance view service to be used
     */
    private SurveillanceViewService m_surveillanceViewService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(VaadinRequest request) {
        /**
         * Force the reload of the configuration
         */
        SurveillanceViewProvider.getInstance().load();

        /**
         * Create the basic layout
         */
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        rootLayout.addComponent(new SurveillanceViewsConfigList(m_surveillanceViewService));
        setContent(rootLayout);
    }

    /**
     * Method for displaying notification for the user.
     *
     * @param message     the message to be displayed
     * @param description the description of this message
     * @param type        the type of this notification
     */
    public void notifyMessage(String message, String description, Notification.Type type) {
        Notification m_notification = new Notification("Message", type);

        m_notification.setCaption(message);
        m_notification.setDescription(description);
        m_notification.setDelayMsec(1000);

        if (getUI() != null) {
            if (getPage() != null) {
                m_notification.show(getUI().getPage());
            }
        }
    }

    /**
     * Method for displaying notification for the user.
     *
     * @param message     the message to be displayed
     * @param description the description of this message
     */
    public void notifyMessage(String message, String description) {
        notifyMessage(message, description, Notification.Type.TRAY_NOTIFICATION);
    }

    /**
     * Method for setting the {@link org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService} instance to be used
     *
     * @param surveillanceViewService the instance to be used
     */
    public void setSurveillanceViewService(SurveillanceViewService surveillanceViewService) {
        this.m_surveillanceViewService = surveillanceViewService;
    }
}
