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

import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.opennms.features.vaadin.dashboard.ui.wallboard.WallboardBody;

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
