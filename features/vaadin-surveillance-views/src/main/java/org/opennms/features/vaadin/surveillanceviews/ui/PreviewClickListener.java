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

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;

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
