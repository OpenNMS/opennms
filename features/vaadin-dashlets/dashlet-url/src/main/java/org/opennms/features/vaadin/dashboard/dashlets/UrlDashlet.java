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
package org.opennms.features.vaadin.dashboard.dashlets;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.Label;

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
    public DashletComponent getWallboardComponent(final UI ui) {
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
    public DashletComponent getDashboardComponent(final UI ui) {
        return getWallboardComponent(ui);
    }
}
