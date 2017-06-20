/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.CharEncoding;
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class implements a {@link Dashlet} for displaying an Grafana dashboard.
 *
 * @author Christian Pape
 */
public class GrafanaDashlet extends AbstractDashlet {

    private DashletComponent m_dashletComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public GrafanaDashlet(String name, DashletSpec dashletSpec) {
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

                    String uri = "";
                    String from = "";
                    String to = "";

                    /**
                     * Retrieving the required parameters
                     */
                    if (getDashletSpec().getParameters().containsKey("uri")) {
                        uri = getDashletSpec().getParameters().get("uri");
                    }

                    if (getDashletSpec().getParameters().containsKey("from")) {
                        from = getDashletSpec().getParameters().get("from");
                    }

                    if (getDashletSpec().getParameters().containsKey("to")) {
                        to = getDashletSpec().getParameters().get("to");
                    }

                    if (!"".equals(uri)) {
                        /**
                         * Retrieving the properties
                         */
                        final String grafanaProtocol = System.getProperty("org.opennms.grafanaBox.protocol", "http");
                        final String grafanaHostname = System.getProperty("org.opennms.grafanaBox.hostname", "localhost");
                        final int grafanaPort = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.port", "3000"));

                        try {
                            /**
                             * Constructing the URL...
                             */
                            URL url = new URL(
                                    String.format(
                                        "%s://%s:%d/dashboard/%s?from=%s&to=%s",
                                        grafanaProtocol,
                                        grafanaHostname,
                                        grafanaPort,
                                        uri,
                                        URLEncoder.encode(from, CharEncoding.UTF_8),
                                        URLEncoder.encode(to, CharEncoding.UTF_8)
                                    )
                            );

                            /**
                             * creating browser frame to display the URL
                             */
                            BrowserFrame browserFrame = new BrowserFrame(null, new ExternalResource(url));
                            browserFrame.setSizeFull();
                            m_verticalLayout.addComponent(browserFrame);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            m_verticalLayout.addComponent(new Label("MalformedURLException: " + e.getMessage()));
                            return;
                        }
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
