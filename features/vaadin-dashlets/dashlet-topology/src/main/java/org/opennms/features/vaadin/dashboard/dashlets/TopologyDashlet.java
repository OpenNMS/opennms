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

import org.opennms.features.topology.link.TopologyLinkBuilder;
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

/**
 * This class implements a {@link Dashlet} for displaying the topology map application.
 *
 * @author Christian Pape
 */
public class TopologyDashlet extends AbstractDashlet {

    private DashletComponent m_dashletComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public TopologyDashlet(String name, DashletSpec dashletSpec) {
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
                    TopologyLinkBuilder linkBuilder = new TopologyLinkBuilder();

                    if (getDashletSpec().getParameters().containsKey("focusNodes")) {
                        linkBuilder.focus(getDashletSpec().getParameters().get("focusNodes").split(","));
                    }
                    if (getDashletSpec().getParameters().containsKey("szl")) {
                        linkBuilder.szl(Integer.valueOf(getDashletSpec().getParameters().get("szl")));
                    }
                    if (getDashletSpec().getParameters().containsKey("provider")) {
                        linkBuilder.provider(() -> getDashletSpec().getParameters().get("provider"));
                    }

                    //creating browser frame to display the topology
                    BrowserFrame browserFrame = new BrowserFrame(null, new ExternalResource(linkBuilder.getLink()));
                    browserFrame.setSizeFull();
                    browserFrame.setId("opsboard-topology-iframe");
                    m_verticalLayout.addComponent(browserFrame);
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
