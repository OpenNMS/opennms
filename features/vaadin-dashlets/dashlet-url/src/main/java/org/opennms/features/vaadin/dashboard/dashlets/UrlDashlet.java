/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class implements a {@link Dashlet} for displaying an external URL.
 *
 * @author Christian Pape
 */
public class UrlDashlet extends AbstractDashlet {

    private DashletComponent m_dashletComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public UrlDashlet(String name, DashletSpec dashletSpec) {
        super(name, dashletSpec);
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_dashletComponent == null) {
            m_dashletComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setSizeFull();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();


                    String url = "";
                    String username = "";
                    String password = "";

                    if (getDashletSpec().getParameters().containsKey("url")) {
                        url = getDashletSpec().getParameters().get("url");
                    }

                    if (getDashletSpec().getParameters().containsKey("username")) {
                        username = getDashletSpec().getParameters().get("username");
                    }

                    if (getDashletSpec().getParameters().containsKey("password")) {
                        password = getDashletSpec().getParameters().get("password");
                    }

                    String usernamePassword = "";

                    if (!"".equals(username) && !"".equals(password)) {
                        usernamePassword = username + ":" + password;

                    }

                    if (!"".equals(url)) {
                        /**
                         * Try to parse the given URL...
                         */
                        URL parsedUrl = null;

                        try {
                            parsedUrl = new URL(url);
                        } catch (MalformedURLException e) {
                            m_verticalLayout.addComponent(new Label("MalformedURLException: " + e.getMessage()));
                            return;
                        }

                        /**
                         * If successful, construct a wellformed URL including the basic auth credentials
                         */
                        URL urlWithAuth = null;

                        try {
                            urlWithAuth = new URI(parsedUrl.getProtocol(), usernamePassword, parsedUrl.getHost(), parsedUrl.getPort() == -1 ? parsedUrl.getDefaultPort() : parsedUrl.getPort(), parsedUrl.getPath(), parsedUrl.getQuery(), parsedUrl.getRef()).toURL();
                        } catch (MalformedURLException e) {
                            m_verticalLayout.addComponent(new Label("MalformedURLException: " + e.getMessage()));
                            return;
                        } catch (URISyntaxException e) {
                            m_verticalLayout.addComponent(new Label("URISyntaxException: " + e.getMessage()));
                            return;
                        }

                        /**
                         * creating browser frame to display the URL
                         */
                        BrowserFrame browserFrame = new BrowserFrame(null, new ExternalResource(urlWithAuth));
                        browserFrame.setSizeFull();
                        m_verticalLayout.addComponent(browserFrame);
                    } else {
                        m_verticalLayout.addComponent(new Label("No URL specified!"));
                    }
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }

        return m_dashletComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        return getWallboardComponent();
    }
}
