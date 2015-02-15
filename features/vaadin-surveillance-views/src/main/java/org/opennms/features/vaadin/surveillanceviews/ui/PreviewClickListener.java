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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewAlarmTable;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewNodeRtcTable;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewNotificationTable;

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

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        final Window window = new Window("Preview");

        window.setModal(true);
        window.setClosable(false);
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

                        SurveillanceViewTable surveillanceViewTable = new SurveillanceViewTable(m_view, m_surveillanceViewService);

                        addComponent(surveillanceViewTable);

                        VerticalLayout secondLayout = new VerticalLayout();
                        secondLayout.setSpacing(true);

                        SurveillanceViewAlarmTable surveillanceViewAlarmTable = new SurveillanceViewAlarmTable(m_surveillanceViewService);
                        SurveillanceViewNotificationTable surveillanceViewNotificationTable = new SurveillanceViewNotificationTable(m_surveillanceViewService);
                        SurveillanceViewNodeRtcTable surveillanceViewNodeRtcTable = new SurveillanceViewNodeRtcTable(m_surveillanceViewService);

                        secondLayout.addComponent(surveillanceViewAlarmTable);
                        secondLayout.addComponent(surveillanceViewNotificationTable);
                        secondLayout.addComponent(surveillanceViewNodeRtcTable);

                        surveillanceViewTable.addDetailsTable(surveillanceViewAlarmTable);
                        surveillanceViewTable.addDetailsTable(surveillanceViewNotificationTable);
                        surveillanceViewTable.addDetailsTable(surveillanceViewNodeRtcTable);

                        addComponent(secondLayout);

                        setExpandRatio(secondLayout, 1.0f);
                    }
                });

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
    }
}
