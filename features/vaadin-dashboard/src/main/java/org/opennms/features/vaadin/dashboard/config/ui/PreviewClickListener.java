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
package org.opennms.features.vaadin.dashboard.config.ui;

import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.ui.wallboard.WallboardBody;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Class implementing {@link Button.ClickListener} for creating the preview window.
 *
 * @author Christian Pape
 */
public class PreviewClickListener implements Button.ClickListener {
    /**
     * The component to use
     */
    private Component m_component;
    /**
     * The wallboard to be displayed
     */
    private Wallboard m_wallboard;

    /**
     * Constructor for creating new instances of this listener.
     *
     * @param component the {@link Component} to use
     * @param wallboard the {@link Wallboard} to display
     */
    public PreviewClickListener(Component component, Wallboard wallboard) {
        m_component = component;
        m_wallboard = wallboard;
    }

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        final Window window = new Window("Preview");

        window.setModal(true);
        window.setClosable(false);
        window.setResizable(false);

        window.setWidth("80%");
        window.setHeight("90%");

        m_component.getUI().addWindow(window);

        final WallboardBody wallboardBody = new WallboardBody();

        window.setContent(new VerticalLayout() {
            {
                setMargin(true);
                setSpacing(true);
                setSizeFull();

                addComponent(wallboardBody);
                setExpandRatio(wallboardBody, 1.0f);
                addComponent(new HorizontalLayout() {
                    {
                        setMargin(true);
                        setSpacing(true);
                        setWidth("100%");

                        Button closeButton = new Button("Close");

                        addComponent(closeButton);
                        setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);
                        closeButton.addClickListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent clickEvent) {
                                window.close();
                            }
                        });
                    }
                });
            }
        });
        wallboardBody.setDashletSpecs(m_wallboard.getDashletSpecs());
    }
}
