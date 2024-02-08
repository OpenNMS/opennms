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
package org.opennms.features.vaadin.surveillanceviews.ui;

import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.config.surveillanceViews.View;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Class implementing {@link com.vaadin.ui.Button.ClickListener} for creating the preview window.
 *
 * @author Christian Pape
 */
public class PreviewClickListener implements Button.ClickListener {
    /**
     * The component to use
     */
    private Component m_component;
    /**
     * The surveillance view to be displayed
     */
    private View m_view;
    /**
     * The surveillance view service
     */
    private SurveillanceViewService m_surveillanceViewService;

    /**
     * Constructor for creating new instances of this listener.
     *
     * @param component the {@link Component} to use
     * @param view      the {@link View} to display
     */
    public PreviewClickListener(SurveillanceViewService surveillanceViewService, Component component, View view) {
        m_surveillanceViewService = surveillanceViewService;
        m_component = component;
        m_view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        final Window window = new Window("Preview");

        window.setModal(true);
        window.setClosable(true);
        window.setResizable(false);

        window.setWidth("80%");
        window.setHeight("90%");

        m_component.getUI().addWindow(window);

        window.setContent(new VerticalLayout() {
            {
                addComponent(new VerticalLayout() {
                    {
                        setMargin(true);
                        setSpacing(true);
                        setSizeFull();

                        addComponent(new SurveillanceView(m_view, m_surveillanceViewService, false, false));
                    }
                });
            }
        });
    }
}
